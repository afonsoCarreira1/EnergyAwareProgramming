package com.tool;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.parse.ModelInfo;
import com.parse.MethodEnergyInfo;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class CalculateEnergy {

    public static List<MethodEnergyInfo> methodsEnergyInfo = new ArrayList<>();

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
                totalMethodEnergy += expressionEvaluated.evaluate();
                System.err.println("New expression -> " + expression);
                System.err.println("Energy for this was -> " + expressionEvaluated.evaluate());
                System.err.println("------------");
            }
            methodEnergyInfo.setTotalEnergy(totalMethodEnergy);
            totalEnergyUsed += totalMethodEnergy;
        }

        System.err.println("total energy used was -> " + totalEnergyUsed + "J");
        return /*totalEnergyUsed +*/ countMethodsUsedEnergy();
    }

    private static double countMethodsUsedEnergy() {
        double moreEnergyToSum = 0.0;
        if (Tool.parser == null) return moreEnergyToSum;
        HashMap<String,Integer> methodCounter = Tool.parser.getToolParser().methodsUsageCounter();
        System.err.println("methodCounter -> "+methodCounter);
        for(MethodEnergyInfo methodEnergyInfo : methodsEnergyInfo) {
            System.err.println("method -> "+methodEnergyInfo.getMethodName() + " | energy -> "+methodEnergyInfo.getTotalEnergy());
            moreEnergyToSum += methodCounter.get(methodEnergyInfo.getMethodName()) * methodEnergyInfo.getTotalEnergy();
        }
        return moreEnergyToSum;
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
