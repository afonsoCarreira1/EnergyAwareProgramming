package java_progs.progs;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Random;

import java_progs.aux.ArrayListAux;
import java_progs.aux.WritePid;

public class InsertEndArrayList {
    static int SIZE = 100_000_000;
    static int min = 0;
    static int max = 100_000;
    static Random rand = new Random();

    private static void insertEndArrayList(ArrayList<Integer> list, int n) {
        list.add(n);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        int size = 20_000_000;
        ArrayList<Integer> list = new ArrayList<Integer>(100_000_000+size);
        WritePid.writeTargetProgInfo(Long.toString(ProcessHandle.current().pid()),size);
        ArrayListAux.insertRandomNumbers(list,SIZE);
        int num = rand.nextInt((max - min) + 1) + min;
        Runtime.getRuntime().exec("kill -USR1 " + args[0]);
        Thread.sleep(100);
        long begin = System.nanoTime();
        long end = begin;
        int i = 0;
        while (end - begin < 1000000000/*1s*/) {
            insertEndArrayList(list,num);
            end = System.nanoTime();
            i++;
        }
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS").format(new java.util.Date());
        WritePid.writeTargetProgInfo(timeStamp,i);
        Runtime.getRuntime().exec("kill -USR2 " + args[0]);
    }
}