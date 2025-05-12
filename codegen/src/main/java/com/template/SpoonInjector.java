package com.template;


import spoon.Launcher;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.code.CtTry;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.ImportCleaner;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.JavaOutputProcessor;

import java.beans.Introspector;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SpoonInjector {
    private static final Set<String> wrapperDefaults = Set.of(
    "java.lang.Long",
    "java.lang.Integer",
    "java.lang.Double",
    "java.lang.Float",
    "java.lang.Boolean",
    "java.lang.Character",
    "java.lang.Byte",
    "java.lang.Short");
    final static String packageToUse = "com.template.";
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
    int valueIndex = 0;
    HashSet<String> imports = new HashSet<>();
    int min = 0 , max;
    CtStatementList statements = null;
    CtStatementList statementsLast = null;
    List<CtField<?>> inputs = null;
    final String changeValueHere = "ChangeValueHere";
    CtClass<?> myClass = null;
    boolean getCustomImports;

    public SpoonInjector(Launcher launcher, Factory factory, int numberOfFunCalls, CtMethod<?> method, CtType<?> collec,String typeToUse,int size, String outputDir,boolean requiresTypesInClass,boolean getCustomImports) {
        this.launcher = launcher;
        this.factory = factory;
        this.numberOfFunCalls = numberOfFunCalls;
        this.method = method;
        this.collec = collec;
        this.isMethodStatic = method.hasModifier(ModifierKind.STATIC);
        this.typeToUse = "changetypehere";
        this.size = size;
        this.outputDir = outputDir;
        String path = packageToUse+"Template";
        this.requiresTypesInClass = requiresTypesInClass;
        if (getCustomImports) {
            addImport(packageToUse+"programsToBenchmark.*");
        }
        myClass = factory.Class().get(path);
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
        String var = "var"+varIndex;
        varIndex++;
        //valueIndex++;
        return var;
        //return "var"+varIndex++;
    }

    public void injectInTemplate() {
        addImport(packageToUse+"aux.DeepCopyUtil");
        addImport("com.fasterxml.jackson.core.type.TypeReference");
        createInitialVar(false);
        createMethodArgs();
        createClassThatHoldsArgs();
        createArrayWithVarAndArgs();
        statements.addStatement(callPopulateMethod());
        callMethods();

        //inject methods
        injectBenchmarkMethod();
        injectComputationMethod();
        injectPopulateArrayMethod();
        //injectClearMethod();
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

    private boolean isWrapper(String className) {
        return wrapperDefaults.contains(className);
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
        try {
            return factory.Type().get(type.getActualClass());
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isArray(String type){
        return type.contains("[]");
    }

    private CtExpression<?> getDefaultValueForType(CtType<?> paramType, boolean isArray) {
        if (isArray) return handleArrayCreationExpression(paramType);
        CtType<?> finalParamType = paramType.getQualifiedName().equals("java.util.Collection") ? (CtType<?>) collec : paramType;
        CtType<?> paramClass = factory.getModel().getAllTypes().stream()
                .filter(type -> type.getQualifiedName().equals(finalParamType.getQualifiedName()))
                .findFirst()
                .orElse(null);

        if (paramClass == null) paramClass = checkForMoreTypes(finalParamType);

        if (paramClass != null) {
            if (isWrapper(paramClass.getTypeErasure().getQualifiedName())) 
            return factory.Code().createLiteral(createRandomLiteral(paramClass.getTypeErasure(),false,false));
            //return factory.Code().createCodeSnippetExpression(getWrapperConstructor(paramClass.getTypeErasure().getQualifiedName())) ;
            CtConstructor<?> paramConstructor = getConstructors(paramClass);
            if (paramConstructor != null) {
                CtConstructorCall<?> nestedConstructorCall = factory.Code().createConstructorCall(paramClass.getReference());
                paramConstructor.getParameters().forEach(nestedParam -> {
                    CtType<?> newTypeToCheck = nestedParam.getType().getTypeDeclaration();
                    CtExpression<?> nestedArg = getDefaultValueForType(newTypeToCheck,newTypeToCheck.isArray());
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
        StringBuilder args = new StringBuilder();
        for (int i = 0; i < varIndex; i++) {
            args.append("arr[iter].var"+i);
            if (i != varIndex-1) args.append(", ");
        }
        statements.addStatement(factory.Code().createCodeSnippetStatement("TemplatesAux.sendStartSignalToOrchestrator(args[0])"));
        statements.addStatement(factory.Code().createCodeSnippetStatement("TemplatesAux.launchTimerThread(1100)"));
        statements.addStatement(factory.Code().createCodeSnippetStatement("iter = computation(arr, arr.length)"));
        callExceptions();
    }

    private void callExceptions() {
        List<CtCatch> catchers = tryBlock.getCatchers();
        String call1 = "TemplatesAux.writeErrorInFile(\""+newClassName+"\", ";
        call1 += "\"Out of memory error caught by the program:\\n\" + e.getMessage())";
        catchers.get(0).getBody().addStatement(factory.Code().createCodeSnippetStatement(call1));
        String call2 = "TemplatesAux.writeErrorInFile(\""+newClassName+"\", ";
        call2 += "\"Error caught by the program:\\n\" + e.getMessage())";
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
            String exp = "this."+var.getSimpleName() +" = DeepCopyUtil.deepCopy("+var.getSimpleName()+", new TypeReference<"+changePrimitiveTypeToWrapperType(var.getType())+">(){})";
            bodyStatements.addStatement(factory.Code().createCodeSnippetStatement(exp));
        }
        constructor.setParameters(params);
        constructor.setBody(bodyStatements);
        innerClass.addConstructor(constructor);
        innerClass.addModifier(ModifierKind.STATIC);
        newClass.addNestedType(innerClass);
    }

    private String changePrimitiveTypeToWrapperType(CtTypeReference<?> type) {
        Map<String, String> primitive_to_wrapper = Map.of(
            "int", "Integer",
            "short", "Short",
            "long", "Long",
            "byte", "Byte",
            "float", "Float",
            "double", "Double",
            "boolean", "Boolean",
            "char", "Character");
            return primitive_to_wrapper.getOrDefault(type.toString(), type.toString());
    }


    private CtField<?> createBenchmarkClassFields(CtLocalVariable<?> var) {
        CtField<?> field = factory.Core().createField();
        field.setSimpleName(var.getSimpleName());
        field.setType(var.getType());
        field.addModifier(ModifierKind.PUBLIC);
        // Don't set any defaultExpression
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
        String p = packageToUse+"aux.ArrayListAux";
        if (containsCollectionToPopulate(var.getType().toString())) {
            addImport(p);
            Object size = createRandomLiteral(factory.createReference(typeToUse),false,false);
            CtStatement st = factory.Code().createCodeSnippetStatement("ArrayListAux.insertRandomNumbers("+var.getSimpleName()+", \""+size+"\", \""+typeToUse +"\")");
            return st;
        }
        return null;
    }

    private boolean containsCollectionToPopulate(String type) {
        if (type.contains("List")) return true;
        if (type.contains("Vector")) return true;
        if (type.contains("Set")) return true;
        if (type.contains("TreeSet")) return true;
        if (type.contains("Map")) return true;
        if (type.contains("Hashtable")) return true;
        return false;
    }
    
    private void addImport(String importPath) {
        imports.add("import "+importPath+";");
    }

    private boolean isCollection(CtLocalVariable<?> var) {
        try {
            return var.getType().isSubtypeOf(factory.Type().createReference("java.util.Collection")) || var.getType().isSubtypeOf(factory.Type().createReference("java.util.Map"));
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

    private int isGeneric(CtTypeReference<?> type) {
        if (isArray(type.toString())) {
            if(isPlaceHolderType(type.toString().split("\\[")[0])) return 0;//return false;
            type = factory.Type().createReference(type.toString().split("\\[")[0]);
        }
        if (isPlaceHolderType(type.toString()) || type.toString().equals(typeToUse)) return 0;//return false;
        CtClass<?> ctClass = (CtClass<?>) type.getTypeDeclaration();
        if (ctClass == null) {
            for (CtType<?> t : launcher.getFactory().Class().getAll()) {
                if (t.getSimpleName().equals(type.toString())) {
                    CtClass<?> ctClass2 = (CtClass<?>) t.getReference().getTypeDeclaration();
                    //return !ctClass2.getFormalCtTypeParameters().isEmpty();
                    return ctClass2.getFormalCtTypeParameters().size();
                } 
            }
            return 0;
            //return false;
        }
        //return !ctClass.getFormalCtTypeParameters().isEmpty();
        return ctClass.getFormalCtTypeParameters().size();
    }

    private CtLocalVariable<?> createVar(CtTypeReference typeRef, String varName, boolean getDefaultValue) {
        CtTypeReference ref = typeRef.toString().contains("Collection") ? factory.Type().createReference(collec) : typeRef;
        if (typeRef.isArray()) ref = typeRef.getTypeErasure();
        //if (isGeneric(ref)) ref.addActualTypeArgument(genericType);
        handleGenericClasses(ref,null);
        CtExpression<?> exp = createVar(ref,getDefaultValue);
        if (isPlaceHolderType(ref.getSimpleName())) ref =factory.createReference("changetypehere");
        CtLocalVariable<?> variable = factory.Code().createLocalVariable(
            ref,           // var type
            varName,          // Variable name
            exp // Initialization
        );
        return variable;
    }

    private void handleGenericClasses(CtTypeReference<?> ref, CtTypeReference<?> genericType) {
        int generic = isGeneric(ref);
        if (generic == 0) return;
        ref.addActualTypeArgument(factory.Type().createReference(typeToUse));
        if (generic == 2) ref.addActualTypeArgument(factory.Type().createReference(typeToUse));
    }

    private CtExpression<?> createVar(CtTypeReference<?> typeRef, boolean getDefaultValue) {       
        if (typeRef.isPrimitive() || isPlaceHolderType(typeRef.toString())) return factory.Code().createLiteral(createRandomLiteral(typeRef,getDefaultValue,false));
        if (typeRef.toString().contains("Collection")) return factory.Code().createConstructorCall(typeRef);
        if (typeRef.isArray()) return getDefaultValueForType((((CtArrayTypeReference<?>) typeRef).getComponentType().getTypeDeclaration()),true);
        return getDefaultValueForType(typeRef.getTypeDeclaration(),false);
    }

    private CtExpression<?> handleArrayCreationExpression(CtType<?> type) {
        //TODO handle multiple dimension arrays
        CtTypeReference<?> t = type.getTypeErasure();
        String exp = "new "+t+"[\""+createRandomLiteral(factory.Type().integerType(), false, false)+"\"]";
        populateArray(t);
        return factory.Code().createCodeSnippetExpression(exp.toString());
    }

    private void populateArray(CtTypeReference<?> typeArr) {
        addImport(packageToUse+"aux.ArrayListAux");
        CtTypeReference<?> type = factory.Type().createReference(typeArr.toString().split("\\[")[0]);
        String populateArrayCall = "ArrayListAux.populateArrayPrimitive("+getLastVarName()+", () ->"; 
        if (!type.toString().equals("Object")) populateArrayCall+=getDefaultValueForType(typeArr.getTypeDeclaration(),false)+")";// se nao for do tipo object, vou buscar a sua expressao
        else populateArrayCall += "ArrayListAux.getRandomValueOfType(\""+typeToUse+"\"))";//se for object mudo para o tipo changetypehere que vai ser depois o (Integer,Boolean,etc...)
        CtStatement statement = factory.Code().createCodeSnippetStatement(populateArrayCall);
        statementsLast.addStatement(statement);
    }

    private String getLastVarName() {
        return "var"+(varIndex-1);
    }


    private Object createRandomLiteral(CtTypeReference<?> typeRef, boolean getDefaultValue, boolean useConstructorSize) {
        Object value = null;
        //if (getDefaultValue) value = getDefaultValues(typeRef.toString());
        /*else*/ if (useConstructorSize) value = "ChangeValueHere"+valueIndex+"_useConstructorSize";//"ChangeValueHere"+(varIndex-1)+"_useConstructorSize";
        else value = getPlaceHolderValue(typeRef.toString());
        saveInput(value);
        return value;
    }

    private void saveInput(Object value) {
        String expression = (value instanceof String) ? "\""+((String)value).length()+"\"" : "\""+value+"\"";
        if (typeToUse.equals("changetypehere")) expression = (String) "\"ChangeValueHere"+valueIndex+"\"";//(String) "\"ChangeValueHere"+(varIndex-1)+"\"";
        CtField<?> inputField = factory.createCtField("input"+valueIndex, factory.Type().stringType(),expression);//factory.createCtField("input"+varIndex, factory.Type().stringType(),expression);
        inputField.addModifier(ModifierKind.PRIVATE);
        inputs.add(inputField);
    }

    @SuppressWarnings("unchecked")
    private <T> T getPlaceHolderValue(String type){
        valueIndex++;
        if (isArray(type)) type = "int";
        if (isPlaceHolderType(type)) return (T) ("ChangeValueHere"+valueIndex+"_"+"changetypehere");//("ChangeValueHere"+(varIndex-1)+"_"+"changetypehere");
        if (typeToUse.equals("changetypehere")) return (T) ("ChangeValueHere"+valueIndex+"_"+type);//("ChangeValueHere"+(varIndex-1)+"_"+type);
        throw new IllegalArgumentException("Unsupported type: " + type);
    }


    private boolean isPlaceHolderType(String ref) {
        if (ref.equals("E") || ref.equals("T") || ref.equals("Object")) return true;
        return false;
    }

    //@SuppressWarnings("unchecked")
    //public <T> T getDefaultValues(String type){
    //    switch (type.toLowerCase()) {
    //        case "int":
    //            return (T) Integer.valueOf(0);
    //        case "double":
    //            return (T) Double.valueOf(0.0);
    //        case "float":
    //            return (T) Float.valueOf(0f);
    //        case "long":
    //            return (T) Long.valueOf(0);
    //        case "boolean":
    //            return (T) Boolean.valueOf(false);
    //        case "short":
    //            return (T) Short.valueOf("0");
    //        case "integer":
    //            return (T) Integer.valueOf(0);
    //        case "character":
    //            return (T) Character.valueOf('a');
    //        default:
    //            throw new IllegalArgumentException("Unsupported type: " + type);
    //    }
    //}


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
                    List<CtTypeReference<?>> l = new ArrayList<>();
                    l.add(factory.Type().createReference("?"));
                    method.getParameters().get(i).getType().setActualTypeArguments(l);
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

        String body ="for (int i = 0;i < \"numberOfFunCalls\";i++) {\n" + //
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
        CtTypeReference returnType = factory.Type().integerPrimitiveType();

        Set<ModifierKind> modifiers = new HashSet<>();
        modifiers.add(ModifierKind.PRIVATE);
        modifiers.add(ModifierKind.STATIC);
        
        CtBlock<Void> methodBody = factory.Core().createBlock();

        String args = String.join(", ", Arrays.stream(getAllVarsAsString().split(", ")).map(s -> "args[i]."+s).toArray(String[]::new));

        String body ="int i = 0;\n" + //
        "while (!TemplatesAux.stop && i < iter) {\n      " + //
            Introspector.decapitalize(newClassName)+"("+ args +");\n" + //
        "       i++;\n" + //
        "}\nreturn iter";

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

    public CtParameter<?> createParameter(String paramType, String paramName, boolean isTypeArray) {
        CtTypeReference<?> type = handleTypeCreation(paramType);
        CtArrayTypeReference<?> typeArr = factory.Type().createArrayReference(type);
        CtParameter<?> param = factory.Core().createParameter();
        param.setSimpleName(paramName);
        if (isTypeArray) param.setType(typeArr);
        else param.setType(type);
        return param;
    }

    private CtTypeReference<?> handleTypeCreation(String paramType) {
        if (!paramType.contains("<")) return factory.Type().createReference(paramType);
        String[] typeAndGeneric = paramType.split("<");
        CtTypeReference<?> type = factory.Type().createReference(typeAndGeneric[0]);
        String[] types = typeAndGeneric[1].substring(0, typeAndGeneric[1].length()-1).split(",");
        for (String t : types) {
            type.addActualTypeArgument(factory.Type().createReference(t.trim()));
        }
        return type;
    }

    public void insertImport() {
        String filename = outputDir+"/"+newClassName+".java";
        try {
            // Read the original file content while preserving format
            String originalContent = new String(Files.readAllBytes(Paths.get(filename)));

            // Build the new content
            StringBuilder newContent = new StringBuilder();

            // Add the new lines
            newContent.append("package "+"com.generated_progs."+newClassName+";\n");
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
            e.printStackTrace();
        }
    }

}