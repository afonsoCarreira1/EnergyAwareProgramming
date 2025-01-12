package java_progs;

import java.io.IOException;
import java.util.Random;

import java_progs.aux.WritePid;

public class MatrixMult {

    static void printMatrix(long M[][], int rowSize,
                            int colSize)
    {
        for (int i = 0; i < rowSize; i++) {
            for (int j = 0; j < colSize; j++)
                System.out.print(M[i][j] + " ");

            System.out.println();
        }
    }

    // Function to multiply
    // two matrices A[][] and B[][]
    static void multiplyMatrix(int row1, int col1,
                               long A[][], int row2,
                               int col2, long B[][])
    {
        int i, j, k;

        // Print the matrices A and B
        //System.out.println("\nMatrix A:");
        //printMatrix(A, row1, col1);
        //System.out.println("\nMatrix B:");
        //printMatrix(B, row2, col2);

        // Check if multiplication is Possible
        if (row2 != col1) {

            System.out.println(
                "\nMultiplication Not Possible");
            return;
        }

        // Matrix to store the result
        // The product matrix will
        // be of size row1 x col2
        long C[][] = new long[row1][col2];

        // Multiply the two matrices
        for (i = 0; i < row1; i++) {
            for (j = 0; j < col2; j++) {
                for (k = 0; k < row2; k++)
                    C[i][j] += (long)A[i][k] * B[k][j];
            }
        }

        // Print the result
        System.out.println("\nResultant Matrix:");
        //printMatrix(C, row1, col2);
    }

    // Function to create a matrix with either random or incremental values
    static long[][] createMatrix(int rows, int cols, boolean randomFactor) {
        long[][] matrix = new long[rows][cols];
        Random rand = new Random();
        int value = 1;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = randomFactor ? rand.nextLong() : value++;
            }
        }
        return matrix;
    }

    public static void main(String[] args) throws IOException
    {
        int val = 1_000;
        int row1 = val, col1 = val, row2 = val, col2 = val;

        /*int A[][] = { { 1, 1, 1 },
                      { 2, 2, 2 },
                      { 3, 3, 3 },
                      { 4, 4, 4 } };

        int B[][] = { { 1, 1, 1, 1 },
                      { 2, 2, 2, 2 },
                      { 3, 3, 3, 3 } };*/

        long A[][] = createMatrix(row1, col1, true);
        long B[][] = createMatrix(row2, col2, true);
        WritePid.writeTargetProgInfo(Long.toString(ProcessHandle.current().pid()),0);
        Runtime.getRuntime().exec(new String[] {"kill", "-USR1", " + args[0]"});
        multiplyMatrix(row1, col1, A, row2, col2, B);
        Runtime.getRuntime().exec(new String[] {"kill", "-USR2", " + args[0]"});
    }
}
