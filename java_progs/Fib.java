package java_progs;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;

public class Fib extends Thread {
    final static int FIB_SIZE = 45;
    final static int NUMBER_THREADS = 1;

    public static void main(String[] args) throws InterruptedException {
        try {
            WritePid.writePidToFile(Long.toString(ProcessHandle.current().pid()));//ManagementFactory.getRuntimeMXBean().getName()
            Runtime.getRuntime().exec("kill -USR1 " + args[0]);
            String ts = LocalDateTime.now().toString();
            for (int i = 0; i < NUMBER_THREADS - 1; i++) {
                Fib thread = new Fib();
                thread.start();
            }
            long f = fibonacci(FIB_SIZE);
            System.out.println("main: " + f);
            String te = LocalDateTime.now().toString();
            Runtime.getRuntime().exec("kill -USR2 " + args[0]);
            
            String time = ts + "\n"+te;
            writeToFile(time, "java_progs/time.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void writeToFile(String time, String filename) {
        
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(time);
            //System.out.println("Successfully wrote " + time + " to " + filename);
        } catch (IOException e) {
            System.out.println("An error occurred while writing to the file: " + e.getMessage());
        }
    }

    public static long fibonacci(int n) {
        if (n == 0)
            return 0;
        else if (n == 1)
            return 1;
        else
            return fibonacci(n - 1) + fibonacci(n - 2);
    }

    @Override
    public void run() {
        long f = fibonacci(FIB_SIZE);
        System.out.println(f);
    }
}