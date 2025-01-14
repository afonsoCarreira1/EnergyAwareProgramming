

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java_progs.aux.ArrayListAux;
import java_progs.aux.WritePid;

public class m {

    static int SIZE = 5_000_000;
    static int loopSize = 1;

    private static Boolean containsAllElemRandomArrayList9(ArrayList<Integer> list, ArrayList<Integer> list2) {
        return list.containsAll(list2);
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws IOException, InterruptedException {
        ArrayList<Integer> list = new ArrayList<Integer>(SIZE*loopSize);
        ArrayList<Integer>[] lists = new ArrayList[loopSize];
        for (int i = 0; i < loopSize; i++) {
            System.out.println(i);
            lists[i] = ArrayListAux.insertRandomNumbers(new ArrayList<Integer>(SIZE),SIZE,"Integer");
            
        }
        
        ArrayListAux.insertRandomNumbers(list, SIZE,"Integer");
        WritePid.writeTargetProgInfo(Long.toString(ProcessHandle.current().pid()), 0);
        
        
        
        long begin = System.nanoTime();
        long end = begin;
        int i = 0;
        System.out.println("Aki");
        while (end - begin < 1000000000/* 1s */&& i < loopSize) {
            containsAllElemRandomArrayList9(list, lists[i]);
            end = System.nanoTime();
            i++;
        }
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS").format(new java.util.Date());
        WritePid.writeTargetProgInfo(timeStamp, i);
        
    }
    
}
