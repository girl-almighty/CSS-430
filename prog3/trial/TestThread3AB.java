import java.util.Date;

class TestThread3AB extends Thread {

    private char threadType;
    private double arg;
    private final char COMP = 'A';
    private final char DISK = 'B';


    public TestThread3AB(String[] args)
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
	    SysLib.exit();
    }

    public void runComp()
    {
        for(int i = 0; i < arg; i++)
            arg = Math.log( Math.pow(arg, 2) );
	    SysLib.cout("comp finished...\n");
    }

    public void runDisk()
    {
        byte[] buffer = new byte[512];
        for(int i = 0; i < arg; i++)
	    {
            SysLib.rawwrite(i, buffer);
	        SysLib.rawread(i, buffer);
	    }
	    SysLib.cout("disk finished...\n");
    }
}
