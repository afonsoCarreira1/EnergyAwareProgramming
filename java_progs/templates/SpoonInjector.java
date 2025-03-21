package java_progs.templates;


import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtNewArray;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.code.CtTry;
import spoon.reflect.code.CtWhile;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.ImportCleaner;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.JavaOutputProcessor;

import java.beans.Introspector;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class SpoonInjector {
    Launcher launcher;
    Factory factory;
    int numberOfFunCalls;
    CtMethod<?> method;
    CtType<?> collec;
    Boolean isMethodStatic;
    String typeToUse;
    int size;
    String outputDir = "";
    boolean requiresTypesInClass;
    String newClassName;
    CtClass<?> newClass;
    CtMethod<?> mainMethod;
    CtTry tryBlock;
    int varIndex = 0;
    HashSet<String> imports = new HashSet<>();
    int min = 0 , max;
    CtStatementList statements = null;
    CtStatementList statementsLast = null;
    List<CtField<?>> inputs = null;
    final String changeValueHere = "ChangeValueHere";

    public SpoonInjector(Launcher launcher, Factory factory, int numberOfFunCalls, CtMethod<?> method, CtType<?> collec,String typeToUse,int size, String outputDir,boolean requiresTypesInClass) {
        this.launcher = launcher;
        this.factory = factory;
        this.numberOfFunCalls = numberOfFunCalls;
        this.method = method;
        this.collec = collec;
        this.isMethodStatic = method.hasModifier(ModifierKind.STATIC);
        this.typeToUse = "changetypehere";
        this.size = size;
        this.outputDir = outputDir;
        String path = "java_progs.templates.Template";
        this.requiresTypesInClass = requiresTypesInClass;
        CtClass<?> myClass = factory.Class().get(path);
        if (myClass == null) {
            System.out.println(path +" not found");
            return;
        }
        this.newClass = myClass.clone();
        this.newClassName = collec.getSimpleName()+"_"+method.getSignature().replaceAll("\\.|,|\\(|\\)|\\[|\\]", "_");//+id;
        this.mainMethod = newClass.getMethod("main", factory.Type().createArrayReference(factory.Type().stringType()));
        this.tryBlock = (CtTry) mainMethod.getElements(el -> el instanceof CtTry).get(0);
        this.statements = factory.Core().createStatementList();
        this.statementsLast = factory.Core().createStatementList();
        this.inputs = new ArrayList<>();
        initMinMax();
    }

    private void initMinMax() {
        if (size-1+min==0) this.max = 0;
        else this.max = size-1;
    }

    private String getVarName() {
        return "var"+varIndex++;
    }

    public void injectInTemplate() {
        addImport("java_progs.aux.DeepCopyUtil");
        createInitialVar(true);
        createMethodArgs();
        createClassThatHoldsArgs();
        createArrayWithVarAndArgs();
        statements.addStatement(callPopulateMethod());
        callMethods();

        //inject methods
        injectBenchmarkMethod();
        injectComputationMethod();
        injectPopulateArrayMethod();
        injectClearMethod();
        insertInTryBlock();

        injectInputFieldsInClass();


        newClass.setSimpleName(newClassName);
        launcher.getFactory().Class().getAll().add(newClass);
        launcher.getModel().getRootPackage().addType(newClass);
        //launcher.prettyprint();
        //saveClassToFile(newClass,"generated_classes");

        ImportCleaner importCleaner = new ImportCleaner();
        importCleaner.process(newClass);

        saveClassToFile(newClass);
    }

    private void saveClassToFile(CtType<?> ctClass) {
        // Ensure output directory exists
        File outputFolder = new File(outputDir);
        if (!outputFolder.exists()) {
            outputFolder.mkdirs();
        }
        // Use JavaOutputProcessor to generate correct imports
        JavaOutputProcessor processor = new JavaOutputProcessor();
        processor.setFactory(factory);
        processor.createJavaFile(ctClass);
    }

    private CtConstructor<?> getConstructors(CtType<?> t) {
        List<CtConstructor<?>> constructors = t.filterChildren(new TypeFilter<>(CtConstructor.class))
        .map(m -> (CtConstructor<?>) m)
        .list();

        if (constructors.isEmpty()) return null;
        constructors.sort(Comparator.comparingInt(c -> c.getParameters().size()));
        CtConstructor<?> shortestConstructor = constructors.get(0);
        return shortestConstructor;
    }

    private CtType<Object> checkForMoreTypes(CtType<?> type){
        if (type.getQualifiedName().equals("java.util."+type.getSimpleName())) return factory.Type().get(type.getActualClass());
        if (type.getQualifiedName().equals("java.io."+type.getSimpleName())) return factory.Type().get(type.getActualClass());
        //if (type.getQualifiedName().equals("java.util.ArrayList")) return factory.Type().get(java.util.ArrayList.class);
        //if (type.getQualifiedName().equals("java.util.Vector")) return factory.Type().get(java.util.Vector.class);
        //if (type.getQualifiedName().equals("java.util.LinkedList")) return factory.Type().get(java.util.LinkedList.class);
        //if (type.getQualifiedName().equals("java.util.concurrent.CopyOnWriteArrayList")) return factory.Type().get(java.util.concurrent.CopyOnWriteArrayList.class);
        return null;
    }

    private boolean isArray(String type){
        return type.contains("[]");
    }

    private CtExpression<?> getDefaultValueForType(CtType<?> paramType) {
        if (paramType.isArray()) return handleArrayCreationExpression(paramType);
        CtType<?> finalParamType = paramType.getQualifiedName().equals("java.util.Collection") ? (CtType<?>) collec : paramType;
        CtType<?> paramClass = factory.getModel().getAllTypes().stream()
                .filter(type -> type.getQualifiedName().equals(finalParamType.getQualifiedName()))
                .findFirst()
                .orElse(null);

        if (paramClass == null) paramClass = checkForMoreTypes(finalParamType);

        if (paramClass != null) {
            CtConstructor<?> paramConstructor = getConstructors(paramClass);

            if (paramConstructor != null) {
                CtConstructorCall<?> nestedConstructorCall = factory.Code().createConstructorCall(paramClass.getReference());

                paramConstructor.getParameters().forEach(nestedParam -> {
                    CtExpression<?> nestedArg = getDefaultValueForType(nestedParam.getType().getTypeDeclaration());
                    nestedConstructorCall.addArgument(nestedArg);
                });

                return nestedConstructorCall;
            }
        }
        return factory.Code().createLiteral(createRandomLiteral(paramType.getReference(),false,false));
    }

    private void injectInputFieldsInClass() {
        for (CtField<?> inputField : inputs) {
            newClass.addField(inputField);
        }   
    }
    
    private void callMethods() {
        CtStatementList methodCalls = factory.createStatementList();
        String vars = getAllVarsAsString();
        CtBlock<?> tryBlockBody = tryBlock.getBody();
        CtWhile whileBlock = null;
        String methodCall = Introspector.decapitalize(newClassName)+"("+vars+")";
        for (CtStatement st : tryBlockBody){
            if (st instanceof CtWhile) {
                whileBlock = (CtWhile) st;
                break; 
            }
        }
        whileBlock.getBody().insertBefore(factory.Code().createCodeSnippetStatement(methodCall));
        methodCalls.addStatement(factory.Code().createCodeSnippetStatement("clearArr(arr)"));
        methodCalls.addStatement(callPopulateMethod());
        methodCalls.addStatement(factory.Code().createCodeSnippetStatement("TemplatesAux.sendStartSignalToOrchestrator(args[0],iter)"));
        methodCalls.addStatement(factory.Code().createCodeSnippetStatement("TemplatesAux.launchTimerThread()"));
        methodCalls.addStatement(factory.Code().createCodeSnippetStatement("computation(arr, iter)"));
        whileBlock.insertAfter(methodCalls);
        callExceptions();
    }

    private void callExceptions() {
        List<CtCatch> catchers = tryBlock.getCatchers();
        String call1 = "TemplatesAux.writeErrorInFile(\""+newClassName+"\", ";
        call1 += "\"Out of memory error caught by the program.\\n\" + e.getMessage())";
        catchers.get(0).getBody().addStatement(factory.Code().createCodeSnippetStatement(call1));
        String call2 = "TemplatesAux.writeErrorInFile(\""+newClassName+"\", ";
        call2 += "\"Error caught by the program.\\n\" + e.getMessage())";
        catchers.get(1).getBody().addStatement(factory.Code().createCodeSnippetStatement(call2));
    }

    private void createClassThatHoldsArgs() {
        String innerClassName = "BenchmarkArgs";
        ArrayList<CtLocalVariable<?>> vars = getAllVars();
        CtClass<?> innerClass = factory.Class().create(innerClassName);
        CtConstructor constructor = factory.createConstructor();
        constructor.setSimpleName(innerClassName);
        ArrayList<CtParameter<?>> params = new ArrayList<>();
        CtBlock<?> bodyStatements = factory.createBlock();
        for (CtLocalVariable<?> var : vars){
            innerClass.addField(createBenchmarkClassFields(var));
            params.add(factory.createParameter(constructor, var.getType(), var.getSimpleName()));
            String exp = "this."+var.getSimpleName() +" = DeepCopyUtil.deepCopy("+var.getSimpleName()+")";
            bodyStatements.addStatement(factory.Code().createCodeSnippetStatement(exp));
            //if (isPrimitive(var.getType().toString())) bodyStatements.addStatement(factory.Code().createCodeSnippetStatement("this."+var.getSimpleName()+" = "+var.getSimpleName()));
            //else if (var.getType().toString().equals("changetypehere")) bodyStatements.addStatement(factory.Code().createCodeSnippetStatement("this."+var.getSimpleName()+" = "+var.getSimpleName()));
            //else bodyStatements.addStatement(factory.Code().createCodeSnippetStatement("this."+var.getSimpleName()+" = ("+collec.getSimpleName()+") "+var.getSimpleName()+".clone()"));
        }
        constructor.setParameters(params);
        constructor.setBody(bodyStatements);
        innerClass.addConstructor(constructor);
        innerClass.addModifier(ModifierKind.STATIC);
        newClass.addNestedType(innerClass);
    }

    private boolean isPrimitive(String type) {
        if (type.equals("int") || type.equals("boolean") || type.equals("char") || type.equals("byte") || type.equals("short") || type.equals("float") || type.equals("double") || type.equals("long")|| type.equals("void")) return true;
        if (type.equals("Integer") || type.equals("Boolean") || type.equals("Character") || type.equals("Byte") || type.equals("Short") || type.equals("Float") || type.equals("Double") || type.equals("Long")) return true;
        return false;
    }

    private CtField<?> createBenchmarkClassFields(CtLocalVariable var) {
        CtField<?> field = factory.createCtField(var.getSimpleName(), var.getType(),var.getDefaultExpression().toString());
        field.addModifier(ModifierKind.PUBLIC);
        return field;
    }
    
    private CtStatement callPopulateMethod(){
        String args = getAllVarsAsString();
        String arr = "arr";
        if (args.length() != 0) arr+=", ";
        String statement ="populateArray("+arr+args+")";
        return factory.Code().createCodeSnippetStatement(statement);
    }

    private void createArrayWithVarAndArgs() {
        String statement = "BenchmarkArgs[] arr = new BenchmarkArgs["+"\"numberOfFunCalls\""/*numberOfFunCalls*/+"]";
        statements.addStatement(factory.createCodeSnippetStatement(statement));
    }

    private void createMethodArgs() {
        List<CtParameter<?>> args = method.getParameters();
        for (CtParameter<?> arg : args) {
            CtTypeReference<?> t = arg.getType();
            //if (isArray(t.toString())){
            //    if (isPlaceHolderType(t.getSimpleName().split("\\[")[0])){
            //        t = factory.Type().createReference(t.getSimpleName());
            //        System.out.println("t .> "+t);
            //    }
            //}
            if (isPlaceHolderType(t.toString())) t = factory.Type().createReference(t.getSimpleName());
            CtLocalVariable<?> var = createVar(t, getVarName(),false);
            statements.addStatement(var);
            callAndClearIntermidiateStatements();
            if(isCollection(var)) statements.addStatement(populateCollection(var,false));
        }
    }

    private void callAndClearIntermidiateStatements() {
        for (int i = 0; i < statementsLast.getStatements().size(); i++) {
            statements.addStatement(statementsLast.getStatements().get(i));
        }
        statementsLast.getStatements().clear();
    }

    private void insertInTryBlock() {
        tryBlock.getBody().insertBegin(statements);
    }
    
    private void createInitialVar(boolean useConstructorSize) {
        String initialArray = "";
        if (isMethodStatic) initialArray += collec.getQualifiedName()+ "()"+"."+method.getSimpleName();//TODO i assume there are no constructors here
        else {
            String varName = getVarName();
            CtLocalVariable<?> var = createVar(factory.Type().createReference(collec), varName,false);
            statements.addStatement(var);
            CtStatement initCollection = populateCollection(var,useConstructorSize);
            if (initCollection != null) statements.addStatement(initCollection);
            initialArray += varName+"."+method.getSimpleName();
            
        }
    }

    private CtStatement populateCollection(CtLocalVariable<?> var, boolean useConstructorSize) {
        if (!isCollection(var)) return null;
        if (var.getType().toString().contains("List")) {
            addImport("java_progs.aux.ArrayListAux");
            CtClass<?> ctClass = factory.Class().get("java_progs.aux.ArrayListAux");
            CtMethod<?> insertRandomNumbersMethod = ctClass.getMethodsByName("insertRandomNumbers").get(0);
            CtInvocation<?> invocation = factory.Core().createInvocation();
            invocation.setExecutable((CtExecutableReference) insertRandomNumbersMethod.getReference()); //adciciona a fun
            invocation.setTarget(factory.createLiteral(ctClass.getReference())); //adiciona a Class.
            invocation.addArgument(factory.Code().createVariableRead(var.getReference(), false));
            if (useConstructorSize) invocation.addArgument(factory.Code().createLiteral(createRandomLiteral(null,false,true)));
            else invocation.addArgument(factory.Code().createLiteral(createRandomLiteral(factory.createReference(typeToUse),false,false)));
            invocation.addArgument(factory.Code().createLiteral(typeToUse));
            return invocation;
        }//TODo do the same for sets and other collections
        return null;
    }
    
    private void addImport(String importPath) {
        imports.add("import "+importPath+";");
    }

    private boolean isCollection(CtLocalVariable<?> var) {
        //System.out.println(var);
        try {
            return var.getType().isSubtypeOf(factory.Type().createReference("java.util.Collection"));
        } catch (Exception e) {
            return false;
        }
        
    }

    private String callArgs() {
        String argsString = "";
        List<CtParameter<?>> args = method.getParameters();
        argsString += "(";
        for (int i = 0; i < args.size(); i++) {
            argsString += args.get(i).getSimpleName();
            if(i != args.size()-1) argsString += ", ";
        }
        argsString += ")";
        return argsString;
    }

    private String getBenchmarkFunBody() {
        String body = "";
        if (isMethodStatic) body += collec.getQualifiedName()+"."+method.getSimpleName();//TODO i assume there are no constructors here
        else body += "var."+method.getSimpleName();
        body += callArgs();
        return body;
    }

    private boolean isGeneric(CtTypeReference<?> type) {
        if (isArray(type.toString())) {
            if(isPlaceHolderType(type.toString().split("\\[")[0])) return false;
        }
        if (isPlaceHolderType(type.toString()) || type.toString().equals(typeToUse)) return false;
        CtClass<?> ctClass = (CtClass<?>) type.getTypeDeclaration();
        return !ctClass.getFormalCtTypeParameters().isEmpty();
    }

    private CtLocalVariable<?> createVar(CtTypeReference typeRef, String varName, boolean getDefaultValue) {
        CtTypeReference ref = typeRef.toString().contains("Collection") ? factory.Type().createReference(collec) : typeRef;
        CtTypeReference<?> genericType = factory.Type().createReference(typeToUse);
        if (typeRef.isArray()) ref = typeRef.getTypeErasure();
        //if (requiresTypesInClass) ref.addActualTypeArgument(genericType);
        if (isGeneric(ref)) ref.addActualTypeArgument(genericType);
        //if (isArray(ref.getSimpleName()) && isPlaceHolderType(ref.getSimpleName().split("\\[")[0])) ref = changeTAndEArray(ref.getSimpleName());
        CtExpression<?> exp = createVar(ref,getDefaultValue);
        if (isPlaceHolderType(ref.getSimpleName())) ref =factory.createReference("changetypehere");
        CtLocalVariable<?> variable = factory.Code().createLocalVariable(
            ref,           // var type
            varName,          // Variable name
            exp // Initialization
        );
        return variable;
    }

    private CtTypeReference<?> changeTAndEArray(String type) {
        int numberOfArrays = countOccurrences(type, "[]");
        StringBuilder newType = new StringBuilder(typeToUse);
        for (int i = 0; i < numberOfArrays; i++) {
            newType.append("[]");
        }
        return factory.Type().createReference(newType.toString());
    }

    private CtExpression<?> createVar(CtTypeReference<?> typeRef, boolean getDefaultValue) {        
        if (typeRef.isPrimitive() || isPlaceHolderType(typeRef.toString())) return factory.Code().createLiteral(createRandomLiteral(typeRef,getDefaultValue,false));
        if (typeRef.toString().contains("Collection")) return factory.Code().createConstructorCall(typeRef);
        //if (isArray(typeRef.toString())) return handleArrayCreationExpression(typeRef.toString());
        //System.out.println("vai entrar um "+typeRef.getTypeDeclaration());
        return getDefaultValueForType(typeRef.getTypeDeclaration());
        //return factory.Code().createConstructorCall(typeRef);
    }

    private CtExpression<?> handleArrayCreationExpression(CtType<?> type) {
        CtTypeReference<?> t = type.getTypeErasure();
        int arrayDepth = countOccurrences(t.toString(), "[]");
        StringBuilder exp = new StringBuilder("new "+t.toString().split("\\[")[0]);
        for (int i = 0; i < arrayDepth; i++) {
            exp.append("[\""+createRandomLiteral(t.getTypeErasure(), false, false)+"\"]");
        }
        populateArray(t);
        return factory.Code().createCodeSnippetExpression(exp.toString());
    }

    private void populateArray(CtTypeReference<?> typeArr) {
        CtTypeReference<?> type = factory.Type().createReference(typeArr.toString().split("\\[")[0]);
        //TODO esta linha de baixo
        if (!type.toString().equals("Object")) getConstructors(type.getTypeDeclaration());// se nao for do tipo object, vou buscar a sua expressao
        //se for object mudo para o tipo changetypehere que vai ser depois o (Integer,Boolean,etc...)
        String populateArrayCall = "ArrayListAux.populateArrayPrimitive("+getLastVarName()+", () ->"+"ArrayListAux.getRandomValueOfType(\""+typeToUse+"\")"+")"; 
        CtStatement statement = factory.Code().createCodeSnippetStatement(populateArrayCall);
        statementsLast.addStatement(statement);
    }

    private String getLastVarName() {
        return "var"+(varIndex-1);
    }

    private static int countOccurrences(String str, String sub) {
        int count = 0;
        int index = 0;

        while ((index = str.indexOf(sub, index)) != -1) {
            count++;
            index += sub.length();
        }

        return count;
    }

    private Object createRandomLiteral(CtTypeReference<?> typeRef, boolean getDefaultValue, boolean useConstructorSize) {
        Object value = null;
        if (getDefaultValue) value = getDefaultValues(typeRef.toString());
        else if (useConstructorSize) value = "ChangeValueHere"+(varIndex-1)+"_useConstructorSize";//value = "useConstructorSize";//size;
        else value = getPlaceHolderValue(typeRef.toString());
        //System.out.println("aki -> "+typeRef);
        //if (isArray(typeRef.toString())) value = "new " + typeRef + "[ChangeValueHere]";
        saveInput(value);
        return value;
    }

    private void saveInput(Object value) {
        String expression = (value instanceof String) ? "\""+((String)value).length()+"\"" : "\""+value+"\"";
        if (typeToUse.equals("changetypehere")) expression = (String) "\"ChangeValueHere"+(varIndex-1)+"\"";
        CtField<?> inputField = factory.createCtField("input"+varIndex, factory.Type().stringType(),expression);
        inputField.addModifier(ModifierKind.PRIVATE);
        inputs.add(inputField);
    }

    @SuppressWarnings("unchecked")
    private <T> T getPlaceHolderValue(String type){
        //if (type.equals(typeToUse)) return (T) typeToUse;
        if (isArray(type)) type = "int";
        //if(isArray(type)) {
        //    //CtNewArray<Object[]> objectArray = factory.Code().createLiteralArray(factory.Code().createCodeSnippetStatement("type"));
        //    //objectArray.setType(factory.Type().createReference(type));
        //    //objectArray.setDimensionExpressions((List<CtExpression<Integer>>) factory.Code().createLiteral("ChangeValueHere"));
        //    String typeWithoutArray = type.split("\\[")[0];
        //    if (typeWithoutArray.equals("Object")) typeWithoutArray = typeToUse;
        //    if (isPlaceHolderType(type)) return (T) factory.Code().createCodeSnippetStatement("new "+typeWithoutArray+"[\"ChangeValueHere"+(varIndex-1)+"_"+typeWithoutArray+"\"]");
        //    if (typeToUse.equals("changetypehere")) return (T) factory.Code().createCodeSnippetStatement("new "+typeWithoutArray+"[\"ChangeValueHere"+(varIndex-1)+"_"+typeWithoutArray+"\"]");
        //}
        if (isPlaceHolderType(type)) return (T) ("ChangeValueHere"+(varIndex-1)+"_"+"changetypehere");
        if (typeToUse.equals("changetypehere")) return (T) ("ChangeValueHere"+(varIndex-1)+"_"+type);
        throw new IllegalArgumentException("Unsupported type: " + type);
    }



    private boolean isPlaceHolderType(String ref) {
        if (ref.equals("E") || ref.equals("T") || ref.equals("Object")) return true;
        return false;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getDefaultValues(String type){
        switch (type.toLowerCase()) {
            case "int":
                return (T) Integer.valueOf(0);
            case "double":
                return (T) Double.valueOf(0.0);
            case "float":
                return (T) Float.valueOf(0f);
            case "long":
                return (T) Long.valueOf(0);
            case "boolean":
                return (T) Boolean.valueOf(false);
            case "short":
                return (T) Short.valueOf("0");
            case "integer":
                return (T) Integer.valueOf(0);
            case "character":
                return (T) Character.valueOf('a');
            default:
                throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }


    private List<CtParameter<?>> getComputationParameters() {
        if (isMethodStatic) return method.getParameters();
        CtParameter<?> param = factory.createParameter();
        param.setSimpleName("var");
        param.setType(collec.getReference());
        List<CtParameter<?>> params = new ArrayList<>();
        params.add(param);
        for (int i = 0; i < method.getParameters().size(); i++) {
            CtTypeReference<?> t = method.getParameters().get(i).getType();
            if (t.isArray()) method.getParameters().get(i).setType(factory.Type().createReference(t.getTypeErasure().toString()));
            if(isPlaceHolderType(t.getSimpleName())) method.getParameters().get(i).setType(factory.createReference("changetypehere"));
            for (CtTypeReference<?> tr : t.getActualTypeArguments()){
                if (tr.toString().contains("? extends E")){// change the type from <? extends E> to <?>
                    method.getParameters().get(i).getType().setActualTypeArguments(List.of(factory.Type().createReference("?")));
                }
            }
            params.add(method.getParameters().get(i));
        }
        return params;
    }

    private void injectBenchmarkMethod() {
        // Define the return type (void in this case)
        CtTypeReference<Void> returnType = factory.Type().voidPrimitiveType();

        Set<ModifierKind> modifiers = new HashSet<>();
        modifiers.add(ModifierKind.PRIVATE);
        modifiers.add(ModifierKind.STATIC);

        CtBlock<Void> methodBody = factory.Core().createBlock();

        CtCodeSnippetStatement snippet = factory.Code().createCodeSnippetStatement(
            getBenchmarkFunBody()
        );
        methodBody.addStatement(snippet);

        // Create the method
        CtMethod<Void> newMethod = factory.Method().create(
                newClass,            // Target class
                modifiers,          // Modifiers
                returnType,         // Return type
                Introspector.decapitalize(newClassName),         // Method name
                getComputationParameters(),  // Parameters (empty)
                Collections.emptySet(),   // Exceptions thrown
                methodBody          // Method body
        );

        // Add method to class
        newClass.addMethod(newMethod);
    }
    
    private void getCollectionMethods(Launcher launcher, String collection) {
        launcher.getFactory().getEnvironment().setAutoImports(true);
        
        CtType<?> collectionType = null;
        if (collection.toLowerCase().equals("arraylist")) collectionType = launcher.getFactory().Type().get(java.util.ArrayList.class);
        if (collection.toLowerCase().equals("vector")) collectionType = launcher.getFactory().Type().get(java.util.Vector.class);
        
        
        // Iterate through all methods
        for (CtMethod<?> method : collectionType.getMethods()) {
            String methodName = method.getSimpleName();
            CtTypeReference<?> returnType = method.getType();
            
            
            // Get parameters
            for (CtParameter<?> param : method.getParameters()) {
                System.out.print(param.getType().getSimpleName() + " " + param.getSimpleName() + ", ");
            }
            
        }
    }

    private String getAllVarsAsString() {
        StringBuilder args = new StringBuilder();
        for (int i = 0; i < varIndex; i++) {
            args.append("var"+i);
            if (i != varIndex-1) args.append(", ");
        }
        return args.toString();
    }

    private ArrayList<CtLocalVariable<?>> getAllVars(){
        ArrayList<CtLocalVariable<?>> vars = new ArrayList<>();
        for (int i = 0; i < statements.getStatements().size(); i++) {
            if (statements.getStatements().get(i) instanceof CtLocalVariable) {
                CtLocalVariable<?> var = (CtLocalVariable<?>) statements.getStatements().get(i);
                vars.add(var);
            }
        }
        return vars;
    }

    private void injectPopulateArrayMethod() {
        ArrayList<CtLocalVariable<?>> vars = getAllVars();
        CtTypeReference<Void> returnType = factory.Type().voidPrimitiveType();

        Set<ModifierKind> modifiers = new HashSet<>();
        modifiers.add(ModifierKind.PRIVATE);
        modifiers.add(ModifierKind.STATIC);

        CtBlock<Void> methodBody = factory.Core().createBlock();

        String args = getAllVarsAsString();

        String body ="for (int i = 0;i < "+vars.size()+";i++) {\n" + //
                     "  arr[i] = new BenchmarkArgs("+args+");\n" + //
                     "}";

        CtCodeSnippetStatement snippet = factory.Code().createCodeSnippetStatement(body);
        methodBody.addStatement(snippet);

        List<CtParameter<?>> params = new ArrayList<>();
        params.add(createParameter("BenchmarkArgs","arr",true));
        
        
        for (CtLocalVariable<?> var : vars) {
            params.add(createParameter(var.getType().toString(),var.getSimpleName(),false/*var.getType().isArray()*/));
        }
        
        CtMethod<Void> newMethod = factory.Method().create(
                newClass,            // Target class
                modifiers,          // Modifiers
                returnType,         // Return type
                "populateArray",         // Method name
                params,  // Parameters 
                Collections.emptySet(),   // Exceptions thrown
                methodBody          // Method body
        );
        newClass.addMethod(newMethod);
    }

    private void injectComputationMethod() {
        CtTypeReference<Void> returnType = factory.Type().voidPrimitiveType();

        Set<ModifierKind> modifiers = new HashSet<>();
        modifiers.add(ModifierKind.PRIVATE);
        modifiers.add(ModifierKind.STATIC);
        
        CtBlock<Void> methodBody = factory.Core().createBlock();

        String args = String.join(", ", Arrays.stream(getAllVarsAsString().split(", ")).map(s -> "args[i]."+s).toArray(String[]::new));

        String body ="int i = 0;\n" + //
        "while (!TemplatesAux.stop && i < iter) {\n      " + //
            Introspector.decapitalize(newClassName)+"("+ args +");\n" + //
        "       i++;\n" + //
        "}";

        CtCodeSnippetStatement snippet = factory.Code().createCodeSnippetStatement(body);
        methodBody.addStatement(snippet);

        List<CtParameter<?>> params = new ArrayList<>();
        params.add(createParameter("BenchmarkArgs","args",true));
        params.add(createParameter("int","iter",false));
        
        CtMethod<Void> newMethod = factory.Method().create(
                newClass,            // Target class
                modifiers,          // Modifiers
                returnType,         // Return type
                "computation",         // Method name
                params,  // Parameters 
                Collections.emptySet(),   // Exceptions thrown
                methodBody          // Method body
        );
        newClass.addMethod(newMethod);
    }

    private void injectClearMethod(){
        CtTypeReference<Void> returnType = factory.Type().voidPrimitiveType();

        Set<ModifierKind> modifiers = new HashSet<>();
        modifiers.add(ModifierKind.PRIVATE);
        modifiers.add(ModifierKind.STATIC);
        
        CtBlock<Void> methodBody = factory.Core().createBlock();
        String body ="for (int i = 0; i < arr.length; i++) {\n" + //
                     "  arr[i] = null;\n" + //
                     "}\n" + //
                     "System.gc()";

        CtCodeSnippetStatement snippet = factory.Code().createCodeSnippetStatement(body);
        methodBody.addStatement(snippet);

        List<CtParameter<?>> params = new ArrayList<>();
        params.add(createParameter("BenchmarkArgs","arr",true));
        
        CtMethod<Void> newMethod = factory.Method().create(
                newClass,            // Target class
                modifiers,          // Modifiers
                returnType,         // Return type
                "clearArr",         // Method name
                params,  // Parameters 
                Collections.emptySet(),   // Exceptions thrown
                methodBody          // Method body
        );
        newClass.addMethod(newMethod);
    }

    public CtParameter<?> createParameter(String paramType, String paramName, boolean isTypeArray) {
        CtTypeReference<?> type = factory.Type().createReference(paramType);
        CtArrayTypeReference<?> typeArr = factory.Type().createArrayReference(type);
        CtParameter<?> param = factory.Core().createParameter();
        param.setSimpleName(paramName);
        if (isTypeArray) param.setType(typeArr);
        else param.setType(type);
        return param;
    }

    public void insertImport() {
        String filename = outputDir+"/"+newClassName+".java";
        try {
            // Read the original file content while preserving format
            String originalContent = new String(Files.readAllBytes(Paths.get(filename)));

            // Build the new content
            StringBuilder newContent = new StringBuilder();

            // Add the new lines
            newContent.append("package "+"generated_progs"/*outputDir*/+";\n");
            for (String line : imports) {
                newContent.append(line).append(System.lineSeparator());
            }
            // Append the original file content
            newContent.append(originalContent);

            // Overwrite the file with the new content
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
                    writer.write(newContent.toString());
                }
        } catch (IOException e) {
            System.err.println("Error modifying the file: " + e.getMessage());
        }
    }

}