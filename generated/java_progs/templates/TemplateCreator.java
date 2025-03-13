package java_progs.templates;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;
import spoon.Launcher;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;
public class TemplateCreator {
    public static void main(String[] args) {
        // initSpoon();
        // initSpoon();
        ArrayList<CtMethod<?>> commonMethods = getCollectionMethods("list");
        List<Integer> sizes = Arrays.asList(150);// createInputRange(1, 1.5, 0);

        int[] funCalls = new int[]{ 20000, 50000, 75000, 100000, 150000 };
        for (CtType<?> collec : getCollections("list")) {
            for (CtMethod<?> method : commonMethods) {
                for (int size : sizes) {
                    if (method.getSimpleName().contains("addAll") && collec.getSimpleName().equals("ArrayList")) {
                        // getGoodInputs(method,collec);
                        Launcher launcher = initSpoon();
                        SpoonInjector spi = new SpoonInjector(launcher, launcher.getFactory(), 0, method, collec, "Integer", size);
                        spi.injectInTemplate();
                        spi.insertImport();
                        // SpoonInjector.injectInTemplate(launcher, factory, 0/*funCall*/, method,collec);
                    }
                }
                for (int funCall : funCalls) {
                    // SpoonInjector.injectInTemplate(launcher, factory, funCall, method);
                }
            }
        }
    }

    private static ArrayList<Integer> createInputRange(int initialvalue, double factor, int exponent) {
        Set<Integer> numberSet = new HashSet<>();
        Random random = new Random(42);
        int max_value = initialvalue * 100000;
        while (initialvalue < max_value) {
            int min = initialvalue;
            int max = initialvalue * 10;
            double nums = Math.pow(factor, exponent);
            for (int j = 0; j < nums; j++) {
                int num = min + random.nextInt((max - min) + 1);
                numberSet.add(num);
            }
            initialvalue = initialvalue * 10;
            exponent++;
        } 
        return new ArrayList<>(numberSet);
    }

    private static Launcher initSpoon() {
        Launcher launcher = new Launcher();
        launcher.addInputResource("java_progs/templates/");
        launcher.addInputResource("java_progs/aux/");
        launcher.getFactory().getEnvironment().setAutoImports(true);
        launcher.setSourceOutputDirectory("generated");
        launcher.buildModel();
        return launcher;
    }

    private static ArrayList<CtMethod<?>> getCollectionMethods(String collection) {
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
                    methodSignature.append((param.getType().getSimpleName() + " ") + param.getSimpleName());
                    if (i != (method.getParameters().size() - 1))
                        methodSignature.append(", ");

                }
                methodSignature.append(") -> " + returnType.getSimpleName());
                if (collectionType.getSimpleName().equals("Stack"))
                    System.out.println(methodSignature.toString());

                methods.put(methodSignature.toString(), methods.get(methodSignature.toString()) != null ? methods.get(methodSignature.toString()) + 1 : 1);
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
        Launcher launcher = null;
        launcher = new Launcher();
        launcher.addInputResource("java_progs/templates/");
        launcher.addInputResource("java_progs/aux/");
        launcher.getFactory().getEnvironment().setAutoImports(true);
        launcher.setSourceOutputDirectory("generated");// Different output folder

        launcher.buildModel();
        CtType<?>[] collectionTypes = null;
        if (collection.toLowerCase().equals("list")) {
            collectionTypes = new CtType<?>[4];
            collectionTypes[0] = launcher.getFactory().Type().get(ArrayList.class);
            collectionTypes[1] = launcher.getFactory().Type().get(Vector.class);
            // collectionTypes[2] = launcher.getFactory().Type().get(java.util.Stack.class);
            // Stack extends Vector
            collectionTypes[2] = launcher.getFactory().Type().get(LinkedList.class);
            collectionTypes[3] = launcher.getFactory().Type().get(CopyOnWriteArrayList.class);
        }
        return collectionTypes;
    }
}
