package com.template.aux;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TemplatesAux {

    public static boolean stop = false;

    public static void sendStopSignalToOrchestrator(String pid, int iter) throws IOException {
        WritePid.writeTargetProgInfo(Long.toString(ProcessHandle.current().pid()), iter);
        Runtime.getRuntime().exec(new String[] { "kill", "-USR2", pid });
    }

    public static void sendStartSignalToOrchestrator(String pid) throws IOException, InterruptedException {
        WritePid.writeTargetProgInfo(Long.toString(ProcessHandle.current().pid()), 0);
        Runtime.getRuntime().exec(new String[] { "kill", "-USR1", pid });
        Thread.sleep(100);
    }

    public static void writeErrorInFile(String filename, String errorMessage) {
        String path = "src/main/java/com/aux_runtime/error_files/";
        //System.out.println("this happened -> "+errorMessage);
        try {
            File myObj = new File(path +filename+".txt");
            myObj.createNewFile();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        try (FileWriter writer = new FileWriter(path + filename+".txt")) {
            writer.write(errorMessage);
        } catch (IOException e) {System.out.println(e.getMessage());}
    }

    public static void writeInFile(String filename, String message) {
        String path = "src/main/java/com/aux_runtime/";
        String fullPath = path +filename+".txt";
        File myObj = new File(fullPath);
        try {
            createFileIfNotExists(fullPath);
            myObj = new File(path +filename+".txt");
            myObj.createNewFile();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        try (FileWriter writer = new FileWriter(path + filename+".txt")) {
            writer.write(message);
        } catch (IOException e) {System.out.println(e.getMessage());}
    }

    public static void createFileIfNotExists(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) file.createNewFile(); 
    }

    public static void deleteFileIfExists(String filePath) {
        File file = new File(filePath);
        if (file.exists()) if (file.delete());
    }

    public static void launchTimerThread(int timeSeconds) {
        Thread timerThread = new Thread(() -> {
            try {
                Thread.sleep(timeSeconds);
                stop = true;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        timerThread.start();
    }

}
