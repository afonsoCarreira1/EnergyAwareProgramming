package java_progs.aux;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TemplatesAux {

    public static void sendStopSignalToOrchestrator(String pid, int i) throws IOException {
        //String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS").format(new java.util.Date());
        WritePid.writeTargetProgInfo("timeStamp", i);
        Runtime.getRuntime().exec(new String[] { "kill", "-USR2", pid });
    }

    public static void sendStartSignalToOrchestrator(String pid) throws IOException {
        WritePid.writeTargetProgInfo(Long.toString(ProcessHandle.current().pid()), 0);
        Runtime.getRuntime().exec(new String[] { "kill", "-USR1", pid });
    }

    public static void writeErrorInFile(String filename, String errorMessage) {
        try {
            File myObj = new File(filename+".txt");
            myObj.createNewFile();
        } catch (IOException e) {}
        try (FileWriter writer = new FileWriter("errorFiles/" + filename+".txt")) {
            writer.write(errorMessage);
        } catch (IOException e) {}
    }

}
