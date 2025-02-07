import java.beans.Introspector;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ProcessBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java_progs.aux.WritePid;

import java.time.LocalDateTime;

import sun.misc.Signal;
import sun.misc.SignalHandler;

public class Runner {
    static String CSV_FILE_NAME = "features.csv";
    static String powerjoularPid = "";
    static String childPid = "";
    static Boolean readCFile = false;
    static Double averageJoules = 0.0;
    static Double averageTime = 0.0;
    static long startTime;
    static long endTime;
    static String frequency = ".1";
    static String loopSize = "";
    static String lastMeasurement = "";
    static HashSet<String> featuresName = new HashSet<>();
    static Thread timeOutThread = null;
    static short timeOutTime = 10;//seconds

    public static void main(String[] args) throws IOException, InterruptedException  {
        File[] programs = getAllProgramsNames();
        for (int i = 0; i < programs.length; i++) {
            //System.out.println(i);
            if (args != null && args.length == 3 && Integer.parseInt(args[2]) > 0) {
                String fileName = programs[i].toString().replace("java_progs/out/java_progs/progs/", "").replace(".class", "");//.replace("java_progs/progs/", "").replace(".java", "");
                if (!(args[0].equals("test") && fileName.equals("ArrayListContainsAllElemRandom"))) continue;//just to test one prog file
                System.out.println("Starting profile for " + fileName + " program");
                readCFile = args[1].equals("t");
                int runs = Integer.parseInt(args[2]);
                System.out.println("Running " + (runs == 1 ? "1 time." : runs + " times."));
                for (int j = 0; j < runs; j++) {
                    System.out.println("---------------------------------------");
                    System.out.println("Run number: " + (j + 1));
                    run(fileName);
                }
                averageJoules /= runs;
                averageTime /= runs;
                System.out.println("In " + runs + " runs the average power was " + averageJoules + "J");
                System.out.println("Average time was " + averageTime / 1000 / 1 + "s");
                System.out.println("Creating features CSV file.");
                averageJoules = 0.0;
                averageTime = 0.0;
            } else {
                System.out.println("Invalid args");
            }
        }
        createFeaturesCSV();
    }

    public static void run(String file) throws IOException, InterruptedException {
        if (readCFile) {
            String[] command = { "pkexec", "./c_progs/" + file, Long.toString(ProcessHandle.current().pid()) };
            // System.out.println("starting run at "+LocalDateTime.now());
            Runtime.getRuntime().exec(command);
        } else {
            String[] command = {
                "java", 
                "-Xmx4056M",
                "-Xms4056M",
                "-cp", 
                "java_progs/out", 
                "java_progs.progs." + file, 
                Long.toString(ProcessHandle.current().pid())
            };
            Runtime.getRuntime().exec(command);
        }

        Signal.handle(new Signal("USR1"), new SignalHandler() {
            public void handle(Signal sig) {
                System.out.println("Received START signal, starting powerjoular at " + LocalDateTime.now());
                if (readCFile) {
                    // System.out.println("Read child PID at "+LocalDateTime.now());
                    childPid = WritePid.captureCommandOutput();
                } else {
                    // System.out.println("Read child PID at "+LocalDateTime.now());
                    ArrayList<String> pidFromFile = WritePid.readTargetProgramInfo();
                    childPid = pidFromFile.get(0);
                }
                // System.out.println("parent " + ProcessHandle.current().pid());
                // System.out.println("child " + childPid);
                startTime = System.currentTimeMillis();
                // System.out.println("Created powerjoular process at "+LocalDateTime.now());
                ProcessBuilder powerjoularBuilder = new ProcessBuilder("powerjoular", "-l", "-p", childPid, "-D",
                        frequency, "-f", "powerjoular.csv");
                try {
                    // System.out.println("Started powerjoular process at "+LocalDateTime.now());
                    Process powerjoularProcess = powerjoularBuilder.start();
                    powerjoularPid = Long.toString(powerjoularProcess.pid());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                timeOutThread = handleTimeOutThread();
                timeOutThread.start();          
            }
        });

        Signal.handle(new Signal("USR2"), new SignalHandler() {
            public void handle(Signal sig) {
                timeOutThread.interrupt();
                // System.out.println("Received END signal, stopping powerjoular at
                // "+LocalDateTime.now());
                endTime = System.currentTimeMillis();
                ArrayList<String> loopSizeFromFile = WritePid.readTargetProgramInfo();
                loopSize = loopSizeFromFile.get(1);
                try {
                    Process killPowerjoular = Runtime.getRuntime().exec(new String[]{"sudo", "kill", powerjoularPid});
                    killPowerjoular.waitFor();
                    Process killTargetProgram = Runtime.getRuntime().exec(new String[]{"sudo", "kill", childPid});
                    killTargetProgram.waitFor();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
                String cpuUsage = readCsv("powerjoular.csv-" + childPid + ".csv");
                System.out.println("Program used " + cpuUsage + "J");
                Double duration = (endTime - startTime) / 1000.0;
                System.out.println("Time taken: " + duration + " seconds, for " + loopSize + " operations");
                averageJoules += Double.parseDouble(cpuUsage);
                averageTime += endTime - startTime;
                try {
                    saveFeature(file, cpuUsage);
                } catch (IOException e) {
                    System.out.println("Error saving feature");
                }
                synchronized (Runner.class) {
                    Runner.class.notify();
                }
            }
        });
        synchronized (Runner.class) {
            Runner.class.wait();
        }
    }

    private static Thread handleTimeOutThread() {
        return new Thread() {
            public void run() {
                try {
                    Thread.sleep(timeOutTime*1000);
                    System.out.println("Program timed out.\nKilling process.");
                    try {
                        Process killPowerjoular = Runtime.getRuntime().exec(new String[]{"sudo", "kill", powerjoularPid});
                        killPowerjoular.waitFor();
                        Process killTargetProgram = Runtime.getRuntime().exec(new String[]{"sudo", "kill", childPid});
                        killTargetProgram.waitFor();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    synchronized (Runner.class) {
                        Runner.class.notify();
                    }
                } catch (InterruptedException e) {
                    if (!(e instanceof InterruptedException)) e.printStackTrace();
                } 
            }  
        };  
    }

    private static String readCsv(String csvFile) {
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
        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("Program ran so fast it did not create a CSV file or other error.");
        }

        Double cpuPower = 0.0;
        Double freq = Double.parseDouble(frequency);
        for (int i = 0; i < cpuPowerValues.size(); i++) {
            cpuPower += Double.parseDouble(cpuPowerValues.get(i)) * freq;
        }
        // return Double.toString(Math.round(cpuPower));
        cpuPower /= Integer.parseInt(loopSize);
        return "" + cpuPower;// String.format("%.5f", cpuPower);
    }

    private static void saveFeature(String file, String cpuUsage) throws IOException  {
        getFeaturesFromParser(file, cpuUsage);
    }

    private static void createFeaturesTempFile(String fileName,Map<String, Object> methodfeatures) throws IOException{
        try (BufferedWriter csvWriter = new BufferedWriter(new FileWriter("tmp/tmp_"+fileName+".csv"))) {
            // Write the header row
            List<String> featureList = new ArrayList<>(methodfeatures.keySet());
            csvWriter.write(String.join(",", featureList));
            csvWriter.newLine();
                List<String> row = new ArrayList<>();
                Map<String, Object> programFeatures = methodfeatures;
                for (String feature : featureList) {
                    if (programFeatures.get(feature) == null) {
                        row.add("0");
                    } else {
                        row.add(programFeatures.get(feature).toString());
                    }
                }
                csvWriter.write(String.join(",", row));
                csvWriter.newLine();
        }
        //tmpFileNumber++;
    }

    private static void getFeaturesFromParser(String file, String cpuUsage) throws IOException {
        HashMap<String, Map<String, Object>> methods = ASTFeatureExtractor.getFeatures(file,true);
        String methodName = getFunMapName(file);
        Map<String, Object> methodfeatures = methods.get(methodName);
        System.out.println(methodfeatures);
        featuresName.addAll(methodfeatures.keySet());
        methodfeatures.put("EnergyUsed", cpuUsage);
        createFeaturesTempFile(file,methodfeatures);
    }

    private static String getFunMapName(String file){
        String mapName = "";
        File myObj = new File("java_progs/progs/"+file+".java");
        try (Scanner myReader = new Scanner(myObj)) {
            StringBuilder f = new StringBuilder();
            while (myReader.hasNextLine()) {
                f.append(myReader.nextLine()).append("\n");
            }
            myReader.close();
            String txt = f.toString();
            String regex = Introspector.decapitalize(file)+"\\s*\\((.*)\\)\\s*\\{";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(txt);
            
            // Check if the pattern matches
            //if (matcher.find()) {
            //    // Extract and print the first group (contents inside the parentheses)
            //    System.out.println("Extracted group: " + matcher.group(1));
            //} else {
            //    System.out.println("No match found.");
            //}
            matcher.find();
            String[] params = matcher.group(1).split(",");
            for(String param : params){
                param = param.strip();
                String type = param.split(" ")[0].replaceAll("<.*>", "");
                if (mapName.isEmpty()) mapName += type;
                else mapName += " | "+type;
            }
            //scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return file + "."+Introspector.decapitalize(file)+"("+mapName+")";// Class.methodName(type1 | type2)
    }

    private static void createFeaturesCSV() throws IOException {
        try (BufferedWriter csvWriter = new BufferedWriter(new FileWriter(CSV_FILE_NAME))) {
            // Write the header row
            List<String> featureList = new ArrayList<>(featuresName);
            featureList.add("EnergyUsed");
            csvWriter.write(String.join(",", featureList));
            csvWriter.newLine();
            File[] tmpFiles = getAllCSVTempFiles();
            for (int i = 0; i < tmpFiles.length; i++) {
                List<String> row = new ArrayList<>();
                Map<String, Object> programFeatures = readCSVTempFile(tmpFiles[i].toString());
                for (String feature : featureList) {
                    if (programFeatures.get(feature) == null) {
                        row.add("0");
                    } else {
                        row.add(programFeatures.get(feature).toString());
                    }
                }
                csvWriter.write(String.join(",", row));
                csvWriter.newLine();
            }
        }
    }

    private static Map<String,Object> readCSVTempFile(String file) throws IOException {
        Map<String, Object> programFeatures = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line0 = br.readLine();  // Read line 0
            String line1 = br.readLine();  // Read line 1
            String[] features = line0.split(",");
            String[] featuresVal = line1.split(",");
            for (int i = 0; i < features.length; i++) {
                programFeatures.put(features[i], featuresVal[i]);
            }
        }
        return programFeatures;
    }

    private static File[] getAllProgramsNames() {
        //return new File("java_progs/progs/").listFiles();
        return new File("java_progs/out/java_progs/progs/").listFiles();
    }

    private static File[] getAllCSVTempFiles() {
        return new File("tmp/").listFiles();
    }
}
