package java_progs.progs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import java_progs.aux.WritePid;

public class GetArrayList {
    static int SIZE = 100_000_000;
    static int LOOP_SIZE = 2_000_000_000;
    static int min = 0;
    static int max = 100_000;
    static Random rand = new Random();

    private static void insertRandomNumbers(ArrayList<Integer> list) {
        for (int i = 0; i < SIZE; i++) {
            int randomNum = rand.nextInt((max - min) + 1) + min;
            list.add(randomNum);
        }
    }

    private static int getArrayList(ArrayList<Integer> list, int n) {
        return list.get(n);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        ArrayList<Integer> list = new ArrayList<Integer>(SIZE);
        WritePid.writeTargetProgInfo(Long.toString(ProcessHandle.current().pid()),LOOP_SIZE);
        insertRandomNumbers(list);
        int num = rand.nextInt((max - min) + 1) + min;
        Runtime.getRuntime().exec("kill -USR1 " + args[0]);
        Thread.sleep(100);
        for (int i = 0; i < LOOP_SIZE; i++) {
            getArrayList(list,num);
        }
        Runtime.getRuntime().exec("kill -USR2 " + args[0]);
    }
}