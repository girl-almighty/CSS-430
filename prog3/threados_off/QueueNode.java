import java.util.*;

public class QueueNode {

    private Vector<Integer> conditionTids;

    public QueueNode()
    {
        conditionTids = new Vector<Integer>();
    }

    public synchronized int sleep()
    {
        if(conditionTids.size() == 0)
        {
            try 
            {
                wait( );
            } 
            catch ( InterruptedException e ) 
            {
                SysLib.cerr(e.toString() + "\n");  //Error message
            }
            return conditionTids.remove(0);
        }
        return -1;
    }

    public synchronized void wakeUp(int tid)
    {
        conditionTids.add(tid);
        notify();
    }
}
