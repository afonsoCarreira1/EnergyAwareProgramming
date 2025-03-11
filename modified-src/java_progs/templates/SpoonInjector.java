package java_progs.templates;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import spoon.Launcher;
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
public class SpoonInjector {
    static int varIndex = 0;

    public static void injectInTemplate(Launcher launcher, Factory factory, int numberOfFunCalls, CtMethod<?> method, CtType<?> collec) {
        String path = "java_progs.templates.Template";
        CtClass<?> myClass = factory.Class().get("java_progs.templates.Template");
        if (myClass == null) {
            System.out.println(path + " not found");
            System.exit(0);
        }
        // getCollectionMethods(launcher,"vector");
        // Inject a new method
        // injectMethod(factory, myClass);
        injectComputationMethod(factory, myClass, method);
        // Save modified source code
        launcher.setSourceOutputDirectory("modified-src");
        launcher.prettyprint();
    }

    private static CtConstructor<?> getConstructors(CtType<?> collec) {
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

    private static void allocateInitialArrayForFunCalls(Factory factory, int numberOfFunCalls, CtMethod<?> method, CtType<?> collec) {
        String initialArray = "";
        if (method.hasModifier(ModifierKind.STATIC)) {
            initialArray += collec.getQualifiedName();
        } else {
            initialArray += createVar(factory, factory.Type().createReference(collec), "initialVar");
            // initializeConstructors(collec);
        }
    }

    private static CtLocalVariable<?> createVar(Factory factory, CtTypeReference typeRef, String varName) {
        CtLocalVariable<?> variable = // var type
        // Variable name
        // Initialization
        factory.Code().createLocalVariable(typeRef, varName, factory.Code().createConstructorCall(typeRef));
        return variable;
    }

    private static String createMethodCallParameters(Factory factory, CtMethod<?> method) {
        String args = "";
        for (int i = 0; i < method.getParameters().size(); i++) {
            CtParameter<?> parameter = method.getParameters().get(i);
            args += (parameter.getType() + " arg") + i;
            if (i != (method.getParameters().size() - 1))
                args += ", ";

        }
        return args;
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

    private static void injectComputationMethod(Factory factory, CtClass<?> templateClass, CtMethod<?> method) {
        // Define the return type (void in this case)
        CtTypeReference<Void> returnType = factory.Type().voidPrimitiveType();
        String methodName = "computation";
        Set<ModifierKind> modifiers = new HashSet<>();
        modifiers.add(ModifierKind.PRIVATE);
        modifiers.add(ModifierKind.STATIC);
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
        factory.Method().create(templateClass, modifiers, returnType, methodName, method.getParameters(), Collections.emptySet(), methodBody);
        // Add method to class
        templateClass.addMethod(newMethod);
    }

    private static void getCollectionMethods(Launcher launcher, String collection) {
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
}
