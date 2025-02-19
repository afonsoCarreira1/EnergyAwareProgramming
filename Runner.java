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
import java.util.Arrays;
import java.util.Comparator;
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
    final static String CSV_FILE_NAME = "features.csv";
    final static String frequency = ".1";
    final static short timeOutTime = 25;//seconds
    static String powerjoularPid = "";
    static String childPid = "";
    static Double averageJoules = 0.0;
    static Double averageTime = 0.0;
    static long startTime;
    static long endTime;
    static String loopSize = null;
    static String lastMeasurement = null;
    static HashSet<String> featuresName = new HashSet<>();
    static Thread timeOutThread = null;
    static Long avoidSize = null;
    static String programToSkip = null;
    static StringBuilder log = new StringBuilder();

    public static void main(String[] args) throws IOException, InterruptedException  {
        //File[] programs = getAllProgramsNames();
        String[] programs = getAllFilenamesInDir("java_progs/out/java_progs/progs/");
        Arrays.sort(programs, Comparator.comparing(Runner::extractFilename).thenComparingInt(Runner::extractNumber));
        String logFilename = createLogFile();
        for (int i = 0; i < programs.length; i++) {
            Thread.sleep(100);
            if (args != null && args.length == 3 && Integer.parseInt(args[2]) > 0) {
                String fileName = programs[i].toString().replace("java_progs/out/java_progs/progs/", "").replace(".class", "");//.replace("java_progs/progs/", "").replace(".java", "");
                //if (!(args[0].equals("test") && fileName.equals("AddAllElemCopyOnWriteArrayList18"))) continue;//just to test one prog file
                log.append("---------------------------------------\n");
                log.append("Program number -> " + i + "\n");
                //System.out.println("Program number -> " + i);
                //if (skipProgram(fileName)) continue;
                log.append("Starting profile for " + fileName + " program\n");
                Boolean readCFile = args[1].equals("t");
                int runs = Integer.parseInt(args[2]);
                log.append("Running " + (runs == 1 ? "1 time.\n" : runs + " times.\n"));
                for (int j = 0; j < runs; j++) {
                    timeOutThread = handleTimeOutThread(fileName);
                    timeOutThread.start();
                    run(fileName,readCFile);
                }
                if (programThrowedError(fileName)) {
                    log.append("Error in "+fileName+". Check logs for more info.\n");
                    continue;
                }
                averageJoules /= runs;
                averageTime /= runs;
                log.append("In " + runs + " runs the average power was " + averageJoules + "J\n");
                log.append("Average time was " + averageTime / 1000 / 1 + "s\n");
                averageJoules = 0.0;
                averageTime = 0.0;
                saveLog(logFilename);
            } else {
                System.out.println("Invalid args");
            }
        }
        createFeaturesCSV();
    }

    public static void run(String filename, Boolean readCFile) throws IOException, InterruptedException {
        if (readCFile) {
            String[] command = { "pkexec", "./c_progs/" + filename, Long.toString(ProcessHandle.current().pid()) };
            Runtime.getRuntime().exec(command);
        } else {
            String[] command = {
                "java", 
                "-Xmx4056M",
                "-Xms4056M",
                "-cp", 
                "java_progs/out", 
                "java_progs.progs." + filename, 
                Long.toString(ProcessHandle.current().pid())
            };
            Runtime.getRuntime().exec(command);
        }
        handleStartSignal(readCFile);
        handleStopSignal(filename);
        synchronized (Runner.class) {
            Runner.class.wait();
        }
    }

    private static void handleStartSignal(Boolean readCFile) {
        Signal.handle(new Signal("USR1"), new SignalHandler() {
            public void handle(Signal sig) {
                log.append("Received START signal, starting powerjoular at " + LocalDateTime.now() + "\n");
                if (readCFile) {
                    childPid = WritePid.captureCommandOutput();
                } else {
                    ArrayList<String> pidFromFile = WritePid.readTargetProgramInfo();
                    childPid = pidFromFile.get(0);
                }
                log.append("ParentPID: "+ProcessHandle.current().pid()+" ChildPID: " + childPid + "\n");
                startTime = System.currentTimeMillis();
                ProcessBuilder powerjoularBuilder = new ProcessBuilder("powerjoular", "-l", "-p", childPid, "-D",
                        frequency, "-f", "powerjoular.csv");
                try {
                    Process powerjoularProcess = powerjoularBuilder.start();
                    powerjoularPid = Long.toString(powerjoularProcess.pid());
                } catch (IOException e) {
                    e.printStackTrace();
                }   
            }
        });
    }

    private static void handleStopSignal(String filename) {
        Signal.handle(new Signal("USR2"), new SignalHandler() {
            public void handle(Signal sig) {
                log.append("Received STOP signal at " + LocalDateTime.now() + "\n");
                timeOutThread.interrupt();
                try {timeOutThread.join();} catch (InterruptedException e) {log.append(e+"\n");}
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
                if (!loopSizeFromFile.get(2).isEmpty()) log.append(loopSizeFromFile.get(2)+"\n");
                if (programThrowedError(filename)) {
                    //synchronized (Runner.class) {
                    //    Runner.class.notify();
                    //}
                    notifyRunnerClass();
                    return;
                }
                String cpuUsage = readCsv("powerjoular.csv-" + childPid + ".csv");
                log.append("Program used " + cpuUsage + "J\n");
                Double duration = (endTime - startTime) / 1000.0;
                log.append("Time taken: " + duration + " seconds, for " + loopSize + " operations\n");
                averageJoules += Double.parseDouble(cpuUsage);
                averageTime += endTime - startTime;
                try {
                    saveFeature(filename, cpuUsage);
                } catch (IOException e) {
                    log.append("Error saving feature\n");
                }
                notifyRunnerClass();
                //synchronized (Runner.class) {
                //    Runner.class.notify();
                //}
            }
        });
    }

    private static void notifyRunnerClass(){
        synchronized (Runner.class) {
            Runner.class.notify();
        }
    }

    private static boolean programThrowedError(String filename) {
        File f = new File("errorFiles/"+filename+".txt");
        if (f.exists() && !f.isDirectory()) return true;
        return false;
    }

    private static boolean skipProgram(String fileName) {
        if (programToSkip == null || avoidSize == null) return false;
        if (!fileName.split("\\d")[0].contains(programToSkip)) {
            programToSkip = null;
            avoidSize = null;
            return false;
        }
        if (getCurrentInputSize(fileName) < avoidSize) return false;
        log.append("Skipping "+fileName+", input size too large. It would take a lot of time.\n");
        log.append("Current program size: "+getCurrentInputSize(fileName) + " avoidSize: "+avoidSize+"\n");
        return true;
    }

    private static String readFile(String file) {
        String program = "";
        File myObj = new File("java_progs/progs/"+file+".java");
        try (Scanner myReader = new Scanner(myObj)) {
            StringBuilder f = new StringBuilder();
            while (myReader.hasNextLine()) {
                f.append(myReader.nextLine()).append("\n");
            }
            myReader.close();
            program = f.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return program;
    }

    private static Long getCurrentInputSize(String fileName) {
        ArrayList<String> inputs = getInputValues(fileName);
        String size = inputs.get(0);
        String loopSize = inputs.get(1);
        long totalValue = size != null ? Long.parseLong(size) : 0;
        if (loopSize != null) {
            long ls = Long.parseLong(loopSize);
            totalValue *= ls;
            totalValue += ls;
        }
        return totalValue;
        //return size != null && loopSize != null ? Long.parseLong(size) + Long.parseLong(loopSize): size != null ? Long.parseLong(size) : 0;
    }

    private static ArrayList<String> getInputValues(String fileName) {
        String program = readFile(fileName);
        String size = findMatchInPattern(program,"static int SIZE = " + "(\\d+)" + ";");
        String loopSize = findMatchInPattern(program,"static int loopSize = " + "(\\d+)" + ";");
        return new ArrayList<>(Arrays.asList(size,loopSize));
    }

    private static String findMatchInPattern(String txt, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(txt);
        if (!matcher.find()) return null;
        return matcher.group(1);
    }

    private static Thread handleTimeOutThread(String filename) {
        return new Thread() {
            public void run() {
                try {
                    Thread.sleep(timeOutTime*1000);
                    log.append("Program timed out.\nKilling process.\n");
                    try {
                        Process killPowerjoular = Runtime.getRuntime().exec(new String[]{"sudo", "kill", powerjoularPid});
                        killPowerjoular.waitFor();
                        Process killTargetProgram = Runtime.getRuntime().exec(new String[]{"sudo", "kill", childPid});
                        killTargetProgram.waitFor();
                        avoidSize = getCurrentInputSize(filename);
                        ArrayList<String> inputs = getInputValues(filename);
                        String s = inputs.get(0) != null ? inputs.get(0) : "0";
                        String ls = inputs.get(1) != null ? inputs.get(1) : "0";
                        log.append("SIZE = "+s+" listSize = "+ls +"\navoidSize = "+avoidSize+"\n");
                        programToSkip = filename.split("\\d")[0];
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    synchronized (Runner.class) {
                        Runner.class.notify();
                    }
                } catch (InterruptedException e) {
                    return;
                } catch (Exception e) {
                    log.append(e.getMessage()+"\n");
                } 
            }  
        };  
    }

    private static String readCsv(String csvFile) {
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
            log.append(e + "\n");
            log.append("Program ran so fast it did not create a CSV file or other error.\n");
        }
        Double cpuPower = 0.0;
        //TODO fix this so when it catches "+Inf***********" ignores it
        try {
            
            Double freq = Double.parseDouble(frequency);
            for (int i = 0; i < cpuPowerValues.size(); i++) {
                cpuPower += Double.parseDouble(cpuPowerValues.get(i)) * freq;
            }
            // return Double.toString(Math.round(cpuPower));
            cpuPower /= Integer.parseInt(loopSize);
            
        } catch (Exception e) {
            cpuPower = 0.0;
            log.append("Error with powerjoular csv.\n");
        }

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
        //System.out.println(methodfeatures);
        ArrayList<String> inputs = getInputValues(file);
        featuresName.addAll(methodfeatures.keySet());
        methodfeatures.put("Input1", inputs.get(0));
        if(inputs.get(1) != null) methodfeatures.put("Input2", inputs.get(1));
        methodfeatures.put("EnergyUsed", cpuUsage);
        methodfeatures.put("Filename", file);
        createFeaturesTempFile(file,methodfeatures);
    }

    private static String getFunMapName(String filename){
        String mapName = "";
        String program = readFile(filename);
        String regex = Introspector.decapitalize(filename)+"\\s*\\((.*)\\)\\s*\\{";
        String match = findMatchInPattern(program,regex);
        String[] params = match.split(",");
        for(String param : params){
            param = param.strip();
            String type = param.split(" ")[0].replaceAll("<.*>", "");
            if (mapName.isEmpty()) mapName += type;
            else mapName += " | "+type;
        }
        return filename + "."+Introspector.decapitalize(filename)+"("+mapName+")";
    }

    private static void createFeaturesCSV() throws IOException {
        try (BufferedWriter csvWriter = new BufferedWriter(new FileWriter(CSV_FILE_NAME))) {
            // Write the header row
            List<String> featureList = new ArrayList<>(featuresName);
            featureList.add("Input1");
            featureList.add("Input2");
            featureList.add("EnergyUsed");
            featureList.add("Filename");
            csvWriter.write(String.join(",", featureList));
            csvWriter.newLine();
            File[] tmpFiles = getAllFilesInDir("tmp/");
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

    private static int extractNumber(String filename) {
        return Integer.parseInt(filename.replaceAll("\\D+",""));
    }

    private static String extractFilename(String filename) {
        return filename.replaceAll("\\d+\\.class$","");
    }

    private static void saveLog(String filename) {
        File file = new File("logs/runner_logs/" + filename + ".txt");
        FileWriter fr;
        try {
            fr = new FileWriter(file, true);
            fr.write(log.toString());
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.setLength(0);
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

    private static String[] getAllFilenamesInDir(String dir) {
        File[] files =  getAllFilesInDir(dir);
        String[] filenames = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            filenames[i] = files[i].getName();
        }
        return filenames;
    }

    private static File[] getAllFilesInDir(String dir) {
        return new File(dir).listFiles();
    }

    private static String createLogFile() {
        String dir = "logs/runner_logs";
        //String[] files = getAllFilenamesInDir(dir);
        //Arrays.sort(files, Comparator.comparing(Runner::extractNumber));
        String filename = "log";//LocalDateTime.now().toString();
        createFile(dir+"/",filename);
        return filename;
        //if (files.length == 0) {
        //    createFile(dir+"/","RunnerLog_0");
        //    return "RunnerLog_0";
        //}
        //else {
        //    String filename = "RunnerLog_"+(Integer.parseInt(files[files.length-1].replaceAll("\\D+",""))+1);
        //    createFile(dir+"/",filename);
        //    return filename;
        //}
    }

    private static void createFile(String dir,String filename) {
        try {
            File myObj = new File(dir + filename + ".txt");
            myObj.createNewFile();
          } catch (IOException e) {
            e.printStackTrace();
          }
    }

}
