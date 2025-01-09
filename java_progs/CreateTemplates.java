package java_progs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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
                50_000,
                100_000,
                250_000,
                500_000,
                1_000_000,
                2_000_000,
                5_000_000,
                10_000_000,
                20_000_000,
                50_000_000,
                100_000_000,
                200_000_000,
                500_000_000,
                1_000_000_000
        };
        static int[] loopSizes = new int[] {
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
            new TypesInfo("int", 4),
            new TypesInfo("char", 1),
            new TypesInfo("double", 8),
            new TypesInfo("long", 8),
            new TypesInfo("short", 2),
            new TypesInfo("float", 4),
            new TypesInfo("boolean", 1),
            new TypesInfo("byte", 1),
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
            //System.out.println(template);
            int progNumber = 0;
            String program = readFile(template.toString());
            String filePath = template.toString().replace("templates","progs2");
            //System.out.println(filePath+progNumber+".java");
            //if (true) return;
            boolean hasLoopSize = program.contains("loopSize");
            for (TypesInfo typeInfo : typesInfo) {
                String programTemp = program.replace("\"Type\"", typeInfo.type);
                ArrayList<String> newPrograms = replaceSizes(programTemp,typeInfo,hasLoopSize);
                for (String newProgram : newPrograms) {
                    createJavaProgramFile(filePath+progNumber+".java", newProgram);
                    progNumber++;
                }
            }
        }
            
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        
    }

    private static ArrayList<String> replaceSizes(String program,TypesInfo typeInfo,boolean hasLoopSize) {
        ArrayList<String> programs = new ArrayList<>();
        for (SizeInfo sizeInfo : typeInfo.sizesWitLoopSize) {
            String programTemp = program.replace("\"size\"", ""+sizeInfo.size);
            if (hasLoopSize) {
                programTemp = programTemp.replace("\"loopSize\"", ""+sizeInfo.loopSize);
            }
            programs.add(programTemp);
        }
        return programs;
    }

    private static void createInputRange(TypesInfo[] typesInfo) {
        for (TypesInfo typeInfo : typesInfo) {
            for (int s : SizeInfo.sizes) {
                for (int ls : SizeInfo.loopSizes) {
                    if (s * ls * typeInfo.memory <= 1500000000/* this is around 1.5GB, a little less */) {
                        typeInfo.sizesWitLoopSize .add(new SizeInfo(s, ls));
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
            f.append(myReader.nextLine());
        }
        myReader.close();
        return f.toString();
    }

    private static void createJavaProgramFile(String file, String program) throws IOException {
        //File myObj = new File(file);
        //myObj.createNewFile(); // create file if needed
        //FileWriter myWriter = new FileWriter(file);
        //myWriter.write(program);
        //myWriter.close();
        BufferedWriter myWriter = new BufferedWriter(new FileWriter(file));
        myWriter.write(program); // Write the content with line breaks
        myWriter.close();
    }

    private static File[] getAllTemplates() {
        return new File("java_progs/templates/").listFiles();
    }

}
