package com.template;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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

    int timeoutMilliseconds = 5_000;//5_000;
    int maxInputToTest = 100_000;
    String filename;
    String program;
    String template;
    int funCallNum;
    String inputsDir = TemplateCreator.initialPath+"generated_InputTestTemplateData";
    List<InputData> inputData;
    Map<String,String> alreadyStoredInputData = new HashMap<>();
    String progFName;

    public InputTest(String filename, String program, String template, int funCallNum) {
        this.filename = filename;
        this.program = program;
        this.template = template;
        this.funCallNum = funCallNum;
        this.inputData = new ArrayList<>();
        this.progFName = "Prog_"+filename;
    }
    

    public  List<Integer> findMaxInput() throws IOException, InterruptedException{
        int numberOfInputs = TemplateCreator.findNumberOfInputs(program);
        StringBuilder inputBuild = new StringBuilder();
        String inputInit = "";
        List<String> valuesToReplace = TemplateCreator.findStringsToReplace(program,"ChangeValueHere\\d+_[^\",;\\s]+");
        String finalProgram = program.replaceAll("public class .* \\{", "public class "+progFName+" {").replaceAll("package .*;", "package com.generated_InputTestTemplate;");
        Map<String,String> inputType = new HashMap<>();
        for (int i = 0; i < valuesToReplace.size(); i++) {
            String valueToReplace = valuesToReplace.get(i);
            String[] valueSplitted = valueToReplace.split("_");
            String type = valueSplitted[1];
            finalProgram = finalProgram.replace("\""+valueToReplace+"\"", "in"+i);
            inputInit+="    static "+type+" in"+i+";\n";
            inputType.put("in"+i, type);
        }
        for (int i = 0; i < numberOfInputs; i++) {
            inputBuild.append("        "+" in"+i+" = "+TemplateCreator.getPrimitiveTypeToWrapper(inputType.get("in"+i))+".valueOf(args["+i+"]);\n");
        }

        finalProgram = finalProgram.replace("public class "+progFName+" {","public class "+progFName+" {"+inputInit);
        

        List<String> types = new ArrayList<String>(inputType.values());
        List<Integer> maxInputsFromData = getInputDataIfItExists(types);

        if (maxInputsFromData != null) return maxInputsFromData;
        finalProgram = finalProgram.replace("int iter = 0;", "int iter = 0;\n"+inputBuild.toString());
        TemplateCreator.createFile(TemplateCreator.initialPath+"generated_InputTestTemplate/"+progFName+".java", finalProgram,false);
        modifyProgramCode();
        compileProgram();
        return runProgramToGetMaxInputs(types); 
    }

    private List<Integer> getInputDataIfItExists(List<String> types) {
        List<Integer> maxInputs = new ArrayList<>();
        new File(TemplateCreator.initialPath+"generated_InputTestTemplateData").mkdirs();
        String path = inputsDir+"/"+filename+".txt";
        if (new File(path).exists()) {
            getInputDataFromFile(path);
            //System.out.println("alreadyStoredInputData → "+alreadyStoredInputData);
            for (int i = 0; i < types.size(); i++) {
                String match = "in" + i + " | " +"arrSize: "+funCallNum+" | "+ "type: "+types.get(i);
                if (alreadyStoredInputData.containsKey(match)) maxInputs.add(Integer.parseInt(alreadyStoredInputData.get(match))); 
                else return null; //se nao encontrar nem que seja um match entao quero ir buscar novos resultados
                // ex: 
                //match -> in0 | arrSize: 75000 | type: int
                //match -> in1 | arrSize: 75000 | type: Double
                // 1st tem match e o 2nd nao, logo preciso de fazer nova procura
            }
        }
        return maxInputs.isEmpty() ? null : maxInputs;
    }

    public void getInputDataFromFile(String path) {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] arrSizeAndType = line.split(" -> ");
                alreadyStoredInputData.put(arrSizeAndType[0],arrSizeAndType[1].split(": ")[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveMaxInputsInFile(List<InputData> inputData) throws IOException{
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < inputData.size(); i++) {
            sb.append(inputData.get(i).toString());
        }
        TemplateCreator.createFile(inputsDir+"/"+filename+".txt", sb.toString().replace("[","").replace("]", ""),true);
    }

    private  String[] createCommandArray(String[] args) throws IOException {
        String cp = new String(Files.readAllBytes(Paths.get("cp.txt"))).trim() + ":target/classes/";
        String[] defaultCommand = {
            "java",
            "-Xmx4056M",
            "-Xms4056M",
            "-cp", cp,
            "com.generated_InputTestTemplate."+progFName};
        String[] command = new String[defaultCommand.length + args.length];
        System.arraycopy(defaultCommand, 0, command, 0, defaultCommand.length);
        System.arraycopy(args, 0, command, defaultCommand.length, args.length);
        return command;
    }

    private  Process runProgram(String[] args) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(createCommandArray(args));
        pb.redirectErrorStream(true);
        return pb.start();      
    }

    private  void compileProgram() throws InterruptedException, IOException {
        String cp = new String(Files.readAllBytes(Paths.get("cp.txt"))).trim()+ ":target/classes";
        ProcessBuilder pb = new ProcessBuilder(
                "javac",
                "-cp", cp,
                "-d", "target/classes",
                "src/main/java/com/generated_InputTestTemplate/"+progFName+".java"
        );
        pb.inheritIO();
        pb.start().waitFor();
    }

    private  List<Integer> runProgramToGetMaxInputs(List<String> types) throws IOException {
        List<Integer> maxInputs = new ArrayList<>();
        List<InputData> inputData = new ArrayList<>();
        for (int i = 0; i < types.size(); i++) {
            String[] args = new String[types.size()];
            Arrays.fill(args, "1");
            int maxInput = findMaxAcceptableInput(maxInputToTest,args,i);
            System.out.println("In"+i+": max input -> "+maxInput +", for type -> "+types.get(i));
            maxInputs.add(maxInput);
            inputData.add(new InputData(i,funCallNum, types.get(i), maxInput));
        }
        saveMaxInputsInFile(inputData);
        return maxInputs;
    }

    private  boolean isInputOk(String[] args) throws IOException {
        MutableBoolean timeoutError = new MutableBoolean(false);
        MutableBoolean programError = new MutableBoolean(false);
        Process process = runProgram(args);
        waitForProgramToFinish(process,timeoutError,programError,argsToString(args));
        if (timeoutError.isTrue() || programError.isTrue()) return false;
        return true;
    }

    private String argsToString(String[] args) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            sb.append(args[i]);
            if (i != args.length-1) sb.append(" "); 
        }
        return sb.toString();
    }

    private  void waitForProgramToFinish(Process process, MutableBoolean timeoutError,MutableBoolean programError,String args) {  
        try {
            boolean finished = process.waitFor(timeoutMilliseconds, java.util.concurrent.TimeUnit.MILLISECONDS);
            if (!finished) {
                timeoutError.setTrue();  // Process did not finish in x seconds
                process.destroy();       // Optionally kill it
                System.out.println("Process timed out in "+timeoutMilliseconds/1000.0+"s for input: "+args);
            } else {
                timeoutError.setFalse(); // Process finished in time
                if (process.exitValue() != 0) programError.setTrue();
                System.out.println("Process finished with exit code: " + process.exitValue() + " for input: "+args);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            timeoutError.setTrue();
        }
    }

    private void modifyProgramCode() throws IOException {
        Launcher launcher = TemplateCreator.initSpoon(new ArrayList<>(Arrays.asList("src/main/java/com/template/","src/main/java/com/generated_InputTestTemplate/")));
        Factory factory = launcher.getFactory();
        CtClass<?> targetClass = factory.Class().get("com.generated_InputTestTemplate."+progFName);
        List<CtTry> tryBlocks = targetClass.getElements(e -> e instanceof CtTry);
        clearAndInsertCatchStatement(factory,tryBlocks,0);
        clearAndInsertCatchStatement(factory,tryBlocks,1);
        tryBlocks.get(0).getFinalizer().delete(); //remove finally
        tryBlocks.get(0).insertAfter(factory.Code().createCodeSnippetStatement("System.out.println(\"aki\");\nSystem.exit(0)"));// // "System.out.println(\"stop\")"
        removeSpecificStatement(tryBlocks.get(0).getBody().getStatements(), "TemplatesAux.sendStartSignalToOrchestrator(args[0])");
        TemplateCreator.createFile(TemplateCreator.initialPath+"generated_InputTestTemplate/"+progFName+".java", getImportsToAdd(template)+targetClass.toString(),false);

    }

    private  void removeSpecificStatement(List<CtStatement> statements, String statement) {
        for (int i = 0; i < statements.size(); i++) {
            if (statements.get(i).toString().equals(statement)) statements.remove(i);
        }
    }

    private  void clearAndInsertCatchStatement(Factory factory, List<CtTry> tryBlocks, int index) {
        CtBlock<?> catchBody = tryBlocks.get(0).getCatchers().get(index).getBody();
        catchBody.getStatements().clear(); //delete catch statements
        catchBody.insertBegin(factory.Code().createCodeSnippetStatement("System.out.println(e.getMessage());\nSystem.exit("+(index+50)+")"));// // System.out.println(\"error\") //TemplatesAux.writeInFile(\"progFile\", \"error,stop\")
    }

    public  String readFileToString(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }

    private  String getImportsToAdd(String template) {
        StringBuilder sb = new StringBuilder("package com.generated_InputTestTemplate;\n");
        List<String> imports = extractFromString(template, "(import .*;)");
        for(String imp : imports) sb.append(imp+"\n");
        return sb.toString();
    }

    private  List<String> extractFromString(String input, String regex) {
        List<String> matches = new ArrayList<>();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            matches.add(matcher.group());
        }
        return matches;
    }

    public  int findMaxAcceptableInput(int max, String args[], int argsPos) throws IOException {
        int low = 0;
        int high = max;
        int result = 0;
    
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
