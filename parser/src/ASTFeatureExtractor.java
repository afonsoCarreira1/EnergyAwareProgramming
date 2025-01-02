import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.code.CtExpressionImpl;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jdt.core.dom.IfStatement;

public class ASTFeatureExtractor {

    public static HashMap<String, Map<String, Object>> getFeatures(String file) {
        String inputPath = "java_progs/"+file+".java";
        Path currentDir = Paths.get("").toAbsolutePath(); // Get the current directory
        Path resolvedPath = currentDir.resolve(inputPath).normalize(); // Resolve and normalize the path
        // Initialize Spoon launcher
        Launcher launcher = new Launcher();
        launcher.addInputResource(resolvedPath.toString());
        launcher.getEnvironment().setNoClasspath(true);
        CtModel model = launcher.buildModel();
        HashMap<String, Map<String, Object>> methodsFeatures = new HashMap<>();
        HashMap<String, CtMethod> methodsBody = new HashMap<>();

        // obtain all methods features
        for (CtMethod<?> method : model.getElements(new TypeFilter<>(CtMethod.class))) {
            System.out.println("Analyzing method: " + method.getSimpleName());
            Map<String, Object> features = extractFeatures(method,"java_progs."+file);
            methodsFeatures.put(method.getSimpleName(), features);
            methodsBody.put(method.getSimpleName(), method);
            System.out.println(features);
            System.out.println("---------------------------------");
        }

        HashMap<String, Map<String, Object>> methodsFullChecked = new HashMap();

        // for each method associate it with features of other methods
        for (CtMethod method : model.getElements(new TypeFilter<>(CtMethod.class))) {
            methodsFullChecked.put(method.getSimpleName(),mergeFeatures(method, methodsFeatures, methodsBody,new HashSet<String>()));
        }
        System.out.println(methodsFullChecked);
        return methodsFullChecked;
    }

    /* 
    public static void main(String[] args) {
        // Path to your Java file
        //String inputPath = "src/Test.java";
        String inputPath = "../java_progs/"+args[0]+".java";
        Path currentDir = Paths.get("").toAbsolutePath(); // Get the current directory
        Path resolvedPath = currentDir.resolve(inputPath).normalize(); // Resolve and normalize the path
        // Initialize Spoon launcher
        Launcher launcher = new Launcher();
        launcher.addInputResource(resolvedPath.toString());
        launcher.getEnvironment().setNoClasspath(true);
        CtModel model = launcher.buildModel();
        HashMap<String, Map<String, Object>> methodsFeatures = new HashMap<>();
        HashMap<String, CtMethod> methodsBody = new HashMap<>();

        // obtain all methods features
        for (CtMethod<?> method : model.getElements(new TypeFilter<>(CtMethod.class))) {
            System.out.println("Analyzing method: " + method.getSimpleName());
            Map<String, Object> features = extractFeatures(method,"java_progs."+args[0]);
            methodsFeatures.put(method.getSimpleName(), features);
            methodsBody.put(method.getSimpleName(), method);
            System.out.println(features);
            System.out.println("---------------------------------");
        }

        HashMap<String, Map<String, Object>> methodsFullChecked = new HashMap();

        // for each method associate it with features of other methods
        for (CtMethod method : model.getElements(new TypeFilter<>(CtMethod.class))) {
            methodsFullChecked.put(method.getSimpleName(),mergeFeatures(method, methodsFeatures, methodsBody,new HashSet<String>()));
        }
        System.out.println(methodsFullChecked);
    }

    */
    public static Map<String, Object> extractFeatures(CtMethod<?> method,String path) {
        Map<String, Object> features = new HashMap<>();

        // 1. Node Types Count
        features.put("VariableDeclarations", method.getElements(new TypeFilter<>(CtLocalVariable.class)).size());
        features.put("Assignments", method.getElements(new TypeFilter<>(CtAssignment.class)).size());
        features.put("BinaryOperators", method.getElements(new TypeFilter<>(CtBinaryOperator.class)).size());
        features.put("MethodInvocations", method.getElements(new TypeFilter<>(CtInvocation.class)).size());
        
        //count if the methods were called by a java collection or a custom object
        Map<String, Integer> methodsUsed = new HashMap<>();
        for (CtInvocation op : method.getElements(new TypeFilter<>(CtInvocation.class))) {
            CtExpression<?> target = op.getTarget();
            if (target != null) {
                CtTypeReference<?> targetType = target.getType();
                if (targetType != null) {
                    String methodUsed = targetType.getQualifiedName();
                    if (targetType.getQualifiedName().startsWith("java.util.")){
                        methodsUsed.merge(methodUsed+"."+op.getExecutable(), 1, Integer::sum);
                    }else {
                        if (!path.equals(op.getTarget().toString())) methodsUsed.merge("CustomObjectWithCustomMethod", 1, Integer::sum);
                    }
                }
            } 
        }
        for (String key : methodsUsed.keySet()) {
            features.put(key, methodsUsed.get(key));
        }


        // 2. Depth of AST
        // features.put("ASTDepth", calculateASTDepth(method));

        // 3. Branch Count
        int branchCount = 0;
        for (CtIf op : method.getElements(new TypeFilter<>(CtIf.class))) {
            if (op.getElseStatement() != null)
                branchCount += 2;
            else
                branchCount++;
        }
        features.put("BranchCount", branchCount);

        // 4. Loop Count
        int loopCount = method.getElements(new TypeFilter<>(CtFor.class)).size() +
                method.getElements(new TypeFilter<>(CtWhile.class)).size() +
                method.getElements(new TypeFilter<>(CtDo.class)).size();
        features.put("LoopCount", loopCount);

        // 5. Literals Count
        features.put("LiteralCount", method.getElements(new TypeFilter<>(CtLiteral.class)).size());

        // 6. Operator Usage
        Map<String, Integer> operatorCounts = new HashMap<>();
        ArrayList<String> operators = OperatorExtractor.extractOperators(method);
        for (int i = 0; i < operators.size(); i++) {
            operatorCounts.merge(operators.get(i), 1, Integer::sum);
        }
        for (String key : operatorCounts.keySet()) {
            features.put(key, operatorCounts.get(key));
        }

        // 7. Variable Count and Reassignments
        features.put("VariableCount", method.getElements(new TypeFilter<>(CtVariable.class)).size());
        features.put("Reassignments", countReassignments(method));

        // 8. Cyclomatic Complexity
        // features.put("CyclomaticComplexity", calculateCyclomaticComplexity(method));

        // 9. Nesting Level
        // features.put("MaxNestingLevel", calculateMaxNestingLevel(method));

        return features;
    }

    private static int calculateASTDepth(CtElement element) {
        if (element == null || element.getDirectChildren().isEmpty()) {
            return 1;
        }
        int maxDepth = 0;
        for (CtElement child : element.getDirectChildren()) {
            maxDepth = Math.max(maxDepth, calculateASTDepth(child));
        }
        return maxDepth + 1;
    }

    private static int countReassignments(CtMethod<?> method) {
        int count = 0;
        List<CtAssignment<?, ?>> assignments = method.getElements(new TypeFilter<>(CtAssignment.class));
        for (CtAssignment<?, ?> assignment : assignments) {
            if (assignment.getAssigned() instanceof CtVariableAccess) {
                count++;
            }
        }
        return count;
    }

    private static int calculateCyclomaticComplexity(CtMethod<?> method) {
        int complexity = 1; // Start with 1 for the method itself
        complexity += method.getElements(new TypeFilter<>(CtIf.class)).size();
        complexity += method.getElements(new TypeFilter<>(CtFor.class)).size();
        complexity += method.getElements(new TypeFilter<>(CtWhile.class)).size();
        complexity += method.getElements(new TypeFilter<>(CtSwitch.class)).size();
        return complexity;
    }

    private static int calculateMaxNestingLevel(CtElement element) {
        if (element instanceof CtLoop || element instanceof CtIf || element instanceof CtSwitch) {
            int maxLevel = 0;
            for (CtElement child : element.getDirectChildren()) {
                maxLevel = Math.max(maxLevel, calculateMaxNestingLevel(child));
            }
            return maxLevel + 1;
        }
        return 0;
    }

    private static Map<String, Object> mergeFeatures(CtMethod method, HashMap<String, Map<String, Object>> allFeatures,
            HashMap<String, CtMethod> methodsBody,HashSet<String> methodsAnalyzed) {
        if (methodsAnalyzed.contains(method.getSimpleName())) return allFeatures.get(method.getSimpleName());
        else methodsAnalyzed.add(method.getSimpleName());
        Map<String, Object> methodfeatures = allFeatures.get(method.getSimpleName());

        System.out.println("Analyzing method: " + method.getSimpleName());
        Map<String, Object> features = new HashMap<>();
        for (CtInvocation methodBody : method.getElements(new TypeFilter<>(CtInvocation.class))) {
            // if (allFeatures.containsKey("allFeatures"))
            // try {
            String[] methodSplit = methodBody.toString().split("\\.");
            String methodName = methodSplit[methodSplit.length - 1].split("\\(")[0];
            // System.out.println("funCALL -> "+methodName);
            if (allFeatures.containsKey(methodName))
                methodfeatures = sumMaps(methodfeatures,
                        (mergeFeatures(methodsBody.get(methodName), allFeatures, methodsBody,methodsAnalyzed)));

            // } catch (Exception e) {
            // System.out.println("Failed for : "+methodBody+" -> "+e);
            // }

        }
        // Map<String, Object> features = extractFeatures(method);
        // System.out.println(features);
        // System.out.println("---------------------------------");
        return methodfeatures;
    }

    public static Map<String, Object> sumMaps(Map<String, Object> map1, Map<String, Object> map2) {
        Map<String, Object> result = new HashMap<>();

        // Add all keys from map1
        for (String key : map1.keySet()) {
            Object value1 = map1.get(key);
            Object value2 = map2.get(key);

            if (value1 instanceof Integer && value2 instanceof Integer) {
                // Sum integer values
                result.put(key, (Integer) value1 + (Integer) value2);
            } else if (value1 != null && value2 == null) {
                // If the key exists only in map1
                result.put(key, value1);
            } else if (value1 instanceof Map && value2 instanceof Map) {
                // Recursively sum nested maps
                result.put(key, sumMaps((Map<String, Object>) value1, (Map<String, Object>) value2));
            }
        }

        // Add all keys from map2 that are not in map1
        for (String key : map2.keySet()) {
            if (!map1.containsKey(key)) {
                result.put(key, map2.get(key));
            }
        }

        return result;
    }
}
