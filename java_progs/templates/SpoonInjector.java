package java_progs.templates;


import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.compiler.FileSystemFolder;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SpoonInjector {
    Launcher launcher;
    Factory factory;
    int numberOfFunCalls;
    CtMethod<?> method;
    CtType<?> collec;
    Boolean isMethodStatic;

    public SpoonInjector(Launcher launcher, Factory factory, int numberOfFunCalls, CtMethod<?> method, CtType<?> collec) {
        this.launcher = launcher;
        this.factory = factory;
        this.numberOfFunCalls = numberOfFunCalls;
        this.method = method;
        this.collec = collec;
        this.isMethodStatic = method.hasModifier(ModifierKind.STATIC);
    }

    static int varIndex = 0;

    public void injectInTemplate() {

        String path = "java_progs.templates.Template";
        CtClass<?> myClass = factory.Class().get("java_progs.templates.Template");
        if (myClass == null) {
            System.out.println(path +" not found");
            System.exit(0);
        }

        //getCollectionMethods(launcher,"vector");
        
        // Inject a new method
        //injectMethod(factory, myClass);
        injectComputationMethod(myClass);

        // Save modified source code
        launcher.setSourceOutputDirectory("modified-src");
        launcher.prettyprint();
    }

    private CtConstructor<?> getConstructors() {
        List<CtConstructor<?>> l = collec.filterChildren(new TypeFilter<>(CtConstructor.class))
        .map(m -> (CtConstructor<?>) m)
        .list();
        CtConstructor<?> shortestConstructor = l.get(0);
        int shortestConstructorCount = Integer.MAX_VALUE;
        for (int i = 0; i < l.size(); i++) {
            if (collec.getQualifiedName().equals(l.get(i).getSignature().split("\\(")[0]) && l.get(i).toString().length() <= shortestConstructorCount) {
                shortestConstructor = l.get(i);
                shortestConstructorCount = l.get(i).toString().length();
                //System.out.println(l.get(i) + " " + l.get(i).toString().length());
            }
        }
        return shortestConstructor;
        //TODO for each parameter check if it is needed to start something
        
    }

    private String allocateInitialArrayForFunCalls() {
        String initialArray = "";
        if (method.hasModifier(ModifierKind.STATIC)){
            initialArray += collec.getQualifiedName();
        }
        else {
            initialArray += createVar(factory.Type().createReference(collec),"initialVar");
            //initializeConstructors(collec);
        }
        for (CtParameter<?> arg : method.getParameters()){
            initialArray += "("+arg+")";
        }
        return initialArray;
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

    private String getComputationBody() {
        String body = "";
        if (isMethodStatic) body += collec.getQualifiedName()+ "()";//TODO i assume there are no constructors here
        else body += "var."+method.getSimpleName();
        body += callArgs();
        return body;
    }

    private CtLocalVariable<?> createVar(CtTypeReference typeRef, String varName) {
        CtLocalVariable<?> variable = factory.Code().createLocalVariable(
            typeRef,           // var type
            varName,          // Variable name
            factory.Code().createConstructorCall(typeRef) // Initialization
        );
        return variable;
    }

    private String createMethodCallParameters() {
        String args = "";
        for (int i = 0; i < method.getParameters().size(); i++) {
            CtParameter<?> parameter = method.getParameters().get(i);
            args += parameter.getType() +" arg"+i;
            if (i != method.getParameters().size() - 1) args += ", ";
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

        CtCodeSnippetStatement snippet = factory.Code().createCodeSnippetStatement(
                "System.out.println(\"Injected method executed!\")"
        );
        methodBody.addStatement(snippet);

        // Create the method
        CtMethod<Void> newMethod = factory.Method().create(
                myClass,            // Target class
                modifiers,          // Modifiers
                returnType,         // Return type
                methodName,         // Method name
                Collections.emptyList(),  // Parameters (empty)
                Collections.emptySet(),   // Exceptions thrown
                methodBody          // Method body
        );

        // Add method to class
        myClass.addMethod(newMethod);
    }

    private List<CtParameter<?>> getComputationParameters() {
        if (isMethodStatic) return method.getParameters();
        CtParameter<?> param = factory.createParameter();
        param.setSimpleName("var");
        param.setType(collec.getReference());
        List<CtParameter<?>> params = new ArrayList<>();
        params.add(param);
        for (int i = 0; i < method.getParameters().size(); i++) {
            for (CtTypeReference<?> tr : method.getParameters().get(i).getType().getActualTypeArguments()){
                if (tr.toString().contains("? extends E")){// change the type from <? extends E> to <?>
                    method.getParameters().get(i).getType().setActualTypeArguments(List.of(factory.Type().createReference("?")));
                }
            }
            params.add(method.getParameters().get(i));
        }
        return params;
    }

    private void injectComputationMethod(CtClass<?> templateClass) {
        // Define the return type (void in this case)
        CtTypeReference<Void> returnType = factory.Type().voidPrimitiveType();

        String methodName = "computation";

        Set<ModifierKind> modifiers = new HashSet<>();
        modifiers.add(ModifierKind.PRIVATE);
        modifiers.add(ModifierKind.STATIC);

        CtBlock<Void> methodBody = factory.Core().createBlock();

        CtCodeSnippetStatement snippet = factory.Code().createCodeSnippetStatement(
            //"System.out.println(\"methodName\");"
            getComputationBody()
        );
        methodBody.addStatement(snippet);

        // Create the method
        CtMethod<Void> newMethod = factory.Method().create(
                templateClass,            // Target class
                modifiers,          // Modifiers
                returnType,         // Return type
                methodName,         // Method name
                getComputationParameters(),  // Parameters (empty)
                Collections.emptySet(),   // Exceptions thrown
                methodBody          // Method body
        );

        // Add method to class
        templateClass.addMethod(newMethod);
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
            
            //System.out.print("Method: " + methodName + "(");
            
            // Get parameters
            for (CtParameter<?> param : method.getParameters()) {
                System.out.print(param.getType().getSimpleName() + " " + param.getSimpleName() + ", ");
            }
            
            //System.out.println(") -> " + returnType.getSimpleName());
        }
    }
}
