package com.parse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import spoon.reflect.CtModel;
import spoon.reflect.code.CtDo;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtFor;
import spoon.reflect.code.CtForEach;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.code.CtWhile;
import spoon.reflect.declaration.CtExecutable;
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
                    System.err.println("Loops around " + modelName + " -> "+countEnclosingLoops(invocation,modelInfo,methodName));
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

    //Faster but will not find loops when calling methods
    private int countEnclosingLoops(CtInvocation<?> invocation, ModelInfo modelInfo, String methodName) {
        int loopCount = 0;
        CtStatement parent = invocation.getParent(CtStatement.class);
        while (parent != null) {
            if (parent instanceof CtLoop) {
                String id = "Method: "+methodName + " loopDepth " + loopCount;
                modelInfo.addId(id);
                modelInfo.addLoopId(id);
                loopCount++;
                
                //List<String> loopVars = getLoopConditionVars((CtLoop) parent);
                //System.err.println("Loop #" + loopCount + " condition vars: " + loopVars);
            }
            parent = parent.getParent(CtStatement.class);
        }
        return loopCount;
    }


    private List<String> getLoopConditionVars(CtLoop loop) {
        List<String> conditionVars = new ArrayList<>();

        if (loop instanceof CtForEach) {
            CtForEach forEach = (CtForEach) loop;
            conditionVars.add(forEach.getExpression().toString());
            return conditionVars;
        }

        CtExpression<Boolean> condition = null;
        if (loop instanceof CtWhile) {
            condition = ((CtWhile) loop).getLoopingExpression();
        } else if (loop instanceof CtFor) {
            condition = ((CtFor) loop).getExpression();
        } else if (loop instanceof CtDo) {
            condition = ((CtDo) loop).getLoopingExpression();
        }

        if (condition != null) {
            // Skip while(true) or while(false)
            if (!(condition instanceof CtLiteral)) {
                Set<CtVariableRead<?>> varReads = new HashSet<>(condition.getElements(new TypeFilter<>(CtVariableRead.class)));
                for (CtVariableRead<?> var : varReads) {
                    conditionVars.add(var.toString());
                }

                Set<CtFieldRead<?>> fieldReads = new HashSet<>(condition.getElements(new TypeFilter<>(CtFieldRead.class)));
                for (CtFieldRead<?> field : fieldReads) {
                    conditionVars.add(field.toString());
                }

                Set<CtLiteral<?>> literals = new HashSet<>(condition.getElements(new TypeFilter<>(CtLiteral.class)));
                for (CtLiteral<?> literal : literals) {
                    conditionVars.add(String.valueOf(literal.getValue()));
                }
            }
        }

        return conditionVars;
    }



    /*might be slower but finds all loops
     n sei quando um metodo é chamado, por exemplo:
     metedo 1 apenas tem loop depth 1
     metedo 2 tem loop depth 2 e chama metedo 1 o que da um total de loop depth 2, mas
     eu nao sei quando vai ser chamado apenas o metodo 1 ou apenas o metodo 2, logo mais
     vale ter apenas a depth que cada metodo tem
     */
    /*private int countEnclosingLoops(CtInvocation<?> invocation, Set<CtExecutable<?>> visited) {
        int loopCount = 0;
        CtStatement parent = invocation.getParent(CtStatement.class);

        // Count lexical loops
        while (parent != null) {
            if (parent instanceof CtLoop) {
                loopCount++;
            }
            parent = parent.getParent(CtStatement.class);
        }

        // Go to enclosing method
        CtExecutable<?> enclosingMethod = invocation.getParent(CtExecutable.class);
        if (enclosingMethod == null || visited.contains(enclosingMethod)) {
            return loopCount;
        }

        visited.add(enclosingMethod);

        // Find all places where this method is called
        Set<CtInvocation<?>> callers = new HashSet<>(invocation.getFactory()
            .getModel()
            .getElements(new TypeFilter<>(CtInvocation.class)));

        int maxCallerLoops = 0;
        for (CtInvocation<?> caller : callers) {
            if (caller.getExecutable().getDeclaration() == enclosingMethod) {
                // Recursively check caller's loop context
                int callerLoops = countEnclosingLoops(caller, new HashSet<>(visited));
                if (callerLoops > maxCallerLoops) {
                    maxCallerLoops = callerLoops;
                }
            }
        }

        return loopCount + maxCallerLoops;
    }*/

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


    public HashMap<String,Integer> methodsUsageCounter() {
        HashMap<String,CtMethod<?>> methodsMap = getAllMethodNames();
        ArrayList<String> methods = new ArrayList<>(methodsMap.keySet()); 
        HashMap<String,Integer> counter = new HashMap<>();
        for (String exploringMethodName : methods) {
            HashSet<String> visited = new HashSet<>();
            methodRecursiveCounter(visited, methodsMap.get(exploringMethodName),exploringMethodName,methodsMap,counter);
        }
        //System.out.println("final counter -> "+counter);
        return counter;
    }

    private void methodRecursiveCounter(
        HashSet<String> visited, 
        CtMethod<?> method,
        String exploringMethod,
        HashMap<String,CtMethod<?>> methodsMap,
        HashMap<String,Integer> counter)
        {
        counter.merge(exploringMethod, 1, Integer::sum);
        if (visited.contains(exploringMethod)) return;
        visited.add(exploringMethod);
        List<CtInvocation<?>> invocations = method.getElements(new TypeFilter<>(CtInvocation.class));
        for (CtInvocation<?> invocation : invocations) {
            String newExploreMethod = buildInvocationString(invocation);
            if (invocation.getExecutable().getDeclaration() == null) continue; //it means the methodCall is not from my code
            methodRecursiveCounter(visited, methodsMap.get(newExploreMethod), newExploreMethod,methodsMap,counter);
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
