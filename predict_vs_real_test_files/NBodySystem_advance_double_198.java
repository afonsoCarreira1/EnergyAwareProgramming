import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class NBodySystem_advance_double_198 {
    public static boolean stop = false;
    static String frequency = ".1";
    public static int arrSize = 150_000;
    public static void main(String[] args) throws Exception {
        int iter = 0;
        BenchmarkArgs[] arr = new BenchmarkArgs[arrSize];
        populateArray(arr);
        String pid = ProcessHandle.current().pid()+"";
        ProcessBuilder powerjoularBuilder = new ProcessBuilder("powerjoular", "-l", "-p", pid, "-D",frequency, "-f", "powerjoular.csv");
        Process powerjoularProcess = powerjoularBuilder.start();
        long start = System.currentTimeMillis();
        launchTimerThread(1100);
        iter = computation(arr, arr.length);
        Process killPowerjoular = Runtime.getRuntime().exec(new String[]{"sudo", "kill", powerjoularProcess.pid()+""});
        killPowerjoular.waitFor();
        long end = System.currentTimeMillis();
        String energyUsed = readPowerjoularCsv(iter,"powerjoular.csv-"+pid+".csv");
        System.out.println("iter -> "+iter);
        
        long diff = end - start;
        System.out.println("Time: "+diff +"ms");
        System.out.println("Energy used was: "+energyUsed + "J");
    }

    static class BenchmarkArgs {
        public NBodySystem var0;

        public double var1;

        BenchmarkArgs() {
            this.var0 = new NBodySystem();
            this.var1 = 23238.1627485398;
        }
    }

    private static void NBodySystem_advance_double_198(NBodySystem var, double dt) {
        var.advance(dt);
    }

    private static int computation(BenchmarkArgs[] args, int iter) {
        int i = 0;
        while (!stop && i < iter) {
              NBodySystem_advance_double_198(args[i].var0, args[i].var1);
               i++;
        }
        if (i == 0) return 1;
        return i;
    }

    private static void populateArray(BenchmarkArgs[] arr) {
        for (int i = 0;i < arrSize;i++) {
          arr[i] = new BenchmarkArgs();
        };
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

}
