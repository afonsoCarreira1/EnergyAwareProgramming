package com.parse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import spoon.reflect.CtModel;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

public class ToolParser {

    CtModel model;
    String file;

    public ToolParser(CtModel model, String file) {
        this.model = model;
        this.file = file;
    }

    public List<MethodEnergyInfo> getMethodsForSliders(HashSet<String> modelsAvailable) {
        List<MethodEnergyInfo> methodsEnergyInfo = new ArrayList<>();
        

        for (CtType<?> ctType : model.getAllTypes()) {
            if (!ctType.getSimpleName().equals(file))continue; //only target class file, ignore other files

            for (CtMethod<?> method : ctType.getMethods()) {
                String methodName = getMethodName(ctType, method);
                MethodEnergyInfo methodEnergyInfo = new MethodEnergyInfo(methodName);
                List<CtInvocation<?>> invocations = method.getElements(new TypeFilter<>(CtInvocation.class));

                List<ModelInfo> modelInfos = new ArrayList<>();
                for (CtInvocation<?> invocation : invocations) {
                    CtExecutableReference<?> execRef = invocation.getExecutable();
                    List<CtTypeReference<?>> paramTypes = execRef.getParameters();

                    StringBuilder paramKey = new StringBuilder();
                    for (CtTypeReference<?> paramType : paramTypes) {
                        paramKey.append(paramType.getQualifiedName().replace(".", "_")).append("_");
                    }

                    String modelName = execRef.getSimpleName() + "_" + (paramKey.length() == 0 ? "_" : paramKey.toString());

                    if (!modelsAvailable.contains(modelName)) continue; // ignore methods that are not trained

                    ModelInfo modelInfo = new ModelInfo(modelName);
                    getFeaturesForTool(modelInfo, invocation);

                    int inputNum = addInput0AsTargetIfExists(invocation, modelInfo, methodName);

                    handleMethodArgs(invocation, modelInfo, methodName, inputNum);

                    modelInfos.add(modelInfo);
                    methodEnergyInfo.addModelInfo(modelInfo);
                }
                methodsEnergyInfo.add(methodEnergyInfo);
            }
        }

        return methodsEnergyInfo;
    }

    private void handleMethodArgs(CtInvocation<?> invocation, ModelInfo modelInfo, String methodContext, int inputNum) {
        List<CtExpression<?>> arguments = invocation.getArguments();
        for (int i = 0; i < arguments.size(); i++) {
            CtExpression<?> arg = arguments.get(i);
            if (arg instanceof CtVariableRead) {
                String id = "Method: " + methodContext + " Variable: " + arg.toString();
                modelInfo.addId(id);
                modelInfo.associateInputToVar("input" + (i + inputNum), id);
            }
        }
    }

    // if method call is like this -> list.add(i) then input0 is list and input1 is
    // i
    // if method call is like this -> Math.cos(i) , then input0 is i
    // so this method return 0 if the input of the first arg will be 0 and 1 if the
    // input of the first arg is 1
    private int addInput0AsTargetIfExists(CtInvocation<?> invocation, ModelInfo modelInfo, String methodContext) {
        if (invocation.getTarget() == null) return 0;
        String id = "Method: " + methodContext + " Variable: " + invocation.getTarget();
        modelInfo.addId(id);
        modelInfo.associateInputToVar("input0", id);
        return 1;
    }

    private void getFeaturesForTool(ModelInfo modelInfo, CtInvocation<?> invocation) {

        CtExecutableReference<?> execRef = invocation.getExecutable();

        // Get type of the target (e.g., the list)
        CtExpression<?> target = invocation.getTarget();
        CtTypeReference<?> targetType = target.getType();
        String colType = targetType.getQualifiedName();
        System.err.println("Target type: " + targetType.getQualifiedName());

        // Get the full method signature
        String methodType = execRef.getDeclaringType().getQualifiedName() + execRef.getSignature();
        System.err.println("Method: " + methodType);

        // Get the argument types
        StringBuilder sb = new StringBuilder();
        if (!invocation.getArguments().isEmpty()) {
            List<CtExpression<?>> methodArgs = invocation.getArguments();
            for (int i = 0; i < methodArgs.size(); i++) {

                CtExpression<?> arg = invocation.getArguments().get(i);
                CtTypeReference<?> argType = arg.getType();
                System.err.println("Argument type: " + argType.getQualifiedName());
                if (i > 0) {
                    sb.append(" | ");
                }
                sb.append(argType.getQualifiedName());
            }
        }
        modelInfo.setColType(colType);
        modelInfo.setMethodType(methodType);
        modelInfo.setArgs(sb.toString());
        System.err.println("----");
        // return modelInfo;
    }


    public void methodsUsageCounter(HashMap<String,HashMap<String,Object>> existingMethods) {
        HashMap<String,CtMethod<?>> methodsMap = getAllMethodNames();
        ArrayList<String> methods = new ArrayList<>(methodsMap.keySet()); 
        HashSet<String> visited = new HashSet<>();
        HashMap<String,Integer> counter = new HashMap<>();
        for (String exploringMethodName : methods) {
            methodRecursiveCounter(existingMethods,visited, methodsMap.get(exploringMethodName),exploringMethodName,methodsMap,counter);
        }
        System.out.println("final counter -> "+counter);
    }

    private void methodRecursiveCounter(
        HashMap<String,HashMap<String,Object>> existingMethods,
        HashSet<String> visited, 
        CtMethod<?> method,
        String exploringMethod,
        HashMap<String,CtMethod<?>> methodsMap,
        HashMap<String,Integer> counter) 
        {
            System.out.println("ja tenhos estes -> "+counter);
            counter.merge(exploringMethod, 1, Integer::sum);
            System.out.println("aumentei para "+exploringMethod);
        if (visited.contains(exploringMethod)) return;
        visited.add(exploringMethod);
        
        List<CtInvocation<?>> invocations = method.getElements(new TypeFilter<>(CtInvocation.class));
        for (CtInvocation<?> invocation : invocations) {
            String newExploreMethod = buildInvocationString(invocation);
            System.err.println("invocation -> "+newExploreMethod + " para existingMethods.containsKey(newExploreMethod) -> "+existingMethods.containsKey(newExploreMethod));

            //if (existingMethods.containsKey(newExploreMethod)){
            //    methodRecursiveCounter(existingMethods, visited, methodsMap.get(newExploreMethod), newExploreMethod,methodsMap,counter);  
            //} else continue;
            if (!existingMethods.containsKey(newExploreMethod)) continue; // only stop if i find a method call for a method i have
            methodRecursiveCounter(existingMethods, visited, methodsMap.get(newExploreMethod), newExploreMethod,methodsMap,counter);
        }
    }

    private HashMap<String,CtMethod<?>> getAllMethodNames() {
        HashMap<String,CtMethod<?>> methods = new HashMap<>();
        for (CtType<?> ctType : model.getAllTypes()) {
            if (!ctType.getSimpleName().equals(file))continue; //only target class file, ignore other files
            for (CtMethod<?> method : ctType.getMethods()) {
                methods.put(getMethodName(ctType, method), method);
            }
        }
        return methods;
    }

    //private String getMethodName(CtType<?> ctType, CtMethod<?> method) {
    //    return ctType.getSimpleName() + "." + method.getSimpleName();
    //}

    private String getMethodName(CtType<?> ctType, CtMethod<?> method) {
        String className = ctType.getQualifiedName();
        String methodName = method.getSimpleName();

        String paramTypes = method.getParameters().stream()
            .map(param -> param.getType().getQualifiedName())
            .collect(Collectors.joining(", "));

        return className + "." + methodName + "(" + paramTypes + ")";
    }

    private String buildInvocationString(CtInvocation<?> invocation) {
       CtExecutableReference<?> execRef = invocation.getExecutable();

        // Step 1: Get declaring class
        String className = (execRef.getDeclaringType() != null)
            ? execRef.getDeclaringType().getQualifiedName()
            : "UNKNOWN_CLASS";

        // Step 2: Get method name
        String methodName = execRef.getSimpleName();

        // Step 3: Get parameter types
        String paramTypes = execRef.getParameters().stream()
            .map(p -> p.getQualifiedName()) // fully qualified type name
            .collect(Collectors.joining(", "));

        // Final result
        String fullyQualifiedCall = className + "." + methodName + "(" + paramTypes + ")";
        return fullyQualifiedCall;
    }
}
