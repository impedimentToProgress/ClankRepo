// Class that implements idempotent checking between resets
// Used to verify the correctness of ClankSim
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class GoldenClank
{
    // Print out all of the memory accesses to std.err
    private static final boolean PRINT_LOG = false;
    
    private HashMap<Integer, Integer> readFirst = new HashMap<Integer, Integer>();
    private HashMap<Integer, Integer> writeFirst = new HashMap<Integer, Integer>();
    private int numAccesses = 0;
    
    public GoldenClank(){;}
    
    public void addWrite(int pAddress)
    {
        ++numAccesses;
        
        if(PRINT_LOG)
            System.err.println("" + ticks + "\t" + insns + "\tW\t" + String.format("%08X", pAddress));
        
        if(readFirst.containsKey(pAddress))
        {
            System.err.println("VERIFICATION FAILED: Idempotency violation");
            System.exit(2);
        }
        else if(!writeFirst.containsKey(pAddress))
            writeFirst.put(pAddress, pAddress);
    }
    
    public void addWriteIgnoredAsDupe(int pAddress)
    {
        ++numAccesses;
        
        if(PRINT_LOG)
            System.err.println("" + ticks + "\t" + insns + "\tW\t" + String.format("%08X", pAddress));
        
        if(!writeFirst.containsKey(pAddress))
            writeFirst.put(pAddress, pAddress);
    }
    
    public void addWritePostCP(int pAddress)
    {
        ++numAccesses;
        
        if(PRINT_LOG)
            System.err.println("" + ticks + "\t" + insns + "\tW\t" + String.format("%08X", pAddress));
    }
    
    public void addRead(int pAddress)
    {
        ++numAccesses;
        
        if(PRINT_LOG)
            System.err.println("" + ticks + "\t" + insns + "\tR\t" + String.format("%08X", pAddress));
        
        if(writeFirst.containsKey(pAddress))
            return;
        
        if(!readFirst.containsKey(pAddress))
            readFirst.put(pAddress, pAddress);
    }
    
    public void reset()
    {
        if(PRINT_LOG)
            System.err.println("CHECKPOINT");
        
        writeFirst.clear();
        readFirst.clear();
        numAccesses = 0;
    }
    
    public int getNumAccesses()
    {
        return numAccesses;
    }
    
    private long ticks = 0;
    private long insns = 0;
    public void updateForBetterDiff(long pTicks, long pInsns)
    {
        ticks = pTicks;
        insns = pInsns;
    }
}
