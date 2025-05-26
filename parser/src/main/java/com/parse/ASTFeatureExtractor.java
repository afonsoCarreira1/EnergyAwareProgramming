package com.parse;

import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtPackageReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

@SuppressWarnings("unchecked")
public class ASTFeatureExtractor {
    Launcher launcher;
    CtModel model;
    String path, file;
    Boolean readOnlyFile;
    Set<String> importSet;

    public ASTFeatureExtractor(String path, String file, Boolean readOnlyFile) {
        this.path = path;
        this.file = file;
        this.readOnlyFile = readOnlyFile;
        // String inputPath = "java_progs/progs/"+file+".java";
        String inputPath = path + file + ".java";
        Path currentDir = Paths.get("").toAbsolutePath(); // Get the current directory
        Path resolvedPath = currentDir.resolve(inputPath).normalize(); // Resolve and normalize the path

        // Initialize Spoon launcher
        Launcher launcher = new Launcher();
        if (readOnlyFile)
            launcher.addInputResource(inputPath);
        else
            addRelevantPackages(launcher);
        // launcher.addInputResource("src");
        // ("example_dir");
        launcher.getEnvironment().setNoClasspath(true);
        model = launcher.buildModel();
        // Map<String, Object> featuresExtractedFromMethod = new HashMap<>();

        importSet = new HashSet<>();
        readImportFromFile(resolvedPath.toString(), importSet);
    }

    public HashMap<String, Map<String, Object>> getFeatures() {

        HashMap<String, Map<String, Object>> methodsFeatures = new HashMap<>();
        HashMap<String, CtMethod<?>> methodsBody = new HashMap<>();
        Map<String, CtMethod<?>> allMethodsImplementations = new HashMap<>();
        for (CtMethod<?> method : model.getElements(new TypeFilter<>(CtMethod.class))) {
            String mapName = getMethodMapName(method);
            allMethodsImplementations.put(mapName, method);
        }

        // obtain all methods features
        for (CtMethod<?> method : model.getElements(new TypeFilter<>(CtMethod.class))) {
            Map<String, Object> features = extractFeatures(method, "java_progs." + file, importSet);
            String mapName = getMethodMapName(method);
            // System.out.println("method -> "+mapName);
            accountFeaturesInsideLoops(features, method.getBody(), allMethodsImplementations, new HashSet<>(), 0);
            removeExtraFeaturesCounted(features);
            methodsFeatures.put(mapName, features);
            // System.out.println(features.get("VariableDeclarationsDepth_0"));
            // System.out.println(features.get("VariableDeclarationsDepth_1"));
            methodsBody.put(mapName, method);
        }

        HashMap<String, Map<String, Object>> methodsFullChecked = new HashMap<String, Map<String, Object>>();
        // for each method associate it with features of other methods
        for (CtMethod<?> method : model.getElements(new TypeFilter<>(CtMethod.class))) {
            String mapName = getMethodMapName(method);
            methodsFullChecked.put(mapName,
                    mergeFeatures(method, methodsFeatures, methodsBody, new HashSet<String>()));
        }

        // for each method count its loopDepth
        for (CtMethod<?> method : model.getElements(new TypeFilter<>(CtMethod.class))) {
            String mapName = getMethodMapName(method);
            Map<String, Object> methodFeatures = methodsFullChecked.get(mapName);
            if (!method.getSimpleName().equals("t"))
                continue;
            // System.out.println(method.getBody());
            int maxLoopDepth = calculateMaxLoopDepth(method.getBody(), allMethodsImplementations, new HashSet<>(), 0);
            // accountFeaturesInsideLoops(featuresExtractedFromMethod,method.getBody(),allMethodsImplementations,new
            // HashSet<>(),0);
            methodFeatures.put("MaxLoopDepth", maxLoopDepth);
            methodsFullChecked.put(mapName, methodFeatures);
        }
        // removeExtraFeaturesCounted(featuresExtractedFromMethod);

        return methodsFullChecked;
    }

    private Map<String, Object> extractFeatures(CtMethod<?> method, String path, Set<String> importSet) {
        Map<String, Object> features = new HashMap<>();

        // 1. Node Types Count
        // features.put("VariableDeclarations", method.getElements(new
        // TypeFilter<>(CtLocalVariable.class)).size());
        // features.put("Assignments", method.getElements(new
        // TypeFilter<>(CtAssignment.class)).size());
        // features.put("BinaryOperators", method.getElements(new
        // TypeFilter<>(CtBinaryOperator.class)).size());
        // features.put("MethodInvocations", method.getElements(new
        // TypeFilter<>(CtInvocation.class)).size());

        // method return type
        CtPackageReference returnTypeInfo = method.getType().getPackage();
        if (returnTypeInfo != null && returnTypeInfo.getSimpleName().isEmpty())
            insertOrSumFeature(features, "ReturnTypeCustomObject");
        else
            insertOrSumFeature(features, "ReturnType_" + method.getType().toString().replace(".", "_"));

        // count if the methods were called by a java collection or a custom object
        countMethodsOrigin(method, path, features);

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
                method.getElements(new TypeFilter<>(CtDo.class)).size() +
                method.getElements(new TypeFilter<>(CtForEach.class)).size();
        features.put("LoopCount", loopCount);

        // 5. Literals Count
        // features.put("LiteralCount", method.getElements(new
        // TypeFilter<>(CtLiteral.class)).size());
        // for (CtLiteral lt : method.getElements(new TypeFilter<>(CtLiteral.class))) {
        // if
        // ((method.getDeclaringType().getSimpleName()+"."+method.getSimpleName()).equals("TestObject2.yes"))
        // System.out.println(lt);
        // }

        // 6. Operator Usage
        // getOperators(method,features);

        // 7. Variable Count and Reassignments
        // features.put("VariableCount", method.getElements(new
        // TypeFilter<>(CtVariable.class)).size());
        // features.put("Reassignments", countReassignments(method));

        // 8. Get variables types
        getVariablesType(method, features);

        getUsedImportsInMethod(method, importSet, features);

        checkThreadUsage(method, features);
        // 8. Cyclomatic Complexity
        features.put("CyclomaticComplexity", calculateCyclomaticComplexity(method));

        // 9. Nesting Level
        // features.put("MaxNestingLevel", calculateMaxNestingLevel(method));

        return features;
    }

    private void getOperators(CtBlock<?> body, Map<String, Object> features, String complementFeatureName) {
        Map<String, Integer> operatorCounts = new HashMap<>();
        ArrayList<String> operators = OperatorExtractor.extractOperators(body, complementFeatureName);
        for (int i = 0; i < operators.size(); i++) {
            operatorCounts.merge(operators.get(i), 1, Integer::sum);
        }
        for (String key : operatorCounts.keySet()) {
            features.put(key, operatorCounts.get(key));
        }
    }

    private int accountFeaturesInsideLoops(Map<String, Object> features, CtElement element,
            Map<String, CtMethod<?>> allMethodsImplementations, HashSet<String> visited, int maxDepthSoFar) {
        if (visited.contains(element.toString()))
            return maxDepthSoFar;
        visited.add(element.toString());
        getFeaturesWithDepth(element, maxDepthSoFar, features);
        int maxDepth = maxDepthSoFar;
        List<CtElement> methodElements = element.getElements(null);
        for (CtElement methodElement : methodElements) {
            int currentMaxDepth = maxDepthSoFar;
            if (methodElement instanceof CtLoop) {
                CtLoop l = (CtLoop) methodElement;
                currentMaxDepth = accountFeaturesInsideLoops(features, l.getBody(), allMethodsImplementations, visited,
                        currentMaxDepth + 1);
            } else if (methodElement instanceof CtInvocation) {
                CtInvocation<?> m = (CtInvocation<?>) methodElement;
                CtExecutableReference<?> executableRef = m.getExecutable();
                String methodName = executableRef.getSimpleName();
                String methodNameWithClass = executableRef.getDeclaringType().getSimpleName() + "." + methodName;
                List<CtTypeReference<?>> parameterTypes = executableRef.getParameters();
                String methodParamsInBody = getParamTypesFromMethodBody(parameterTypes);
                String methodNameWithClassAndParams = methodNameWithClass + "(" + methodParamsInBody + ")";
                if (allMethodsImplementations.containsKey(methodNameWithClassAndParams)) {
                    currentMaxDepth = accountFeaturesInsideLoops(features,
                            allMethodsImplementations.get(methodNameWithClassAndParams).getBody(),
                            allMethodsImplementations, visited, currentMaxDepth);
                }
            }
            maxDepth = Math.max(maxDepth, currentMaxDepth);
        }
        return maxDepth;
    }

    private void insertOrSumFeature(Map<String, Object> features, String key) {
        features.put(key, features.containsKey(key) ? (Integer) features.get(key) + 1 : 1);
    }

    private void getFeaturesWithDepth(CtElement element, int maxDepthSoFar, Map<String, Object> features) {
        if (element instanceof CtBlock) {
            getOperators((CtBlock<?>) element, features, "Depth_" + maxDepthSoFar);
            HashMap<String, Integer> featuresToAdd = new HashMap<>();
            featuresToAdd.put("VariableDeclarationsDepth_" + maxDepthSoFar,
                    element.getElements(new TypeFilter<>(CtLocalVariable.class)).size());
            featuresToAdd.put("AssignmentsDepth_" + maxDepthSoFar,
                    element.getElements(new TypeFilter<>(CtAssignment.class)).size());
            featuresToAdd.put("BinaryOperatorsDepth_" + maxDepthSoFar,
                    element.getElements(new TypeFilter<>(CtBinaryOperator.class)).size());
            featuresToAdd.put("MethodInvocationsDepth_" + maxDepthSoFar,
                    element.getElements(new TypeFilter<>(CtInvocation.class)).size());
            featuresToAdd.put("LiteralCountDepth_" + maxDepthSoFar,
                    element.getElements(new TypeFilter<>(CtLiteral.class)).size());
            featuresToAdd.put("VariableCountDepth_" + maxDepthSoFar,
                    element.getElements(new TypeFilter<>(CtVariable.class)).size());
            featuresToAdd.put("ReassignmentsDepth_" + maxDepthSoFar, countReassignments((CtBlock<?>) element));
            List<String> keys = new ArrayList<>(featuresToAdd.keySet());
            for (String key : keys) {
                features.put(key, features.containsKey(key) ? (Integer) features.get(key) + featuresToAdd.get(key)
                        : featuresToAdd.get(key));
            }
        }
    }

    // TODO change this so i dont have to write every new feature that accounts for
    // depth
    private void removeExtraFeaturesCounted(Map<String, Object> features) {
        // ArrayList<String> featuresToClean = new ArrayList<> ();
        ArrayList<String> featuresToClean = new ArrayList<>(
                Arrays.asList("VariableDeclarationsDepth",
                        "AssignmentsDepth",
                        "BinaryOperatorsDepth",
                        "MethodInvocationsDepth",
                        "LiteralCountDepth",
                        "VariableCountDepth",
                        "ReassignmentsDepth",
                        "PLUSDepth", "MINUSDepth", "MULDepth", "DIVDepth", "MODDepth",
                        "LTDepth", "LEDepth", "GTDepth", "GEDepth", "EQDepth",
                        "POSTINCDepth", "POSTDECDepth", "PREINCDepth", "PREDECDepth"));
        // for (String feature : features.keySet()) {
        // if (feature.contains("Depth_")) featuresToClean.add(feature.split("_")[0]);
        // //System.out.println(feature);
        // }
        for (String featureToClean : featuresToClean) {
            int startingDepth = 0;
            while (true) {
                String startingKey = featureToClean + "_" + startingDepth;
                if (!features.containsKey(startingKey))
                    break;
                int currentDepth = startingDepth;
                int extraVars = 0;
                String key = featureToClean + "_" + (currentDepth + 1);
                if (!features.containsKey(key))
                    break;
                extraVars += (Integer) features.get(key);
                currentDepth++;
                features.put(startingKey, (Integer) features.get(startingKey) - extraVars);
                startingDepth++;
            }
        }
    }

    private String getMethodMapName(CtMethod<?> method) {
        return method.getDeclaringType().getSimpleName() + "." + method.getSimpleName() + "("
                + getMethodParamsType(method) + ")";
    }

    private void addRelevantPackages(Launcher launcher) {
        Path currentDir = Paths.get(System.getProperty("user.dir"));
        String[] pathSplit = currentDir.toString().split("/");
        String parentDir = pathSplit[pathSplit.length - 1];
        launcher.addInputResource(parentDir);
        try {
            Files.walkFileTree(currentDir, new SimpleFileVisitor<Path>() {
                // @Override
                // private FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                // throws IOException {
                // System.out.println("File: " + file);
                // return FileVisitResult.CONTINUE;
                // }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    // System.out.println("Directory: " + dir);
                    launcher.addInputResource(dir.toString());
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private String getMethodParamsType(CtMethod<?> method) {
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

    private void readImportFromFile(String file, Set<String> importSet) {
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

    private void getUsedImportsInMethod(CtMethod<?> method, Set<String> importSet,
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

    private void countMethodsOrigin(CtMethod<?> method, String path, Map<String, Object> features) {
        Map<String, Integer> methodsUsed = new HashMap<>();
        for (CtInvocation<?> op : method.getElements(new TypeFilter<>(CtInvocation.class))) {
            CtExpression<?> target = op.getTarget();
            if (target != null) {
                CtTypeReference<?> targetType = target.getType();
                if (targetType != null) {
                    String methodUsed = targetType.getQualifiedName();
                    if (targetType.getQualifiedName().startsWith("java.util.")
                            || targetType.getQualifiedName().startsWith("sun.")) {
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
    }

    private void getVariablesType(CtMethod<?> method, Map<String, Object> features) {
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

    private String getFullTypeName(CtTypeReference<?> typeRef) {
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

    private boolean isCustomObject(CtTypeReference<?> typeRef) {
        if (typeRef.getPackage() == null)
            return false;
        String packageName = typeRef.getPackage().getQualifiedName();
        return !(packageName.startsWith("java.") || packageName.startsWith("javax.") || packageName.startsWith("org.")
                || packageName.startsWith("sun."));
    }

    private int countReassignments(CtBlock<?> body) {
        int count = 0;
        List<CtAssignment<?, ?>> assignments = body.getElements(new TypeFilter<>(CtAssignment.class));
        for (CtAssignment<?, ?> assignment : assignments) {
            if (assignment.getAssigned() instanceof CtVariableAccess) {
                count++;
            }
        }
        return count;
    }

    private int calculateCyclomaticComplexity(CtMethod<?> method) {
        int complexity = 1; // Start with 1 for the method itself
        complexity += method.getElements(new TypeFilter<>(CtIf.class)).size();
        complexity += method.getElements(new TypeFilter<>(CtLoop.class)).size();
        complexity += method.getElements(new TypeFilter<>(CtSwitch.class)).size();
        complexity += method.getElements(new TypeFilter<>(CtCase.class)).size();
        complexity += method.getElements(new TypeFilter<>(CtConditional.class)).size();
        complexity += method.getElements(new TypeFilter<>(CtCatch.class)).size();
        return complexity;
    }

    private int calculateMaxLoopDepth(CtElement element, Map<String, CtMethod<?>> allMethodsImplementations,
            HashSet<String> visited, int maxDepthSoFar) {
        if (visited.contains(element.toString()))
            return maxDepthSoFar;
        visited.add(element.toString());
        int maxDepth = maxDepthSoFar;
        List<CtElement> methodElements = element.getElements(null);
        for (CtElement methodElement : methodElements) {
            int currentMaxDepth = maxDepthSoFar;
            // System.out.println(methodElement.getClass().getSimpleName());
            if (methodElement instanceof CtLoop) {
                CtLoop l = (CtLoop) methodElement;
                currentMaxDepth = calculateMaxLoopDepth(l.getBody(), allMethodsImplementations, visited,
                        currentMaxDepth + 1);
            } else if (methodElement instanceof CtInvocation) {
                CtInvocation<?> m = (CtInvocation<?>) methodElement;
                CtExecutableReference<?> executableRef = m.getExecutable();
                String methodName = executableRef.getSimpleName();
                String methodNameWithClass = executableRef.getDeclaringType().getSimpleName() + "." + methodName;
                List<CtTypeReference<?>> parameterTypes = executableRef.getParameters();
                String methodParamsInBody = getParamTypesFromMethodBody(parameterTypes);
                String methodNameWithClassAndParams = methodNameWithClass + "(" + methodParamsInBody + ")";
                if (allMethodsImplementations.containsKey(methodNameWithClassAndParams)) {
                    currentMaxDepth = calculateMaxLoopDepth(
                            allMethodsImplementations.get(methodNameWithClassAndParams).getBody(),
                            allMethodsImplementations, visited, currentMaxDepth);
                }
            }
            maxDepth = Math.max(maxDepth, currentMaxDepth);
        }
        return maxDepth;
    }

    private void checkThreadUsage(CtMethod<?> method, Map<String, Object> features) {
        CtClass<?> declaringClass = (CtClass<?>) method.getDeclaringType();

        int threadUsageCount = 0;

        // Check if the declaring class extends Thread or implements Runnable
        boolean isThreadSubclass = isSubclassOf(declaringClass, "java.lang.Thread");
        if (isThreadSubclass) {
            threadUsageCount++;
        }

        boolean isRunnableImplementation = implementsInterface(declaringClass, "java.lang.Runnable");
        if (isRunnableImplementation) {
            threadUsageCount++;
        }

        // Check for thread-related invocations in the method body
        // long threadInvocationCount = 0;
        // if (method.getBody() != null) {
        // threadInvocationCount += method.getBody().getElements(new
        // TypeFilter<>(CtInvocation.class)).stream()
        // .mapToLong(invocation -> {
        // String methodName = invocation.getExecutable().getSimpleName();
        // if (methodName.equals("start") || methodName.equals("run") ||
        // methodName.equals("submit")) {
        // return getLoopMultiplier(invocation);
        // }
        // return 0;
        // }).sum();
        // }
        long threadInvocationCount = method.getBody() != null
                ? method.getBody().getElements(new TypeFilter<>(CtInvocation.class)).stream()
                        .filter(invocation -> {
                            String methodName = invocation.getExecutable().getSimpleName();
                            String declaringType = invocation.getExecutable().getDeclaringType() != null
                                    ? invocation.getExecutable().getDeclaringType().getQualifiedName()
                                    : "";
                            HashSet<String> threadLibs = new HashSet<>(Arrays.asList("java.lang.Thread",
                                    "java.util.concurrent", "java.lang.Runnable", "java.util.Timer"));
                            return (threadLibs.contains(declaringType) && (methodName.equals("start")
                                    || methodName.equals("run") || methodName.equals("submit")));
                        }).count()
                : 0;
        threadUsageCount += threadInvocationCount;

        // Check for direct instantiation of Thread or Runnable in the method
        // long threadInstantiationCount = 0;
        // if (method.getBody() != null) {
        // threadInstantiationCount += method.getBody().getElements(new
        // TypeFilter<>(CtConstructorCall.class)).stream()
        // .mapToLong(ctor -> {
        // String typeName = ctor.getType().getQualifiedName();
        // if (typeName.equals("java.lang.Thread") ||
        // typeName.equals("java.lang.Runnable")) {
        // return getLoopMultiplier(ctor);
        // }
        // return 0;
        // }).sum();
        // }
        long threadInstantiationCount = method.getBody() != null
                ? method.getBody().getElements(new TypeFilter<>(CtConstructorCall.class)).stream()
                        .filter(ctor -> {
                            String typeName = ctor.getType().getQualifiedName();
                            return typeName.equals("java.lang.Thread") || typeName.equals("java.lang.Runnable");
                        }).count()
                : 0;
        threadUsageCount += threadInstantiationCount;
        features.put("ThreadUsage", threadUsageCount);

    }

    private boolean isSubclassOf(CtClass<?> clazz, String superclass) {
        if (clazz == null)
            return false;
        CtTypeReference<?> superClassRef = clazz.getSuperclass();
        return superClassRef != null && superClassRef.getQualifiedName().equals(superclass);
    }

    private boolean implementsInterface(CtClass<?> clazz, String interfaceName) {
        if (clazz == null)
            return false;
        return clazz.getSuperInterfaces().stream()
                .anyMatch(iface -> iface.getQualifiedName().equals(interfaceName));
    }

    private Map<String, Object> mergeFeatures(CtMethod<?> method,
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

    private String getParamTypesFromMethodBody(List<CtTypeReference<?>> parameterTypes) {
        StringBuilder paramTypesString = new StringBuilder();
        for (int i = 0; i < parameterTypes.size(); i++) {
            if (i > 0) {
                paramTypesString.append(" | ");
            }
            paramTypesString.append(parameterTypes.get(i).getSimpleName());
        }

        return paramTypesString.toString();
    }

    private Map<String, Object> sumMaps(Map<String, Object> map1, Map<String, Object> map2) {
        Map<String, Object> result = new HashMap<>();

        // Add all keys from map1
        for (String key : map1.keySet()) {
            Object value1 = map1.get(key);
            Object value2 = map2.get(key);
            if (key.startsWith("VariableDeclarationsDepth")) {
                result.put(key, value1);
            } else if (value1 instanceof Integer && value2 instanceof Integer) {
                // Sum integer values
                // System.out.println(key);
                // System.out.println("value1: " +value1 +" + " +"value2: "+value2 );
                result.put(key, (Integer) value1 + (Integer) value2);
            } else if (value1 != null && value2 == null) {
                // If the key exists only in map1
                result.put(key, value1);
            } else if (value1 instanceof Map && value2 instanceof Map) {
                // Recursively sum nested maps
                result.put(key, sumMaps((Map<String, Object>) value1, (Map<String, Object>) value2));
            } else if (key.equals("MethodReturnType")) {
                result.put(key, value1);
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

    public List<String> getNumberOfInputs() {
        List<String> inputValues = new ArrayList<>();
        String regex = "^input\\d+$";
        for (CtVariable<?> inputVar : model.getElements(new TypeFilter<>(CtVariable.class))) {
            if (inputVar.getSimpleName().matches(regex)) {
                inputValues.add(inputVar.getDefaultExpression().toString().replace("\"", ""));
            }
        }
        return inputValues;
    }

    public HashSet<String> getMethodsForSliders(HashSet<String> modelsAvailable) {
        HashSet<String> featuresToSlider = new HashSet<>();
        for (CtType<?> ctType : model.getAllTypes()) {
            if (ctType.getSimpleName().equals(file)) {
                for (CtInvocation<?> invocation : ctType.getElements(new TypeFilter<>(CtInvocation.class))) {
                    CtExecutableReference<?> execRef = invocation.getExecutable();
                    List<CtTypeReference<?>> paramTypes = execRef.getParameters();
                    StringBuilder paramKey = new StringBuilder();
                    for (int i = 0; i < paramTypes.size(); i++) {
                        paramKey.append(paramTypes.get(i).getQualifiedName().replace(".", "_") + "_");
                    }
                    String finalName = execRef.getSimpleName() + "_" + paramKey;
                    if (paramKey.isEmpty()) finalName += "_";
                    //System.err.println(finalName);
                    if (modelsAvailable.contains(finalName)){
                        List<CtExpression<?>> arguments = invocation.getArguments();
                        CtMethod<?> parentMethod = invocation.getParent(CtMethod.class); // ← Get enclosing method
                        String methodContext = parentMethod != null ? parentMethod.getSimpleName() : "UNKNOWN_METHOD";
                        for (int i = 0; i < arguments.size(); i++) {
                            CtExpression<?> arg = arguments.get(i);
                            if (arg instanceof CtVariableRead)
                            featuresToSlider.add("Method: "+methodContext+" Variable: " + arg.toString());
                        }
                        featuresToSlider.add("Method: "+methodContext+" Variable: " + invocation.getTarget());
                    }
                }
            }
        }
        return featuresToSlider;
    }

}
/*
 * for (CtType<?> ctType : model.getAllTypes()) {
 * if (ctType.getSimpleName().equals(file)) {
 * // Now we are inside the type corresponding to your file
 * for (CtInvocation<?> invocation : model.getElements(new
 * TypeFilter<>(CtInvocation.class))) {
 * System.out.println(invocation);
 * System.exit(0);
 * var execRef = invocation.getExecutable();
 * var paramTypes = execRef.getReferencedTypes();
 * 
 * System.out.println("Method: " + execRef.getSimpleName());
 * ArrayList<CtTypeReference<?>> typesList = new ArrayList<>(paramTypes);
 * for (int i = 0; i < typesList.size(); i++) {
 * var typeRef = typesList.get(i);
 * System.out.println("Parameter " + i + ": " + typeRef.getQualifiedName());
 * }
 * }
 * 
 * }
 * }
 */