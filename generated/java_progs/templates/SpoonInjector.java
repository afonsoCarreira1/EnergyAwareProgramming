package java_progs.templates;
import java.beans.Introspector;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import spoon.Launcher;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtFor;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtNewArray;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.code.CtTry;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;
public class SpoonInjector {
    Launcher launcher;

    Factory factory;

    int numberOfFunCalls;

    CtMethod<?> method;

    CtType<?> collec;

    Boolean isMethodStatic;

    String typeToUse;

    int size;

    String newClassName;

    CtClass<?> newClass;

    CtMethod<?> mainMethod;

    CtTry tryBlock;

    int varIndex = 0;

    HashSet<String> imports = new HashSet<>();

    int min = 0;

    int max;

    public SpoonInjector(Launcher launcher, Factory factory, int numberOfFunCalls, CtMethod<?> method, CtType<?> collec, String typeToUse, int size) {
        this.launcher = launcher;
        this.factory = factory;
        this.numberOfFunCalls = numberOfFunCalls;
        this.method = method;
        this.collec = collec;
        this.isMethodStatic = method.hasModifier(ModifierKind.STATIC);
        this.typeToUse = typeToUse;
        this.size = size;
        String path = "java_progs.templates.Template";
        CtClass<?> myClass = factory.Class().get(path);
        if (myClass == null) {
            System.out.println(path + " not found");
            return;
        }
        this.newClass = myClass.clone();
        this.newClassName = (myClass.getSimpleName() + "_") + method.getSignature().replaceAll("\\.|,|\\(|\\)", "_");
        this.mainMethod = newClass.getMethod("main", factory.Type().createArrayReference(factory.Type().stringType()));
        this.tryBlock = ((CtTry) (mainMethod.getElements(el -> el instanceof CtTry).get(0)));
        initMinMax();
    }

    private void initMinMax() {
        if (((size - 1) + min) == 0)
            this.max = 0;
        else
            this.max = size - 1;

    }

    private String getVarName() {
        return "var" + (varIndex++);
    }

    public void injectInTemplate() {
        // getCollectionMethods(launcher,"vector");
        // Inject a new method
        // injectMethod(factory, myClass);
        CtStatementList statements = factory.Core().createStatementList();
        createInitialVar(statements);
        createMethodArgs(statements);
        createClassThatHoldsArgs(statements);
        createArrayWithVarAndArgs(statements);
        createSampleArray(statements);
        insertInTryBlock(statements);
        injectBenchmarkMethod(newClass);
        // Save modified source code -> this changes the current class
        // launcher.setSourceOutputDirectory("modified-src");
        // launcher.prettyprint();
        newClass.setSimpleName(newClassName);
        launcher.getFactory().Class().getAll().add(newClass);// Register the new class

        launcher.getModel().getRootPackage().addType(newClass);
        // System.out.println(launcher.getModel().getRootPackage());
        launcher.prettyprint();
        // insertImport();
        // insertImport();
    }

    private CtConstructor<?> getConstructors() {
        List<CtConstructor<?>> l = collec.filterChildren(new TypeFilter<>(CtConstructor.class)).map(m -> ((CtConstructor<?>) (m))).list();
        CtConstructor<?> shortestConstructor = l.get(0);
        int shortestConstructorCount = Integer.MAX_VALUE;
        for (int i = 0; i < l.size(); i++) {
            if (collec.getQualifiedName().equals(l.get(i).getSignature().split("\\(")[0]) && (l.get(i).toString().length() <= shortestConstructorCount)) {
                shortestConstructor = l.get(i);
                shortestConstructorCount = l.get(i).toString().length();
                // System.out.println(l.get(i) + " " + l.get(i).toString().length());
            }
        }
        return shortestConstructor;
        // TODO for each parameter check if it is needed to start something
    }

    private void createClassThatHoldsArgs(CtStatementList statements) {
        String innerClassName = "BenchmarkArgs";
        ArrayList<CtLocalVariable<?>> vars = new ArrayList<>();
        for (int i = 0; i < statements.getStatements().size(); i++) {
            if (statements.getStatements().get(i) instanceof CtLocalVariable) {
                CtLocalVariable<?> var = ((CtLocalVariable<?>) (statements.getStatements().get(i)));
                vars.add(var);
            }
        }
        CtClass<?> innerClass = factory.Class().create(innerClassName);
        CtConstructor constructor = factory.createConstructor();
        constructor.setSimpleName(innerClassName);
        ArrayList<CtParameter<?>> params = new ArrayList<>();
        CtBlock<?> bodyStatements = factory.createBlock();
        for (CtLocalVariable<?> var : vars) {
            innerClass.addField(createBenchmarkClassFields(var));
            params.add(factory.createParameter(constructor, var.getType(), var.getSimpleName()));
            if (isPrimitive(var.getType().toString()))
                bodyStatements.addStatement(factory.Code().createCodeSnippetStatement((("this." + var.getSimpleName()) + " = ") + var.getSimpleName()));
            else
                bodyStatements.addStatement(factory.Code().createCodeSnippetStatement(((((("this." + var.getSimpleName()) + " = (") + collec.getSimpleName()) + ") ") + var.getSimpleName()) + ".clone()"));

        }
        constructor.setParameters(params);
        constructor.setBody(bodyStatements);
        innerClass.addConstructor(constructor);
        innerClass.addModifier(ModifierKind.STATIC);
        newClass.addNestedType(innerClass);
    }

    private boolean isPrimitive(String type) {
        if ((((((((type.equals("int") || type.equals("boolean")) || type.equals("char")) || type.equals("byte")) || type.equals("short")) || type.equals("float")) || type.equals("double")) || type.equals("long")) || type.equals("void"))
            return true;

        if (((((((type.equals("Integer") || type.equals("Boolean")) || type.equals("Character")) || type.equals("Byte")) || type.equals("Short")) || type.equals("Float")) || type.equals("Double")) || type.equals("Long"))
            return true;

        return false;
    }

    private CtField<?> createBenchmarkClassFields(CtLocalVariable var) {
        CtField<?> field = factory.createCtField(var.getSimpleName(), var.getType(), var.getDefaultExpression().toString());
        field.addModifier(ModifierKind.PUBLIC);
        return field;
    }

    private void createSampleArray(CtStatementList statements) {
        StringBuilder args = new StringBuilder();
        for (int i = 0; i < varIndex; i++) {
            args.append("var" + i);
            if (i != (varIndex - 1))
                args.append(", ");

        }
        String statement = ((((("for (int i = 0;i < " + varIndex) + ";i++) {\n")// 
         + "   arr[i] = new BenchmarkArgs(") + args) + ");\n")// 
         + "}";
        statements.addStatement(factory.Code().createCodeSnippetStatement(statement));
    }

    private void createSampleArray2(CtStatementList statements, CtLocalVariable<?> arrWithArgs) {
        CtNewArray<Object> arrayExpr = factory.Core().createNewArray();
        arrayExpr.setType(factory.Type().createArrayReference(factory.Type().objectType(), 2));
        arrayExpr.addDimensionExpression(factory.Code().createLiteral(numberOfFunCalls));
        arrayExpr.addDimensionExpression(((CtExpression<Integer>) (accessVarField(arrWithArgs, "length"))));
        CtLocalVariable<?> arr2 = factory.Code().createLocalVariable(arrayExpr.getType(), "arr", arrayExpr);
        statements.addStatement(arr2);
        // System.out.println(arrLength);
        CtFor forLoop = factory.createFor();
        List<CtStatement> initStatements = new ArrayList<>();
        CtLocalVariable<?> varI = createVar(factory.Type().integerPrimitiveType(), "i", true);
        initStatements.add(varI);
        CtVariableRead<?> var1Read = ((CtVariableRead<?>) (factory.Code().createVariableRead(varI.getReference(), false)));
        CtExpression<Boolean> condition = createBinOp(var1Read, accessVarField(arrWithArgs, "length"), BinaryOperatorKind.LT);
        // Create the binary operator (var1 < var2)
        addImport("java.util.Arrays");
        forLoop.setForInit(initStatements);
        forLoop.setExpression(condition);
        forLoop.addForUpdate(factory.createCodeSnippetStatement("i++"));
        String line = (((((" new " + collec.getReference()) + "<>((") + collec.getReference()) + "<") + typeToUse) + ">) argsArr[j]);\n";
        String statement = ((((((("arr[i] = new Object[argsArr.length];\n"// 
         + "       for (int j = 0; j < argsArr.length; j++) {\n")// 
         + "           if (argsArr[j] instanceof Collection) {\n")// 
         + "               arr[i][j] =") + line)// 
         + "           } else {\n")// 
         + "               arr[i][j] = argsArr[j];\n")// 
         + "           }\n")// 
         + "       }";
        // "arr[i] = Arrays.copyOf(argsArr, argsArr.length)"
        forLoop.setBody(factory.createCodeSnippetStatement(statement));
        statements.addStatement(forLoop);
    }

    private CtBinaryOperator<Boolean> createBinOp(CtExpression<?> var1, CtExpression<?> var2, BinaryOperatorKind op) {
        return // Left operand (var1)
        // Right operand (var2)
        factory.Code().createBinaryOperator(var1, var2, op);
    }

    private CtFieldRead<?> accessVarField(CtLocalVariable<?> var, String field) {
        CtTypeReference<Integer> intType = factory.Type().integerPrimitiveType();
        CtFieldReference<Integer> lengthRef = factory.Core().createFieldReference();
        lengthRef.setDeclaringType(var.getType());// Set the array type

        lengthRef.setSimpleName(field);// choose field

        lengthRef.setType(intType);// type of the field

        CtFieldRead<Integer> arrLength = factory.Core().createFieldRead();
        arrLength.setTarget(factory.Code().createVariableRead(var.getReference(), false));
        arrLength.setVariable(lengthRef);
        return arrLength;
    }

    private void createArrayWithVarAndArgs(CtStatementList statements) {
        String statement = ("BenchmarkArgs[] arr = new BenchmarkArgs[" + numberOfFunCalls) + "]";
        statements.addStatement(factory.createCodeSnippetStatement(statement));
    }

    private CtLocalVariable<?> createArrayWithVarAndArgs2(CtStatementList statements) {
        ArrayList<Object> varStatements = new ArrayList<>();
        for (int i = 0; i < statements.getStatements().size(); i++) {
            if (statements.getStatements().get(i) instanceof CtLocalVariable) {
                CtLocalVariable<?> var = ((CtLocalVariable<?>) (statements.getStatements().get(i)));
                varStatements.add(factory.Code().createVariableRead(var.getReference(), false));
            }
        }
        Object[] vars = varStatements.toArray();
        CtNewArray<Object[]> arrayExpr = factory.Code().createLiteralArray(vars);
        CtLocalVariable<?> arr = factory.Code().createLocalVariable(arrayExpr.getType(), "argsArr", arrayExpr);
        statements.addStatement(arr);
        return arr;
    }

    private void createMethodArgs(CtStatementList statements) {
        List<CtParameter<?>> args = method.getParameters();
        for (CtParameter<?> arg : args) {
            CtLocalVariable<?> var = createVar(arg.getType(), getVarName(), false);
            statements.addStatement(var);
            if (isCollection(var))
                statements.addStatement(populateCollection(var));

        }
    }

    private void initVar(CtElement elem) {
        // if (!isCollection((CtLocalVariable<?>) elem)) {
        // 
        // }
    }

    private void insertInTryBlock(CtStatementList statements) {
        tryBlock.getBody().insertBegin(statements);
    }

    private void createInitialVar(CtStatementList statements) {
        String initialArray = "";
        if (isMethodStatic)
            initialArray += ((collec.getQualifiedName() + "()") + ".") + method.getSimpleName();
        else // TODO i assume there are no constructors here
        {
            String varName = getVarName();
            CtLocalVariable<?> var = createVar(factory.Type().createReference(collec), varName, false);
            statements.addStatement(var);
            CtStatement initCollection = populateCollection(var);
            if (initCollection != null)
                statements.addStatement(initCollection);

            initialArray += (varName + ".") + method.getSimpleName();
        }
        // for (CtParameter<?> arg : method.getParameters()){
        // initialArray += "("+arg+")";
        // }
        // return initialArray;
    }

    private CtStatement populateCollection(CtLocalVariable<?> var) {
        if (!isCollection(var))
            return null;

        if (var.getType().toString().contains("List")) {
            addImport("java_progs.aux.ArrayListAux");
            CtClass<?> ctClass = factory.Class().get("java_progs.aux.ArrayListAux");
            CtMethod<?> insertRandomNumbersMethod = ctClass.getMethodsByName("insertRandomNumbers").get(0);
            CtInvocation<?> invocation = factory.Core().createInvocation();
            invocation.setExecutable(((CtExecutableReference) (insertRandomNumbersMethod.getReference())));// adciciona a fun

            invocation.setTarget(factory.createLiteral(ctClass.getReference()));// adiciona a Class.

            invocation.addArgument(factory.Code().createVariableRead(var.getReference(), false));
            invocation.addArgument(factory.Code().createLiteral(10));
            invocation.addArgument(factory.Code().createLiteral(typeToUse));
            return invocation;
        }// TODo do the same for sets and other collections

        return null;
    }

    private void addImport(String importPath) {
        imports.add(("import " + importPath) + ";");
    }

    private boolean isCollection(CtLocalVariable<?> var) {
        return var.getType().isSubtypeOf(factory.Type().createReference("java.util.Collection"));
    }

    private String callArgs() {
        String argsString = "";
        List<CtParameter<?>> args = method.getParameters();
        argsString += "(";
        for (int i = 0; i < args.size(); i++) {
            argsString += args.get(i).getSimpleName();
            if (i != (args.size() - 1))
                argsString += ", ";

        }
        argsString += ")";
        return argsString;
    }

    private String getBenchmarkFunBody() {
        String body = "";
        if (isMethodStatic)
            body += collec.getQualifiedName() + "()";
        else// TODO i assume there are no constructors here

            body += "var." + method.getSimpleName();

        body += callArgs();
        return body;
    }

    private CtLocalVariable<?> createVar(CtTypeReference typeRef, String varName, boolean getDefaultValue) {
        CtTypeReference ref = (typeRef.toString().contains("Collection")) ? factory.Type().createReference(collec) : typeRef;
        CtTypeReference<?> genericType = factory.Type().createReference(typeToUse);
        ref.addActualTypeArgument(genericType);
        CtExpression<?> exp = createVar(ref, getDefaultValue);
        CtLocalVariable<?> variable = // var type
        // Variable name
        // Initialization
        factory.Code().createLocalVariable(ref, varName, exp);
        return variable;
    }

    private CtExpression<?> createVar(CtTypeReference<?> typeRef, boolean getDefaultValue) {
        if (typeRef.isPrimitive())
            return factory.Code().createLiteral(createRandomLiteral(typeRef, getDefaultValue));

        if (typeRef.toString().contains("Collection"))
            return factory.Code().createConstructorCall(typeRef);
        // System.out.println(collec.getReference());

        return factory.Code().createConstructorCall(typeRef);
    }

    private Object createRandomLiteral(CtTypeReference<?> typeRef, boolean getDefaultValue) {
        if (getDefaultValue)
            return getDefaultValues(typeRef.toString());

        // if (typeRef.toString().equals("int")) return getRandomValueOfType(typeRef.toString());
        // if (typeRef.toString().equals("double")) return getRandomValueOfType(typeRef.toString());
        // if (typeRef.toString().equals("float")) return getRandomValueOfType(typeRef.toString());
        // if (typeRef.toString().equals("long")) return getRandomValueOfType(typeRef.toString());
        // if (typeRef.toString().equals("boolean")) return getRandomValueOfType(typeRef.toString());
        // if (typeRef.toString().equals("short")) return getRandomValueOfType(typeRef.toString());
        // if (typeRef.toString().equals("integer")) return getRandomValueOfType(typeRef.toString());
        return getRandomValueOfType(typeRef.toString());
    }

    @SuppressWarnings("unchecked")
    private <T> T getRandomValueOfType(String type) {
        Random rand = new Random(42);
        switch (type.toLowerCase()) {
            case "int" :
                return ((T) (Integer.valueOf(rand.nextInt((max - min) + 1) + min)));
            case "double" :
                return ((T) (Double.valueOf(min + ((max - min) * rand.nextDouble()))));
            case "float" :
                return ((T) (Float.valueOf(min + ((max - min) * rand.nextFloat()))));
            case "long" :
                return ((T) (Long.valueOf(rand.nextLong(min, max + 1))));
            case "boolean" :
                return ((T) (Boolean.valueOf(rand.nextBoolean())));
            case "short" :
                return ((T) (Short.valueOf(((short) (rand.nextInt((max - min) + 1) + min)))));
            case "integer" :
                return ((T) (Integer.valueOf(rand.nextInt((max - min) + 1) + min)));
            case "character" :
                char minChar = 'a';
                char maxChar = 'z';
                char randomChar = ((char) (rand.nextInt((maxChar - minChar) + 1) + minChar));
                return ((T) (Character.valueOf(randomChar)));
            default :
                throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getDefaultValues(String type) {
        switch (type.toLowerCase()) {
            case "int" :
                return ((T) (Integer.valueOf(0)));
            case "double" :
                return ((T) (Double.valueOf(0.0)));
            case "float" :
                return ((T) (Float.valueOf(0.0F)));
            case "long" :
                return ((T) (Long.valueOf(0)));
            case "boolean" :
                return ((T) (Boolean.valueOf(false)));
            case "short" :
                return ((T) (Short.valueOf("0")));
            case "integer" :
                return ((T) (Integer.valueOf(0)));
            case "character" :
                return ((T) (Character.valueOf('a')));
            default :
                throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }

    private String createMethodCallParameters() {
        String args = "";
        for (int i = 0; i < method.getParameters().size(); i++) {
            CtParameter<?> parameter = method.getParameters().get(i);
            args += (parameter.getType() + " arg") + i;
            if (i != (method.getParameters().size() - 1))
                args += ", ";

        }
        return args;
    }

    private void injectMethod(CtClass<?> myClass) {
        // Define the return type (void in this case)
        CtTypeReference<Void> returnType = factory.Type().voidPrimitiveType();
        // Define the method name
        String methodName = "injectedMethod";
        // Define the method modifiers (public)
        Set<ModifierKind> modifiers = new HashSet<>();
        modifiers.add(ModifierKind.PUBLIC);
        // Define the body of the method
        CtBlock<Void> methodBody = factory.Core().createBlock();
        CtCodeSnippetStatement snippet = factory.Code().createCodeSnippetStatement("System.out.println(\"Injected method executed!\")");
        methodBody.addStatement(snippet);
        // Create the method
        CtMethod<Void> newMethod = // Target class
        // Modifiers
        // Return type
        // Method name
        // Parameters (empty)
        // Exceptions thrown
        // Method body
        factory.Method().create(myClass, modifiers, returnType, methodName, Collections.emptyList(), Collections.emptySet(), methodBody);
        // Add method to class
        myClass.addMethod(newMethod);
    }

    private List<CtParameter<?>> getComputationParameters() {
        if (isMethodStatic)
            return method.getParameters();

        CtParameter<?> param = factory.createParameter();
        param.setSimpleName("var");
        param.setType(collec.getReference());
        List<CtParameter<?>> params = new ArrayList<>();
        params.add(param);
        for (int i = 0; i < method.getParameters().size(); i++) {
            for (CtTypeReference<?> tr : method.getParameters().get(i).getType().getActualTypeArguments()) {
                if (tr.toString().contains("? extends E")) {
                    // change the type from <? extends E> to <?>
                    method.getParameters().get(i).getType().setActualTypeArguments(List.of(factory.Type().createReference("?")));
                }
            }
            params.add(method.getParameters().get(i));
        }
        return params;
    }

    private void injectBenchmarkMethod(CtClass<?> templateClass) {
        // Define the return type (void in this case)
        CtTypeReference<Void> returnType = factory.Type().voidPrimitiveType();
        Set<ModifierKind> modifiers = new HashSet<>();
        modifiers.add(ModifierKind.PRIVATE);
        modifiers.add(ModifierKind.STATIC);
        CtBlock<Void> methodBody = factory.Core().createBlock();
        CtCodeSnippetStatement snippet = // "System.out.println(\"methodName\");"
        factory.Code().createCodeSnippetStatement(getBenchmarkFunBody());
        methodBody.addStatement(snippet);
        // Create the method
        CtMethod<Void> newMethod = // Target class
        // Modifiers
        // Return type
        // Method name
        // Parameters (empty)
        // Exceptions thrown
        // Method body
        factory.Method().create(templateClass, modifiers, returnType, Introspector.decapitalize(newClassName), getComputationParameters(), Collections.emptySet(), methodBody);
        // Add method to class
        templateClass.addMethod(newMethod);
    }

    private void getCollectionMethods(Launcher launcher, String collection) {
        launcher.getFactory().getEnvironment().setAutoImports(true);
        CtType<?> collectionType = null;
        if (collection.toLowerCase().equals("arraylist"))
            collectionType = launcher.getFactory().Type().get(ArrayList.class);

        if (collection.toLowerCase().equals("vector"))
            collectionType = launcher.getFactory().Type().get(Vector.class);

        // Iterate through all methods
        for (CtMethod<?> method : collectionType.getMethods()) {
            String methodName = method.getSimpleName();
            CtTypeReference<?> returnType = method.getType();
            // System.out.print("Method: " + methodName + "(");
            // Get parameters
            for (CtParameter<?> param : method.getParameters()) {
                System.out.print(((param.getType().getSimpleName() + " ") + param.getSimpleName()) + ", ");
            }
            // System.out.println(") -> " + returnType.getSimpleName());
        }
    }

    public void insertImport() {
        String filename = ("generated/" + newClassName) + ".java";
        try {
            // Read the original file content while preserving format
            String originalContent = new String(Files.readAllBytes(Paths.get(filename)));
            // Build the new content
            StringBuilder newContent = new StringBuilder();
            // Add the new lines
            newContent.append("package generated;\n");
            for (String line : imports) {
                newContent.append(line).append(System.lineSeparator());
            }
            // System.out.println(filename + " original content -> \n"+originalContent);
            // Append the original file content
            newContent.append(originalContent);
            // Overwrite the file with the new content
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
                writer.write(newContent.toString());
            }
            // System.out.println("File successfully updated.");
            // System.out.println(newContent);
        } catch (IOException e) {
            System.err.println("Error modifying the file: " + e.getMessage());
        }
    }
}
