import java.util.*;

public class Scheduler extends Thread
{
    private Vector[] queues; // will contain 3 vectors as the 3 queues
    private int timeSlice;
    private static final int DEFAULT_TIME_SLICE = 500; // quantum

    private boolean[] tids; // Indicate which ids have been used
    private static final int DEFAULT_MAX_THREADS = 10000;

    // Allocate an ID array, each element indicating if that id has been used
    private int nextId = 0;


    private void initTid( int maxThreads ) 
    {
    	tids = new boolean[maxThreads];
    	for ( int i = 0; i < maxThreads; i++ )
    	    tids[i] = false;
    }

    // Search an available thread ID and provide a new thread with this ID
    private int getNewTid( ) 
    {
    	for ( int i = 0; i < tids.length; i++ ) 
        {
    	    int tentative = ( nextId + i ) % tids.length;
    	    if ( tids[tentative] == false ) 
            {
        		tids[tentative] = true;
        		nextId = ( tentative + 1 ) % tids.length;
        		return tentative;
    	    }
    	}
    	return -1;
    }

    // Return the thread ID and set the corresponding tids element to be unused
    private boolean returnTid( int tid ) 
    {
    	if ( tid >= 0 && tid < tids.length && tids[tid] == true ) 
        {
    	    tids[tid] = false;
    	    return true;
    	}
    	return false;
    }

    // Retrieve the current thread's TCB from the queue
    public TCB getMyTcb( ) 
    {
    	Thread myThread = Thread.currentThread( ); // Get my thread object
    	synchronized( queues ) 
        {
            // for every vector in the array of vectors,
            // we look through the elements of each vector for the current thread.
    	    for ( int i = 0; i < queues.length; i++ ) 
            {
                for(int j = 0; j < queues[i].size(); j++)
                {
            		TCB tcb = ( TCB )queues[i].elementAt( j );
            		Thread thread = tcb.getThread( );
            		if ( thread == myThread ) // if this is my TCB, return it
            		    return tcb;
                }
    	    }
    	}
    	return null;
    }

    // Return the maximal number of threads to be spawned in the system
    public int getMaxThreads( ) 
    {
	    return tids.length;
    }

    public Scheduler( ) 
    {
    	timeSlice = DEFAULT_TIME_SLICE;
        queues = new Vector[3];
        for(int i = 0; i < queues.length; i++) // creating 3 vectors into the queues array
    	    queues[i] = new Vector( );
    	initTid( DEFAULT_MAX_THREADS );
    }

    public Scheduler( int quantum ) 
    {
    	timeSlice = quantum;
        queues = new Vector[3];
        for(int i = 0; i < queues.length; i++)
            queues[i] = new Vector( );
    	initTid( DEFAULT_MAX_THREADS );
    }

    // A constructor to receive the max number of threads to be spawned
    public Scheduler( int quantum, int maxThreads ) 
    {
    	timeSlice = quantum;
        queues = new Vector[3];
        for(int i = 0; i < queues.length; i++)
            queues[i] = new Vector( );
    	initTid( maxThreads );
    }

    private void schedulerSleep( ) 
    {
    	try 
        {
    	    Thread.sleep( timeSlice );
    	} 
        catch ( InterruptedException e ) 
        {}
    }

    // A modified addThread of p161 example
    public TCB addThread( Thread t ) 
    {
    	TCB parentTcb = getMyTcb( ); // get my TCB and find my TID
    	int pid = ( parentTcb != null ) ? parentTcb.getTid( ) : -1;
    	int tid = getNewTid( ); // get a new TID
    	if ( tid == -1)
    	    return null;
    	TCB tcb = new TCB( t, tid, pid ); // create a new TCB
    	queues[0].add( tcb );
    	return tcb;
    }

    // Removing the TCB of a terminating thread
    public boolean deleteThread( ) 
    {
	TCB tcb = getMyTcb( ); 
	if ( tcb != null )
	    return tcb.setTerminated( );
	else
	    return false;
    }

    public void sleepThread( int milliseconds ) 
    {
    	try 
        {
    	    sleep( milliseconds );
    	} 
        catch ( InterruptedException e ) 
        {}
    }

    // runs execution of all threads in the first queue which has the highest priority.
    // each execution is done for 500ms.
    public void runQueueZero()
    {
        Thread current = null;

        while(true)
        {
            try
            {
                // represents the element of the current queue in the array of queues.
                int queueNum = 0;
                
                // if queue 0 is empty, we go back to the run() method which
                // will then execute the execution of the second queue.
                if(queues[queueNum].size() == 0)
                    return;

                // we traverse the queue, checking each element if it is terminated.
                // if it is, we remove it from the queue, restart this while loop,
                // and check the new front of the queue until we get a thread that is not terminated.
                TCB currentTCB = (TCB)queues[queueNum].firstElement( );
                if ( currentTCB.getTerminated( ) == true ) 
                {
                    queues[queueNum].remove( currentTCB );
                    returnTid( currentTCB.getTid( ) );
                    continue;
                }
                
                // at this point, the current thread at the front of the queue is not terminated.
                // if it's started execution, we resume it, otherwise, we start its execution.
                current = currentTCB.getThread( );
                if ( current != null ) 
                {
                    if ( current.isAlive( ) )
                        current.resume();
                    else 
                        current.start( ); 
                }
                
                // we put the scheduler to sleep according to the quantum which is 500ms.
                // this is so the thread can do its job for the span of the quantum.
                schedulerSleep( );
                // System.out.println("* * * Context Switch * * * ");

                // we lock the array of vectors so only one thread can access it at a time.
                // if the current thread hasn't finished exxecuting, we suspend it.
                // we then remove it from the first queue and add it to the back of the second queue.
                synchronized ( queues ) 
                {
                    if ( current != null && current.isAlive( ) )
                        current.suspend();
                    queues[queueNum].remove( currentTCB );
                    queues[queueNum + 1].add( currentTCB );
                }
            } 
            catch ( NullPointerException e3 ) 
            { };
        }
    }

    // runs execution of all threads in the second queue which
    // has the next highest priority after the first queue.
    // each thread in this queue is executed for 1000ms, but in 500ms increments,
    // because for every 500ms, we want to check the first queue
    // if it has any new threads to execute since those need to be executed first
    // due to their highest priority.
    public void runQueueOne()
    {
        Thread current = null;

        while(true)
        {
            try
            {
                // setting the element to the second vector of the queues array
                int queueNum = 1;
                
                if(queues[queueNum].size() == 0)
                    return;

                TCB currentTCB = (TCB)queues[queueNum].firstElement( );
                if ( currentTCB.getTerminated( ) == true ) 
                {
                    queues[queueNum].remove( currentTCB );
                    returnTid( currentTCB.getTid( ) );
                    continue;
                }
                current = currentTCB.getThread( );
                if ( current != null ) 
                {
                    if ( current.isAlive( ) )
                        current.resume();
                    else 
                        current.start( ); 
                }
                
                // scheduler sleeps for 500ms (first increment) ...
                schedulerSleep( );
                // System.out.println("* * * Context Switch * * * ");

                // if the thread hasn't finished executing after the first 500 ms increment,
                // we suspend it and execute runQueueZero() which will check the first queue
                // for new threads with higher priority.
                // we then resume and let the scheduler sleep for the second and final 500 ms increment,
                // which allows threads in the second queue to execute with a quantum of 1000 ms total.
                if ( current != null && current.isAlive( ) )
                {
                    current.suspend();
                    runQueueZero();
                    current.resume();
                    schedulerSleep();
                }

                // we lock the array of queues.
                // if the current thread hasn't finished executing after the 1000 ms time slice,
                // we remove it from the second queue and add it to the back of the third queue.
                synchronized ( queues ) 
                {
                    if ( current != null && current.isAlive( ) )
                        current.suspend();
                    queues[queueNum].remove( currentTCB );
                    queues[queueNum + 1].add( currentTCB );
                }
            } 
            catch ( NullPointerException e3 ) 
            { };
        }
    }

    // runs the execution of the threads in the third and final queue which has lowest priority.
    // each thread is executed for 2000 ms but in 4 500 ms increments, to allow checking of
    // new threads in queue 1 and 2 after each increment since those threads must be executed first.
    public void runQueueTwo()
    {
        Thread current = null;

        while(true)
        {
            try
            {
                // setting the element to the third vector of the queues array
                int queueNum = 2;
                if(queues[queueNum].size() == 0)
                    return;

                TCB currentTCB = (TCB)queues[queueNum].firstElement( );
                if ( currentTCB.getTerminated( ) == true ) 
                {
                    queues[queueNum].remove( currentTCB );
                    returnTid( currentTCB.getTid( ) );
                    continue;
                }
                current = currentTCB.getThread( );
                if ( current != null ) 
                {
                    if ( current.isAlive( ) )
                        current.resume();
                    else 
                        current.start( ); 
                }
                
                // 1st 500 ms increment of the total quantum
                schedulerSleep( );
                // System.out.println("* * * Context Switch * * * ");

                // we set an increment counter for a while loop to 1
                // since we've already finished executing 1 increment.
                int timeIncrement = 1;
                
                // for every increment, we check if the thread has finished executing.
                while(timeIncrement < 4)
                {
                    // if not, we suspend it and call on the execution of the first queue
                    // then the second queue so that any new threads on those queues may be executed.
                    // we then go back to the thread in this queue and resume its execution
                    // for the next increment.
                    if ( current != null && current.isAlive( ) )
                    {
                        current.suspend();
                        runQueueZero();
                        runQueueOne();
                        current.resume();
                        schedulerSleep();
                        timeIncrement++;
                    }
                    // this loop stops after the fourth increment which totals to 2000 ms execution time,
                    // or if the current thread in this queue has finished execution before the 2000 ms
                    // quantum which triggers the else condition.
                    else
                        break;
                }

                // we lock the array of queues.
                // if the current thread has not finished executing after the 2000 ms quantum,
                // we suspend it and rotate it to the back of the same third queue.
                synchronized ( queues ) 
                {
                    if ( current != null && current.isAlive( ) )
                        current.suspend();
                    queues[queueNum].remove( currentTCB ); // rotate this TCB to the end
                    queues[queueNum].add( currentTCB );
                }
            } 
            catch ( NullPointerException e3 ) 
            { };
        }       
    }

    // overrides the run() method of the thread,
    // so that each thread executes this run() instead.
    // every thread basically is constantly checking queue 1, 2, and 3 in that order,
    // executing the threads in each one according to their quantums and priorities.
    public void run( ) 
    {   
        while ( true ) 
        {
            runQueueZero();
            runQueueOne();
            runQueueTwo();
        }
    }
}
