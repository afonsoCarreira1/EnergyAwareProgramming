package java_progs.templates;


import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
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
    public static void main(String[] args) {
        // Initialize Spoon Launcher
        Launcher launcher = new Launcher();
        
        // Set the input directory where the source files exist
        launcher.addInputResource("java_progs/templates/"); // Adjust path

        // Build the model
        launcher.buildModel();
        
        // Get the factory
        Factory factory = launcher.getFactory();

        // Get the class by name (replace with your class name)
        CtClass<?> myClass = factory.Class().get("java_progs.templates.Template");
        List<CtType<?>> l = factory.Class().getAll();
        System.out.println(l.size());
        for (int i = 0; i < l.size(); i++) {
            System.out.println(l.get(i).getQualifiedName());
        }
        if (myClass == null) {
            System.out.println("null");
            System.exit(0);
        }
        // Inject a new method
        injectMethod(factory, myClass);

        // Save modified source code
        launcher.setSourceOutputDirectory("modified-src");
        launcher.prettyprint();
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
}
