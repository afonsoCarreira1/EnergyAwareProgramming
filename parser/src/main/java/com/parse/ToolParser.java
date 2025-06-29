package com.parse;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

public class ToolParser {

    CtModel model;
    String file;
    String path;

    public ToolParser(CtModel model, String file, String path) {
        this.model = model;
        this.file = file;
        this.path = path;
    }


    public List<MethodEnergyInfo> getMethodsForSliders(HashSet<String> modelsAvailable) {
        List<MethodEnergyInfo> methodsEnergyInfo = new ArrayList<>();
        

        for (CtType<?> ctType : model.getAllTypes()) {
            //if (!ctType.getSimpleName().equals(file))continue; //only target class file, ignore other files
            for (CtMethod<?> method : ctType.getMethods()) {
                String methodName = getMethodName(ctType, method);
                MethodEnergyInfo methodEnergyInfo = new MethodEnergyInfo(methodName,Paths.get(path,file+".java"));
                List<CtInvocation<?>> invocations = method.getElements(new TypeFilter<>(CtInvocation.class));
                
                List<ModelInfo> modelInfos = new ArrayList<>();
                for (CtInvocation<?> invocation : invocations) {
                    CtExecutableReference<?> execRef = invocation.getExecutable();
                    List<CtTypeReference<?>> paramTypes = execRef.getParameters();

                    StringBuilder paramKey = new StringBuilder();
                    for (CtTypeReference<?> paramType : paramTypes) {
                        paramKey.append(paramType.getQualifiedName().replace(".", "_")).append("_");
                    }

                    String declaringType = execRef.getDeclaringType() != null ? execRef.getDeclaringType().getSimpleName() : "UnknownType";

                    String modelName = declaringType+"_"+execRef.getSimpleName() + "_" + (paramKey.length() == 0 ? "_" : paramKey.toString().replace("$", "_"));
                    System.err.println("modelName -> "+modelName);
                    boolean isModelMethod = modelsAvailable.contains(modelName);
                    //if (!modelsAvailable.contains(modelName)) continue; // ignore methods that are not trained
                    
                    int loops = 0;

                    //se for um metodo do user quero o guardar ate agora e dar skip ao resto
                    if (invocation.getExecutable().getDeclaration() != null) {
                        System.err.println("from "+ methodName+ " called "+invocation.getExecutable().getSimpleName());
                        methodsEnergyInfo.add(methodEnergyInfo);
                        ModelInfo modelInfo = new ModelInfo(getMethodNameFromInvocation(invocation),invocation.toString());
                        modelInfo.setMethodCall(true);
                        modelInfo.setLine(invocation.getPosition().getLine());
                        loops = countEnclosingLoops(invocation,modelInfo,methodName);
                        modelInfos.add(modelInfo);
                        methodEnergyInfo.addModelInfo(modelInfo);
                        continue;
                    }
                    if (!isModelMethod) continue; // ignore methods that are not trained
                    
                    ModelInfo modelInfo = new ModelInfo(modelName,invocation.toString());
                    loops = countEnclosingLoops(invocation,modelInfo,methodName);
                    System.err.println("Loops around " + modelName + " -> "+loops);
                    getFeaturesForTool(modelInfo, invocation);

                    int inputNum = addInput0AsTargetIfExists(invocation, modelInfo, methodName);

                    handleMethodArgs(invocation, modelInfo, methodName, inputNum);

                    modelInfo.setLine(invocation.getPosition().getLine());
                    modelInfos.add(modelInfo);
                    methodEnergyInfo.addModelInfo(modelInfo);
                }
                methodsEnergyInfo.add(methodEnergyInfo);
            }
        }

        return methodsEnergyInfo;
    }

    //Faster but will not find loops when calling methods
    private int countEnclosingLoops(CtInvocation<?> invocation, ModelInfo modelInfo, String methodName) {
        int loopCount = 0;
        CtStatement parent = invocation.getParent(CtStatement.class);
        while (parent != null) {
            //System.out.println("parent -> "+parent);
            if (parent instanceof CtLoop) {
                String id = getIdForLoop(invocation,methodName, loopCount,modelInfo.getModelName());
                modelInfo.addId(id);
                modelInfo.addLoopId(id);
                loopCount++;
            }
            parent = parent.getParent(CtStatement.class);
        }
        return loopCount;
    }

    private String getIdForLoop(CtInvocation<?> invocation, String methodName,int loopCount,String methodCallName) {
        return "Method: "+methodName + " | loopDepth " + loopCount + " | calledMethod: "+ methodCallName +" | Line: " + invocation.getPosition().getLine();
    }

    private String getId(CtInvocation<?> invocation, String methodName,String variable) {
        return "Method: " + methodName + " | Variable: " + variable;
    }


    private void handleMethodArgs(CtInvocation<?> invocation, ModelInfo modelInfo, String methodContext, int inputNum) {
        List<CtExpression<?>> arguments = invocation.getArguments();
        for (int i = 0; i < arguments.size(); i++) {
            CtExpression<?> arg = arguments.get(i);
            if (arg instanceof CtVariableRead) {//test(var)
                String id = getId(invocation,methodContext, arg.toString());
                modelInfo.addId(id);
                modelInfo.associateInputToVar("input" + (i + inputNum), id,false);
            } else if (arg instanceof CtInvocation) {//test(fun(10))
                CtInvocation<?> innerInvocation = (CtInvocation<?>) arg;
                String id = getId(invocation, methodContext, innerInvocation.toString());
                modelInfo.addId(id);
                modelInfo.associateInputToVar("input" + (i + inputNum), id,false);
            } else if (arg instanceof CtLiteral) { //test(10) or test("10")
                CtLiteral<?> literal = (CtLiteral<?>) arg;
                Object value = literal.getValue();
                if (value instanceof Integer) {
                    Integer val = (Integer) value;
                    modelInfo.associateInputToVar("input" + (i + inputNum), val+"",true);
                } else if (value instanceof String) {
                    String val = (String) value;
                    modelInfo.associateInputToVar("input" + (i + inputNum), val.length()+"",true);
                } 
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
        if (invocation.getExecutable().isStatic()) return 0;
        String id = getId(invocation,methodContext, invocation.getTarget().toString());
        modelInfo.addId(id);
        modelInfo.associateInputToVar("input0", id,false);
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

    
    public HashMap<String,Map<String,Object>> methodsUsageCounter() {
        HashMap<String,CtMethod<?>> methodsMap = getAllMethodNames();
        ArrayList<String> methods = new ArrayList<>(methodsMap.keySet()); 
        HashMap<String,Integer> counter = new HashMap<>();
        //HashMap<String, List<String>> callGraph = new HashMap<>();
        //Map<String, Integer> indegree = new HashMap<>();
        //HashSet<String> visited = new HashSet<>();
        HashMap<String,Map<String,Object>> savedMethodPaths = new HashMap<>();
        for (String exploringMethodName : methods) {
            HashSet<String> visited = new HashSet<>();
            HashMap<String, List<String>> callGraph = new HashMap<>();
            Map<String, Integer> indegree = new HashMap<>();
            indegree.putIfAbsent(exploringMethodName, 0);
            methodRecursiveCounter(visited, methodsMap.get(exploringMethodName),exploringMethodName,methodsMap,counter,callGraph,indegree);
            savedMethodPaths.put(exploringMethodName, Map.of("callGraph",callGraph,"indegree",indegree));
        }
        //System.err.println("callGraph -> " + callGraph);
        //System.err.println("indegree -> " + indegree);
        //Map<String,Object> m = Map.of("callGraph",callGraph,"indegree",indegree);
        //return m;
        return savedMethodPaths;
    }

    private void methodRecursiveCounter(
        HashSet<String> visited, 
        CtMethod<?> method,
        String exploringMethod,
        HashMap<String,CtMethod<?>> methodsMap,
        HashMap<String,Integer> counter,
        HashMap<String, List<String>> callGraph,
        Map<String, Integer> indegree
        )
        {
        counter.merge(exploringMethod, 1, Integer::sum);
        if (visited.contains(exploringMethod)) return;
        visited.add(exploringMethod);
        List<CtInvocation<?>> invocations = method.getElements(new TypeFilter<>(CtInvocation.class));
        for (CtInvocation<?> invocation : invocations) {
            String newExploreMethod = getMethodNameFromInvocation(invocation);
            if (invocation.getExecutable().getDeclaration() == null) continue; //it means the methodCall is not from my code
            callGraph.computeIfAbsent(exploringMethod, k -> new ArrayList<>()).add(newExploreMethod);
            indegree.putIfAbsent(newExploreMethod, 0);
            indegree.putIfAbsent(exploringMethod, 0);
            indegree.put(newExploreMethod, indegree.get(newExploreMethod) + 1);
            methodRecursiveCounter(visited, methodsMap.get(newExploreMethod), newExploreMethod,methodsMap,counter,callGraph,indegree);
        }
    }

    private HashMap<String,CtMethod<?>> getAllMethodNames() {
        HashMap<String,CtMethod<?>> methods = new HashMap<>();
        for (CtType<?> ctType : model.getAllTypes()) {
            //if (!ctType.getSimpleName().equals(file))continue; //only target class file, ignore other files
            for (CtMethod<?> method : ctType.getMethods()) {
                methods.put(getMethodName(ctType, method), method);
            }
        }
        return methods;
    }

    private String getMethodName(CtType<?> ctType, CtMethod<?> method) {
        String className = ctType.getQualifiedName();
        String methodName = method.getSimpleName();

        String paramTypes = method.getParameters().stream()
            .map(param -> param.getType().getQualifiedName())
            .collect(Collectors.joining(", "));

        return className + "." + methodName + "(" + paramTypes + ")";
    }

    private String getMethodNameFromInvocation(CtInvocation<?> invocation) {
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
