import java.util.Date;

class TestThread3 extends Thread {

    private char threadType;
    private double arg;
    private final char COMP = 'A';
    private final char DISK = 'B';


    public TestThread3(String[] args)
    {
        threadType = args[0].charAt(0);
        arg = Double.parseDouble(args[1]);
        if(arg < 0 || arg > 1000)
            arg = 1000;
    }

    public void run( ) 
    {
        if(threadType == COMP)
            runComp();
        else if(threadType == DISK)
            runDisk();
    }

    public void runComp()
    {
        Double loop = Math.pow(arg, arg);
        for(int i = 0; i < loop; i++)
            arg = Math.pow(arg, 2);
    }

    public void runDisk()
    {
        byte[] buffer = new byte[512];
        for(int i = 0; i < arg; i++)
            SysLib.rawwrite(i, buffer);
        for(int i = 0; i < arg; i++)
            SysLib.rawread(i, buffer);
    }
}
