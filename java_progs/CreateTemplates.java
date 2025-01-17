package java_progs;

import java.beans.Introspector;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

public class CreateTemplates {

    static class SizeInfo {
        public int size;
        public int loopSize;
        static int[] sizes = new int[] {
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
        };
        static int[] loopSizes = new int[] {
                1,
                10,
                100,
                //500,
                1_000,
                //2_500,
                5_000,
                //7_500,
                10_000,
                20_000,
                //30_000,
                50_000,
                //75_000,
                100_000
        };

        SizeInfo(int size, int loopSize) {
            this.size = size;
            this.loopSize = loopSize;
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

    public static void main(String[] args) throws IOException, FileNotFoundException {
        createInputRange(typesInfo);
        try {
            File[] templates = getAllTemplates();
            for (File template : templates) {
                String program = readFile(template.toString());
                String fileName = template.toString().replace("java_progs/templates/Template", "");
                String filePath = template.toString().replace("templates", "progs");
                filePath = filePath.replace("Template", "");
                boolean hasLoopSize = program.contains("loopSize");
                String[] listCollections = new String[] {"ArrayList","Vector","LinkedList","Stack","CopyOnWriteArrayList"};
            for (String listCollection : listCollections) {
                int progNumber = 0;
                String pTemp = program.replace("\"ListCollection\"", listCollection);
                pTemp = changeListPackage(listCollection,pTemp);
                pTemp = changeListSize(listCollection,pTemp);
                for (TypesInfo typeInfo : typesInfo) {
                    if (skipChar(fileName,typeInfo)) continue;
                    String programTemp = pTemp.replace("\"Type\"", typeInfo.type);;
                    ArrayList<String> newPrograms = replaceSizes(programTemp, typeInfo, hasLoopSize);
                    for (String newProgram : newPrograms) {
                        String newMethodName = Introspector.decapitalize(fileName);
                        newProgram = newProgram.replace("Template" + fileName, fileName+listCollection+progNumber);
                        newProgram = newProgram.replace(newMethodName, newMethodName + listCollection + progNumber);
                        newProgram = newProgram.replace("\"filename\"",listCollection+progNumber);
                        createJavaProgramFile(filePath +listCollection+ progNumber + ".java", newProgram);
                        progNumber++;
                    }
                }
            }
                
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static String changeListPackage(String listCollection,String program) {
        if (listCollection.equals("CopyOnWriteArrayList")) return program.replace("\"ImportListCollection\"", "concurrent."+listCollection);
        else return program.replace("\"ImportListCollection\"", listCollection);
    }

    private static String changeListSize(String listCollection,String program) {
        if (listCollection.equals("ArrayList") || listCollection.equals("Vector")) return program.replaceAll("\"\\((.*)\\)\"", "\\($1\\)");
                else return program.replaceAll("\"\\(.*\\)\"", "\\(\\)");
    }

    private static Boolean skipChar(String fileName,TypesInfo typeInfo) {
        if (fileName.toLowerCase().contains("sum") || fileName.toLowerCase().contains("increment")){
            if (typeInfo.type.equals("Character") || typeInfo.type.equals("char")) {return true;}
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

}
