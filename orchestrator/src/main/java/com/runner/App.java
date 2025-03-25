package com.runner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.parse.ASTFeatureExtractor;
import com.template.Programs;
import com.template.TemplateCreator;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception
    {
        run();
        //File[] files = getAllFilesInDir("codegen/target/classes/com/generated_progs/");
        //System.out.println(files);
        //for (File f : files) {
        //    System.out.println(f.getName());
        //}
        //System.out.println(Programs.getGeneratedFiles());
        //HashMap<String, Map<String, Object>> methods = ASTFeatureExtractor.getFeatures("src/main/java/com/parse/","TestFile",false);
        //Map<String, Object> methodfeatures = methods.get("TestFile.t()");
        //System.out.println(methodfeatures);
    }

    private static File[] getAllFilesInDir(String dir) {
        return new File(dir).listFiles();
    }

    private static void run() {
        try {
            // Step 1: Get the current working directory
            File currentDir = new File(".").getCanonicalFile();

            // Step 2: Go up one level (parent directory)
            File parentDir = currentDir.getParentFile();
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
            File targetClasses = new File(codegenDir, "target/classes");
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
            String dependencies = new String(Files.readAllBytes(Paths.get("cp.txt"))).trim();

            // Step 6: Construct the Java command with the correct classpath
            String javaCmd = "java";
            String classpath = targetClasses.getAbsolutePath() + File.pathSeparator + dependencies;
            String mainClass = "com.generated_progs.ArrayList_add_java_lang_Object_10";//"com.template.t";
            

            ProcessBuilder processBuilder = new ProcessBuilder(javaCmd, "-cp", classpath, mainClass);
            processBuilder.inheritIO(); // Show output in the console
            Process process = processBuilder.start();
            process.waitFor();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
