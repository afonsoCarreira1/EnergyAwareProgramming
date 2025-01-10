package java_progs.progs_edit_later;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java_progs.aux.ArrayListAux;
import java_progs.aux.WritePid;

public class ContainsElemTrueArrayList {

    static int SIZE = 10_000_000;
    static int loopSize = 1_000;

    private static Boolean containsElemTrueArrayList(ArrayList<Integer> list, int n) {
        return list.contains(n);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        ArrayList<Integer> list = new ArrayList<Integer>(SIZE);
        ArrayListAux.insertRandomNumbers(list, SIZE);
        WritePid.writeTargetProgInfo(Long.toString(ProcessHandle.current().pid()), loopSize);
        int num = ArrayListAux.getRandomNumber();
        Runtime.getRuntime().exec(new String[] { "kill", "-USR1", args[0] });
        Thread.sleep(100);
        long begin = System.nanoTime();
        long end = begin;
        int i = 0;
        while (end - begin < 1000000000/* 1s */) {
            containsElemTrueArrayList(list, num);
            end = System.nanoTime();
            i++;
        }
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS").format(new java.util.Date());
        WritePid.writeTargetProgInfo(timeStamp, i);
        Runtime.getRuntime().exec(new String[] { "kill", "-USR2", args[0] });
    }

}
