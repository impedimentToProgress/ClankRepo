import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.lang.StringBuilder;

public class Trial extends Thread
{
    public StringBuilder  result = new StringBuilder(5000);
    private static final boolean DETAIL_PRINT = false;
    private static final boolean MEM_LOG_PRINT = false;
    private static final boolean VERIFY_CORRECTNESS = false;
    
    // Verifier for clank
    private final GoldenClank golden;
    
    // Cost of checkpoint operations---highly dependent on ISA and memory access time
    private static final int CP_CYCLES = 40;   // Cycles required to backup registers to NV-RAM
    private static final int CP_BUFF_BASE = 2; // Base cycle cost for accessing WBB
    private static final int CP_BUFF_MULT = 2; // Incremental cycle cost for each WBB entry
    
    // Clank optimizations
    private final boolean RE_WRITE_CHECK;     // Writes that don't not change the value of a memory location can be ignored
    private final boolean OPT_BUFFS;          // Once an address is added to the write-back buffer, it no longer needs to be tracked by the read buffer
    private final boolean WRITE_BUFF_NO_OVRFLW;     // Should we cause a checkpoint to occur when the write buffer fills
    private final boolean IGNORE_TEXT_SEG_ACCESSES; //Ignore memory access to the---usually read-only---text segment, for safety, any write to the text segment will create a checkpoint
    private final boolean LATEST_CHKPT;             // Push off taking a checkpoint until the latest possible time
    
    // Clank configuration
    private final ArrayList<MemoryAccess> accesses;
    private final int rbs;
    private final int wbs;
    private final int wbbs;
    private final int entryBits;
    private final int aps;
    private final int wdt_load;
    private final int ON_PERIOD;
    
    // Address masks
    private final int prefixMask;
    private final int mask;
    
    // Calculated sizes
    private final int addedBits;

    // Keep track of section sizes
    private static final int MAX_SECTION_LENGTH = 50000;
    private static final int SECTION_LENGTH_BIN_SIZE = 10;
    private static final int SECTION_LENGTH_LAST_BIN = MAX_SECTION_LENGTH / SECTION_LENGTH_BIN_SIZE;
    private final int sectionSizeList[] = new int[SECTION_LENGTH_LAST_BIN + 1];
    
    public Trial(ArrayList<MemoryAccess> accesses, int rbs, int wbs, int wbbs, int entryBits, int aps, int wdt_load, int oP, boolean RE_WRITE_CHECK, boolean OPT_BUFFS, boolean WRITE_BUFF_NO_OVRFLW, boolean IGNORE_TEXT_SEG_ACCESSES, boolean LATEST_CHKPT)
    {
        this.accesses = accesses;
        this.rbs = rbs;
        this.wbs = wbs;
        this.wbbs = wbbs;
        this.entryBits = entryBits;
        this.aps = aps;
        this.wdt_load = wdt_load;
        this.ON_PERIOD = oP;
        this.RE_WRITE_CHECK = RE_WRITE_CHECK;
        this.OPT_BUFFS = OPT_BUFFS;
        this.WRITE_BUFF_NO_OVRFLW = WRITE_BUFF_NO_OVRFLW;
        this.IGNORE_TEXT_SEG_ACCESSES = IGNORE_TEXT_SEG_ACCESSES;
        this.LATEST_CHKPT = LATEST_CHKPT;
        
	if(aps == 0)
	{
	    assert(entryBits == 30);
	}
	else
	{
	    assert(aps > 0);
	}
        assert(rbs > 0);
        assert(wbs >= 0);
        assert(wbbs >= 0);
        assert(entryBits >= 0);
        
        // Save/check the address prefix (the part we don't use)
        // Needed to esure that all our addresses are from the same region of memory
        // Otherwise, we get aliasing and incorrect results
        prefixMask = entryBits > 29 ? 0 : 0xFFFFFFFF >> (entryBits + 2) << (entryBits + 2);
        // Determine the address mask based on the number of bits in a buffer entry
        // Only keep entry bits worth of the MSbs, keep in mind the two bits we
        // already shaved off the back to ddress words not bytes
        mask = ~prefixMask & 0xFFFFFFFC;
        
        addedBits = getClankHardwareRequirement();
        
        if(VERIFY_CORRECTNESS)
            golden = new GoldenClank();
	else
	    golden = null;
    }
    
    // Given the Clank buffer config, calculate the number of bits required to implement it
    private int getClankHardwareRequirement()
    {
        int addressPrefixSize = aps * (30 - entryBits); // Ignore two LSb's since we address words not bytes
        int prefixSelectBits = aps == 0 ? 0 : (int)Math.ceil(Math.log(aps) / Math.log(2)); // Need to tag each entry with a prefix, log2(1) = 0
        int effectiveEntrySize = entryBits + prefixSelectBits;
        
        int readBufferSize = rbs * effectiveEntrySize;
        int writeBufferSize = wbs * effectiveEntrySize;
        int writeBackBufferSize = (wbbs * effectiveEntrySize) + (wbbs * 32); // address, data
        
        return addressPrefixSize + readBufferSize + writeBufferSize + writeBackBufferSize;
    }
    
    // Clank buffers
    private ArrayList<Integer> readFirst = new ArrayList<Integer>();
    private ArrayList<Integer> writeFirst = new ArrayList<Integer>();
    private HashMap<Integer, Integer> writeBack = new HashMap<Integer, Integer>();
    private HashMap<Integer, Integer> addressPrefix = new HashMap<Integer, Integer>();
    
    // Tracks overhead due to checkpoints
    private long overhead = 0;
    
    // Watchdog timers
    private int wdt;
    private int wdt_progress;
    
    // Need to put off checkpoints for as long as possible
    private boolean chkptOnNextTextSegWrite = false;
    private boolean chkptOnNextAPFWrite = false;
    private boolean chkptOnNextWrite = false;

    private long ticksAtLastCheckpoint = 0;
    private void checkpoint()
    {
        if(MEM_LOG_PRINT){System.err.println("" + readFirst.size() + ", " + writeFirst.size() + ", " + writeBack.size() + ", " + addressPrefix.size());}

	if(DETAIL_PRINT) {
	    // Keep track of the number of instructions per section
	    long ticks = accesses.get(access).ticks;
	    int cyclesInSection = (int)(ticks - ticksAtLastCheckpoint);
	    int bin = cyclesInSection / SECTION_LENGTH_BIN_SIZE;
	    if(bin > SECTION_LENGTH_LAST_BIN)
		sectionSizeList[SECTION_LENGTH_LAST_BIN]++;
	    else
		sectionSizeList[bin]++;
	    ticksAtLastCheckpoint = ticks;
	}
	
        // Make sure golden was updated correctly
        if(VERIFY_CORRECTNESS)
        {
            golden.reset();
        }
        
        chkptOnNextTextSegWrite = false;
        chkptOnNextAPFWrite = false;
        chkptOnNextWrite = false;
        reloadCheckpointWatchDogTimer();
        reloadProgressWatchDogTimer();
        overhead += CP_CYCLES + (wbbs == 0 ? 0 : CP_BUFF_BASE) + (CP_BUFF_MULT * writeBack.size());
        clearClankBuffers();
    }
    
    private void reloadCheckpointWatchDogTimer()
    {
        wdt = wdt_load;
    }
    
    private void reloadProgressWatchDogTimer()
    {
        wdt_progress = ON_PERIOD;
    }
    
    private void clearClankBuffers()
    {
        addressPrefix.clear();
        readFirst.clear();
        writeFirst.clear();
        writeBack.clear();
    }
    
    
    // Process an address and update Clank buffers
    // possibly create a checkpoint
    private void processAddress(long ticks, long ticksLast, int fullAddress, char op, boolean valueChanged, int newValue)
    {
        // Check watchdog timers
        wdt -= (ticks - ticksLast);
        if(wdt <= 0)
        {
            ++wdts;
            checkpoint();
            if(MEM_LOG_PRINT){System.err.println("Performance Watchdog Timer");}
        }
        
        wdt_progress -= (ticks - ticksLast);
        if(wdt_progress <= 0)
        {
            ++wdt_progress_count;
            checkpoint();
            if(MEM_LOG_PRINT){System.err.println("Progress Watchdog Timer");}
        }
        
        // Assume the text segment is read only
        // Tradeoff is that any write causes a checkpoint
        if(IGNORE_TEXT_SEG_ACCESSES)
        {
            //System.err.println("Ignore text segment");
            if((fullAddress & 0xF0000000) == 0)
            {
                // Ignore reads to text segment
                if(op == 'R')
                {
                    chkptOnNextTextSegWrite = true;
                    if(VERIFY_CORRECTNESS){golden.addRead(fullAddress);}
                    return;
                }
		// Check for false writes
                else if(RE_WRITE_CHECK && !valueChanged)
		{
		    if(VERIFY_CORRECTNESS){golden.addWriteIgnoredAsDupe(fullAddress);}
		    return;
		}
                // All writes cause a checkpoint
		else
                {
                    if(LATEST_CHKPT)
                    {
                        // Really only need to checkpoint on R->W
                        if(chkptOnNextTextSegWrite)
                        {
                            ++textSegWrites;
                            checkpoint();
                            if(MEM_LOG_PRINT){System.err.println("Latest: Text Segment");}
                        }
                    }
                    else
                    {
                        ++textSegWrites;
                        checkpoint();
                        if(MEM_LOG_PRINT){System.err.println("Not Latest: Text Segment");}
                    }
                    
                    if(VERIFY_CORRECTNESS){golden.addWrite(fullAddress);}
                    return;
                }
            }
        }
        
        // Check for address prefix buffer fills and updates
        int prefix = fullAddress & prefixMask;

	if(aps > 0){
        if(!addressPrefix.containsKey(prefix))
        {
            //System.err.println("Adding new prefix: " + Integer.toHexString((fullAddress & prefixMask)));
            // Need to checkpoint due to addresses coming from different memory ranges and prefix buffer full
            if(addressPrefix.size() == aps)
            {
                // We can safely ignore writes from different regions, because they are write dominated by definition
                // as long as we haven't seen an untracked read
                if(op == 'W') // Need to add re_write guard here
                {
		    // Check for false writes
		    if(RE_WRITE_CHECK && !valueChanged)
		    {
			if(VERIFY_CORRECTNESS){golden.addWriteIgnoredAsDupe(fullAddress);}
			return;
		    }
                    else if(LATEST_CHKPT)
                    {
                        // A untracked read->untracked write must cause a checkpoint
                        if(chkptOnNextAPFWrite)
                        {
                            ++apfs;
                            checkpoint();
                            if(MEM_LOG_PRINT){System.err.println("Latest: Address Prefix");}
                            processAddress(ticksLast, ticksLast, fullAddress, op, valueChanged, newValue);
                            return;
                        }
                        else
                        {
                            if(VERIFY_CORRECTNESS){golden.addWrite(fullAddress);}
                            return;
                        }
                    }
                    else
                    {
                        ++apfs;
                        checkpoint();
                        if(MEM_LOG_PRINT){System.err.println("Not Latest: Address Prefix");}
                        processAddress(ticksLast, ticksLast, fullAddress, op, valueChanged, newValue);
                        return;
                    }
                }
                else
                {
                    chkptOnNextAPFWrite = true;
                    if(VERIFY_CORRECTNESS){golden.addRead(fullAddress);}
                    return;
                }
            }
            
            addressPrefix.put(prefix, 0);
        }
        
        //fullAddress = fullAddress & mask; replace with assert,
        // due to added overhead of actually implementing tag bits
        assert(addressPrefix.containsKey(fullAddress & prefixMask));
	}

        // Handle read accesses
        if(op == 'R')
        {
            //System.err.println("Handling read access");
            
            if(!writeFirst.contains(fullAddress))
                if(!OPT_BUFFS || !writeBack.containsKey(fullAddress))
                    if(!readFirst.contains(fullAddress))
                        if(readFirst.size() < rbs)
                            readFirst.add(fullAddress);
                        else
                        {
                            if(LATEST_CHKPT)
                            {
                                //System.err.println("Untracked read");
                                chkptOnNextWrite = true;
                            }
                            else
                            {
                                ++readFulls;
                                checkpoint();
                                if(MEM_LOG_PRINT){System.err.println("Not Latest: Read buffer full");}
                                processAddress(ticksLast, ticksLast, fullAddress, op, valueChanged, newValue);
                                return;
                            }
                        }
            
            if(VERIFY_CORRECTNESS){golden.addRead(fullAddress);}
        }
        // Handle write accesses
        else
        {
            //System.err.println("Handling write access");
            if(readFirst.contains(fullAddress))
            {
                //System.err.println("Address already in read buffer");
                if(!RE_WRITE_CHECK || valueChanged)
                {
                    // Check if in write-back buffer
                    if(writeBack.containsKey(fullAddress))
                    {
                        ++wbbHits;
                        if(VERIFY_CORRECTNESS){golden.addWritePostCP(fullAddress);}
                        return;
                    }
                    // If not, try to add it
                    else if(writeBack.size() < wbbs)
                    {
                        ++wbbHits;
                        writeBack.put(fullAddress, newValue);
                        if(OPT_BUFFS)
                        {
                            readFirst.remove(readFirst.indexOf(fullAddress));
                        }
                        if(VERIFY_CORRECTNESS){golden.addWritePostCP(fullAddress);}
                        return;
                    }
                    // If no room in the write-back buffer, we have a idempotency violation
                    else
                    {
                        ++violations;
                        checkpoint();
                        if(MEM_LOG_PRINT){System.err.println("Idempotency violation and WBB full");}
                        processAddress(ticksLast, ticksLast, fullAddress, op, valueChanged, newValue);
                        return;
                    }
                }
                else if(VERIFY_CORRECTNESS){golden.addWriteIgnoredAsDupe(fullAddress);}
            }
            else if(!writeFirst.contains(fullAddress)) // Probably should guard with !RE_WRITE_CHECK || valueChanged
            {
                //System.err.println("Address not in write buffer");
                if(!OPT_BUFFS || !writeBack.containsKey(fullAddress))
                {
		    // Check for false writes
		    if(RE_WRITE_CHECK && !valueChanged)
		    {
			if(VERIFY_CORRECTNESS){golden.addWriteIgnoredAsDupe(fullAddress);}
			return;
		    }

                    if(LATEST_CHKPT)
                    {
                        // Untracked reads->not already tracked write causes a checkpoint
                        if(chkptOnNextWrite)
                        {
                            //System.err.println("Untracked read -> untracked write chkpt");
                            ++readFulls;
                            checkpoint();
                            if(MEM_LOG_PRINT){System.err.println("Latest: Write when read buffer full");}
                            processAddress(ticksLast, ticksLast, fullAddress, op, valueChanged, newValue);
                            return;
                        }
                    }
                    
                    // Either way, let's try to add this write to the write first buffer
                    if(writeFirst.size() < wbs)
                    {
                        writeFirst.add(fullAddress);
                        if(VERIFY_CORRECTNESS){golden.addWrite(fullAddress);}
                    }
                    else if(!WRITE_BUFF_NO_OVRFLW)
                    {
			// Check for false writes
			if(RE_WRITE_CHECK && !valueChanged)
			{
			    if(VERIFY_CORRECTNESS){golden.addWriteIgnoredAsDupe(fullAddress);}
			    return;
			}

                        ++writeFulls;
                        checkpoint();
                        if(MEM_LOG_PRINT){System.err.println("Write buffer full");}
                        
                        // Handle case where write buffer has 0 entries
                        if(wbs == 0)
                        {
                            if(VERIFY_CORRECTNESS){golden.addWritePostCP(fullAddress);}
                        }
                        else
                        {
                            //System.err.println("Reprocess write");
                            processAddress(ticksLast, ticksLast, fullAddress, op, valueChanged, newValue);
                        }
                        return;
                    }
                    else if(VERIFY_CORRECTNESS){golden.addWrite(fullAddress);}
                }
                else if(VERIFY_CORRECTNESS){golden.addWritePostCP(fullAddress);}
            }
            else if(VERIFY_CORRECTNESS){golden.addWrite(fullAddress);}
        }
    }
    
    // Clank performance counters
    int readFulls = 0;
    int writeFulls = 0;
    int wbbHits = 0;
    int violations = 0;
    int wdts = 0;
    int wdt_progress_count = 0;
    int apfs = 0;
    int textSegWrites = 0;
    int access = 0;
    
    public void run()
    {
        result.append(String.format("%d, %d, %d, %d, %d, %d, %d, ", rbs,  wbs, wbbs, entryBits, aps, wdt_load, addedBits));
        
        // Get everything ready to start
        reloadCheckpointWatchDogTimer();
        reloadProgressWatchDogTimer();
        clearClankBuffers();
        
        // Go through all of the memory accesses
        int numAccessesInGolden = 0;
        for(access = 0; access < accesses.size(); ++access)
        {
            long ticksLast = access == 0 ? 0 : accesses.get(access - 1).ticks;

            if(VERIFY_CORRECTNESS)
            {
                golden.updateForBetterDiff(accesses.get(access).ticks, accesses.get(access).insns);
            }
            
            processAddress(accesses.get(access).ticks, ticksLast, accesses.get(access).address, accesses.get(access).type, accesses.get(access).valueChanged, accesses.get(access).newValue);
            
            // Make sure golden gets updated if we are verifying correctness
            if(VERIFY_CORRECTNESS)
            {
                if((numAccessesInGolden + 1) != golden.getNumAccesses() && (wdt != wdt_load || golden.getNumAccesses() != 1))
                {
                    System.err.println("VERIFICATION FAILED: missed memory access: " +  accesses.get(access));
                    System.err.println("" + numAccessesInGolden + ", " + golden.getNumAccesses());
                    System.exit(2);
                }
                
                numAccessesInGolden = golden.getNumAccesses();
            }
            
        }
        
        assert(accesses.size() > 0);
        long ticks = accesses.get(accesses.size() - 1).ticks;
        long total = violations + readFulls + wdts + writeFulls + wdt_progress_count + apfs + textSegWrites;
        long cpcp = total == 0 ? ticks : ticks/total;
        double cpOverhead = (overhead + ticks)*100.0/ticks;
        double rexOverhead = (ticks + (cpcp / 2) * (ticks / ON_PERIOD))*100.0/ticks;
        result.append(String.format("%.2f, %.2f, %.2f\n", cpOverhead, rexOverhead, (cpOverhead + rexOverhead - 100)));
        if(DETAIL_PRINT)
        {
            result.append("Buffer hits:\t" + wbbHits);
            result.append("\tRead fulls:\t" + readFulls);
            result.append("\tWrite fulls:\t" + writeFulls);
            result.append("\tViolations:\t" + violations);
            result.append("\tAddress prefix faults:\t" + apfs);
            result.append("\tWrites to text segment:\t" + textSegWrites);
            result.append("\tPerformance:\t" + wdts);
            result.append("\tProgress:\t" + wdt_progress_count);
            result.append("\tTotal CPs:\t" + total);
            //result += String.format("\tCheckpoint Overhead:\t%.2f\n", cpOverhead);
            //result += String.format("\tRe-execution Overhead:\t%.2f\n", rexOverhead);
            result.append("\tCPCP:\t" + cpcp);
	    result.append("\nSection Lengths: ");
	    // Find last non-zero result
	    int lastNonZeroBin;
	    for(lastNonZeroBin = SECTION_LENGTH_LAST_BIN; lastNonZeroBin > 0; --lastNonZeroBin) {
		if(sectionSizeList[lastNonZeroBin] != 0)
		    break;
	    }
	    for(int x = 0; x <= lastNonZeroBin; ++x) {
		result.append(sectionSizeList[x] + ",");
	    }
        }
    }
}
