package com.template;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtTry;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;

public class InputTest {

    public static void findMaxInput(String program) throws IOException, InterruptedException{
        int numberOfInputs = TemplateCreator.findNumberOfInputs(program);
        StringBuilder inputBuild = new StringBuilder();
        
        List<String> valuesToReplace = TemplateCreator.findStringsToReplace(program,"ChangeValueHere\\d+_[^\",;\\s]+");
        String finalProgram = program.replaceAll("public class .* \\{", "public class Prog {").replaceAll("package .*;", "package com.generated_InputTestTemplate;");
        Map<String,String> inputType = new HashMap<>();
        for (int i = 0; i < valuesToReplace.size(); i++) {
            String valueToReplace = valuesToReplace.get(i);
            String[] valueSplitted = valueToReplace.split("_");
            String type = valueSplitted[1];
            finalProgram = finalProgram.replace("\""+valueToReplace+"\"", "in"+i);
            inputType.put("in"+i, type);
        }
        for (int i = 0; i < numberOfInputs; i++) {
            inputBuild.append("        "+inputType.get("in"+i)+" in"+i+" = "+TemplateCreator.getPrimitiveTypeToWrapper(inputType.get("in"+i))+".valueOf(args["+i+"]);\n");
        }
        
        finalProgram = finalProgram.replace("int iter = 0;", "int iter = 0;\n"+inputBuild.toString());
        TemplateCreator.createJavaProgramFile(TemplateCreator.initialPath+"generated_InputTestTemplate/Prog.java", finalProgram);
        //compileProgram();
        modifyProgramCode();
        System.exit(0);
    }

    private static void runProgram(String args) throws IOException {
        String classpath = "target/classes/";
        String[] command = {
            "java", 
            "-Xmx4056M",
            "-Xms4056M",
            "-cp", 
            classpath, 
            "com.generated_progs.Prog", 
            args
        };
        Runtime.getRuntime().exec(command);
    }

    private static void compileProgram() throws InterruptedException, IOException {
        String cp = new String(Files.readAllBytes(Paths.get("cp.txt"))).trim()+ ":target/classes";
        ProcessBuilder pb = new ProcessBuilder(
                "javac",
                "-cp", cp,
                "-d", "target/classes",
                "src/main/java/com/generated_InputTestTemplate/Prog.java"
        );
        pb.inheritIO();
        pb.start().waitFor();
    }

    private static void runProgramToGetMaxInputs(int numberOfInputs) {
        for (int i = 0; i < numberOfInputs; i++) {
            
        }
        long start = System.currentTimeMillis();
        long timeout = 60_000; // 1 minute
        boolean timeoutError = false;

        while (!SharedFlag.stop) {
            if (System.currentTimeMillis() - start >= timeout) {
                timeoutError = true;
                break;
            }
            try {
                Thread.sleep(100); // check every 100ms
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (SharedFlag.error || timeoutError) {
            //stop input expansion
        }
    }

    private static void modifyProgramCode() throws IOException {
        Launcher launcher = TemplateCreator.initSpoon(new ArrayList<>(Arrays.asList("src/main/java/com/template/","src/main/java/com/generated_InputTestTemplate/")));
        Factory factory = launcher.getFactory();
        CtClass<?> targetClass = factory.Class().get("com.generated_InputTestTemplate.Prog");
        List<CtTry> tryBlocks = targetClass.getElements(e -> e instanceof CtTry);
        clearAndInsertCatchStatement(factory,tryBlocks,0);
        clearAndInsertCatchStatement(factory,tryBlocks,1);
        tryBlocks.get(0).getFinalizer().delete(); //delete finally
        removeSpecificStatement(tryBlocks.get(0).getBody().getStatements(), "TemplatesAux.sendStartSignalToOrchestrator(args[0])");
        TemplateCreator.createJavaProgramFile(TemplateCreator.initialPath+"generated_InputTestTemplate/Prog.java", getImportsToAdd()+targetClass.toString());

    }

    private static void removeSpecificStatement(List<CtStatement> statements, String statement) {
        for (int i = 0; i < statements.size(); i++) {
            if (statements.get(i).toString().equals(statement)) statements.remove(i);
        }
    }

    private static void clearAndInsertCatchStatement(Factory factory, List<CtTry> tryBlocks, int index) {
        CtBlock<?> catchBody = tryBlocks.get(0).getCatchers().get(index).getBody();
        catchBody.getStatements().clear(); //delete catch statements
        catchBody.insertBegin(factory.Code().createCodeSnippetStatement("SharedFlag.stop = true;\nSharedFlag.error = true"));
    }

    private static String getImportsToAdd() {
        return "package com.generated_InputTestTemplate;\n"+
                "import com.template.SharedFlag;\n"+
                "import com.template.aux.DeepCopyUtil;\n"+
                "import com.fasterxml.jackson.core.type.TypeReference;\n"+
                "import com.template.aux.TemplatesAux;\n"+
                "import com.template.programsToBenchmark.Fibonacci;\n"+
                "import com.template.programsToBenchmark.Test;\n";
    }
    
}
