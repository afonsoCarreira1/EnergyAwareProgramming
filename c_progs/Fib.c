#include <stdio.h>
#include <stdlib.h>
#include <signal.h>
#include <unistd.h>
#include <sys/types.h>
#include <unistd.h>
#include <limits.h>
#include <linux/limits.h>
#include <errno.h>

static int FIB_SIZE = 45;


long fib(int n) {
    if(n == 0)
    {return 0;}
    else if(n == 1)
    {return 1;}
    else
    {return fib(n - 1) + fib(n - 2);}
}

void create_directory(const char *dir_name) {
    // Attempt to create the directory
    if (mkdir(dir_name, 0755) == -1) {
        // Check if the error is that the directory already exists
        if (errno == EEXIST) {
            printf("Directory '%s' already exists.\n", dir_name);
        } else {
            // Print error message for other errors
            perror("Error creating directory");
        }
    } else {
        printf("Directory '%s' created successfully.\n", dir_name);
    }
}


void write_pid_to_file(const char *filename) {
    const char *directory_name = "c_progs";
    create_directory(directory_name);

    // Open the file for writing; create it if it doesn't exist
    FILE *file = fopen(filename, "w");
    if (file == NULL) {
        perror("Failed to open file");
        return;
    }

    // Get the PID of the current process
    pid_t myPid = getpid();

    // Write the PID to the file, overwriting any previous content
    fprintf(file, "%d\n", myPid);

    // Close the file
    fclose(file);
}


int main(int argc, char *argv[]) {
    if (argc < 2) {
        fprintf(stderr, "Usage: %s <pid>\n", argv[0]);
        return 1;
    }

    // Parse the PID from command line argument
    pid_t pid = atoi(argv[1]);
    //printf("pid: %d\n",pid);
    write_pid_to_file("c_progs/pidfile.txt");
    sleep(1);
    // Send SIGUSR1 to the target process
    if (kill(pid, SIGUSR1) == -1) {
        perror("Failed to send SIGUSR1");
        return 1;
    }
    // Compute the Fibonacci number
    long f = fib(FIB_SIZE);
    printf("main: %ld\n", f);

    // Send SIGUSR2 to the target process
    if (kill(pid, SIGUSR2) == -1) {
        perror("Failed to send SIGUSR2");
        return 1;
    }

    return 0;
}