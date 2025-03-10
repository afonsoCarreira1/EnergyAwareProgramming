package java_progs.templates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

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
        getCollectionMethods("list");
    }

    private static void initSpoon() {
        launcher = new Launcher();
        launcher.addInputResource("java_progs/templates/");
        launcher.buildModel();
        factory = launcher.getFactory();
    }

    private static void getCollectionMethods(String collection) {
        launcher.getFactory().getEnvironment().setAutoImports(true);
        HashMap<String,Integer> methods = new HashMap<String,Integer>();
        CtType<?>[] collectionTypes = getCollections(collection);
        
        
        for (CtType<?> collectionType : collectionTypes) {
            for (CtMethod<?> method : collectionType.getMethods()) {
                StringBuilder methodName = new StringBuilder();
                methodName.append(method.getSimpleName());
                CtTypeReference<?> returnType = method.getType();
                methodName.append("(");
                
                // Get parameters
                for (CtParameter<?> param : method.getParameters()) {
                    methodName.append(param.getType().getSimpleName() + " " + param.getSimpleName() + ", ");
                }
                methodName.append(") -> " + returnType.getSimpleName());
                if(collectionType.getSimpleName().equals("Stack")) System.out.println(methodName.toString());
                methods.put(methodName.toString(), methods.get(methodName.toString()) != null ? methods.get(methodName.toString())+1 : 1);
            }
        }
        List<String> keys = new ArrayList<>(methods.keySet());
        for (int i = 0; i < keys.size(); i++) {
            if (methods.get(keys.get(i)) == collectionTypes.length) {
                System.out.println(keys.get(i));
            }
        }
    }

    private static CtType<?>[] getCollections(String collection) {
        CtType<?>[] collectionTypes = null;
        if (collection.toLowerCase().equals("list")){
            collectionTypes = new CtType<?>[4];
            collectionTypes[0] = launcher.getFactory().Type().get(java.util.ArrayList.class);
            collectionTypes[1] = launcher.getFactory().Type().get(java.util.Vector.class);
            //collectionTypes[2] = launcher.getFactory().Type().get(java.util.Stack.class); Stack extends Vector
            collectionTypes[2] = launcher.getFactory().Type().get(java.util.LinkedList.class);
            collectionTypes[3] = launcher.getFactory().Type().get(java.util.concurrent.CopyOnWriteArrayList.class);
        }
        return collectionTypes;
    }
    
}
