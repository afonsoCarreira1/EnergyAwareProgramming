import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.*;
import spoon.reflect.cu.CompilationUnit;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.CtImportVisitor;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.code.CtExpressionImpl;
import spoon.support.reflect.declaration.CtImportImpl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.eclipse.jdt.core.dom.IfStatement;

import com.fasterxml.jackson.annotation.JsonCreator.Mode;

@SuppressWarnings("unchecked")
public class ASTFeatureExtractor {

    public static HashMap<String, Map<String, Object>> getFeatures(String file) {
        // String inputPath = "java_progs/progs/"+file+".java";
        String inputPath = "src/TestFile.java";
        file = "TestFile";
        Path currentDir = Paths.get("").toAbsolutePath(); // Get the current directory
        Path resolvedPath = currentDir.resolve(inputPath).normalize(); // Resolve and normalize the path
        // Initialize Spoon launcher
        Launcher launcher = new Launcher();
        addRelevantPackages(launcher);
        //launcher.addInputResource("src");
        //  ("example_dir");
        launcher.getEnvironment().setNoClasspath(true);
        CtModel model = launcher.buildModel();
        HashMap<String, Map<String, Object>> methodsFeatures = new HashMap<>();
        HashMap<String, CtMethod<?>> methodsBody = new HashMap<>();

        Set<String> importSet = new HashSet<>();
        readImportFromFile(resolvedPath.toString(), importSet);

        // obtain all methods features
        for (CtMethod<?> method : model.getElements(new TypeFilter<>(CtMethod.class))) {
            Map<String, Object> features = extractFeatures(method, "java_progs." + file, importSet);
            String methodParams = getMethodParamsType(method);
            String mapName = method.getDeclaringType().getSimpleName() + "." + method.getSimpleName() + "("
                    + methodParams + ")";
            methodsFeatures.put(mapName, features);
            methodsBody.put(mapName, method);
        }

        HashMap<String, Map<String, Object>> methodsFullChecked = new HashMap<String, Map<String, Object>>();

        // for each method associate it with features of other methods
        for (CtMethod<?> method : model.getElements(new TypeFilter<>(CtMethod.class))) {
            String methodParams = getMethodParamsType(method);
            String mapName = method.getDeclaringType().getSimpleName() + "." + method.getSimpleName() + "("
                    + methodParams + ")";
            methodsFullChecked.put(mapName,
                    mergeFeatures(method, methodsFeatures, methodsBody, new HashSet<String>()));
        }
        return methodsFullChecked;
    }

    public static void addRelevantPackages(Launcher launcher) {
        Path currentDir = Paths.get(System.getProperty("user.dir"));
        String[] pathSplit = currentDir.toString().split("/");
        String parentDir = pathSplit[pathSplit.length-1];
        launcher.addInputResource(parentDir);
            try {
                Files.walkFileTree(currentDir, new SimpleFileVisitor<Path>() {
                    //@Override
                    //public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    //    System.out.println("File: " + file);
                    //    return FileVisitResult.CONTINUE;
                    //}

                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        //System.out.println("Directory: " + dir);
                        launcher.addInputResource(dir.toString());
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
    }

    public static String getMethodParamsType(CtMethod<?> method) {
        StringBuilder paramString = new StringBuilder();
        List<CtParameter<?>> l = method.getParameters();
        for (CtParameter<?> parameter : l) {
            if (paramString.length() > 0) {
                paramString.append(" | ");
            }
            paramString.append(parameter.getType().getSimpleName()); // Add type    
        }
        return paramString.toString();
    }

    public static void readImportFromFile(String file, Set<String> importSet) {
        File myObj = new File(file);
        try (Scanner myReader = new Scanner(myObj)) {
            StringBuilder f = new StringBuilder();
            while (myReader.hasNextLine()) {
                f.append(myReader.nextLine()).append("\n");
            }
            myReader.close();
            Scanner scanner = new Scanner(f.toString());
            Stream<MatchResult> stream = scanner.findAll(Pattern.compile("import.*;"));
            stream.forEach(i -> importSet.add(i.group().replace("import", "").replace(";", "").strip()));
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Object> extractFeatures(CtMethod<?> method, String path, Set<String> importSet) {
        Map<String, Object> features = new HashMap<>();

        // 1. Node Types Count
        features.put("VariableDeclarations", method.getElements(new TypeFilter<>(CtLocalVariable.class)).size());
        features.put("Assignments", method.getElements(new TypeFilter<>(CtAssignment.class)).size());
        features.put("BinaryOperators", method.getElements(new TypeFilter<>(CtBinaryOperator.class)).size());
        features.put("MethodInvocations", method.getElements(new TypeFilter<>(CtInvocation.class)).size());

        // count if the methods were called by a java collection or a custom object
        Map<String, Integer> methodsUsed = new HashMap<>();
        for (CtInvocation op : method.getElements(new TypeFilter<>(CtInvocation.class))) {
            CtExpression<?> target = op.getTarget();
            if (target != null) {
                CtTypeReference<?> targetType = target.getType();
                if (targetType != null) {
                    String methodUsed = targetType.getQualifiedName();
                    if (targetType.getQualifiedName().startsWith("java.util.")) {
                        String removedComma = op.getExecutable().toString().replace(",", " | ");
                        methodsUsed.merge(methodUsed + "." + removedComma, 1, Integer::sum);
                    } else {
                        if (!path.equals(op.getTarget().toString()))
                            methodsUsed.merge("CustomObjectWithCustomMethod", 1, Integer::sum);
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
        // for (CtLiteral lt : method.getElements(new TypeFilter<>(CtLiteral.class))) {
        // if
        // ((method.getDeclaringType().getSimpleName()+"."+method.getSimpleName()).equals("TestObject2.yes"))
        // System.out.println(lt);
        // }

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

        // 8. Get variables types
        getVariablesType(method, features);

        getUsedImportsInMethod(method, importSet, features);
        // 8. Cyclomatic Complexity
        // features.put("CyclomaticComplexity", calculateCyclomaticComplexity(method));

        // 9. Nesting Level
        // features.put("MaxNestingLevel", calculateMaxNestingLevel(method));

        return features;
    }

    private static void getUsedImportsInMethod(CtMethod<?> method, Set<String> importSet,
            Map<String, Object> features) {
        Set<String> usedImports = new HashSet<>();

        // Find type references in the method body
        List<CtTypeReference<?>> typeReferences = method.getElements(new TypeFilter<>(CtTypeReference.class));

        for (CtTypeReference<?> typeRef : typeReferences) {
            String qualifiedName = typeRef.getQualifiedName();
            for (String importStatement : importSet) {
                // Check if the import is part of the type reference
                if (qualifiedName.startsWith(importStatement.replace("import ", "").replace(";", ""))) {
                    usedImports.add(importStatement);
                }
            }
        }
        features.put("ImportsUsed", usedImports.size());
    }

    private static void getVariablesType(CtMethod<?> method, Map<String, Object> features) {
        Map<String, Integer> typeCounts = new HashMap<>();
        List<CtVariable<?>> variables = method.getElements(new TypeFilter<>(CtVariable.class));
        for (CtVariable<?> variable : variables) {
            CtTypeReference<?> typeRef = variable.getType();
            // String typeName = typeRef.getQualifiedName();
            String typeName = getFullTypeName(typeRef);

            if (isCustomObject(typeRef)) {
                typeName = "CustomObject";
            }
            typeCounts.put(typeName, typeCounts.getOrDefault(typeName, 0) + 1);
        }
        for (String key : typeCounts.keySet()) {
            features.put(key, typeCounts.get(key));
        }
    }

    private static String getFullTypeName(CtTypeReference<?> typeRef) {
        if (isCustomObject(typeRef)) {
            return "CustomObject";
        }

        StringBuilder typeName = new StringBuilder(typeRef.getQualifiedName());
        List<CtTypeReference<?>> generics = typeRef.getActualTypeArguments();

        if (!generics.isEmpty()) {
            typeName.append("<");
            for (int i = 0; i < generics.size(); i++) {
                if (i > 0) {
                    typeName.append(" | ");
                }
                // Recursive call for nested generics, with custom type handling
                typeName.append(getFullTypeName(generics.get(i)));
            }
            typeName.append(">");
        }
        return typeName.toString();
    }

    private static boolean isCustomObject(CtTypeReference<?> typeRef) {
        if (typeRef.getPackage() == null)
            return false;
        String packageName = typeRef.getPackage().getQualifiedName();
        return !(packageName.startsWith("java.") || packageName.startsWith("javax.") || packageName.startsWith("org."));
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

    private static Map<String, Object> mergeFeatures(CtMethod<?> method,
            HashMap<String, Map<String, Object>> allFeatures,
            HashMap<String, CtMethod<?>> methodsBody, HashSet<String> methodsAnalyzed) {
        String methodParams = getMethodParamsType(method);
        String mapName = method.getDeclaringType().getSimpleName() + "." + method.getSimpleName() + "(" + methodParams
                + ")";
        if (methodsAnalyzed.contains(mapName))
            return allFeatures.get(mapName);
        else
            methodsAnalyzed.add(mapName);
        Map<String, Object> methodfeatures = allFeatures.get(mapName);

        for (CtInvocation<?> methodBody : method.getElements(new TypeFilter<>(CtInvocation.class))) {

            CtExecutableReference<?> executableRef = methodBody.getExecutable();
            String methodName = executableRef.getSimpleName();
            String methodNameWithClass = executableRef.getDeclaringType().getSimpleName() + "." + methodName;
            List<CtTypeReference<?>> parameterTypes = executableRef.getParameters();
            String methodParamsInBody = getParamTypesFromMethodBody(parameterTypes);
            String methodNameWithClassAndParams = methodNameWithClass + "(" + methodParamsInBody + ")";
            if (allFeatures.containsKey(methodNameWithClassAndParams)) 
                methodfeatures = sumMaps(methodfeatures,
                        (mergeFeatures(methodsBody.get(methodNameWithClassAndParams), allFeatures, methodsBody,
                                methodsAnalyzed)));
        }
        return methodfeatures;
    }

    public static String getParamTypesFromMethodBody(List<CtTypeReference<?>> parameterTypes) {
        StringBuilder paramTypesString = new StringBuilder();
        for (int i = 0; i < parameterTypes.size(); i++) {
            if (i > 0) {
                paramTypesString.append(" | "); // Separator
            }
            paramTypesString.append(parameterTypes.get(i).getSimpleName()); // Append type name
        }

        return paramTypesString.toString();
    }

    public static Map<String, Object> sumMaps(Map<String, Object> map1, Map<String, Object> map2) {
        Map<String, Object> result = new HashMap<>();
        // if (map1 == null || map2 == null) return result;

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
