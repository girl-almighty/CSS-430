/*----------- processes.cpp -----------*/
/*                                     */
/*  Iris Favoreal - CSS 430 B          */
/*                                     */
/*  PROGRAM 1 - PART 1                 */
/*  Created - 4/3/18                   */
/*  Last modified - 4/11/18            */
/*                                     */
/*  Purpose:                           */
/*  This program ultimately            */
/*  emulates what the command line:    */
/*  "ps -A | grep [argument] | wc -l"  */
/*  executes. This is done through 4   */
/*  processes:                         */
/*      - parent: waits                */
/*      - child: executes wc -l        */
/*      - grandchild: executes grep    */
/*        [argument]                   */
/*      - great-grandchild: executes   */
/*         ps -A                       */
/*                                     */
/*-------------------------------------*/

#include <iostream>
#include <unistd.h> // fork, pipe, execlp methods
#include <stdlib.h> // exit method
#include <stdio.h> // perror method
#include <sys/wait.h> // wait methods
using namespace std;

// ULTIMATELY, this program executes "ps -A | grep argv[1] | wc -l"
// like you would on a command-line terminal

int main (int argc, const char * argv[])
{
    // initializing variables
    enum {READ, WRITE};             // pipe's read and write ends
    pid_t pid;                      // tracks current process ID
    int pipeFD[2], pipeFD_2[2];     // file descriptors
    
    // double checks that there are two arguments:
    // one for the program execution (./processes) and
    // another to use as the grep argument.
    if(argc != 2)
        cout << "Invalid arguments." << endl;

    // forking child, and making sure it's succesful.
    if((pid = fork()) < 0)
    {
        perror("Error during fork.");
        exit(EXIT_FAILURE);
    }
    
    // piping the first file descriptor, and making sure it's succesful.
    // only the child, grandchild, and great-grandchild will have access to this pipe.
    if(pipe(pipeFD) < 0)
    {
        perror("Error in creating pipe one.");
        exit(EXIT_FAILURE);
    }

    // checking pid to see which process we are in.
    if(pid == 0) // child process
    {
        // forking grandchild, and making sure it's succesful.
        if((pid = fork()) < 0)
        {
            perror("Error during fork.");
            exit(EXIT_FAILURE);
        }
        
        // piping the second file descriptor, and making sure it's succesful.
        // only the grandchild, and great-grandchild will have access to this pipe.
        if(pipe(pipeFD_2) < 0)
        {
            perror("Error in creating pipe two.");
            exit(EXIT_FAILURE);
        }

        // checking pid to see which process we are in.
        if(pid == 0) // grandchild process
        {
            // forking great-grandchild, and making sure it's succesful.
            if((pid = fork()) < 0)
            {
                perror("Error during fork.");
                exit(EXIT_FAILURE);
            }

            // checking pid to see which process we are in.
            if(pid == 0) // great-grandchild process' execution call
            {
                // great-grandchild has access to first pipe but doesn't need/use it.
                // so we close the read and write ends of the first pipe.
                close(pipeFD[READ]);
                close(pipeFD[WRITE]);
                
                // it only needs the second pipe
                // but it only needs to write to it whatever output it generates.
                // so we close the read end of the second pipe, and redirect it's stdout
                // to the write end of the second pipe.
                close(pipeFD_2[READ]);
                dup2(pipeFD_2[WRITE], 1);
                
                // once we have all the pipes set up,
                // we can now do system call and execute "ps -A".
                // the output will be sent to the write end of pipe 2.
                execlp("ps", "ps", "-A", NULL);
            }
            else // grandchild process' execution call
            {
                // the grandchild process has access to both pipes.
                // grep needs to execute using the output from ps -A.
                // it needs to search the data from the pipe that the great-grandchild wrote to.
                // it needs to read from pipe 2 only, so we close the write end of pipe 2.
                // it also needs to only write its resulting output to the first pipe,
                // so we close the read end of the first pipe.
                close(pipeFD[READ]);
                close(pipeFD_2[WRITE]);
                
                // we redirect its stdout so that it is then sent to the write end of pipe 1.
                // we also redirect its stdin so that it reads data from the read end of pipe 2.
                dup2(pipeFD[WRITE], 1);
                dup2(pipeFD_2[READ], 0);
                
                // once we have all the pipes set up,
                // we can now do system call and execute "grep (term to look for)".
                // the term we use to execute grep will come from the second argument typed on terminal
                // during the command-line program call.
                // the output will be sent to the write end of pipe 1.
                execlp("grep", "grep", argv[1], NULL);
            }
            exit(EXIT_SUCCESS); // terminates grandchild/great-grandchild processes once execution is finished.
        }
        else // child process' execution call
        {
            // this process only has access to the first pipe,
            // but it only needs to read from this pipe so we close the write end of pipe 1.
            close(pipeFD[WRITE]);
            
            // we then redirect its stdin so that it reads and executes based on the data from pipe 1.
            dup2(pipeFD[READ], 0);
            
            // once the pipe is set up,
            // we then execute "wc -l".
            // the output will be generated normally and sent to the normal stdout.
            execlp("wc", "wc", "-l", NULL);
        }
        exit(EXIT_SUCCESS); // terminates grandchild/child processes once execution is finished.
    }
    else // parent process
        wait(NULL); // parent waits for all child processes to finish execution and terminate.
    
    exit(EXIT_SUCCESS); // once here, we executed everything perfectly, so we exit succesfully.
    return 0;
}
