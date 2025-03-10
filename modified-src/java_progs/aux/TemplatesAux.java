package java_progs.aux;
public class TemplatesAux {
    public static boolean stop = false;

    // public static void writeProgramInfo(int i) {
    // WritePid.writeTargetProgInfo("timeStamp", i);
    // }
    public static void sendStopSignalToOrchestrator(java.lang.String pid) throws java.io.IOException {
        java.lang.Runtime.getRuntime().exec(new java.lang.String[]{ "kill", "-USR2", pid });
    }

    public static void sendStartSignalToOrchestrator(java.lang.String pid, int iter) throws java.io.IOException, java.lang.InterruptedException {
        java_progs.aux.WritePid.writeTargetProgInfo(java.lang.Long.toString(java.lang.ProcessHandle.current().pid()), iter);
        java.lang.Runtime.getRuntime().exec(new java.lang.String[]{ "kill", "-USR1", pid });
        java.lang.Thread.sleep(100);
    }

    public static void writeErrorInFile(java.lang.String filename, java.lang.String errorMessage) {
        java.lang.System.out.println("this happened -> " + errorMessage);
        try {
            java.io.File myObj = new java.io.File(("errorFiles/" + filename) + ".txt");
            myObj.createNewFile();
        } catch (java.io.IOException e) {
            java.lang.System.out.println(e.getMessage());
        }
        try (java.io.FileWriter writer = new java.io.FileWriter(("errorFiles/" + filename) + ".txt")) {
            writer.write(errorMessage);
        } catch (java.io.IOException e) {
            java.lang.System.out.println(e.getMessage());
        }
    }

    public static void launchTimerThread() {
        java.lang.Thread timerThread = new java.lang.Thread(() -> {
            try {
                java.lang.Thread.sleep(1100);
                java_progs.aux.TemplatesAux.stop = true;
            } catch (java.lang.InterruptedException e) {
                java.lang.Thread.currentThread().interrupt();
            }
        });
        timerThread.start();
    }
}
