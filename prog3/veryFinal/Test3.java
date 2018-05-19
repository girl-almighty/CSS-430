import java.util.Date;

class Test3 extends Thread {

    // command-line argument.
    // should be a number from 1-4.
    // signifies number of pairs of childs threads that will be spawned
    // by this test.
    private int pairs;

    // default constructor which takes in 1 integer argument.
    public Test3(String[] args)
    {
        // parse the first (and only) string argument into an integer.
        pairs = Integer.parseInt(args[0]);
    }

    public void run( ) 
    {
        // we parse 2 strings into two separate arrays of strings that will
        // be the commands executed by this Test program.
        // compArgs = initializes A threads that will run computations in 50 loops.
        // diskArgs = initializes B threads that will read/write to/from disk on 500 blocks.
        String[] compArgs = SysLib.stringToArgs("TestThread3AB A 50");
        String[] diskArgs = SysLib.stringToArgs("TestThread3AB B 500");

        // we document the time right before we execute the above arguments.
        // this signifies the spawn time of child threads.
        long start = (new Date()).getTime();

        // we then spawn pairs of child threads according to the command-line argument.
        // each pair will consist of one thread doing computations and another thread
        // doing disk reads & writes.
        for(int i = 0; i < pairs; i++)
        {
            SysLib.exec(compArgs);
            SysLib.exec(diskArgs);
        }

        // after initialization and execution of all threads, we make this parent thread
        // do SysLib.join() system calls according to the number of child threads it spawned.
        // this is so it terminates only when all of its child threads terminate.
        for(int i = 0; i < (pairs * 2); i++)
            SysLib.join();

        // at this point, all child threads have terminated so we document
        // the time at which all of them have exited.
        long end = (new Date()).getTime();

        // calculate duration from spawn time to termination time and output it.
        SysLib.cout("elapsed time = " + (end - start) + " msec. \n");

        // parent terminates.
        SysLib.exit();
    }
}
