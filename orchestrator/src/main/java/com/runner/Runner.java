package com.runner;
import java.beans.Introspector;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ProcessBuilder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.parse.ASTFeatureExtractor;
import com.template.aux.WritePid;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import sun.misc.Signal;
import sun.misc.SignalHandler;

public class Runner {

    final static String CSV_FILE_NAME = "features.csv";
    final static String frequency = ".1";
    final static short timeOutTime = 60;//seconds
    static String classpath = "";
    static String dependencies = "";
    static String powerjoularPid = "";
    static String targetProgramFiles = "";
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
    static volatile boolean notifiedRunnerClass = false;

    public static void main(String[] args) throws IOException, InterruptedException  {
        dependencies = new String(Files.readAllBytes(Paths.get("cp.txt"))).trim();
        File parentDir = new File(".").getCanonicalFile().getParentFile();
        File codegenDir = new File(parentDir, "codegen");
        targetProgramFiles = new File(codegenDir, "src/main/java/com/generated_progs").getAbsolutePath();
        File targetClasses = new File(codegenDir, "target/classes");
        classpath = targetClasses.getAbsolutePath() + File.pathSeparator + dependencies;
        File[] dirsToRun = reviewBeforeRunning();
        for (File dirToRun : dirsToRun) {
            List<String> programs = getAllFilenamesInDir(dirToRun);//getAllFilenamesInDir(targetClassesPath);
            String currentDirBeingTested = dirToRun.getAbsolutePath();
            //Arrays.sort(programs, Comparator.comparing(Runner::extractFilename).thenComparingInt(Runner::extractNumber));
            String logFilename = createLogFile();
            for (int i = 0; i < programs.size(); i++) {
                //Thread.sleep(100);
                if (args != null && args.length == 3 && Integer.parseInt(args[2]) > 0) {
                    String fileName = programs.get(i).toString().replace(".class", "");//programs[i].toString().replace("java_progs/out/java_progs/progs/", "").replace(".class", "");//.replace("java_progs/progs/", "").replace(".java", "");
                    //if (!(args[0].equals("test") && fileName.equals("ArrayList_add_int_java_lang_Object_0"))) continue;//just to test one prog file
                    //if (!(args[0].equals("test") && fileName.equals("ArrayList_addAll_int_java_util_Collection_2400"))) continue;//just to test one prog file
                    log.append("---------------------------------------\n");
                    log.append("Program number -> " + i + "\n");
                    //System.out.println("Program number -> " + i);
                    //if (skipProgram(fileName,currentDirBeingTested)) continue;
                    log.append("Starting profile for " + fileName + " program\n");
                    Boolean readCFile = args[1].equals("t");
                    int runs = Integer.parseInt(args[2]);
                    log.append("Running " + (runs == 1 ? "1 time.\n" : runs + " times.\n"));
                    for (int j = 0; j < runs; j++) {
                        timeOutThread = handleTimeOutThread(fileName,currentDirBeingTested);
                        timeOutThread.start();
                        run(fileName,readCFile,currentDirBeingTested);
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
    }

    private static void runProgramCommand(String filename,String currentDirBeingTested) throws IOException{
        String[] command = {
            "java", 
            "-Xmx1024M",
            "-Xms1024M",
            "-cp", 
            classpath, 
            "com.generated_progs."+currentDirBeingTested.split("/")[currentDirBeingTested.split("/").length-1] +"." + filename, 
            Long.toString(ProcessHandle.current().pid())
        };
        Runtime.getRuntime().exec(command);
    }

    public static void run(String filename, Boolean readCFile, String currentDirBeingTested) throws IOException, InterruptedException {
        runProgramCommand(filename,currentDirBeingTested);
        handleStartSignal(readCFile);
        handleStopSignal(filename,currentDirBeingTested);
        synchronized (Runner.class) {
            Runner.class.wait();
        }
        notifiedRunnerClass = false;
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

    private static void handleStopSignal(String filename,String currentDirBeingTested) {
        Signal.handle(new Signal("USR2"), new SignalHandler() {
            public void handle(Signal sig) {
                log.append("Received STOP signal at " + LocalDateTime.now() + "\n");
                if (notifiedRunnerClass) return;
                notifiedRunnerClass = true;
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
                    saveFeature(filename, cpuUsage,currentDirBeingTested);
                } catch (IOException e) {
                    log.append("Error saving feature\n");
                }
                notifyRunnerClass();
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

    private static boolean skipProgram(String fileName,String currentDirBeingTested) throws IOException {
        if (programToSkip == null || avoidSize == null) return false;
        if (!fileName.split("\\d")[0].contains(programToSkip)) {
            programToSkip = null;
            avoidSize = null;
            return false;
        }
        if (getCurrentInputSize(fileName,currentDirBeingTested) < avoidSize) return false;
        log.append("Skipping "+fileName+", input size too large. It would take a lot of time.\n");
        log.append("Current program size: "+getCurrentInputSize(fileName,currentDirBeingTested) + " avoidSize: "+avoidSize+"\n");
        return true;
    }

    private static String readFile(String file,String currentDirBeingTested) throws IOException {
        String program = "";
        
            
        File codegenDir = new File(new File(".").getCanonicalFile().getParentFile(),"codegen");
        String dir = currentDirBeingTested.split("/")[currentDirBeingTested.split("/").length-1];
        File myObj = new File(codegenDir.getAbsolutePath()+"/src/main/java/com/generated_progs/"+dir+"/"+file+".java");
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

    private static Long getCurrentInputSize(String fileName, String currentDirBeingTested) throws IOException {
        ArrayList<String> inputs = getInputValues(fileName, currentDirBeingTested);
        String size = inputs.get(0);
        String loopSize = inputs.get(1);
        //long totalValue = size != null ? Long.parseLong(size) : 0;
        //if (loopSize != null) {
        //    long ls = Long.parseLong(loopSize);
        //    totalValue *= ls;
        //    totalValue += ls;
        //}
        //return totalValue;
        return size != null && loopSize != null ? Long.parseLong(size) + Long.parseLong(loopSize): size != null ? Long.parseLong(size) : 0;
    }

    private static ArrayList<String> getInputValues(String fileName, String currentDirBeingTested) throws IOException {
        String program = readFile(fileName,currentDirBeingTested);
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

    private static Thread handleTimeOutThread(String filename,String currentDirBeingTested) {
        return new Thread() {
            public void run() {
                try {
                    Thread.sleep(timeOutTime*1000);
                    // Ensure only one notification happens
                    if (notifiedRunnerClass) return;
                    log.append("Program timed out.\nKilling process.\n");
                    try {
                        Process killPowerjoular = Runtime.getRuntime().exec(new String[]{"sudo", "kill", powerjoularPid});
                        killPowerjoular.waitFor();
                        Process killTargetProgram = Runtime.getRuntime().exec(new String[]{"sudo", "kill", childPid});
                        killTargetProgram.waitFor();
                        avoidSize = getCurrentInputSize(filename,currentDirBeingTested);
                        ArrayList<String> inputs = getInputValues(filename,currentDirBeingTested);
                        String s = inputs.get(0) != null ? inputs.get(0) : "0";
                        String ls = inputs.get(1) != null ? inputs.get(1) : "0";
                        log.append("SIZE = "+s+" listSize = "+ls +"\navoidSize = "+avoidSize+"\n");
                        programToSkip = filename.split("\\d")[0];
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    notifiedRunnerClass = true;
                    notifyRunnerClass();
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

    private static void saveFeature(String file, String cpuUsage, String currentDirBeingTested) throws IOException  {
        getFeaturesFromParser(file, cpuUsage,currentDirBeingTested);
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

    private static void getFeaturesFromParser(String file, String cpuUsage,String currentDirBeingTested) throws IOException {
        String path = targetProgramFiles+"/"+file.replaceAll("\\d+", "")+"/";
        ASTFeatureExtractor parser = new ASTFeatureExtractor(path,file,true);
        HashMap<String, Map<String, Object>> methods = parser.getFeatures();
        List<String> inputValues = parser.getNumberOfInputs();
        
        String methodName = getFunMapName(file,currentDirBeingTested);
        Map<String, Object> methodfeatures = methods.get(methodName);
        for (int i = 0; i < inputValues.size(); i++) {
            //System.out.println(inputValues);
            methodfeatures.put("input"+i, inputValues.get(i));
        }
        //ArrayList<String> inputs = getInputValues(file,currentDirBeingTested);
        featuresName.addAll(methodfeatures.keySet());
        //methodfeatures.put("Input1", inputs.get(0));
        //if(inputs.get(1) != null) methodfeatures.put("Input2", inputs.get(1));
        methodfeatures.put("EnergyUsed", cpuUsage);
        methodfeatures.put("Filename", file);
        createFeaturesTempFile(file,methodfeatures);
    }

    private static String getFunMapName(String filename,String currentDirBeingTested) throws IOException{
        String mapName = "";
        String program = readFile(filename,currentDirBeingTested);  
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
            //featureList.add("Input1");
            //featureList.add("Input2");
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

    private static File[] reviewBeforeRunning() {
        File[] subDirs = null;
        try {
            // Step 1: Get the current working directory
            File currentDir = new File(".").getCanonicalFile();

            // Step 2: Go up one level (parent directory)
            File parentDir = currentDir.getParentFile();
            if (parentDir == null) {
                throw(new Exception("Error: Cannot find parent directory!"));
            }

            // Step 3: Locate the `codegen` directory
            File codegenDir = new File(parentDir, "codegen");
            if (!codegenDir.exists()) {
                throw(new Exception("Error: 'codegen' directory not found in " + parentDir.getAbsolutePath()));
            }

            // Step 4: Check if compiled classes exist
            File targetClasses = new File(codegenDir, "target/classes/com/generated_progs/");
            //File targetClasses = new File(codegenDir, "target/classes/");
            if (!targetClasses.exists()) {
                throw(new Exception("Error: Compiled classes not found! Run 'mvn clean compile' first."));
            }

            subDirs = targetClasses.listFiles(File::isDirectory);

            // Step 5: Read the classpath from cp.txt
            File cpFile = new File("cp.txt");
            if (!cpFile.exists()) {
                throw(new Exception("Error: cp.txt not found! Run 'mvn dependency:build-classpath' first."));
            } 
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return subDirs;
    }

    private static String[] getAllFilenamesInDir(String dir) {
        List<File> files =  getAllTargetedFilesInDir(dir);
        String[] filenames = new String[files.size()];
        for (int i = 0; i < files.size(); i++) {
            filenames[i] = files.get(i).getName();
        }
        return filenames;
    }

    private static List<String> getAllFilenamesInDir(File dir) {
        File[] files =  dir.listFiles(File::isFile);
        List<String> fileNames = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            if(!files[i].getName().contains("BenchmarkArgs")) fileNames.add(files[i].getName());
        }
        return fileNames;
    }

    

    private static String createLogFile() {
        String dir = System.getProperty("user.dir")+"/logs/runner_logs";
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
            System.out.println(e.getMessage() + " -> "+dir+filename);
            //e.printStackTrace();
          }
    }

    private static ArrayList<File> getAllTargetedFilesInDir(String dir) {
        File[] files = getAllFilesInDir(dir);
        ArrayList<File> filesFiltered = new ArrayList<>();
        for (File f : files) {
            if (!f.getName().contains("BenchmarkArgs")) filesFiltered.add(f);
        }
        return filesFiltered;
    }

    private static File[] getAllFilesInDir(String dir) {
        return new File(dir).listFiles();
    }

}
