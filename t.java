

import java.io.IOException;
import java.util.ArrayList;

import java_progs.aux.ArrayListAux;
import java_progs.aux.TemplatesAux;

public class t {
    static int SIZE = 50_000_000;
    static int loopSize = 2;

    private static Boolean addAllElemArrayList6(ArrayList<Integer> list, ArrayList<Integer> list2) {
        return list.addAll(list2);
    }

    private static void computation(ArrayList<Integer> list,ArrayList<Integer>[] lists, int iter) {
        int i = 0;
        while (!TemplatesAux.stop && i<iter) {
            addAllElemArrayList6(list, lists[i]);
            i++;
        }
    }
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws IOException, InterruptedException {
        int iter = 0;
        long st = System.currentTimeMillis();
        try {
            ArrayList<Integer> list  = new ArrayList <Integer>();
            ArrayList<Integer>[] lists  = (ArrayList<Integer>[]) new ArrayList[loopSize];
            for (int i = 0; i < loopSize; i++) {
                ArrayList<Integer> l  = new ArrayList <Integer>();
                ArrayListAux.insertRandomNumbers(l,SIZE,"Integer");
                lists[i] = l;
            }
            ArrayListAux.insertRandomNumbers(list, SIZE,"Integer");
            long begin = System.nanoTime();
            long end = begin;
            while (end - begin < 1000000000/* 1s */&& iter < loopSize) {
                addAllElemArrayList6(list, lists[iter]);
                end = System.nanoTime();
                iter++;
            }
            TemplatesAux.launchTimerThread();
            computation(list,lists,iter);
        } catch (OutOfMemoryError e) {
            TemplatesAux.writeErrorInFile("AddAllElemArrayList6", "Out of memory error caught by the program.\n" + e.getMessage());
        } 
        catch (Exception e) {
            TemplatesAux.writeErrorInFile("AddAllElemArrayList6","Error caught by th program.\n"+e.getMessage());
        }
        double duration = (System.currentTimeMillis()-st) / 1000.0;
        System.out.println(duration);
    }
    
}
