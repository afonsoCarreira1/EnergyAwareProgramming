package java_progs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class InsertEndArrayList {
    static int min = 0;
    static int max = 100_000;
    static Random rand = new Random();

    private static void insertRandomNumbers(ArrayList<Integer> list) {
        for (int i = 0; i < 100_000_000; i++) {
            int randomNum = rand.nextInt((max - min) + 1) + min;
            list.add(randomNum);
        }
    }

    private static void insertEndArrayList(ArrayList<Integer> list, int n) {
        list.add(n);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        int size = Integer.parseInt(args[1]);
        ArrayList<Integer> list = new ArrayList<Integer>(100_000_000+size);
        WritePid.writePidToFile(Long.toString(ProcessHandle.current().pid()));
        insertRandomNumbers(list);
        int num = rand.nextInt((max - min) + 1) + min;
        Runtime.getRuntime().exec("kill -USR1 " + args[0]);
        for (int i = 0; i < size; i++) {
            insertEndArrayList(list,num);
        }
        Runtime.getRuntime().exec("kill -USR2 " + args[0]);
    }
}