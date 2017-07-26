// Multithreaded Clank simulation driver

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.lang.StringBuilder;

/*
 * OPTIMIZATIONS EXPLORED
 *
 * RE_WRITE_CHECK: Writes that don't not change the value of a memory location can be ignored
 * OPT_BUFFS: Once an address is added to the write-back buffer, it no longer needs to be tracked by the read buffer
 * WRITE_BUFF_NO_OVRFLW: Should we cause a checkpoint for write buffer overflows
 * IGNORE_TEXT_SEG_ACCESSES: Ignore memory access to the---usually read-only---text segment, for safety, any write to the text segment will create a checkpoint
 * LATEST_CHKPT: Push off taking a checkpoint until the latest possible time
 * COMPILER_SUPPORT: Ignore Program Idempotent memory accesses.  This requires changes to the input memory access trace; place here for documentation.
*/

class MemoryAccess
{
    public final long ticks;
    public final long insns;
    public final int address;
    public final char type;
    public final boolean valueChanged;
    public final int newValue;
    
    public MemoryAccess(long pTicks, long pInsns, int pAddress, char pType, int pOldValue, int pNewValue)
    {
        ticks = pTicks;
        insns = pInsns;
        address = pAddress;
        type = pType;
        newValue = pNewValue;
        
        valueChanged = !(pOldValue == pNewValue);
    }
    
    public String toString()
    {
        return String.format("%ld\t%ld\t%c\t%08X", ticks, insns, type, address);
    }
}

public class Clank
{
    private static int ON_PERIOD = 100000;  // 100ms to match other work in the space
    private static int WDT = 99999; // Really large to effectively disable watchdog timer
    private static final boolean EXPLORE_BUFFER_OPTS = true;
    private static final boolean ONE_OFF = false;
    
    private static boolean RE_WRITE_CHECK = true;           // Writes that don't not change the value of a memory location can be ignored
    private static boolean OPT_BUFFS = true;                // Once an address is added to the write-back buffer, it no longer needs to be tracked by the read buffer
    private static boolean WRITE_BUFF_NO_OVRFLW = true;    // Should we cause a checkpoint for write buffer overflows
    private static boolean IGNORE_TEXT_SEG_ACCESSES = true; // Ignore memory access to the---usually read-only---text segment, for safety, any write to the text segment will create a checkpoint
    private static boolean LATEST_CHKPT = true;             // Push off taking a checkpoint until the latest possible time
    
    private static final ArrayList<MemoryAccess> accesses = new ArrayList<MemoryAccess>();
    
    private static void readMemoryAccessLog(String pFilename)throws Exception
    {
        try{
            BufferedReader in = new BufferedReader(new FileReader(pFilename));
            String line;
            while((line = in.readLine()) != null)
            {
                // Line: ticks  insns   R/W address oldV    [newV]
                String[] parts = line.split("\t");
                if(parts.length < 5)
                {
                    continue;
                }
                
                int newValue = 0;
                
                long ticks = Long.parseLong(parts[0]);
                long insns = Long.parseLong(parts[1]);
                char op = parts[2].charAt(0);
                int address = Integer.parseInt(parts[3], 16);
                int oldValue = Integer.parseInt(parts[4]);
                
                if(parts.length > 5)
                {
                    newValue = Integer.parseInt(parts[5]);
                }
                else
                {
                    newValue = oldValue;
                }

                accesses.add(new MemoryAccess(ticks, insns, address, op, oldValue, newValue));
            }
            
            in.close();
        }
        catch(IOException ioe)
        {
            System.err.println("ERROR: Could not open file: " + pFilename);
            System.exit(1);
        }
    }
    
    public static void main(String args[])throws Exception
    {
        if(args.length != 2)
        {
            System.err.println("Usage: java Clank file.ext numThreads");
            System.exit(1);
        }

        // Parse the number of threads to use from the command line
        int numThreads = Integer.parseInt(args[1]);
        
        // Read the entire memory access log into memory
        readMemoryAccessLog(args[0]);
        
        // Output stats about the loaded memory access log
        System.err.println("" + accesses.size() + " memory accesses");
        System.err.println("Spanning " + accesses.get(0).ticks + " to " + accesses.get(accesses.size() - 1).ticks + " ticks");

        Trial[] trials = new Trial[numThreads];
        
        // Skip to the end if we are not exploring buffer optimizations
        for(int optConfig = EXPLORE_BUFFER_OPTS ? 0 : 31; optConfig < 32; ++optConfig)
        {
            // Select the current configuration of on-chip buffers
            RE_WRITE_CHECK = ((optConfig & 0x1) == 0) ? false : true;
            OPT_BUFFS = ((optConfig & 0x2) == 0) ? false : true;
            WRITE_BUFF_NO_OVRFLW = ((optConfig & 0x4) == 0) ? false : true;
            IGNORE_TEXT_SEG_ACCESSES = ((optConfig & 0x8) == 0) ? false : true;
            LATEST_CHKPT = ((optConfig & 0x10) == 0) ? false : true;
            
            // Print out the filename with opt config appended
            System.out.println("File: " + args[0] + "_" + String.format("%02X", optConfig & 0x1F));
            System.err.println("File: " + args[0] + "_" + String.format("%02X", optConfig & 0x1F));
            
            int trialsActive = 0;
            // Experiment with no APB and then a range of entries from 1 to 16, doubling num entries each iteration
            for(int addressPrefixEntries = 0; addressPrefixEntries <= 16; addressPrefixEntries = addressPrefixEntries == 0 ? 1 : addressPrefixEntries << 1)
            {
                // Only one value for no APB, otherwise decrease address entry size by 2 bits down to 4 bits
                for(int entryBits = addressPrefixEntries == 0 ? 30 : 20; entryBits >= 4 && entryBits != 28; entryBits -= 2)
                {
                    // Must have at least 1 RFB entry
                    for(int rbs = 1; rbs <= 32; rbs <<= 1)
                    {
                        for(int wbs = 0; wbs <= 32; wbs = (wbs == 0) ? 1 : wbs << 1)
                        {
                            for(int wbbs = 0; wbbs <= 32; wbbs = (wbbs == 0) ? 1 : wbbs << 1)
                            {
                                trials[trialsActive] = new Trial(accesses, rbs, wbs, wbbs, entryBits, addressPrefixEntries, WDT, ON_PERIOD, RE_WRITE_CHECK, OPT_BUFFS, WRITE_BUFF_NO_OVRFLW, IGNORE_TEXT_SEG_ACCESSES, LATEST_CHKPT);
                                ++trialsActive;
                                
                                // Run when we have created numThreads trials
                                if((trialsActive % numThreads) == 0)
                                {
                                    runNTrials(trials, trialsActive);
                                    trialsActive = 0;
                                }
                            }
                        }
                    }
                }
            }
            
            // Run through any remaining trials
            runNTrials(trials, trialsActive);
        }
    }
    
    private static void runNTrials(Trial[] pTrials, int pNumTrials)throws Exception
    {
        // Start all of the threads
        for(int thread = 0; thread < pNumTrials; ++thread)
        {
            pTrials[thread].start();
        }
        
        // Wait for the threads to complete execution
        // Print results, in order, when done
        for(int thread = 0; thread < pNumTrials; ++thread)
        {
            pTrials[thread].join();
            System.out.println(pTrials[thread].result.toString());
        }
    }
}
