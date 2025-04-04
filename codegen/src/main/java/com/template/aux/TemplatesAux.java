package com.template.aux;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TemplatesAux {

    public static boolean stop = false;

    public static void sendStopSignalToOrchestrator(String pid) throws IOException {
        Runtime.getRuntime().exec(new String[] { "kill", "-USR2", pid });
    }

    public static void sendStartSignalToOrchestrator(String pid, int iter) throws IOException, InterruptedException {
        WritePid.writeTargetProgInfo(Long.toString(ProcessHandle.current().pid()), iter);
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

    public static void launchTimerThread() {
        Thread timerThread = new Thread(() -> {
            try {
                Thread.sleep(1100);
                stop = true;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        timerThread.start();
    }

}
