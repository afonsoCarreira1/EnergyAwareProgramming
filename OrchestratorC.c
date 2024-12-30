#include <stdio.h>
#include <stdlib.h>
#include <signal.h>
#include <unistd.h>
#include <string.h>
#include <sys/types.h>
#include <limits.h>
#include <linux/limits.h>
#include <errno.h>
#include <sys/time.h>
#include <wait.h>
#include <fcntl.h>



#define BUFFER_SIZE 256
pid_t childPid;
pid_t powerjoularPid;
time_t startTime;
time_t endTime;
double lastTime;
double totalTime;
short runCProgram;
float totalPower = 0.0;
float lastPower = 0.0;
char* frequency = ".1";


long long getCurrentTimestampMs()
{
    struct timeval tv;
    gettimeofday(&tv, NULL);
    return (long long)(tv.tv_sec) * 1000 + (tv.tv_usec / 1000);
}

void killProcess(pid_t pid)
{
    // Send SIGINT to the specified process ID
    if (kill(pid, SIGINT) == -1)
    {
        perror("Failed to send SIGINT");
        exit(EXIT_FAILURE);
    }
}

pid_t runProcess(const char *command, char *const args[]) {
    pid_t processPid = fork();

    if (processPid < 0) {
        perror("fork failed");
        exit(EXIT_FAILURE);
    }

    if (processPid == 0) {
        // Child process
        int null_fd = open("/dev/null", O_WRONLY);
        if (null_fd < 0)
        {
            perror("Failed to open /dev/null");
            exit(EXIT_FAILURE);
        }

        // Redirect stdout and stderr to /dev/null
        dup2(null_fd, STDOUT_FILENO); // Redirect stdout
        dup2(null_fd, STDERR_FILENO); // Redirect stderr

        // Close the file descriptor for /dev/null as it's no longer needed
        close(null_fd);

        // Run the specified command with arguments
        execvp(command, args);

        // If execvp returns, there was an error
        perror("execvp failed");
        exit(EXIT_FAILURE);
    } else {
        // Parent process can optionally wait for the child to complete
        //int status;
        //waitpid(processPid, &status, 0);
        //printf("Process %d finished with status %d\n", processPid, status);
        return processPid;
    }
}

void runPowejoular()
{
    char childPidStr[20];
    char *command[8];
    snprintf(childPidStr, sizeof(childPidStr), "%d", childPid);
        command[0] = "powerjoular";
        command[1] = "-D";
        command[2] = frequency;
        command[3] = "-p";
        command[4] = childPidStr;
        command[5] = "-f";
        command[6] = "powerjoular.csv";
        command[7] = NULL;
    powerjoularPid = runProcess("powerjoular", command);
}

float sumCpuPower(const char *filename)
{
    FILE *file = fopen(filename, "r"); // Open the file for reading
    if (file == NULL)
    {
        perror("Error opening file");
        return -1; // Return -1 to indicate an error
    }

    char buffer[BUFFER_SIZE];  // Buffer to hold each line
    float totalCpuPower = 0.0; // Variable to accumulate CPU power
    int firstLine = 1;         // Flag to skip the header line

    // Read each line of the file
    while (fgets(buffer, sizeof(buffer), file) != NULL)
    {
        // Skip the header line
        if (firstLine)
        {
            firstLine = 0;
            continue;
        }

        // Split the line by commas
        char *token = strtok(buffer, ",");
        int column = 0;
        float cpuPower = 0.0;

        double freq = strtod(frequency, NULL); //cast frequency to double

        // Iterate through tokens to find the CPU Power column
        while (token != NULL)
        {
            // If we are at the CPU Power column (3rd column, index 2)
            if (column == 2)
            {
                cpuPower = strtof(token, NULL); // Convert the string to float
                totalCpuPower += cpuPower * freq;      // Accumulate the CPU power
                break;                          // No need to process further tokens
            }
            token = strtok(NULL, ",");
            column++;
        }
    }

    fclose(file);         // Close the file
    return totalCpuPower; // Return the total CPU power
}

pid_t readPidInFile(const char *filename)
{
    FILE *file = fopen(filename, "r"); // Open the file for reading
    if (file == NULL)
    {
        perror("Error opening file");
        return -1; // Return -1 to indicate an error
    }

    char buffer[BUFFER_SIZE]; // Buffer to hold the line
    if (fgets(buffer, sizeof(buffer), file) != NULL)
    {
        // Convert the string to pid_t
        char *endptr;
        errno = 0; // Reset errno before conversion
        long pid = strtol(buffer, &endptr, 10);

        // Check for conversion errors
        if (errno != 0)
        {
            perror("Error converting to pid_t");
            fclose(file);
            return -1; // Return -1 to indicate an error
        }

        // Check if the entire string was converted
        if (endptr == buffer)
        {
            printf("No digits were found in the first line.\n");
            fclose(file);
            return -1; // Return -1 to indicate an error
        }

        fclose(file);      // Close the file
        return (pid_t)pid; // Return the converted pid_t
    }
    else
    {
        printf("Error reading from file or file is empty.\n");
        fclose(file);
        return -1; // Return -1 to indicate an error
    }
}

void handleStartSignal(int sig)
{
    if (runCProgram == 0)
    {
        childPid = readPidInFile("java_progs/pid.txt");
    }
    else
    {
        childPid = readPidInFile("c_progs/pidfile.txt");
    }
    char command[BUFFER_SIZE];
    runPowejoular();
    startTime = getCurrentTimestampMs();
}

void handleEndSignal(int sig)
{
    endTime = getCurrentTimestampMs();
    killProcess(powerjoularPid);
    killProcess(childPid);
    char file[BUFFER_SIZE];
    snprintf(file, sizeof(file), "powerjoular.csv-%d.csv", childPid);
    lastPower = sumCpuPower(file);
    lastTime = (endTime - startTime) / 1000.0;
    lastTime = (endTime - startTime) / 1000.0;
    totalPower += lastPower;
    totalTime += lastTime;
}

void runner(char *filename, float totalPower, float totalTime, short runCProgram, pid_t parentPid)
{
    if (signal(SIGUSR1, handleStartSignal) == SIG_ERR)
    {
        perror("Failed to set up SIGUSR1 handler");
        exit(1);
    }
    if (signal(SIGUSR2, handleEndSignal) == SIG_ERR)
    {
        perror("Failed to set up SIGUSR2 handler");
        exit(1);
    }
    char parentPidStr[20];
    char programPath[100];
    char *command[5];
    snprintf(parentPidStr, sizeof(parentPidStr), "%d", parentPid);
    if (runCProgram == 1) {
        snprintf(programPath, sizeof(programPath), "./c_progs/%s", filename);
        command[0] = "sudo";
        command[1] = programPath;
        command[2] = parentPidStr;
        command[3] = NULL;
        }
        else{
        snprintf(programPath, sizeof(programPath), "java_progs/%s", filename);
        command[0] = "sudo";
        command[1] = "java";
        command[2] = programPath;
        command[3] = parentPidStr;
        command[4] = NULL;
        }
    pid_t commandProcessId = runProcess("sudo", command);
    int status;
    waitpid(commandProcessId, &status, 0);
}

int main(int argc, char *argv[])
{
    pid_t parentPid = getpid();
    char *filename = argv[1];
    runCProgram = (short)atoi(argv[2]);
    short numberOfRuns = (short)atoi(argv[3]);
    printf("Running %d times\n", numberOfRuns);
    for (int i = 0; i < numberOfRuns; i++)
    {
        printf("--------------------------------\n");
        printf("Run number %d\n", i + 1);
        runner(filename, totalPower, totalTime, runCProgram, parentPid);
        printf("Program used %fj\n", lastPower);
        printf("Time taken %fs\n", lastTime);
    }
    printf("--------------------------------\n");
    totalPower /= numberOfRuns;
    totalTime /= numberOfRuns;
    printf("In %d runs average power was %fj\n", numberOfRuns, totalPower);
    printf("Average time was %fs\n", totalTime);  
    return 0;
}