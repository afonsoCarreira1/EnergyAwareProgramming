package java_progs.progs;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Random;

import java_progs.aux.ArrayListAux;
import java_progs.aux.WritePid;

public class GetArrayList {
    static int SIZE = 100_000_000;
    static int LOOP_SIZE = 2_000_000_000;

    private static int getArrayList(ArrayList<Integer> list, int n) {
        return list.get(n);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        ArrayList<Integer> list = new ArrayList<Integer>(SIZE);
        WritePid.writeTargetProgInfo(Long.toString(ProcessHandle.current().pid()),LOOP_SIZE);
        ArrayListAux.insertRandomNumbers(list,SIZE);
        int num = ArrayListAux.rand.nextInt((ArrayListAux.max - ArrayListAux.min) + 1) + ArrayListAux.min;
        Runtime.getRuntime().exec("kill -USR1 " + args[0]);
        Thread.sleep(100);
        long begin = System.nanoTime();
        long end = begin;
        int i = 0;
        while (end - begin < 1000000000/*1s*/) {
            getArrayList(list,num);
            end = System.nanoTime();
            i++;
        }
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS").format(new java.util.Date());
        WritePid.writeTargetProgInfo(timeStamp,i);
        Runtime.getRuntime().exec("kill -USR2 " + args[0]);
    }
}