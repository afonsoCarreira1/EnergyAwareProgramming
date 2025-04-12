package com.template;

import java.beans.Introspector;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import spoon.Launcher;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;

public class TemplateCreator {

    final static String initialPath = "src/main/java/com/";
    static String outputDir = initialPath+"generated_templates";
    static int id = 0;
    static CtTypeReference<?> ref;
    static boolean isGeneric = true;

    private static HashSet<String> getTargetMethodSet(String[] args) {
        return args.length > 1 ? new HashSet<>(Arrays.asList(args[1].split(","))) : new HashSet<>();
    }
    public static void main(String[] args) throws Exception {
        args = new String[]{"Fibonacci"};//Fibonacci //TestTwoInputs
        if (args == null || args.length == 0) return;
        HashSet<String> targetMethods = getTargetMethodSet(args);
        String programToRun;
        List<CtMethod<?>> methods;
        List<CtType<?>> collections;
        boolean getCustomImports = false;
        if (args[0].equals("lists") || args[0].equals("sets")|| args[0].equals("maps")){
            collections = Arrays.asList(getCollections(args[0]));
            methods = getCollectionMethods(args[0]);
        } else {
            programToRun = args[0];
            Launcher launcher = initSpoon(new ArrayList<>(Arrays.asList("src/main/java/com/template/")));
            methods = getPublicMethodsInClass(launcher,programToRun);
            collections = Arrays.asList(ref.getTypeDeclaration());
            getCustomImports = true;
        }
        new File(initialPath+"generated_InputTestTemplate").mkdirs();
        createTemplates(collections,methods,getCustomImports,targetMethods);
        createProgramsFromTemplates();
    }

    private static void createTemplates(List<CtType<?>> collections, List<CtMethod<?>> methods,boolean getCustomImports,HashSet<String> targetMethods) {
        for (CtType<?> collec : collections) {
            for (CtMethod<?> method : methods) {
                //if (!collec.getSimpleName().equals("ArrayList")) continue;
                if (!targetMethods.contains(method.getSimpleName()) && !targetMethods.isEmpty()) continue;
                //System.out.println("Collec -> "+ collec.getSimpleName()+" method -> "+method.getSimpleName());
                Launcher launcher = initSpoon(new ArrayList<>(Arrays.asList("src/main/java/com/template/")));
                SpoonInjector spi = new SpoonInjector(launcher, launcher.getFactory(), 0, method.clone(),
                collec, "", 0, outputDir,isGeneric,getCustomImports);
                spi.injectInTemplate();
                spi.insertImport();
            }
        }
    }

    private static List<CtMethod<?>> getPublicMethodsInClass(Launcher launcher,String programName) {
        List<CtMethod<?>> publicMethods = new ArrayList<>();
        for (CtType<?> ctType : launcher.getModel().getAllTypes()) {
            if (ctType instanceof CtClass<?>) { 
                CtClass<?> ctClass = (CtClass<?>) ctType;
                if (!ctClass.getSimpleName().toLowerCase().equals(programName.toLowerCase())) continue;
                // check if i need to do Class<Type>() or just Class()
                if (ctClass instanceof CtClass<?>) {
                    if (!ctClass.getFormalCtTypeParameters().isEmpty()) isGeneric = true;
                    else isGeneric = false;
                }
                ref = ctClass.getReference();
                Set<CtMethod<?>> methods = ctClass.getMethods();
                for (CtMethod<?> method : methods) {
                    if (method.isPrivate()) continue; //so quero os public
                    if (method.isProtected()) continue; //nao quero metodos private
                    if (method.getSimpleName().toLowerCase().equals("clone")) continue; //nao quero o metodo clone
                    publicMethods.add(method);
                }
            }
        }
        return publicMethods;
    }

    private static void createProgramsFromTemplates() throws IOException, InterruptedException {
        List<Integer> sizes = createInputRange(1, 1.5, 0);//Arrays.asList(150);
        int[] funCalls =  new int[] { /*20_000, 50_000,*/ 75_000, 100_000, 150_000 };//{20_000};
        File[] templates = getAllTemplates();
        int id = 0;
        for (File templateFile : templates) {
            String className = templateFile.toString().replace(outputDir+"/","").split("\\.java")[0];
            String dirName = initialPath+"generated_progs/"+className;
            new File(dirName).mkdirs();
            String template = readFile(templateFile.toString());
            for (String type : getTypes()) {
                String programChangedType = template.replace("changetypehere", type);
                for (int funCall : funCalls) {
                    String programChangedFunCall = programChangedType.replace("\"numberOfFunCalls\"", funCall+"");
                    InputTest inputTest = new InputTest(className,programChangedFunCall,template,funCall);
                    List<Integer> maxInputs = inputTest.findMaxInput();
                    System.out.println(maxInputs);
                    
                    for (int size : sizes) {
                        String finalProg = replaceValues(programChangedFunCall,size,maxInputs);
                        String methodNameForClass = Introspector.decapitalize(className);
                        finalProg = finalProg.replaceAll("(?<!generated_progs\\.)"+className+"",className+id);
                        finalProg = finalProg.replaceAll("(?<!generated_progs\\.)"+methodNameForClass+"",methodNameForClass+id);
                        //finalProg = finalProg.replace(className,className+id);
                        createFile(dirName+"/"+className+id+".java",finalProg,false);
                        id++;
                    }
                }
            }  
        }
    }

    static int findNumberOfInputs(String program){
        String keyword = "input\\d+";
        Matcher matcher = Pattern.compile(keyword).matcher(program);
        HashSet<String> results = new HashSet<>();
        while (matcher.find()) {
            results.add(matcher.group());
        }    
        return results.size();
    } 

    

    public static String getPrimitiveTypeToWrapper(String type) {
        switch (type) {
            case "int": return "Integer";
            case "boolean": return "Boolean";
            case "char": return "Character";
            case "byte": return "Byte";
            case "short": return "Short";
            case "long": return "Long";
            case "float": return "Float";
            case "double": return "Double";
            case "void": return "Void";
            default: return type; // not a primitive, return as is
        }
    }
    


    public static List<String> findStringsToReplace(String input,String keyword){
        Matcher matcher = Pattern.compile(keyword).matcher(input);
        HashSet<String> results = new HashSet<>();
        while (matcher.find()) {
            results.add(matcher.group());
        }    
        return new ArrayList<>(results);
    }

    private static String replaceValues(String program,int size, List<Integer> maxInputs) {
        int min = 1;

        List<String> valuesToReplace = findStringsToReplace(program,"ChangeValueHere\\d+_[^\",;\\s]+");
        String finalProgram = program;
        for (int i = 0; i < valuesToReplace.size(); i++) {
            String valueToReplace = valuesToReplace.get(i);
            String[] valueSplitted = valueToReplace.split("_");
            String replaceInput = "\""+valueSplitted[0]+"\"";
            String type = valueSplitted[1];
            String value = getRandomValueOfType(type, maxInputs.get(i)-min >= 0 ? min : 0/*passo o min se of max-min for maior q 0, se nao mando 0, pq o max vai ser 0 e assim nao rebenta*/ , maxInputs.get(i));
            finalProgram = finalProgram.replace("\""+valueToReplace+"\"", value);
            finalProgram = finalProgram.replace(replaceInput, "\""+value+"\"");
        }
        
        return finalProgram;
    }

    
    private static <T> String getRandomValueOfType(String type, int min, int max){
        Random rand = new Random();//new Random(42);
        switch (type.toLowerCase()) {
            case "int":
                return Integer.valueOf(rand.nextInt((max - min) + 1) + min)+"";
            case "double":
                return Double.valueOf(min + (max - min) * rand.nextDouble())+"";
            case "float":
                return Float.valueOf(min + (max - min) * rand.nextFloat())+"";
            case "long":
                return Long.valueOf(rand.nextLong(min, max + 1))+"";
            case "boolean":
                return Boolean.valueOf(rand.nextBoolean())+"";
            case "short":
                return Short.valueOf((short) (rand.nextInt((max - min) + 1) + min))+"";
            case "integer":
                return Integer.valueOf(rand.nextInt((max - min) + 1) + min)+"";
            case "char":
                char minChar = 'a';
                char maxChar = 'z';
                char randomChar = (char) (rand.nextInt((maxChar - minChar) + 1) + minChar);
                return Character.valueOf(randomChar)+"";
            case "character":
                char minChar2 = 'a';
                char maxChar2 = 'z';
                char randomChar2 = (char) (rand.nextInt((maxChar2 - minChar2) + 1) + minChar2);
                return "\""+Character.valueOf(randomChar2)+"\"";
            case "string":
                return "\""+generateRandomString(rand)+"\"";
            default:
                throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }

    private static String generateRandomString(Random rand) {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        int length = rand.nextInt(26); // Random length from 0 to 25
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(alphabet.charAt(rand.nextInt(alphabet.length())));
        }
        return sb.toString();
    }

    public static void createFile(String filename, String input, boolean append) throws IOException {
        BufferedWriter myWriter = new BufferedWriter(new FileWriter(filename,append));
        myWriter.write(input);
        myWriter.close();
    }

    private static String readFile(String file) throws FileNotFoundException {
        File myObj = new File(file);
        Scanner myReader = new Scanner(myObj);
        StringBuilder f = new StringBuilder();
        while (myReader.hasNextLine()) {
            f.append(myReader.nextLine()).append("\n"); // Append newline after each line
        }
        myReader.close();
        return f.toString();
    }

    private static File[] getAllTemplates() {
        return getFiles(outputDir+"/");
    }

    private static File[] getFiles(String dir){
        return new File(dir).listFiles();
    }

    private static String[] getTypes() {
        return new String[] { "Integer", "Double", "Long", "Float", "Short" };//, "Character"
    }

    private static ArrayList<Integer> createInputRange(int initialvalue, double factor, int exponent) {
        Set<Integer> numberSet = new HashSet<>();
        Random random = new Random(42);
        int max_value = initialvalue * 100_000;
        while (initialvalue < max_value) {
            int min = initialvalue;
            int max = initialvalue * 10;
            double nums = Math.pow(factor, exponent);
            for (int j = 0; j < nums; j++) {
                int num = min + random.nextInt(max - min + 1);
                numberSet.add(num);
            }
            initialvalue = initialvalue * 10;
            exponent++;
        }
        return new ArrayList<>(numberSet);
    }

    public static Launcher initSpoon(List<String> paths) {
        Launcher launcher = new Launcher();
        for (int i = 0; i < paths.size(); i++) {
            launcher.addInputResource(paths.get(i));
        }
        
        launcher.getFactory().getEnvironment().setAutoImports(true);
        launcher.getEnvironment().setNoClasspath(false);
        launcher.setSourceOutputDirectory(outputDir);
        launcher.buildModel();
        return launcher;
    }

    private static ArrayList<CtMethod<?>> getCollectionMethods(String collection) {
        HashMap<String, Integer> methods = new HashMap<String, Integer>();
        HashMap<String, CtMethod<?>> methodsParameters = new HashMap<String, CtMethod<?>>();
        CtType<?>[] collectionTypes = getCollections(collection);

        for (CtType<?> collectionType : collectionTypes) {
            for (CtMethod<?> method : collectionType.getMethods()) {
                if (method.isPrivate()) continue; //nao quero metodos private
                if (method.isProtected()) continue; //nao quero metodos private
                StringBuilder methodSignature = new StringBuilder();
                methodSignature.append(method.getSimpleName());
                CtTypeReference<?> returnType = method.getType();
                methodSignature.append("(");

                // Get parameters
                for (int i = 0; i < method.getParameters().size(); i++) {
                    CtParameter<?> param = method.getParameters().get(i);
                    methodSignature.append(param.getType().getSimpleName() + " " + param.getSimpleName());
                    if (i != method.getParameters().size() - 1)
                        methodSignature.append(", ");
                }

                methodSignature.append(") -> " + returnType.getSimpleName());
                //if (collectionType.getSimpleName().equals("Stack"))
                    //System.out.println(methodSignature.toString());
                methods.put(methodSignature.toString(),
                        methods.get(methodSignature.toString()) != null ? methods.get(methodSignature.toString()) + 1
                                : 1);
                methodsParameters.put(methodSignature.toString(), method);
            }
        }
        ArrayList<CtMethod<?>> commonMethods = new ArrayList<>();
        List<String> keys = new ArrayList<>(methods.keySet());
        for (int i = 0; i < keys.size(); i++) {
            if (methods.get(keys.get(i)) == collectionTypes.length) {
                commonMethods.add(methodsParameters.get(keys.get(i)));
            }
        }
        return commonMethods;
    }

    private static CtType<?>[] getCollections(String collection) {
        Launcher launcher = null;
        launcher = new Launcher();
        launcher.addInputResource("templates/");
        launcher.addInputResource("aux/");
        launcher.getFactory().getEnvironment().setAutoImports(true);
        launcher.setSourceOutputDirectory("generated"); // Different output folder
        launcher.buildModel();
        CtType<?>[] collectionTypes = null;
        if (collection.toLowerCase().equals("lists")) {
            collectionTypes = new CtType<?>[4];
            collectionTypes[0] = launcher.getFactory().Type().get(java.util.ArrayList.class);
            collectionTypes[1] = launcher.getFactory().Type().get(java.util.Vector.class);
            // collectionTypes[2] = launcher.getFactory().Type().get(java.util.Stack.class);
            // Stack extends Vector
            collectionTypes[2] = launcher.getFactory().Type().get(java.util.LinkedList.class);
            collectionTypes[3] = launcher.getFactory().Type().get(java.util.concurrent.CopyOnWriteArrayList.class);
        }
        return collectionTypes;
    }

}