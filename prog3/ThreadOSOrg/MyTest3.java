import java.util.Date;

class MyTest3 extends Thread {

    private int pairs;

    public MyTest3(String[] args)
    {
        pairs = Integer.parseInt(args[1]);
    }

    public void run( ) 
    {
        String[] compArgs = SysLib.stringToArgs("TestThread3 A 500");
        String[] diskArgs = SysLib.stringToArgs("TestThread3 B 1000");

        long start = (new Date()).getTime();

        for(int i = 0; i < pairs; i++)
        {
            SysLib.exec(compArgs);
            SysLib.exec(diskArgs);
        }

        for(int i = 0; i < (pairs * 2); i++)
            SysLib.join();

        long end = (new Date()).getTime();

        SysLib.cout("elapsed time = " + (start - end) + " msec. \n");
        SysLib.exit();
    }
}
