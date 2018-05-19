import java.util.Date;

class MyTest3 extends Thread {

    private int pairs;

    public MyTest3(String[] args)
    {
        pairs = Integer.parseInt(args[0]);
    }

    public void run( ) 
    {
        String[] compArgs = SysLib.stringToArgs("TestThread3AB A 50");
        String[] diskArgs = SysLib.stringToArgs("TestThread3AB B 500");

        long start = (new Date()).getTime();

        for(int i = 0; i < pairs; i++)
        {
            SysLib.exec(compArgs);
            SysLib.exec(diskArgs);
        }

        for(int i = 0; i < (pairs * 2); i++)
            SysLib.join();

        long end = (new Date()).getTime();

        SysLib.cout("elapsed time = " + (end - start) + " msec. \n");
        SysLib.exit();
    }
}
