package java_progs.templates;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import spoon.Launcher;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

public class InputTest {
    static StringBuilder program;

    public static void getInputs(Launcher launcher, Factory factory ,CtMethod<?> method, CtType<?> collec) {
        program = new StringBuilder();
        createFile("java_progs/templates", "Test",".java");
        getTestFile(factory,method,collec);
        writeFile("java_progs/templates/Test.java",program.toString());
    }

    private static String getTestFile(Factory factory, CtMethod<?> method, CtType<?> collec) {
        program.append("package java_progs.templates;\n");
        program.append("import "+ collec.getQualifiedName() +";\n");
        program.append("public class Test {\n");
        program.append("    public static void main(String[] args) {\n");
        //System.out.println(collec.getQualifiedName());
        CtTypeReference<?> typeRef = factory.Type().createReference(collec);
        CtLocalVariable<?> var = createVar(factory, typeRef);
        program.append("        "+var+";\n");
        program.append("    }\n");
        String args = createMethodCallParameters(factory, method);
        program.append("    public static void testMethod("+args+"){\n");
        program.append("        ");
        callMethod(factory, method, collec);
        program.append("    }\n");
        program.append("}\n");
        //initializeConstructors(collec);
        return program.toString();
    }

    private static void initializeConstructors(CtType<?> collec) {
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
        //TODO for each parameter check if it is needed to start something
        
    }

    private static void callMethod(Factory factory,CtMethod<?> method,CtType<?> collec) {
        String call = "";
        if (method.hasModifier(ModifierKind.STATIC)) call = collec.getQualifiedName() + "()."+method.getSimpleName();
        else {
            
            String s = createVar(factory, factory.Type().createReference(collec))+";\n";
            program.append(s);
            program.append("        myVar.get(arg0);\n");
            System.out.println("aki -> " + s);
            //initializeConstructors(collec);
        }
    }

    private static CtLocalVariable<?> createVar(Factory factory,CtTypeReference typeRef) {
        CtLocalVariable<?> variable = factory.Code().createLocalVariable(
            typeRef,           // var type
            "myVar",          // Variable name
            factory.Code().createConstructorCall(typeRef) // Initialization
        );
        return variable;
    }

    private static String createMethodCallParameters(Factory factory,CtMethod<?> method) {
        String args = "";
        for (int i = 0; i < method.getParameters().size(); i++) {
            CtParameter<?> parameter = method.getParameters().get(i);
            args += parameter.getType() +" arg"+i;
            if (i != method.getParameters().size() - 1) args += ", ";
        }
        return args;
    }

    private static void createCallMethod(Factory factory,CtMethod method) {
        
    }

    private static void createFile(String dir, String filename, String fileType) {
        File myObj = new File(dir + filename + fileType);
        if (myObj.exists()) {
            myObj.delete();  // Ensure deletion before creating
        }
        try {
            myObj.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void writeFile(String file, String program) {
        try (FileWriter fr = new FileWriter(file, false)) { 
            fr.write(program);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

}
