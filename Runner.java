import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.ProcessBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.time.LocalDateTime;
import java_progs.WritePid;
import sun.misc.Signal;
import sun.misc.SignalHandler;

public class Runner {
    static String powerjoularPid = "";
    static String childPid = "";
    static Boolean readCFile = false;
    static Double averageJoules = 0.0;
    static Double averageTime = 0.0;
    static long startTime;
    static long endTime;
    static String frequency = ".1";
    static String loopSize = ""+20_000_000;
    static HashSet<String> features = new HashSet<>();

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args != null && args.length == 3 && Integer.parseInt(args[2]) > 0) {
            String fileName = args[0];
            readCFile = args[1].equals("t");
            int runs = Integer.parseInt(args[2]);
            if (runs == 1) {
                System.out.println("Running 1 time.");
            } else {
                System.out.println("Running "+runs+" times.");
            }
            for (int i = 0; i < runs; i++) {
                System.out.println("---------------------------------------");
                System.out.println("Run number: "+(i+1));
                run(fileName);
            }
            averageJoules /=  runs;
            averageTime /= runs;
            System.out.println("In "+ runs + " runs the average power was " + averageJoules + "J");
            System.out.println("Average time was " + averageTime/1000/1+"s");
        } else {
            System.out.println("Invalid args");
        }
    }

    public static void run(String file) throws IOException, InterruptedException{
        if (readCFile) {
            String[] command = {"pkexec", "./c_progs/" + file, Long.toString(ProcessHandle.current().pid())};
            //System.out.println("starting run at "+LocalDateTime.now());
            Runtime.getRuntime().exec(command);
        }else {
            //System.out.println("starting run at "+LocalDateTime.now());
            String[] command = {"/bin/sh", "-c", "java java_progs/" + file + " " + ProcessHandle.current().pid() + " " + loopSize};
            //System.out.println("starting run at "+LocalDateTime.now());
            Runtime.getRuntime().exec(command);
        }

        Signal.handle(new Signal("USR1"), new SignalHandler() {
            public void handle(Signal sig){
                System.out.println("Received START signal, starting powerjoular at "+LocalDateTime.now());
                if (readCFile) {
                    //System.out.println("Read child PID at "+LocalDateTime.now());
                    childPid = WritePid.captureCommandOutput();
                }else {
                    //System.out.println("Read child PID at "+LocalDateTime.now());
                    childPid = WritePid.readPidFromFile();
                }
                //System.out.println("parent " + ProcessHandle.current().pid());
                //System.out.println("child " + childPid);
                startTime = System.currentTimeMillis();
                //System.out.println("Created powerjoular process at "+LocalDateTime.now());
                ProcessBuilder powerjoularBuilder = new ProcessBuilder("powerjoular", "-l", "-p",childPid, "-D", frequency,"-f", "powerjoular.csv");
                try {
                    //System.out.println("Started powerjoular process at "+LocalDateTime.now());
                    Process powerjoularProcess = powerjoularBuilder.start();
                    powerjoularPid = Long.toString(powerjoularProcess.pid());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Signal.handle(new Signal("USR2"), new SignalHandler() {
            public void handle(Signal sig) {
                //System.out.println("Received END signal, stopping powerjoular at "+LocalDateTime.now());
                endTime = System.currentTimeMillis();
                try {
                    Process killPowerjoular = Runtime.getRuntime().exec("sudo kill "+powerjoularPid);
                    killPowerjoular.waitFor();
                    Process killTargetProgram = Runtime.getRuntime().exec("sudo kill "+childPid);
                    killTargetProgram.waitFor();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
                String cpuUsage = readCsv("powerjoular.csv-"+childPid+".csv");
                System.out.println("Program used "+ cpuUsage +"J");
                Double duration = (endTime-startTime)/1000.0;
                System.out.println("Time taken: " + duration + " seconds, for "+loopSize + " operations");
                averageJoules += Double.parseDouble(cpuUsage);
                averageTime += endTime-startTime;
                synchronized (Runner.class) {
                    Runner.class.notify();
                }
            }
        });
        synchronized (Runner.class) {
            Runner.class.wait();
        }
    }

    private static String readCsv(String csvFile){ 
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

            if (cpuPowerColumnIndex == -1) {
                System.out.println("Column 'CPU power' not found in the CSV file.");
                return "Error";
            }

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                cpuPowerValues.add(values[cpuPowerColumnIndex]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Double cpuPower = 0.0;
        Double freq = Double.parseDouble(frequency);
        for (int i = 0; i < cpuPowerValues.size(); i++) {
            cpuPower += Double.parseDouble(cpuPowerValues.get(i)) * freq;
        }
        //return Double.toString(Math.round(cpuPower));
        cpuPower /= Integer.parseInt(loopSize);
        return ""+cpuPower;//String.format("%.5f", cpuPower);
    }
    

    private static void saveFeatureInTempFile() {

    }

    private static void getFeaturesFromParser(String file) {
        String[] command = {"/bin/sh", "-c", "java java_progs/" + file + " " + ProcessHandle.current().pid() + " " + loopSize};
    }
}
