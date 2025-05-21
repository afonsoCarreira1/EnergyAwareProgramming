import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TestModelProg {

    static String frequency = ".1";
    static int loopSize = 10_000;
    public static void main(String[] args) {
        int maxListSize = 2000;
        int max = 100;
        Random rand = new Random();
        ArrayList<Integer> l = new ArrayList<>();
        ArrayList<Integer> l2 = new ArrayList<>();
        for (int index = 0; index < maxListSize; index++) {
            l.add(rand.nextInt(max));
            l2.add(rand.nextInt(max));
        }
        String pid = ProcessHandle.current().pid()+"";
        ProcessBuilder powerjoularBuilder = new ProcessBuilder("powerjoular", "-l", "-p", pid, "-D",frequency, "-f", "powerjoular.csv");
        try {
            Process powerjoularProcess = powerjoularBuilder.start();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
        long start = System.currentTimeMillis();
        for (int i = 0; i < loopSize; i++) {
            
            l.addAll(l2);
            
            //l.size();
            
            //l.equals(l2);
        }
        long end = System.currentTimeMillis();
        long diff = end - start;
        //System.out.println("dif -> "+diff);
        String energyUsed = readPowerjoularCsv("powerjoular.csv-"+pid+".csv");
        System.out.println("Energy used was: "+energyUsed + "J");
        double addAllPrediction = ((maxListSize + ((((maxListSize * -0.00020094767) + Math.cos(Math.log(maxListSize))) * maxListSize) + maxListSize)) * 1.9027777e-7) + (0 * Math.sin((Math.cos(Math.log(maxListSize) * 0.33795547) * -0.00022468693) + 0.00018942966));
        //Math.log(maxListSize) * 6.469111e-5
        System.out.println("Energy from addAll model was: "+addAllPrediction + "J");
        double sizePrediction = Math.log(maxListSize) * 7.2244998e-6;
        System.out.println("Energy from size model was: "+sizePrediction + "J");
        double equalsPrediction = Math.log(maxListSize) * 7.43854e-6;
        System.out.println("Energy from equals model was: "+equalsPrediction + "J");
        System.out.println("Energy from model summed is: "+(addAllPrediction+sizePrediction+equalsPrediction) + "J");
        ProcessBuilder removePowerjoularFiles = new ProcessBuilder("rm", "powerjoular.csv*");
        try {
            removePowerjoularFiles.start();
        } catch (Exception e) {}
    }

    public static void sendStopSignalToOrchestrator(String pid, int iter) throws IOException {
        Runtime.getRuntime().exec(new String[] { "kill", "-USR2", pid });
    }

    public static void sendStartSignalToOrchestrator(String pid) throws IOException, InterruptedException {
        Runtime.getRuntime().exec(new String[] { "kill", "-USR1", pid });
        Thread.sleep(100);
    }

    private static String readPowerjoularCsv(String csvFile) {
        try {Thread.sleep(100);
        } catch (InterruptedException e) {e.printStackTrace();}
        List<String> cpuPowerValues = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            String[] headers = br.readLine().split(",");
            int cpuPowerColumnIndex = -1;

            for (int i = 0; i < headers.length; i++) {
                if ("CPU Power".equalsIgnoreCase(headers[i])) {
                    cpuPowerColumnIndex = i;
                    break;
                }
            }

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                cpuPowerValues.add(values[cpuPowerColumnIndex]);
            }
        } catch (Exception e) {
            //e.printStackTrace();
            System.out.println("Program ran so fast it did not create a CSV file or other error.\n");
        }
        Double cpuPower = 0.0;
        //TODO fix this so when it catches "+Inf***********" ignores it
        try {
            
            Double freq = Double.parseDouble(frequency);
            for (int i = 0; i < cpuPowerValues.size(); i++) {
                cpuPower += Double.parseDouble(cpuPowerValues.get(i)) * freq;
            }
            // return Double.toString(Math.round(cpuPower));
            cpuPower /= loopSize;
            
        } catch (Exception e) {
            cpuPower = 0.0;
            System.out.println("Error with powerjoular csv.\n");
        }
        return "" + cpuPower;// String.format("%.5f", cpuPower);
    }
}


