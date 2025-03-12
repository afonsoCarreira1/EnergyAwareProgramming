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
import java.util.Set;
import java.util.Vector;
import spoon.Launcher;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.code.CtTry;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;
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

    String newClassName;

    CtClass<?> newClass;

    CtMethod<?> mainMethod;

    CtTry tryBlock;

    int varIndex = 0;

    ArrayList<String> imports = new ArrayList<>();

    public SpoonInjector(Launcher launcher, Factory factory, int numberOfFunCalls, CtMethod<?> method, CtType<?> collec, String typeToUse) {
        this.launcher = launcher;
        this.factory = factory;
        this.numberOfFunCalls = numberOfFunCalls;
        this.method = method;
        this.collec = collec;
        this.isMethodStatic = method.hasModifier(ModifierKind.STATIC);
        this.typeToUse = typeToUse;
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
    }

    private String getVarName() {
        return "var" + (varIndex++);
    }

    public void injectInTemplate() {
        // getCollectionMethods(launcher,"vector");
        // Inject a new method
        // injectMethod(factory, myClass);
        allocateInitialArrayForFunCalls();
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

    private void allocateInitialArrayForFunCalls() {
        String initialArray = "";
        if (isMethodStatic)
            initialArray += collec.getQualifiedName() + "()";
        else // TODO i assume there are no constructors here
        {
            CtBlock<?> tryBody = tryBlock.getBody();
            String varName = getVarName();
            CtLocalVariable<?> var = createVar(factory.Type().createReference(collec), varName);
            CtStatementList statements = factory.Core().createStatementList();
            statements.addStatement(var);
            CtStatement initCollection = populateCollection(var, tryBody);
            if (initCollection != null)
                statements.addStatement(initCollection);

            initialArray += (varName + ".") + method.getSimpleName();
            tryBody.insertBegin(statements);
        }
        for (CtParameter<?> arg : method.getParameters()) {
            initialArray += ("(" + arg) + ")";
        }
        // return initialArray;
    }

    private CtStatement populateCollection(CtLocalVariable<?> var, CtBlock<?> tryBody) {
        if (!var.getType().isSubtypeOf(factory.Type().createReference("java.util.Collection")))
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

    private CtLocalVariable<?> createVar(CtTypeReference typeRef, String varName) {
        CtLocalVariable variable = // var type
        // Variable name
        // Initialization
        factory.Code().createLocalVariable(typeRef, varName, factory.Code().createConstructorCall(typeRef));
        return variable;
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
            System.out.println("File successfully updated.");
            // System.out.println(newContent);
        } catch (IOException e) {
            System.err.println("Error modifying the file: " + e.getMessage());
        }
        try {
            String originalContent = new String(Files.readAllBytes(Paths.get(filename)));
            System.out.println(originalContent);
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
