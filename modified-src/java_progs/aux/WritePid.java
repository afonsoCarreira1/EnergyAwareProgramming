package java_progs.aux;
public class WritePid {
    static java.lang.String filename = "java_progs/pid.txt";

    // Function to write an integer to a file, overwriting the file if it exists
    public static void writeTargetProgInfo(java.lang.String pid, int loopSize) {
        try (java.io.FileWriter writer = new java.io.FileWriter(java_progs.aux.WritePid.filename)) {
            writer.write((pid + "\n") + loopSize);
            // System.out.println("Successfully wrote " + pid + " to " + filename);
        } catch (java.io.IOException e) {
            java.lang.System.out.println("An error occurred while writing to the file: " + e.getMessage());
        }
    }

    // Function to read an integer from a file
    public static java.util.ArrayList<java.lang.String> readTargetProgramInfo() {
        java.lang.String pid = "";
        java.lang.String loopSize = "";
        java.lang.String log = "";
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(java_progs.aux.WritePid.filename))) {
            pid = reader.readLine().trim();
            loopSize = reader.readLine().trim();
            // System.out.println("Successfully read " + pid + " from " + filename);
        } catch (java.lang.Exception e) {
            log = e.getMessage();
            // System.out.println("An error occurred while reading the file: " + e.getMessage());
        }
        return new java.util.ArrayList<java.lang.String>(java.util.Arrays.asList(pid, loopSize, log));
    }

    public static java.lang.String captureCommandOutput() {
        try {
            // Execute the command
            java.lang.Process process = java.lang.Runtime.getRuntime().exec(new java.lang.String[]{ "pkexec", "cat", "c_progs/pidfile.txt" });
            // Capture the output
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
            java.lang.StringBuilder output = new java.lang.StringBuilder();
            java.lang.String line;
            // Read the output line by line
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            } 
            // Close the reader
            reader.close();
            return output.toString().trim();
        } catch (java.io.IOException e) {
            return e.getMessage();
        }
    }
}
