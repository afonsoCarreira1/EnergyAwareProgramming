import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class TestModelProg {
    public static void main(String[] args) {
        int maxListSIze = 1000;
        int max = 100;
        Random rand = new Random();
        ArrayList<Integer> l = new ArrayList<>();
        ArrayList<Integer> l2 = new ArrayList<>();
        for (int index = 0; index < maxListSIze; index++) {
            l.add(rand.nextInt(max));
            l2.add(rand.nextInt(max));
        }
        String pid = ProcessHandle.current().pid()+"";
        ProcessBuilder powerjoularBuilder = new ProcessBuilder("powerjoular", "-l", "-p", pid, "-D",".1", "-f", "powerjoular.csv");
        try {
            Process powerjoularProcess = powerjoularBuilder.start();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10_000; i++) {
            l.addAll(l2);
        }
        long end = System.currentTimeMillis();
        long diff = end - start;
        System.out.println("dif -> "+diff);
    }

    public static void sendStopSignalToOrchestrator(String pid, int iter) throws IOException {
        Runtime.getRuntime().exec(new String[] { "kill", "-USR2", pid });
    }

    public static void sendStartSignalToOrchestrator(String pid) throws IOException, InterruptedException {
        Runtime.getRuntime().exec(new String[] { "kill", "-USR1", pid });
        Thread.sleep(100);
    }
}
