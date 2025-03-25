package com.template;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Programs {
    final static String OUTPUT_DIR = System.getProperty("user.dir") +"/src/main/java/com/generated_progs";
    final static String outputProgramsDir = "src/main/java/com/template/generated_progs/";
    final static String outputProgramsCompiledDir = "src/main/java/com/template/generated_progs_comp";

    public static List<String> getGeneratedFiles() {
        System.out.println(System.getProperty("user.dir"));
        File dir = new File(OUTPUT_DIR);
        List<String> filePaths = new ArrayList<>();
        if (dir.exists() && dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                filePaths.add(file.getAbsolutePath());
            }
        }
        return filePaths;
    }

    public static void createOutputDirForClasses(String outputDir) {
        File outputDirFile = new File(outputDir);
        if (!outputDirFile.exists()) {
            outputDirFile.mkdirs();
        }
    }

    public static void compileJavaFiles() throws IOException {
        File auxFile = File.createTempFile("javac_aux", ".java");
        List<String> sources = new ArrayList<>();

        createOutputDirForClasses(outputProgramsCompiledDir);

        // Find all .java files in the search directory and add them to sources
        Files.walk(Paths.get(OUTPUT_DIR))
            .filter(path -> path.toString().endsWith(".java"))
            .forEach(path -> sources.add(path.toString()));

        // Include the auxiliary file
        sources.add(auxFile.getPath());

        // Create a temporary file to store all the source paths
        File tempFile = File.createTempFile("javac_sources", ".txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            for (String source : sources) {
                writer.write(source);
                writer.newLine();
            }
        }

        // Compile using javac with the @file argument for efficiency
        ProcessBuilder processBuilder = new ProcessBuilder("javac", "-d", outputProgramsCompiledDir, "@"+tempFile.getAbsolutePath());
        processBuilder.inheritIO();  // To show output in the console
        Process process = processBuilder.start();
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Remove the temporary file after compilation
        //tempFile.delete();
        //auxFile.delete();
        System.out.println("Compilation complete!");
    }

    public static void deleteFolder(String folderPath) {
        // Create a File object from the folder path
        File folder = new File(folderPath);

        // If the folder does not exist or is not a directory, do nothing
        if (folder == null || !folder.exists()) {
            System.out.println("Folder does not exist: " + folderPath);
            return;
        }

        // If the folder contains files, delete them
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // Recursively delete subdirectories
                    deleteFolder(file.getAbsolutePath());
                } else {
                    // Delete individual files
                    file.delete();
                    System.out.println("Deleted file: " + file.getAbsolutePath());
                }
            }
        }

        // Finally, delete the folder itself
        folder.delete();
        System.out.println("Deleted folder: " + folder.getAbsolutePath());
    }
    
}
