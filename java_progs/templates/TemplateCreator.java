package java_progs.templates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import spoon.Launcher;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;

public class TemplateCreator {

    static Launcher launcher;
    static Factory factory;

    public static void main(String[] args) {
        initSpoon();
        ArrayList<CtMethod<?>> commonMethods = getCollectionMethods("list");
        int[] funCalls = new int[] { 20_000, 50_000, 75_000, 100_000, 150_000 };
        for (CtType<?> collec : getCollections("list")) {
            for (CtMethod<?> method : commonMethods) {
                if (method.getSimpleName().contains("get"))
                    getGoodInputs(method,collec);

                for (int funCall : funCalls) {
                    // SpoonInjector.injectInTemplate(launcher, factory, funCall, method);
                }
            }
        }

    }

    private static void getGoodInputs(CtMethod<?> method, CtType<?> collec) {
        InputTest.getInputs(launcher, factory, method, collec);
    }

    private static void initSpoon() {
        launcher = new Launcher();
        launcher.addInputResource("java_progs/templates/");
        launcher.buildModel();
        factory = launcher.getFactory();
    }

    private static ArrayList<CtMethod<?>> getCollectionMethods(String collection) {
        launcher.getFactory().getEnvironment().setAutoImports(true);
        HashMap<String, Integer> methods = new HashMap<String, Integer>();
        HashMap<String, CtMethod<?>> methodsParameters = new HashMap<String, CtMethod<?>>();
        CtType<?>[] collectionTypes = getCollections(collection);

        for (CtType<?> collectionType : collectionTypes) {
            for (CtMethod<?> method : collectionType.getMethods()) {
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
                if (collectionType.getSimpleName().equals("Stack"))
                    System.out.println(methodSignature.toString());
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
        CtType<?>[] collectionTypes = null;
        if (collection.toLowerCase().equals("list")) {
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
