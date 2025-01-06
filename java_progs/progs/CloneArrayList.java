package java_progs.progs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import java_progs.aux.WritePid;

public class CloneArrayList {
    static int INIT_SIZE = 100_000;
    static int ITER = 100_000;
    static int min = 0;
    static int max = 100_000;
    static Random rand = new Random();

    private static void insertRandomNumbers(ArrayList<Integer> list) {
        for (int i = 0; i < INIT_SIZE; i++) {
            int randomNum = rand.nextInt((max - min) + 1) + min;
            list.add(randomNum);
        }
    }

    private static ArrayList<Integer> cloneArrayList(ArrayList<Integer> list) {
        return (ArrayList<Integer>) list.clone();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        ArrayList<Integer> list = new ArrayList<Integer>(INIT_SIZE);
        WritePid.writeTargetProgInfo(Long.toString(ProcessHandle.current().pid()),ITER);
        insertRandomNumbers(list);
        Runtime.getRuntime().exec("kill -USR1 " + args[0]);
        Thread.sleep(100);
        ArrayList<Integer> tempArrayList = new ArrayList<Integer>();
        for (int i = 0; i < ITER; i++) {
            tempArrayList = cloneArrayList(list);
        }
        Runtime.getRuntime().exec("kill -USR2 " + args[0]);
    }
}