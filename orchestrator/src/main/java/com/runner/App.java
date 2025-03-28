package com.runner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception
    {
        //Runner.main(new String[]{"test","f","1"});
        runCommand();
    }

    private static void reviewBeforeRunning() {
        try {
            // Step 1: Get the current working directory

            // Step 2: Go up one level (parent directory)
            File parentDir = new File(".").getCanonicalFile().getParentFile();
            if (parentDir == null) {
                System.out.println("Error: Cannot find parent directory!");
                return;
            }

            // Step 3: Locate the `codegen` directory
            File codegenDir = new File(parentDir, "codegen");
            if (!codegenDir.exists()) {
                System.out.println("Error: 'codegen' directory not found in " + parentDir.getAbsolutePath());
                return;
            }

            // Step 4: Check if compiled classes exist
            File targetClasses = new File(codegenDir, "target/classes/com/generated_progs/ArrayList_add_java_lang_Object_");
            if (!targetClasses.exists()) {
                System.out.println("Error: Compiled classes not found! Run 'mvn clean compile' first.");
                return;
            }

            // Step 5: Read the classpath from cp.txt
            File cpFile = new File("cp.txt");
            if (!cpFile.exists()) {
                System.out.println("Error: cp.txt not found! Run 'mvn dependency:build-classpath' first.");
                return;
            }
            
            //runCommand(targetClasses);
            ArrayList<File> files = getAllTargetedFilesInDir(targetClasses.getAbsolutePath());
            

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void runCommand() throws IOException, InterruptedException {
        String dependencies = new String(Files.readAllBytes(Paths.get("cp.txt"))).trim();
        File parentDir = new File(".").getCanonicalFile().getParentFile();
        File codegenDir = new File(parentDir, "codegen");

        File targetClasses = new File(codegenDir, "target/classes");
    
        String javaCmd = "java";
        String classpath = targetClasses.getAbsolutePath() + File.pathSeparator + dependencies;

        String mainClass = "com.generated_progs.ArrayList_add_java_lang_Object_.ArrayList_add_java_lang_Object_10";
    
        ProcessBuilder processBuilder = new ProcessBuilder(javaCmd, "-cp", classpath, mainClass,"1");
        processBuilder.inheritIO();
        Process process = processBuilder.start();
        process.waitFor();
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
