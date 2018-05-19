import java.util.Date;

class TestThread3AB extends Thread {

    
    private char threadType;        // command-line argument specifying what job to do
    private double arg;             // number used in the job
    private final char COMP = 'A';  // constant signifying A arg to do computations
    private final char DISK = 'B';  // constant signifying B arg to do disk reads/writes


    // default constructor taking in arguments from the command line
    public TestThread3AB(String[] args)
    {
        // first argument is parsed into a char and stored in class variable
        threadType = args[0].charAt(0);

        // second argument is parsed into a double and stored in class variable
        arg = Double.parseDouble(args[1]);
        if(arg < 0 || arg > 1000)
            arg = 1000;
    }

    // thread job
    public void run( ) 
    {
        if(threadType == COMP)
            runComp();
        else if(threadType == DISK)
            runDisk();

        // after thread executs its specified job, we exit
	    SysLib.exit();
    }

    // A-type threads do this job which is just computations
    public void runComp()
    {
        for(int i = 0; i < arg; i++)
            arg = Math.log( Math.pow(arg, 2) );
	    SysLib.cout("comp finished...\n");
    }

    // B-type threads do this job which is disk reads/writes
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
