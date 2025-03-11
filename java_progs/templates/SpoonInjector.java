package java_progs.templates;


import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.compiler.FileSystemFolder;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SpoonInjector {

    public static void injectInTemplate(Launcher launcher, Factory factory, int numberOfFunCalls, CtMethod<?> method) {

        String path = "java_progs.templates.Template";
        CtClass<?> myClass = factory.Class().get("java_progs.templates.Template");
        if (myClass == null) {
            System.out.println(path +" not found");
            System.exit(0);
        }

        getCollectionMethods(launcher,"vector");
        
        // Inject a new method
        //injectMethod(factory, myClass);

        // Save modified source code
        //launcher.setSourceOutputDirectory("modified-src");
        //launcher.prettyprint();
    }

    private static void allocateInitialArrayForFunCalls(int numberOfFunCalls) {

    }

    private static void injectMethod(Factory factory, CtClass<?> myClass) {
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

    private static void injectComputationMethod(Factory factory, CtClass<?> templateClass) {

// Define the return type (void in this case)
        CtTypeReference<Void> returnType = factory.Type().voidPrimitiveType();

        String methodName = "computation";

        Set<ModifierKind> modifiers = new HashSet<>();
        modifiers.add(ModifierKind.PRIVATE);

        CtBlock<Void> methodBody = factory.Core().createBlock();

        CtCodeSnippetStatement snippet = factory.Code().createCodeSnippetStatement(
                "System.out.println(\"Injected method executed!\")"
        );
        methodBody.addStatement(snippet);

        // Create the method
        CtMethod<Void> newMethod = factory.Method().create(
                templateClass,            // Target class
                modifiers,          // Modifiers
                returnType,         // Return type
                methodName,         // Method name
                Collections.emptyList(),  // Parameters (empty)
                Collections.emptySet(),   // Exceptions thrown
                methodBody          // Method body
        );

        // Add method to class
        templateClass.addMethod(newMethod);
    }
    
    private static void getCollectionMethods(Launcher launcher, String collection) {
        launcher.getFactory().getEnvironment().setAutoImports(true);
        
        CtType<?> collectionType = null;
        if (collection.toLowerCase().equals("arraylist")) collectionType = launcher.getFactory().Type().get(java.util.ArrayList.class);
        if (collection.toLowerCase().equals("vector")) collectionType = launcher.getFactory().Type().get(java.util.Vector.class);
        
        
        // Iterate through all methods
        for (CtMethod<?> method : collectionType.getMethods()) {
            String methodName = method.getSimpleName();
            CtTypeReference<?> returnType = method.getType();
            
            System.out.print("Method: " + methodName + "(");
            
            // Get parameters
            for (CtParameter<?> param : method.getParameters()) {
                System.out.print(param.getType().getSimpleName() + " " + param.getSimpleName() + ", ");
            }
            
            System.out.println(") -> " + returnType.getSimpleName());
        }
    }
}
