package java_progs.progs_edit_later;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Random;

import java_progs.aux.ArrayListAux;
import java_progs.aux.WritePid;

public class CloneArrayList {
    static int SIZE = 100_000;
    static int ITER = 100_000;
    static int min = 0;
    static int max = 100_000;

    private static ArrayList<Integer> cloneArrayList(ArrayList<Integer> list) {
        return (ArrayList<Integer>) list.clone();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        ArrayList<Integer> list = new ArrayList<Integer>(SIZE);
        WritePid.writeTargetProgInfo(Long.toString(ProcessHandle.current().pid()),ITER);
        ArrayListAux.insertRandomNumbers(list,SIZE);
        Runtime.getRuntime().exec("kill -USR1 " + args[0]);
        Thread.sleep(100);
        ArrayList<Integer> tempArrayList = new ArrayList<Integer>();
        long begin = System.nanoTime();
        long end = begin;
        int i = 0;
        while (end - begin < 1000000000/*1s*/) {
            tempArrayList = cloneArrayList(list);
            end = System.nanoTime();
            i++;
        }
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS").format(new java.util.Date());
        WritePid.writeTargetProgInfo(timeStamp,i);
        Runtime.getRuntime().exec("kill -USR2 " + args[0]);
    }
}