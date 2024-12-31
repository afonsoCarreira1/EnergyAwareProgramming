package java_progs;

import java.util.ArrayList;
import java.util.Random;
import java.io.IOException;

public class TestList {
    static int min = 0;
    static int max = 100_000;
    static Random rand = new Random();

    private static void insertRandomNumbers(ArrayList<Integer> list) {
        for (int i = 0; i < 100_000_000; i++) {
            int randomNum = rand.nextInt((max - min) + 1) + min;
            list.add(randomNum);
        }
    }

    @SuppressWarnings("unused")
    private static void insertInBegin(ArrayList<Integer> list) {
        list.add(0, 100);
    }

    @SuppressWarnings("unused")
    private static void insertInEnd(ArrayList<Integer> list, int n) {
        list.add(n);
    }

    @SuppressWarnings("unused")
    private static void insertInMiddle(ArrayList<Integer> list) {
        list.add(list.size() / 2, 100);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        int size = Integer.parseInt(args[1]);
        ArrayList<Integer> list = new ArrayList<Integer>(100_000_000+size);
        WritePid.writePidToFile(Long.toString(ProcessHandle.current().pid()));
        insertRandomNumbers(list);
        int num = rand.nextInt((max - min) + 1) + min;
        Runtime.getRuntime().exec("kill -USR1 " + args[0]);
        for (int i = 0; i < size; i++) {
            insertInEnd(list,num);
        }
        Runtime.getRuntime().exec("kill -USR2 " + args[0]);
    }

}
