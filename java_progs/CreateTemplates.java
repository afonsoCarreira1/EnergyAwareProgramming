package java_progs;

import java.beans.Introspector;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

public class CreateTemplates {

    static class CollectionInfo {
        private String collectionName;
        private String collectionType;
        private HashSet<String> invalidTemplatesForThisCollection;

        public String getCollectionName() {
            return this.collectionName;
        }

        public String getCollectionType() {
            return this.collectionType;
        }

        public Boolean isTemplateInvalidForThisCollection(String templateName) {
            return invalidTemplatesForThisCollection.contains(templateName);
        }

        CollectionInfo(String collectionName, String collectionType, HashSet<String> invalidMethods) {
            this.collectionName = collectionName;
            this.collectionType = collectionType;
            this.invalidTemplatesForThisCollection = invalidMethods;
        }
    }

    static class SizeInfo {
        public int size;
        public int loopSize;
        public static ArrayList<Integer> sizes = createInputRange(5,1.5,1);
        public static ArrayList<Integer> loopSizes = createInputRange(1, 1.5, 0);
        /*static int[] sizes = new int[] {
            1,  
            5,  
            10,  
            25,  
            50,  
            100,  
            250,  
            500,  
            1_000,  
            2_500,  
            5_000,  
            10_000,  
            25_000,  
            50_000,  
            75_000,  
            100_000,  
            150_000,  
            200_000,  
            250_000,  
            350_000,  
            500_000,  
            750_000,  
            1_000_000,  
            1_500_000,  
            2_000_000,  
            3_000_000,
            4_000_000,  
            5_000_000,  
            7_500_000,  
            10_000_000,  
            15_000_000,  
            20_000_000,  
            30_000_000, 
            40_000_000,
            45_000_000,
            50_000_000,
            55_000_000, 
            60_000_000,
            75_000_000,  
            100_000_000,  
            150_000_000,  
            //200_000_000,  
            //300_000_000,  
            //500_000_000,  
            //750_000_000,  
            //1_000_000_000  
        };*/
        /*static int[] loopSizes = new int[] {
            1,  
            5,  
            10,  
            25,  
            50,  
            100,  
            250,  
            500,  
            1_000,  
            2_000,  
            2_500,  
            5_000,  
            7_000,  
            7_500,  
            10_000,  
            15_000,  
            20_000,  
            25_000,  
            30_000,  
            40_000,  
            50_000,  
            60_000,  
            75_000,  
            100_000  
        };*/
        /*static int[] sizes = new int[] {
                1,
                10,
                100,
                1_000,
                10_000,
                 //50_000,
                100_000,
                 //250_000,
                500_000,
                1_000_000,
                 //2_000_000,
                5_000_000,
                 //10_000_000,
                 //20_000_000,
                50_000_000,
                 //100_000_000,
                200_000_000,
                 //500_000_000,
                 //1_000_000_000
        };*/
        /*static int[] loopSizes = new int[] {
                1,
                10,
                100,
                500,
                1_000,
                2_500,
                5_000,
                7_500,
                10_000,
                20_000,
                30_000,
                50_000,
                75_000,
                100_000
        };*/

        SizeInfo(int size, int loopSize) {
            this.size = size;
            this.loopSize = loopSize;
        }

        private static ArrayList<Integer> createInputRange(int initialvalue, double factor, int exponent){
            Set<Integer> numberSet = new HashSet<>();
            Random random = new Random();
            int max_value = initialvalue * 100_000;
            while (initialvalue < max_value) {
                int min = initialvalue;
                int max = initialvalue*10;
                double nums = Math.pow(factor, exponent);
                for (int j = 0; j < nums; j++) {
                    int num = min + random.nextInt(max - min + 1);
                    numberSet.add(num);
                }
                initialvalue = initialvalue*10;
                exponent++;
            }
            return new ArrayList<>(numberSet);
        }

    }

    static class TypesInfo {
        public String type;
        public int memory;
        public ArrayList<SizeInfo> sizesWitLoopSize = new ArrayList<>();

        TypesInfo(String type, int memory) {
            this.type = type;
            this.memory = memory;
        }

        @Override
        public String toString() {
            return type;
        }
    }

    static TypesInfo[] typesInfo = new TypesInfo[] {
            // new TypesInfo("int", 4),
            // new TypesInfo("char", 1),
            // new TypesInfo("double", 8),
            // new TypesInfo("long", 8),
            // new TypesInfo("short", 2),
            // new TypesInfo("float", 4),
            // new TypesInfo("boolean", 1),
            // new TypesInfo("byte", 1),
            new TypesInfo("Integer", 16),
            new TypesInfo("Double", 16),
            new TypesInfo("Float", 16),
            new TypesInfo("Character", 16),
            new TypesInfo("Long", 16)
    };

    static ArrayList<SizeInfo> sizeCombinations = new ArrayList<>();

    public static ArrayList<CollectionInfo> getCollections() {
        ArrayList<CollectionInfo> collections = new ArrayList<>();
        // lists
        collections.add(new CollectionInfo("ArrayList", "List", new HashSet<>(Arrays.asList("RemoveRandomElemSets"))));
        collections.add(new CollectionInfo("Vector", "List", new HashSet<>(Arrays.asList("RemoveRandomElemSets"))));
        collections.add(new CollectionInfo("LinkedList", "List", new HashSet<>(Arrays.asList("RemoveRandomElemSets"))));
        collections.add(new CollectionInfo("Stack", "List", new HashSet<>(Arrays.asList("RemoveRandomElemSets"))));
        collections.add(new CollectionInfo("CopyOnWriteArrayList", "List", new HashSet<>(Arrays.asList("RemoveRandomElemSets"))));
        // sets
        //collections.add(new CollectionInfo("HashSet", "Set", new HashSet<>(Arrays.asList("GetCol","IndexOfElemFalseCol","IndexOfElemTrueCol","InsertMiddleCol","InsertStartCol","LastIndexOfElemTrueCol","LastIndexOfElemFalseCol","SetElemCol","RemoveRandomElemCol"))));
        //collections.add(new CollectionInfo("LinkedHashSet", "Set", new HashSet<>(Arrays.asList("GetCol","IndexOfElemFalseCol","IndexOfElemTrueCol","InsertMiddleCol","InsertStartCol","LastIndexOfElemTrueCol","LastIndexOfElemFalseCol","SetElemCol","RemoveRandomElemCol"))));
        //collections.add(new CollectionInfo("TreeSet", "Set", new HashSet<>(Arrays.asList("GetCol","IndexOfElemFalseCol","IndexOfElemTrueCol","InsertMiddleCol","InsertStartCol","LastIndexOfElemTrueCol","LastIndexOfElemFalseCol","SetElemCol","RemoveRandomElemCol"))));
        //collections.add(new CollectionInfo("ConcurrentSkipListSet", "Set", new HashSet<>(Arrays.asList("GetCol","IndexOfElemFalseCol","IndexOfElemTrueCol","InsertMiddleCol","InsertStartCol","LastIndexOfElemTrueCol","LastIndexOfElemFalseCol","SetElemCol","RemoveRandomElemCol"))));
        //collections.add(new CollectionInfo("CopyOnWriteArraySet", "Set", new HashSet<>(Arrays.asList("GetCol","IndexOfElemFalseCol","IndexOfElemTrueCol","InsertMiddleCol","InsertStartCol","LastIndexOfElemTrueCol","LastIndexOfElemFalseCol","SetElemCol","CloneCol","RemoveRandomElemCol"))));
        return collections;
    }

    public static void main(String[] args) throws IOException, FileNotFoundException {
        createInputRange(typesInfo);
        System.out.println(SizeInfo.loopSizes.size());
        System.out.println(SizeInfo.sizes.size());
        try {
            File[] templates = getAllTemplates();
            for (File template : templates) {
                String program = readFile(template.toString());
                String fileName = template.toString().replace("java_progs/templates/Template", "");
                String filePath = template.toString().replace("templates", "progs");
                filePath = filePath.replace("Template", "");
                boolean hasLoopSize = program.contains("loopSize");
                ArrayList<CollectionInfo> collections = getCollections();
                // String[] listCollections = new String[]
                // {"ArrayList","Vector","LinkedList","Stack","CopyOnWriteArrayList"};
                // for (String listCollection : listCollections) {
                for (CollectionInfo collection : collections) {
                    int progNumber = 0;
                    String pTemp = program.replace("\"ListCollection\"", collection.getCollectionName());
                    pTemp = changeListPackage(collection.getCollectionName(), pTemp);
                    // pTemp = changeListSize(listCollection,pTemp);
                    for (TypesInfo typeInfo : typesInfo) {
                        if (skipProgram(fileName, collection)) continue;
                        if (skipChar(fileName, typeInfo)) continue;
                        String programTemp = pTemp.replace("\"Type\"", typeInfo.type);
                        programTemp = createArrayOrCollection(programTemp, typeInfo, collection);
                        ArrayList<String> newPrograms = replaceSizes(programTemp, typeInfo, hasLoopSize);
                        for (String newProgram : newPrograms) {
                            String newMethodName = Introspector.decapitalize(fileName);
                            newProgram = newProgram.replace("Template" + fileName,
                                    fileName + collection.collectionName + progNumber);
                            newProgram = newProgram.replace(newMethodName,
                                    newMethodName + collection.collectionName + progNumber);
                            newProgram = newProgram.replace("\"filename\"", collection.collectionName + progNumber);
                            //if (!programHasErrors(newProgram))
                            createJavaProgramFile(filePath + collection.collectionName + progNumber + ".java",
                                    newProgram);
                            progNumber++;
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static String createArrayOrCollection(String program, TypesInfo typeInfo, CollectionInfo collection) {
        String pTemp = createNewCollectionLeftSide("createNewCollectionLeftSide", program, typeInfo, collection);
        pTemp = createNewCollectionRightSide("createNewCollectionRightSide", pTemp, typeInfo, collection);
        pTemp = createCollectionInitArrayLeftSide("createCollectionInitArrayLeftSide", pTemp, typeInfo, collection);
        pTemp = createCollectionInitArrayRightSide("createCollectionInitArrayRightSide", pTemp, typeInfo, collection);
        return pTemp;
    }

    private static String changeListPackage(String listCollection, String program) {
        if (listCollection.contains("CopyOnWriteArray") || listCollection.contains("Concurrent"))
            return program.replace("\"ImportListCollection\"", "concurrent." + listCollection);
        else
            return program.replace("\"ImportListCollection\"", listCollection);
    }

    private static String changeListSize(String listCollection, String program) {
        if (listCollection.equals("ArrayList") || listCollection.equals("Vector"))
            return program.replaceAll("\"\\((.*)\\)\"", "\\($1\\)");
        else
            return program.replaceAll("\"\\(.*\\)\"", "\\(\\)");
    }

    private static Boolean skipChar(String fileName, TypesInfo typeInfo) {
        if (fileName.toLowerCase().contains("sum") || fileName.toLowerCase().contains("increment")) {
            if (typeInfo.type.equals("Character") || typeInfo.type.equals("char")) {
                return true;
            }
        }
        return false;
    }

    private static ArrayList<String> replaceSizes(String program, TypesInfo typeInfo, boolean hasLoopSize) {
        HashSet<String> programs = new HashSet<String>();
        for (SizeInfo sizeInfo : typeInfo.sizesWitLoopSize) {
            String programTemp = program.replace("\"size\"", "" + sizeInfo.size);
            if (hasLoopSize) {
                programTemp = programTemp.replace("\"loopSize\"", "" + sizeInfo.loopSize);
            }
            programs.add(programTemp);
        }
        return new ArrayList<>(programs);
    }

    private static void createInputRange(TypesInfo[] typesInfo) {
        for (TypesInfo typeInfo : typesInfo) {
            for (int s : SizeInfo.sizes) {
                for (int ls : SizeInfo.loopSizes) {
                    // 16000000000
                    long memoryUsageExpected = (long) s * ls * typeInfo.memory;
                    if (memoryUsageExpected <= 1500000000/* this is around 1.5GB, a little less */) {
                        typeInfo.sizesWitLoopSize.add(new SizeInfo(s, ls));
                    }
                }
            }
        }

    }

    private static String createNewCollectionLeftSide(String replaceKeyword, String program, TypesInfo typeInfo,
            CollectionInfo collection) {
        String newLine = collection.collectionName + "<" + typeInfo.type + ">";
        return program.replace(replaceKeyword, newLine);
    }

    private static String createNewCollectionRightSide(String replaceKeyword, String program, TypesInfo typeInfo,
            CollectionInfo collection) {
        String newLine = " = new " + collection.collectionName + " <" + typeInfo.type + ">();";
        return program.replace(replaceKeyword, newLine);
    }

    private static String createCollectionInitArrayLeftSide(String replaceKeyword, String program, TypesInfo typeInfo,
            CollectionInfo collection) {
        String newLine = collection.collectionName + "<" + typeInfo.type + ">[]";
        return program.replace(replaceKeyword, newLine);
    }

    private static String createCollectionInitArrayRightSide(String replaceKeyword, String program, TypesInfo typeInfo,
            CollectionInfo collection) {
        String newLine = " = ";
        newLine += "(" + collection.collectionName + "<" + typeInfo.type + ">[]) ";
        newLine += "new ";
        newLine += collection.collectionName + "[loopSize];";
        return program.replace(replaceKeyword, newLine);
    }

    private static String readFile(String file) throws FileNotFoundException {
        File myObj = new File(file);
        Scanner myReader = new Scanner(myObj);
        StringBuilder f = new StringBuilder();
        while (myReader.hasNextLine()) {
            f.append(myReader.nextLine()).append("\n"); // Append newline after each line
        }
        myReader.close();
        return f.toString();
    }

    private static void createJavaProgramFile(String file, String program) throws IOException {
        BufferedWriter myWriter = new BufferedWriter(new FileWriter(file));
        myWriter.write(program);
        myWriter.close();
    }

    private static File[] getAllTemplates() {
        return new File("java_progs/templates/").listFiles();
    }

    private static Boolean skipProgram(String templateName, CollectionInfo collectionInfo) {
        return collectionInfo.isTemplateInvalidForThisCollection(templateName);
    }
}
