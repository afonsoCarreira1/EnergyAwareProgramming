import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class BinaryTrees_createTree_int_101 {

    public static boolean stop = false;
    static String frequency = ".1";

    public static void main(String[] args) throws Exception {
        int iter = 0;
        //try {
            BenchmarkArgs[] arr = new BenchmarkArgs[75000];
            populateArray(arr);
            //TemplatesAux.sendStartSignalToOrchestrator(args[0]);
            String pid = ProcessHandle.current().pid()+"";
            ProcessBuilder powerjoularBuilder = new ProcessBuilder("powerjoular", "-l", "-p", pid, "-D",frequency, "-f", "powerjoular.csv");
            Process powerjoularProcess = powerjoularBuilder.start();
            launchTimerThread(1100);
            iter = computation(arr, arr.length);
            Process killPowerjoular = Runtime.getRuntime().exec(new String[]{"sudo", "kill", powerjoularProcess.pid()+""});
            killPowerjoular.waitFor();
            String energyUsed = readPowerjoularCsv(iter,"powerjoular.csv-"+pid+".csv");
            System.out.println("iter -> "+iter);
            System.out.println("Energy used was: "+energyUsed + "J");
        //} catch (OutOfMemoryError e) {
        //    TemplatesAux.writeErrorInFile("BinaryTrees_createTree_int_101", "Out of memory error caught by the program:\n" + e.getMessage());
        //} catch (Exception e) {
        //    TemplatesAux.writeErrorInFile("BinaryTrees_createTree_int_101", "Error caught by the program:\n" + e.getMessage());
        //} finally {
        //    TemplatesAux.sendStopSignalToOrchestrator(args[0], iter);
        //}
    }

    private static String readPowerjoularCsv(int iter, String csvFile) {
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
            cpuPower /= iter;
            
        } catch (Exception e) {
            cpuPower = 0.0;
            System.out.println("Error with powerjoular csv.\n");
        }
        return "" + cpuPower;// String.format("%.5f", cpuPower);
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

    static class BenchmarkArgs {
        public int var0;

        BenchmarkArgs() {
            this.var0 = 13;
        }
    }

    private static void binaryTrees_createTree_int_101(int depth) {
        BinaryTrees.createTree(depth);
    }

    private static int computation(BenchmarkArgs[] args, int iter) {
        int i = 0;
        while (!stop && i < iter) {
            System.out.println("fiz "+i);
            binaryTrees_createTree_int_101(args[i].var0);
            i++;
        }
        if (i == 0 ) return 1;
        return i;
    }

    private static void populateArray(BenchmarkArgs[] arr) {
        for (int i = 0;i < 75000;i++) {
          arr[i] = new BenchmarkArgs();
        };
    }

    private String input1 = "26";
}
