package com.tool;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.parse.ModelInfo;
import com.parse.MethodEnergyInfo;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class CalculateEnergy {

    public static List<MethodEnergyInfo> methodsEnergyInfo = new ArrayList<>();
    public static Map<String,Double> methodsEnergyForContainer = new HashMap<>(); 

    public static double calculateEnergy(String modelPath) {
        double totalEnergyUsed = 0;
        for (MethodEnergyInfo methodEnergyInfo : methodsEnergyInfo) {
            double totalMethodEnergy = 0.0;
            ArrayList<ModelInfo> modelInfos = methodEnergyInfo.getModelInfos();
            for (ModelInfo modelInfo : modelInfos) {
                String expression = getModelExpression(modelPath + modelInfo.getModelName() + ".csv");
                System.err.println("Using Expression -> " + expression + " for model " + modelInfo.getModelName());
                // System.err.println("I have this inputs -> "+modelInfo.getInputToVarName());
                expression = replaceExpressionInputsWithValues(modelInfo, expression);
                expression = replaceExpressionWithFeatures(modelInfo, expression);
                Expression expressionEvaluated = new ExpressionBuilder(expression).build();
                totalMethodEnergy += accountForLoops(modelInfo.getLoopIds(), expressionEvaluated.evaluate());// expressionEvaluated.evaluate();
                System.err.println("New expression -> " + expression);
                System.err.println("Energy for this was -> " + expressionEvaluated.evaluate());
                System.err.println("------------");
            }
            methodEnergyInfo.setTotalEnergy(totalMethodEnergy);
            totalEnergyUsed += totalMethodEnergy;
        }

        System.err.println("total energy used was -> " + totalEnergyUsed + "J");
        double e = countMethodsUsedEnergy();
        
        System.err.println(e);
        return e; /* totalEnergyUsed + */ // countMethodsUsedEnergy();
    }

    public static Map<String, Integer> computeMethodCalls(
            Map<String, List<String>> callGraph,
            Map<String, Integer> indegree) {

        Map<String, Integer> callCount = new HashMap<>();

        Queue<String> queue = new LinkedList<>();
        for (String method : indegree.keySet()) {
            if (indegree.get(method) == 0) {
                callCount.put(method, 1);
                queue.add(method);
            }
        }

        while (!queue.isEmpty()) {
            String current = queue.poll();
            int currentCalls = callCount.get(current);

            List<String> children = callGraph.getOrDefault(current, Collections.emptyList());
            Map<String, Integer> childFrequency = new HashMap<>();

            for (String child : children) {
                childFrequency.put(child, childFrequency.getOrDefault(child, 0) + 1);
            }

            for (Map.Entry<String, Integer> entry : childFrequency.entrySet()) {
                String child = entry.getKey();
                int timesCalled = entry.getValue();
                int totalCalls = currentCalls * timesCalled;

                callCount.put(child, callCount.getOrDefault(child, 0) + totalCalls);
                indegree.put(child, indegree.get(child) - timesCalled);

                if (indegree.get(child) == 0) {
                    queue.add(child);
                }
            }
        }

        return callCount;
    }

    public static double countMethodsUsedEnergy() {
        methodsEnergyForContainer = new HashMap<>();
        double totalEnergy = 0.0;
        if (Tool.parser == null) return totalEnergy;
        HashMap<String, Map<String, Object>> savedMethodPaths = Tool.parser.getToolParser().methodsUsageCounter();
        for (String methodName : savedMethodPaths.keySet()) {
            HashMap<String, List<String>> callGraph = (HashMap<String, List<String>>) savedMethodPaths.get(methodName).get("callGraph");
            Map<String, Integer> indegree = (Map<String, Integer>) savedMethodPaths.get(methodName).get("indegree");
            System.err.println("---------------------------------");
            System.err.println("Method: " + methodName);
            System.err.println("callGraph -> " + callGraph + "\nindegree -> " + indegree);
            //Map<String, Map<String, Integer>> countedGraph = countCalls(callGraph);
            Map<String, Integer> m = computeMethodCalls(callGraph,indegree);
            System.err.println("compute .> "+m);
            System.err.println("method " + methodName + " uses methods-> " + calculateTopologicSort(callGraph, indegree));
            double methodEnergy = sumUserMethods2(methodName,m,calculateTopologicSort(callGraph, indegree));
            //System.err.println("method " + methodName + " uses methods-> " + calculateTopologicSort(countedGraph, indegree));
            //double methodEnergy = sumUserMethods(calculateTopologicSort(countedGraph, indegree));
            methodsEnergyForContainer.put(methodName, methodEnergy);
            System.err.println("ENERGY -> "+methodEnergy);
            totalEnergy += methodEnergy;
            
        }

        return totalEnergy;

    }

    private static double sumUserMethods2(String methodName, Map<String, Integer> methodCalls, Map<String, Double> energy) {
        double totalEnergy = 0.0;
        for (String methodUsed : methodCalls.keySet() ) {
            System.err.println("methodUsed: " + methodUsed + " energy: " + energy.get(methodUsed) + " methodCalls: "+methodCalls.get(methodUsed));
            totalEnergy += energy.get(methodUsed) * methodCalls.get(methodUsed);
        }
        return totalEnergy;
    }

    private static double sumUserMethods(Map<String, Double> energy) {
        double totalEnergy = 0.0;
        for (String methodUsed : energy.keySet() ) {
            totalEnergy += energy.get(methodUsed);
        }
        return totalEnergy;
    }

    private static Map<String, Double> calculateTopologicSort(HashMap<String, List<String>> callGraph, Map<String, Integer> indegree) {
        Map<String, Double> baseEnergy = getMethodsEnergy(indegree);

        Map<String, List<String>> reverseGraph = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : callGraph.entrySet()) {
            String caller = entry.getKey();
            for (String callee : entry.getValue()) {
                reverseGraph.computeIfAbsent(callee, k -> new ArrayList<>()).add(caller);
            }
        }

        Map<String, Integer> indegreeCopy = new HashMap<>(indegree);

        Map<String, Double> totalEnergy = new HashMap<>();


        Queue<String> queue = new LinkedList<>();
        for (String method : indegreeCopy.keySet()) {
            if (indegreeCopy.get(method) == 0) {
                queue.offer(method);
                totalEnergy.put(method, baseEnergy.getOrDefault(method, 0.0));
            }
        }

        while (!queue.isEmpty()) {
            String method = queue.poll();
            double methodEnergy = totalEnergy.getOrDefault(method, baseEnergy.getOrDefault(method, 0.0));

            for (String caller : reverseGraph.getOrDefault(method, Collections.emptyList())) {

                double prevEnergy = totalEnergy.getOrDefault(caller, baseEnergy.getOrDefault(caller, 0.0));
                //totalEnergy.put(caller, prevEnergy + methodEnergy);


                indegreeCopy.put(caller, indegreeCopy.get(caller) - 1);
                if (indegreeCopy.get(caller) == 0) {
                    queue.offer(caller);
                }
            }
        }

        for (String method : baseEnergy.keySet()) {
            totalEnergy.putIfAbsent(method, baseEnergy.get(method));
        }

        //for (String methodName : totalEnergy.keySet()) {
        //    int methodCalls = indegree.get(methodName);
        //    if (methodCalls > 1) totalEnergy.put(methodName, totalEnergy.get(methodName)*methodCalls);
        //}

        return totalEnergy;
    }


    private static double accountForLoops(ArrayList<String> loopIds, double energy) {
        for (String loopId : loopIds) {
            energy *= (Integer) Sliders.sliders.get(loopId).get("val");
        }
        return energy;
    }

    // loop all inputs, get their var names, go to the sliders, get their current
    // values, and replace them in the expression
    private static String replaceExpressionInputsWithValues(ModelInfo modelInfo, String expression) {
        ArrayList<String> inputs = new ArrayList<>(modelInfo.getInputToVarName().keySet());
        for (String input : inputs) {
            String varName = modelInfo.getInputToVarName().get(input);
            Integer inputVal = (Integer) Sliders.sliders.get(varName).get("val");
            expression = expression.replaceAll(input, inputVal + "");
        }
        return expression;
    }

    private static String replaceExpressionWithFeatures(ModelInfo modelInfo, String expression) {
        System.err.println(
                "Before -> " + modelInfo.getColType() + " " + modelInfo.getMethodType() + " " + modelInfo.getArgs());
        String cleanedColType = modelInfo.getColType().replaceAll("\\.", "");
        String cleanedMethodType = modelInfo.getMethodType().replaceAll("\\.|\\(|\\)", "");// remove . ( )
        String cleanedArgs = modelInfo.getArgs().replaceAll("\\.|\\|", "");// remove . |
        System.err.println("After -> " + cleanedColType + " " + cleanedMethodType + " " + cleanedArgs);
        expression = expression.replaceAll(cleanedColType, "1");
        expression = expression.replaceAll(cleanedMethodType, "1");
        if (!cleanedArgs.isEmpty())
            expression = expression.replaceAll(cleanedArgs, "1"); // TODO isto esta mal, se tiver mais q um arg da erro
                                                                  // pq vai ter Double | Integer e dps n da replace

        // this final part replaces the features needed by the expression, that the code
        // does not have by 0.
        String allowedFunctions = "log|sin|cos|tan|sqrt|exp|abs|min|max|pow";
        expression = expression.replaceAll("\\b(?!(" + allowedFunctions + ")\\b)[a-zA-Z_][a-zA-Z_0-9]*\\b", "0");

        return expression;
    }

    public static Map<String, Double> getMethodsEnergy() {
        return methodsEnergyForContainer;
    }

    public static Map<String, Double> getMethodsEnergy(Map<String, Integer> indegree) {
        HashMap<String, Double> methodsEnergy = new HashMap<>();
        for (MethodEnergyInfo methodEnergyInfo : methodsEnergyInfo) {
            String methodName = methodEnergyInfo.getMethodName();
            if (indegree.containsKey(methodName))
            methodsEnergy.put(methodName, methodEnergyInfo.getTotalEnergy());
        }
        return methodsEnergy;
    }

    private static String getModelExpression(String modelPath) {
        String expression = "";
        String filePath = modelPath;
        String line;
        String[] headers = null;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            // Read the first line (headers)
            if ((line = br.readLine()) != null) {
                headers = line.split(",");
            }

            // Find the indices for "complexity" and "equation"
            int complexityIndex = -1;
            int expressionIndex = -1;

            for (int i = 0; i < headers.length; i++) {
                if (headers[i].trim().equalsIgnoreCase("complexity")) {
                    complexityIndex = i;
                }
                if (headers[i].trim().equalsIgnoreCase("equation")) {
                    expressionIndex = i;
                }
            }

            if (complexityIndex == -1 || expressionIndex == -1) {
                System.err.println("Required columns not found.");
                return "";
            }

            // Read each row
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length > complexityIndex && values[complexityIndex].trim().equals("5")
                        || values.length > complexityIndex && values[complexityIndex].trim().equals("6")) {
                    expression = values[expressionIndex].replaceAll("\"", "");
                    break;
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return expression;
    }

}
