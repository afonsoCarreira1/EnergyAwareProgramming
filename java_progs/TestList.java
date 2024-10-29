package java_progs;

import java.util.ArrayList;
import java.util.Random;
import java.io.IOException;

public class TestList {

    private static void insertRandomNumbers(ArrayList<Integer> list) {
        int min = 0;
        int max = 100_000;
        Random rand = new Random();
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
    private static void insertInEnd(ArrayList<Integer> list) {
        list.add(list.size() - 1, 100);
    }

    private static void insertInMiddle(ArrayList<Integer> list) {
        list.add(list.size() / 2, 100);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        ArrayList<Integer> list = new ArrayList<Integer>();
                WritePid.writePidToFile(Long.toString(ProcessHandle.current().pid()));
                insertRandomNumbers(list);
                Runtime.getRuntime().exec("kill -USR1 " + args[0]);
                //Thread.sleep(100);
                insertInBegin(list);
                //insertInEnd(list);
                //insertInMiddle(list);
                Runtime.getRuntime().exec("kill -USR2 " + args[0]);
    }

}
