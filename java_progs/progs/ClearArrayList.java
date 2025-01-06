package java_progs.progs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import java_progs.aux.WritePid;

public class ClearArrayList {
    static int SIZE = 100_000;
    static int loopSize = 1000;
    static int min = 0;
    static int max = 100_000;
    static Random rand = new Random();

    private static ArrayList<Integer> insertRandomNumbers(ArrayList<Integer> list) {
        for (int i = 0; i < SIZE; i++) {
            int randomNum = rand.nextInt((max - min) + 1) + min;
            list.add(randomNum);
        }
        return list;
    }

    private static void clearArrayList(ArrayList<Integer> list) {
        list.clear();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        ArrayList<ArrayList<Integer>> lists = new ArrayList<ArrayList<Integer>>(loopSize);
        for (int i = 0; i < loopSize; i++) {
            lists.add(insertRandomNumbers(new ArrayList<>(SIZE)));
        }
        ArrayList<Integer> l = insertRandomNumbers(new ArrayList<>(SIZE));
        WritePid.writeTargetProgInfo(Long.toString(ProcessHandle.current().pid()),loopSize);
        Runtime.getRuntime().exec("kill -USR1 " + args[0]);
        Thread.sleep(100);
        for (int i = 0; i < loopSize; i++) {
            clearArrayList(lists.get(i));
        }
        Runtime.getRuntime().exec("kill -USR2 " + args[0]);
    }
}