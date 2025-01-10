

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java_progs.aux.ArrayListAux;
import java_progs.aux.WritePid;

public class TemplateAddAllElemArrayList {
    static int SIZE = 250000;
    static int loopSize = 100;

    private static Boolean addAllElemArrayList(ArrayList<Double> list, ArrayList<Double> list2) {
        return list.addAll(list2);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        ArrayList<Double> list = new ArrayList<Double>(SIZE*loopSize);
        ArrayList<Double>[] lists = new ArrayList<Double>[loopSize];
        for (int i = 0; i < loopSize; i++) {
            lists[i] = ArrayListAux.insertRandomNumbers(new ArrayList<>(SIZE),SIZE,"Double");
        }
        ArrayListAux.insertRandomNumbers(list, SIZE,"Double");
        WritePid.writeTargetProgInfo(Long.toString(ProcessHandle.current().pid()), 0);
        Runtime.getRuntime().exec(new String[] { "kill", "-USR1", args[0] });
        Thread.sleep(100);
        long begin = System.nanoTime();
        long end = begin;
        int i = 0;
        while (end - begin < 1000000000/* 1s */&& i < loopSize) {
            addAllElemArrayList(list, lists[i]);
            end = System.nanoTime();
            i++;
        }
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS").format(new java.util.Date());
        WritePid.writeTargetProgInfo(timeStamp, i);
        Runtime.getRuntime().exec(new String[] { "kill", "-USR2", args[0] });
    }
    
}
