#include <stdio.h>
#include <stdlib.h>
#include <signal.h>
#include <unistd.h>
#include <sys/types.h>
#include <unistd.h>
#include <limits.h>
#include <linux/limits.h>
#include <errno.h>
#include <sys/stat.h>

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


// Function to print a matrix
void printMatrix(long **M, int rowSize, int colSize) {
    for (int i = 0; i < rowSize; i++) {
        for (int j = 0; j < colSize; j++) {
            printf("%ld ", M[i][j]);
        }
        printf("\n");
    }
}

// Function to multiply two matrices A and B
void multiplyMatrix(int row1, int col1, long **A, int row2, int col2, long **B) {
    // Check if multiplication is possible
    if (col1 != row2) {
        printf("Multiplication Not Possible\n");
        return;
    }

    // Allocate memory for result matrix C
    long **C = (long **)malloc(row1 * sizeof(long *));
    for (int i = 0; i < row1; i++) {
        C[i] = (long *)calloc(col2, sizeof(long));
    }

    // Multiply matrices
    for (int i = 0; i < row1; i++) {
        for (int j = 0; j < col2; j++) {
            for (int k = 0; k < col1; k++) {
                C[i][j] += A[i][k] * B[k][j];
            }
        }
    }

    // Print the resultant matrix
    //printf("\nResultant Matrix:\n");
    //printMatrix(C, row1, col2);

    // Free allocated memory for matrix C
    /*for (int i = 0; i < row1; i++) {
        free(C[i]);
    }
    free(C);*/
}

// Function to create a matrix with either random or incremental values
long **createMatrix(int rows, int cols, int randomFactor) {
    long **matrix = (long **)malloc(rows * sizeof(long *));
    for (int i = 0; i < rows; i++) {
        matrix[i] = (long *)malloc(cols * sizeof(long));
    }

    srand(time(NULL));
    int value = 1;

    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            matrix[i][j] = randomFactor ? rand() : value++;
        }
    }
    return matrix;
}

// Main function
int main(int argc, char *argv[]) {
    int val = 1000;
    int row1 = val, col1 = val, row2 = val, col2 = val;

    // Create matrices A and B
    long **A = createMatrix(row1, col1, 1);
    long **B = createMatrix(row2, col2, 1);

    pid_t pid = atoi(argv[1]);
    write_pid_to_file("c_progs/pidfile.txt");
    //sleep(1);
    if (kill(pid, SIGUSR1) == -1) {
        perror("Failed to send SIGUSR1");
        return 1;
    }
    
    // Multiply matrices A and B
    multiplyMatrix(row1, col1, A, row2, col2, B);
    if (kill(pid, SIGUSR2) == -1) {
        perror("Failed to send SIGUSR2");
        return 1;
    }

    // Free allocated memory for matrices A and B
    for (int i = 0; i < row1; i++) {
        free(A[i]);
    }
    free(A);

    for (int i = 0; i < row2; i++) {
        free(B[i]);
    }
    free(B);

    return 0;
}
