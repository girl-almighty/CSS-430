

/*------------- shell.java ------------*/
/*                                     */
/*  Iris Favoreal - CSS 430 B          */
/*                                     */
/*  PROGRAM 1 - PART 2                 */
/*  Created - 4/3/18                   */
/*  Last modified - 4/10/18            */
/*                                     */
/*  Purpose:                           */
/*  This is a shell program that       */
/*  executes commands from the         */
/*  command-line console, making       */
/*  sure that processes are executed   */
/*  according to the usage of & and ;  */
/*  on the command line. Commands      */
/*  delimited by "&" will be executed  */
/*  concurrently, while commands       */
/*  delimited by ";" will be executed  */
/*  sequentially according to the      */
/*  order they were typed in.          */
/*                                     */
/*-------------------------------------*/

import java.util.*;

public class Shell extends Thread 
{
    private int line = 1; // a counter: keeps track of the number of the command line.
    boolean loop = true; //boolean loop = true; // shell command line keeps running until this is switched to false.

    public void run() 
    {
        // loops the command line shell prompt.
        // one loop = one command line = one line of keyboard input.
        while (loop) 
		{
            // each command line starts with this output
            // along with the current number of the command line.
            SysLib.cout("Shell[" + line + "]% ");
            line++;

            // cin returns a line of keyboard input into stringbuffer,
            // which is then converted into a string.
            StringBuffer buffer = new StringBuffer();
            SysLib.cin(buffer);
            String input = buffer.toString();

            // converted string is then checked for three conditions:
            // 1) if empty, ignore the rest of the while block and restart loop.
            if (input.isEmpty())
                continue;

            // 2) if string = "exit", switch the loop's boolean to false
            //    so that loops exits and executes SysLib.exit() and sync() outside the while loop.
            else if (input.compareTo("exit") == 0 || input.compareTo(" exit") == 0 ||
                    input.compareTo("Exit") == 0 || input.compareTo(" Exit") == 0)
                loop = false;

            // 3) anything else will be parsed into multiple strings using ";" as a delimeter.
            //    any resulting strings that are empty will be ignored,
            //    while other strings will be passed onto execute() for further processing.
            //    the purpose of this is so that each command delimited by ";" is executed
            //    one-by-one in the order they had as the keyboard input.
            //    we also pass in a null vector, specifying that this there are no existing
            //    child threads that we need to track in order to terminate later.
            else 
			{
                for (String commandLine : input.split(";")) 
				{
                    if (commandLine.isEmpty())
                        continue;
                    execute(commandLine, null);
                }
            }
        }

        // this command will only be reached if the keyboard input was "exit".
        SysLib.sync();
        SysLib.exit();
    }

    // any keyboard input passed onto this command will not contain ";".
    // input passed onto this may only be a single command,
    // or commands separated by "&" which will be executed concurrently.
    public void execute(String commandLine, Vector<Integer> p)
	{

        // a vector is created to store thread IDs of the non-parent threads.
        // if the param is null, it is given an initial size of 1,
        // in case there is actually only one command because we haven't parsed
        // the commandLine param yet into possibly several commands delimited by "&".
        // we want the vector's size to reflect the actual # of child threads it is tracking,
        // so that when we remove them one-by-one from the vector as they terminate,
        // we can accurately check if the vector is empty, meaning all concurrent commands,
        // or single commands in this sequence have executed and we can go back to run().
        Vector<Integer> processes = p;
        if(processes == null)
            processes = new Vector<Integer>(1);

        // for each command line delimited by ";", we parse it further
        // to split commands that may be delimited by "&".
        for (String input : commandLine.split("&")) 
		{

            // resulting empty string is ignored.
            // may happen if there was a lone "&" nested between ";".
            if(input.isEmpty())
                continue;

            // any exit commands will switch the main shell prompt while loop in run()
            // so that it doesn't prompt again, and reaches the exit() command.
            if(input.compareTo("exit") == 0 || input.compareTo(" exit") == 0 ||
                    input.compareTo("Exit") == 0 || input.compareTo(" Exit") == 0 ||
                    input.compareTo("exit ") == 0 || input.compareTo("Exit ") == 0)
            {
                loop = false;
                terminateChildren(processes);
                return;
            }
            else {
                // each single command is then converted into a string array as arguments
                // each command array is then passed to exec() so it can be executed.
                // if the exec() call doesn't return -1, the command was executed properly,
                // which means that a new thread was created and a thread ID was instead returned.
                // we add this child process ID into our vector.
                String[] args = SysLib.stringToArgs(input);
                int tid = SysLib.exec(args);
                if (tid != -1)
                    processes.add(tid);
            }
        }

        // if the command ends in a "&", we want current threads to run in the background
        // while we keep prompting the shell command line to accept more input for more threads.
        // we don't want to execute just yet. we also pass in our vector of thread IDs
        // because we don't want to lose this, since we need it to terminate the child threads later.
        if (commandLine.charAt(commandLine.length() - 1) == '&') {
            concurrentCommands(processes);
        }

        // once all concurrent commands have been executed, we terminate children pthreads.
        terminateChildren(processes);
    }

    // this input getter is mainly for when the previous command ended in "&".
    public void concurrentCommands(Vector<Integer> processes)
    {
        boolean concurrentLoop = true; // shell prompt loops while this is true.

        while (loop)
        {
            // each command line starts with this output
            // along with the current number of the command line.
            SysLib.cout("Shell[" + line + "]% ");
            line++;

            // cin returns a line of keyboard input into stringbuffer,
            // which is then converted into a string.
            StringBuffer buffer = new StringBuffer();
            SysLib.cin(buffer);
            String input = buffer.toString();

            // converted string is then checked for three conditions: same as before.
            if (input.isEmpty())
                continue;

            // if we encounter an exit, we switch both input prompt loops to false.
            // the first input prompt loop is in run(), and this loop is the second one.
            else if (input.compareTo("exit") == 0 || input.compareTo(" exit") == 0 ||
                    input.compareTo("Exit") == 0 || input.compareTo(" Exit") == 0)
                loop = concurrentLoop = false;

            // if we get a non-empty command, we stop this loop, so that the only input prompt
            // is coming from run().
            else
            {
                for (String commandLine : input.split(";"))
                {
                    if (commandLine.isEmpty())
                        continue;
                    execute(commandLine, processes);
                }
                concurrentLoop = false;
            }
        }
    }

    public void terminateChildren(Vector<Integer> processes)
    {
        // all single commands or concurrent commands have been executed at this point.
        // they were executed concurrently, because none of the threads
        // were made to wait for other threads to finish executing first.

        // at this point, we want all existing concurrent threads being tracked by our vector
        // to terminate and join their parent. each child thread terminating will be removed
        // from the vector. once the vector is empty, it means that all child threads have terminated.
        // we are then free to go back to run() and execute the next command in sequence
        // as delimited by ";".
        while (!processes.isEmpty())
        {
            // parent thread waits for a child thread to terminate using join().
            // join() will then return the terminated child's thread ID.
            int tid = SysLib.join();

            // if the child's thread ID is being tracked by the vector,
            // we remove it from the vector.
            if (processes.contains(tid))
                processes.removeElement(tid);
        }
    }
}

