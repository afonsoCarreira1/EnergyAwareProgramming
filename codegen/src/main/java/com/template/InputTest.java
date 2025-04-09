package com.template;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.mutable.MutableBoolean;

import spoon.Launcher;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtTry;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.factory.Factory;

public class InputTest {

    static int timeoutMilliseconds = 5_000;
    static int maxInputToTest = 100_000;

    public static List<Integer> findMaxInput(String program, String template) throws IOException, InterruptedException{
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
        modifyProgramCode(template);
        compileProgram();
        return runProgramToGetMaxInputs(inputType);
        
    }

    private static String[] createCommandArray(String[] args) throws IOException {
        String cp = new String(Files.readAllBytes(Paths.get("cp.txt"))).trim() + ":target/classes/";
        String[] defaultCommand = {
            "java",
            "-Xmx4056M",
            "-Xms4056M",
            "-cp", cp,
            "com.generated_InputTestTemplate.Prog"};
        //String[] command = new String[defaultCommand.length+args.length];
        //int index = 0;
        //for (int i = 0; i < defaultCommand.length; i++) {
        //    command[i] = defaultCommand[i];
        //    index++;
        //}
        //for (int i = 0; i < args.length; i++) {
        //    command[index] = args[i];
        //    index++;
        //}
        String[] command = new String[defaultCommand.length + args.length];
        System.arraycopy(defaultCommand, 0, command, 0, defaultCommand.length);
        System.arraycopy(args, 0, command, defaultCommand.length, args.length);
        return command;
    }

    private static Process runProgram(String[] args) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(createCommandArray(args));
        pb.redirectErrorStream(true);
        return pb.start();      
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

    private static List<Integer> runProgramToGetMaxInputs(Map<String,String> inputType) throws IOException {
        List<String> types = new ArrayList<String>(inputType.values());
        List<Integer> maxInputs = new ArrayList<>();
        for (int i = 0; i < types.size(); i++) {
            String[] args = new String[types.size()];
            Arrays.fill(args, "1");
            int maxInput = findMaxAcceptableInput(maxInputToTest,args,i);
            //System.out.println("In"+i+": max input -> "+maxInput +", for type -> "+types.get(i));
            maxInputs.add(maxInput);
        }
        return maxInputs;
    }

    private static boolean isInputOk(String[] args) throws IOException {
        MutableBoolean timeoutError = new MutableBoolean(false);
        MutableBoolean programError = new MutableBoolean(false);
        Process process = runProgram(args);
        waitForProgramToFinish(process,timeoutError,programError,argsToString(args));
        if (timeoutError.isTrue() || programError.isTrue()) return false;
        return true;
    }

    private static String argsToString(String[] args) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            sb.append(args[i]);
            if (i != args.length-1) sb.append(" "); 
        }
        return sb.toString();
    }

    private static void waitForProgramToFinish(Process process, MutableBoolean timeoutError,MutableBoolean programError,String args) {  
        try {
            boolean finished = process.waitFor(timeoutMilliseconds, java.util.concurrent.TimeUnit.MILLISECONDS);
            if (!finished) {
                timeoutError.setTrue();  // Process did not finish in x seconds
                process.destroy();       // Optionally kill it
                //System.out.println("Process timed out in "+timeoutMilliseconds/1000.0+"s for input: "+args);
            } else {
                timeoutError.setFalse(); // Process finished in time
                if (process.exitValue() != 0) programError.setTrue();
                //System.out.println("Process finished with exit code: " + process.exitValue() + " for input: "+args);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            timeoutError.setTrue();
        }
    }

    private static void modifyProgramCode(String template) throws IOException {
        Launcher launcher = TemplateCreator.initSpoon(new ArrayList<>(Arrays.asList("src/main/java/com/template/","src/main/java/com/generated_InputTestTemplate/")));
        Factory factory = launcher.getFactory();
        CtClass<?> targetClass = factory.Class().get("com.generated_InputTestTemplate.Prog");
        List<CtTry> tryBlocks = targetClass.getElements(e -> e instanceof CtTry);
        clearAndInsertCatchStatement(factory,tryBlocks,0);
        clearAndInsertCatchStatement(factory,tryBlocks,1);
        tryBlocks.get(0).getFinalizer().delete(); //remove finally
        tryBlocks.get(0).insertAfter(factory.Code().createCodeSnippetStatement("System.out.println(\"aki\");\nSystem.exit(0)"));// // "System.out.println(\"stop\")"
        removeSpecificStatement(tryBlocks.get(0).getBody().getStatements(), "TemplatesAux.sendStartSignalToOrchestrator(args[0])");
        TemplateCreator.createJavaProgramFile(TemplateCreator.initialPath+"generated_InputTestTemplate/Prog.java", getImportsToAdd(template)+targetClass.toString());

    }

    private static void removeSpecificStatement(List<CtStatement> statements, String statement) {
        for (int i = 0; i < statements.size(); i++) {
            if (statements.get(i).toString().equals(statement)) statements.remove(i);
        }
    }

    private static void clearAndInsertCatchStatement(Factory factory, List<CtTry> tryBlocks, int index) {
        CtBlock<?> catchBody = tryBlocks.get(0).getCatchers().get(index).getBody();
        catchBody.getStatements().clear(); //delete catch statements
        catchBody.insertBegin(factory.Code().createCodeSnippetStatement("System.out.println(e.getMessage());\nSystem.exit("+(index+1)+")"));// // System.out.println(\"error\") //TemplatesAux.writeInFile(\"progFile\", \"error,stop\")
    }

    public static String readFileToString(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }

    private static String getImportsToAdd(String template) {
        StringBuilder sb = new StringBuilder("package com.generated_InputTestTemplate;\n");
        List<String> imports = extractFromString(template, "(import .*;)");
        for(String imp : imports) sb.append(imp+"\n");
        return sb.toString();
        //return "package com.generated_InputTestTemplate;\n"+
        //        "import com.template.SharedFlag;\n"+
        //        "import com.template.aux.DeepCopyUtil;\n"+
        //        "import com.fasterxml.jackson.core.type.TypeReference;\n"+
        //        "import com.template.aux.TemplatesAux;\n"+
        //        "import com.template.programsToBenchmark.Fibonacci;\n"+
        //        "import com.template.programsToBenchmark.Test;\n";
    }

    private static List<String> extractFromString(String input, String regex) {
        List<String> matches = new ArrayList<>();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            matches.add(matcher.group());
        }
        return matches;
    }

    public static int findMaxAcceptableInput(int max, String args[], int argsPos) throws IOException {
        int low = 0;
        int high = max;
        int result = 1;
    
        while (low <= high) {
            int mid = low + (high - low) / 2;
            args[argsPos] = mid+"";
            if (isInputOk(args)) {
                result = mid;     // this input is acceptable, try higher
                low = mid + 1;
            } else {
                high = mid - 1;   // too high, go lower
            }
        }
        return result;
    }
    
}
