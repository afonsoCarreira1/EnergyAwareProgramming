package java_progs;

import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class WritePid {
    static String filename = "java_progs/pid.txt";

    // Function to write an integer to a file, overwriting the file if it exists
    public static void writePidToFile(String pid) {
        
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(pid);
            System.out.println("Successfully wrote " + pid + " to " + filename);
        } catch (IOException e) {
            System.out.println("An error occurred while writing to the file: " + e.getMessage());
        }
    }

    // Function to read an integer from a file
    public static String readPidFromFile() {
        String pid = "";
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            pid = reader.readLine().trim();
            System.out.println("Successfully read " + pid + " from " + filename);
        } catch (IOException | NumberFormatException e) {
            System.out.println("An error occurred while reading the file: " + e.getMessage());
        }
        return pid;
    }

    public static String captureCommandOutput(){
        try {
            // Execute the command
            Process process = Runtime.getRuntime().exec("pkexec cat c_progs/pidfile.txt");

            // Capture the output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;

            // Read the output line by line
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            
            // Close the reader
            reader.close();
            return output.toString().trim();

        } catch (IOException e) {
           return e.getMessage();
        }
    }
}
