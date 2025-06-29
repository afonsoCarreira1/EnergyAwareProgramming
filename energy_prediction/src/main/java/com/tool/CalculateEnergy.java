package com.tool;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
    public static HashMap<String,String> lineExpressions = new HashMap<>();
    public static HashMap<String, HashMap<String,String>> mostEnergyExpensiveLines = new HashMap<>();

    public static double calculateEnergy(String modelPath) {
        //System.err.println("----------------------------");
        //System.err.println("vou calcular para estes metodos: ");
        lineExpressions.clear();
        mostEnergyExpensiveLines.clear();
        //double totalEnergyUsed = 0;
        HashMap<String,HashSet<String>> methodsLoops = new HashMap<>();
        for (MethodEnergyInfo methodEnergyInfo : methodsEnergyInfo) {
            System.err.println("----------------------------------");
            System.err.println("method: "+methodEnergyInfo.getMethodName());
            double totalMethodEnergy = 0.0;
            ArrayList<ModelInfo> modelInfos = methodEnergyInfo.getModelInfos();
            for (ModelInfo modelInfo : modelInfos) {
                if (modelInfo.isMethodCall()) continue;
                String expression = getModelExpression(modelPath + modelInfo.getModelName() + ".csv",8,9);
                //System.err.println("Using Expression -> " + expression + " for model " + modelInfo.getModelName());
                // System.err.println("I have this inputs -> "+modelInfo.getInputToVarName());
                Map<String, String> expressions = replaceExpressionInputsWithValues(modelInfo, expression);
                expression = expressions.get("expression"); 
                expression = replaceExpressionWithFeatures(modelInfo, expression);
                Expression expressionEvaluated = new ExpressionBuilder(expression).build();
                modelInfo.setExpression(expressions.get("expressionUi"));
                lineExpressions.put(modelInfo.toString() + " | "+modelInfo.getLine(), modelInfo.getExpression());
                double currentEnergy = accountForLoops(modelInfo.getLoopIds(), expressionEvaluated.evaluate());
                totalMethodEnergy += currentEnergy;// expressionEvaluated.evaluate();
                System.err.println("New expression -> " + expression);
                System.err.println("Energy for this was -> " + expressionEvaluated.evaluate());
                System.err.println("Energy for this after loops -> " + currentEnergy);
                //System.err.println("------------");
                insertIfHigher(methodEnergyInfo, currentEnergy,modelInfo.getLine());
            }
            totalMethodEnergy = capEnergy(totalMethodEnergy);
            methodEnergyInfo.setTotalEnergy(totalMethodEnergy);
            //totalEnergyUsed += totalMethodEnergy;
            System.err.println("energia final metodos -> "+methodEnergyInfo.getTotalEnergy());
        }
        addMethodLoops(methodsLoops);
        //System.err.println("Final loops -> "+methodsLoops);
        //System.err.println("total energy used was -> " + totalEnergyUsed + "J");
        double e = countMethodsUsedEnergy(methodsLoops);
        System.err.println("e -> "+e);
        //System.err.println("energia final -> "+e);
        //System.err.println("mostEnergyExpensiveLines -> "+mostEnergyExpensiveLines);
        e = capEnergy(e);
        return e; /* totalEnergyUsed + */ // countMethodsUsedEnergy();
    }

    //avoid infinity energy
    private static double capEnergy(double energy) {
        return energy > 10_000_000 ? 10_000_000 : energy;
    }

    private static void addMethodLoops(HashMap<String,HashSet<String>> methodsLoops) {
        for (MethodEnergyInfo methodEnergyInfo : methodsEnergyInfo){
              for (ModelInfo modelInfo : methodEnergyInfo.getModelInfos()){
                if (modelInfo.getLoopIds() == null || modelInfo.getLoopIds().isEmpty()) continue;
                //System.err.println("Vou guardar para o "+methodEnergyInfo.getMethodName() +" os ids "+modelInfo.getLoopIds().toString());
                String key = methodEnergyInfo.getMethodName();
                if(methodsLoops.containsKey(key)) {
                    HashSet<String> loops = methodsLoops.get(key);
                    loops.addAll(modelInfo.getLoopIds());
                    methodsLoops.put(key, loops);
                } else {
                    methodsLoops.put(key, new HashSet<String>(modelInfo.getLoopIds())); 
                }
              }  
        }
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

    @SuppressWarnings("unchecked")
    public static double countMethodsUsedEnergy(Map<String,HashSet<String>> methodsLoops) {
        methodsEnergyForContainer = new HashMap<>();
        double totalEnergy = 0.0;
        if (Tool.parser == null) return totalEnergy;
        HashMap<String, Map<String, Object>> savedMethodPaths = Tool.parser.getToolParser().methodsUsageCounter();
        //System.err.println("savedMethodPaths -> "+savedMethodPaths);
        for (String methodName : savedMethodPaths.keySet()) {
            HashMap<String, List<String>> callGraph = (HashMap<String, List<String>>) savedMethodPaths.get(methodName).get("callGraph");
            Map<String, Integer> indegree = (Map<String, Integer>) savedMethodPaths.get(methodName).get("indegree");
            //System.err.println("---------------------------------");
            //System.err.println("Method: " + methodName);
            //System.err.println("callGraph -> " + callGraph + "\nindegree -> " + indegree);
            //Map<String, Map<String, Integer>> countedGraph = countCalls(callGraph);
            Map<String, Integer> m = computeMethodCalls(callGraph,indegree);
            //System.err.println("compute .> "+m);
            //System.err.println("method " + methodName + " uses methods-> " + calculateTopologicSort(callGraph, indegree));
            double methodEnergy = sumUserMethods(methodName,m,calculateTopologicSort(callGraph, indegree),methodsLoops.get(methodName));
            //System.err.println("method " + methodName + " uses methods-> " + calculateTopologicSort(countedGraph, indegree));
            //double methodEnergy = sumUserMethods(calculateTopologicSort(countedGraph, indegree));
            methodsEnergyForContainer.put(methodName, methodEnergy);
            //System.err.println("ENERGY -> "+methodEnergy);
            totalEnergy += methodEnergy;    
        }

        return totalEnergy;

    }

    private static double sumUserMethods(String methodName, Map<String, Integer> methodCalls, Map<String, Double> energy, HashSet<String> loopIds) {
        double totalEnergy = 0.0;
        HashMap<String,Integer> numberOfMethodCallsInLoopForMethod = new HashMap<>();
        
        MethodEnergyInfo mei = null;
        for (MethodEnergyInfo mei2 : methodsEnergyInfo) {
            if (mei2.getMethodName().equals(methodName)) {
                mei=mei2;
                break;
            }
        }
        HashMap<String,Integer> methodUsedLine = getCallsLine(mei);
        
        //System.err.println("---------------------");
        //System.err.println("summing method: "+mei.getMethodName());
        //System.err.println("Methods used: "+methodCalls);
        //System.err.println("Energies data: "+energy);
            for (ModelInfo mi : mei.getModelInfos()){
                if (!mi.isMethodCall()) continue;
                String methodCalled = mi.getModelName();
                //System.err.println("first call is: "+methodCalled);
                if (mi.getLoopIds() != null && !mi.getLoopIds().isEmpty()) {
                    int loopsMultiplied = 1;
                    for (String loopId : mi.getLoopIds()) {
                        //System.err.println("loop: "+loopId);
                        loopsMultiplied *= (Integer) Sliders.sliders.get(loopId).get("val");
                    }
                    totalEnergy += energy.get(methodCalled) * loopsMultiplied;
                    if (methodUsedLine.containsKey(methodCalled)) insertIfHigher(mei, totalEnergy, methodUsedLine.get(methodCalled));
                    //System.err.println("found "+loopsMultiplied +" loops energy = "+totalEnergy);
                    numberOfMethodCallsInLoopForMethod.put(methodCalled, numberOfMethodCallsInLoopForMethod.getOrDefault(methodCalled, 0) + 1);
                }
                
            }
            
            for (String methodUsed : methodCalls.keySet()) {    
                int dif = methodCalls.get(methodUsed) - numberOfMethodCallsInLoopForMethod.getOrDefault(methodUsed,0);
                if (dif > 0) totalEnergy += energy.get(methodUsed) * dif;
                //System.err.println(methodName + " uses "+methodUsed+ " -> " +methodUsedLine);
                if (methodUsedLine.containsKey(methodUsed)) insertIfHigher(mei, totalEnergy, methodUsedLine.get(methodUsed));
                //System.err.println("dif -> "+dif +" | "+methodCalls.get(methodUsed) +" | "+ numberOfMethodCallsInLoopForMethod.get(methodUsed));
            }
            //System.err.println("total energy: "+totalEnergy);
        return totalEnergy;
    }

    private static HashMap<String, Integer> getCallsLine(MethodEnergyInfo mei) {
        HashMap<String, Integer> m = new HashMap<>();
        for (ModelInfo mi : mei.getModelInfos()) {
            m.put(mi.getModelName(), mi.getLine());
        }
        return m;
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

            for (String caller : reverseGraph.getOrDefault(method, Collections.emptyList())) {


                indegreeCopy.put(caller, indegreeCopy.get(caller) - 1);
                if (indegreeCopy.get(caller) == 0) {
                    queue.offer(caller);
                }
            }
        }

        for (String method : baseEnergy.keySet()) {
            totalEnergy.putIfAbsent(method, baseEnergy.get(method));
        }

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
    private static Map<String,String> replaceExpressionInputsWithValues(ModelInfo modelInfo, String expression) {
        String expressionForUi = expression;
        System.err.println("model -> "+modelInfo.getModelName());
        ArrayList<String> inputs = new ArrayList<>(modelInfo.getInputToVarName().keySet());
        System.err.println("all -> "+modelInfo.getInputToVarName());
        for (String input : inputs) {
            System.err.println("input -> "+input);
            String varName = (String) modelInfo.getInputToVarName().get(input).get("input");
            boolean bool = (Boolean) modelInfo.getInputToVarName().get(input).get("isLiteral");
            Integer inputVal = 0;
            System.err.println("varname -> "+varName);
            if (bool) inputVal = Integer.parseInt(varName);
            else inputVal = (Integer) Sliders.sliders.get(varName).get("val");
            expression = expression.replaceAll(input, inputVal + "");

            if (!bool) expressionForUi=expressionForUi.replaceAll(input, varName.split(" \\| ")[1]);
            else expressionForUi=expressionForUi.replaceAll(input, varName);
        }
        return Map.of("expression",expression,"expressionUi",expressionForUi);
    }

    private static String replaceExpressionWithFeatures(ModelInfo modelInfo, String expression) {
        //System.err.println("Before -> " + modelInfo.getColType() + " " + modelInfo.getMethodType() + " " + modelInfo.getArgs());
        String cleanedColType = modelInfo.getColType().replaceAll("\\.", "");
        String cleanedMethodType = modelInfo.getMethodType().replaceAll("\\.|\\(|\\)|,", "");// remove . ( )
        String cleanedArgs = modelInfo.getArgs().replaceAll("\\.|\\|", "");// remove . |
        //System.err.println("After -> " + cleanedColType + " " + cleanedMethodType + " " + cleanedArgs);
        //System.err.println("joined -> "+cleanedMethodType);
        expression = expression.replaceAll(cleanedMethodType, "1");
        expression = expression.replaceAll(cleanedColType, "1");
        
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

    private static void insertIfHigher(MethodEnergyInfo method, double energy, Integer line) {
        if (!mostEnergyExpensiveLines.containsKey(method.getMethodName())) {
            HashMap<String,String> m = new HashMap<>();
            m.put("Energy", energy+"");
            m.put("Line", line+"");
            m.put("Uri", method.getUri());
            mostEnergyExpensiveLines.put(method.getMethodName(), m);
            return;
        }
        HashMap<String,String> info = mostEnergyExpensiveLines.get(method.getMethodName());
        double e = Double.parseDouble(info.get("Energy"));
        if (energy <= e) return;
        info.put("Energy", e + "");
        mostEnergyExpensiveLines.put(method.getMethodName(), info);
    }

    private static String getModelExpression(String modelPath, int targetComplexity, int backupComplexity) {
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
                if (values.length > complexityIndex && values[complexityIndex].trim().equals(targetComplexity+"")
                        || values.length > complexityIndex && values[complexityIndex].trim().equals(backupComplexity+"")) {
                    expression = values[expressionIndex].replaceAll("\"", "");
                    break;
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return expression;
    }

    public static Map<String, List<String>> getMostEnergyExpensiveLines() {
        Map<String, List<String>> linesPath = new HashMap<>();
        for (String key : mostEnergyExpensiveLines.keySet()){
            String uri = mostEnergyExpensiveLines.get(key).get("Uri");
            String line = mostEnergyExpensiveLines.get(key).get("Line");
            linesPath.computeIfAbsent(uri, k -> new ArrayList<>()).add(line);
        }
        return linesPath;
    }

}
