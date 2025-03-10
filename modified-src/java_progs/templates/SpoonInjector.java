package java_progs.templates;
public class SpoonInjector {
    public static void main(java.lang.String[] args) {
        // Initialize Spoon Launcher
        spoon.Launcher launcher = new spoon.Launcher();
        // Set the input directory where the source files exist
        launcher.addInputResource("java_progs/templates/");// Adjust path

        // Build the model
        launcher.buildModel();
        // Get the factory
        spoon.reflect.factory.Factory factory = launcher.getFactory();
        // Get the class by name (replace with your class name)
        spoon.reflect.declaration.CtClass<?> myClass = factory.Class().get("java_progs.templates.Template");
        java.util.List<spoon.reflect.declaration.CtType<?>> l = factory.Class().getAll();
        java.lang.System.out.println(l.size());
        for (int i = 0; i < l.size(); i++) {
            java.lang.System.out.println(l.get(i).getQualifiedName());
        }
        if (myClass == null) {
            java.lang.System.out.println("null");
            java.lang.System.exit(0);
        }
        // Inject a new method
        java_progs.templates.SpoonInjector.injectMethod(factory, myClass);
        // Save modified source code
        launcher.setSourceOutputDirectory("modified-src");
        launcher.prettyprint();
    }

    private static void injectMethod(spoon.reflect.factory.Factory factory, spoon.reflect.declaration.CtClass<?> myClass) {
        // Define the return type (void in this case)
        spoon.reflect.reference.CtTypeReference<java.lang.Void> returnType = factory.Type().voidPrimitiveType();
        // Define the method name
        java.lang.String methodName = "injectedMethod";
        // Define the method modifiers (public)
        java.util.Set<spoon.reflect.declaration.ModifierKind> modifiers = new java.util.HashSet<>();
        modifiers.add(spoon.reflect.declaration.ModifierKind.PUBLIC);
        // Define the body of the method
        spoon.reflect.code.CtBlock<java.lang.Void> methodBody = factory.Core().createBlock();
        spoon.reflect.code.CtCodeSnippetStatement snippet = factory.Code().createCodeSnippetStatement("System.out.println(\"Injected method executed!\")");
        methodBody.addStatement(snippet);
        // Create the method
        spoon.reflect.declaration.CtMethod<java.lang.Void> newMethod = // Target class
        // Modifiers
        // Return type
        // Method name
        // Parameters (empty)
        // Exceptions thrown
        // Method body
        factory.Method().create(myClass, modifiers, returnType, methodName, java.util.Collections.emptyList(), java.util.Collections.emptySet(), methodBody);
        // Add method to class
        myClass.addMethod(newMethod);
    }
}
