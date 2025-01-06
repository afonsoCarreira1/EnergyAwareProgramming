package java_progs.progs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import java_progs.aux.WritePid;

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
        int size = 20_000_000;
        ArrayList<Integer> list = new ArrayList<Integer>(100_000_000+size);
        WritePid.writeTargetProgInfo(Long.toString(ProcessHandle.current().pid()),size);
        insertRandomNumbers(list);
        int num = rand.nextInt((max - min) + 1) + min;
        Runtime.getRuntime().exec("kill -USR1 " + args[0]);
        Thread.sleep(100);
        for (int i = 0; i < size; i++) {
            insertEndArrayList(list,num);
        }
        Runtime.getRuntime().exec("kill -USR2 " + args[0]);
    }
}