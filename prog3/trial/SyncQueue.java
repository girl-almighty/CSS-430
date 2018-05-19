import java.util.*;

public class SyncQueue {

    private QueueNode[] queue;

    public void init(int numQNodes)
    {
        queue = new QueueNode[numQNodes];
        for(int i = 0; i < queue.length; i++)
            queue[i] = new QueueNode();
    }   

    public SyncQueue() 
    {
        init(10);
    }

    public SyncQueue(int condMax)
    {
        if(condMax > 0)
            init(condMax);
        else
            init(10);
    }

    // wait
    public int enqueueAndSleep(int condition)
    {
        if(condition >= 0 && condition < queue.length)
            return queue[condition].sleep();
        return -1;
        //if(condition >= queue.length || condition < 0)
        //    return -1;
        //return queue[condition].sleep();
    }

    public void dequeueAndWakeup(int condition, int tid)
    {
        if(condition >= 0 && condition < queue.length)
            queue[condition].wakeUp(tid);
    }

    public void dequeueAndWakeup(int condition)
    {
        dequeueAndWakeup(condition, 0);
    }
}
