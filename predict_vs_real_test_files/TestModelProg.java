import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class TestModelProg {

    static String frequency = ".1";
    public static boolean stop = false;

    public static void main(String[] args) throws IOException, InterruptedException {
        // String input = generateRandomWordString(1000);//"Java is simple. Java is
        // powerful.";
        int loopSize = 0;
        int val = Integer.parseInt(args[0]);
        String pid = ProcessHandle.current().pid() + "";
        long start = System.currentTimeMillis();
        ProcessBuilder powerjoularBuilder = new ProcessBuilder("powerjoular", "-l", "-p", pid, "-D", frequency, "-f",
                "powerjoular.csv");
        Process powerjoularProcess = powerjoularBuilder.start();
        launchTimerThread(1100);
        formatter.format(new spectralnorm().Approximate(val));
        //while (!stop) {
        //    fannkuchredux.fannkuch(val);
        //    loopSize++;
        //}
        Process killPowerjoular = Runtime.getRuntime()
                .exec(new String[] { "sudo", "kill", powerjoularProcess.pid() + "" });
        killPowerjoular.waitFor();
        long end = System.currentTimeMillis();
        long diff = end - start;
        String energyUsed = readPowerjoularCsv("powerjoular.csv-" + pid + ".csv", loopSize);
        System.out.println("Energy used was: " + energyUsed + "J" + " in " + diff + "ms");
    }

    public static void compute(String input) {
        HashMap<String, Integer> result = countWordFrequency(input);
        System.out.println(result);
    }

    static class BenchmarkArgs {
        public NBodySystem var0;

        public double var1;

        BenchmarkArgs() {
            this.var0 = new NBodySystem();
            this.var1 = 1000;
        }
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

    public static List<Integer> generateRandomNumberList(int size) {
        Random rand = new Random();
        List<Integer> numbers = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            numbers.add(rand.nextInt(100)); // random number between 0 and 99
        }

        return numbers;
    }

    public static HashMap<String, Integer> countWordFrequency(String text) {
        HashMap<String, Integer> frequencyMap = new HashMap<>();
        String[] words = text.toLowerCase().split("\\W+");
        for (String word : words) {
            if (word.isEmpty())
                continue;
            frequencyMap.put(word, frequencyMap.getOrDefault(word, 0) + 1);
        }
        return frequencyMap;
    }

    public static String generateRandomWordString(int wordCount) {
        String[] words = { "red", "blue", "green" };
        Random rand = new Random();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < wordCount; i++) {
            sb.append(words[rand.nextInt(words.length)]);
            if (i < wordCount - 1) {
                sb.append(" ");
            }
        }

        return sb.toString();
    }

    private static String readPowerjoularCsv(String csvFile, int loopSize) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
            // e.printStackTrace();
            System.out.println("Program ran so fast it did not create a CSV file or other error.\n");
        }
        Double cpuPower = 0.0;
        // TODO fix this so when it catches "+Inf***********" ignores it
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
