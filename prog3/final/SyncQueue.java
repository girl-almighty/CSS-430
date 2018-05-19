/*----------- QueueNode.java ----------*/
/*                                     */
/*  Iris Favoreal - CSS 430 B          */
/*                                     */
/*  PROGRAM 3 - PART 1                 */
/*  Created - 5/6/18                   */
/*  Last modified - 5/9/18             */
/*                                     */
/*  Purpose:                           */
/*  
/*                                     */
/*-------------------------------------*/


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

    public int enqueueAndSleep(int condition)
    {
        if(condition >= 0 && condition < queue.length)
            return queue[condition].sleep();
        return -1;
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
