import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import java_progs.aux.ArrayListAux;
import java_progs.aux.TemplatesAux;

public class t {

    static int SIZE = 330787;
    static int loopSize = 24;

    private static void defaultSort(ArrayList<Integer> list) {
        Collections.sort(list);
    }

    private static void computation(ArrayList<Integer>[] arr, int iter) {
        int i = 0;
        System.out.println("iter -> "+iter);
        while (!TemplatesAux.stop && i < iter) {
            defaultSort(arr[i]);
            i++;
        }
        System.out.println("i -> "+i);
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws IOException, InterruptedException {
        try {
            ArrayList<Integer> list = new ArrayList<Integer>();
            ArrayList<Integer>[] arr = new ArrayList[loopSize];
            for (int i = 0; i < loopSize; i++) {
                ArrayList<Integer> l = new ArrayList<Integer>();
                ArrayListAux.insertRandomNumbers(l, SIZE, "Integer");
                arr[i] = l;
            }
            ArrayListAux.insertRandomNumbers(list, SIZE, "Integer");
            long begin = System.nanoTime();
            long end = begin;
            int iter = 0;
            while (end - begin < 1000000000/* 1s */ && iter < loopSize) {
                defaultSort(arr[iter]);
                end = System.nanoTime();
                iter++;
            }
            System.out.println("first end -> " + ((end-begin))+"ns");
            
            System.out.println("amansa -> " + arr[0].get(0));
            list.clear();
            for (int i = 0; i < loopSize; i++) {
                arr[i].clear();
            }Arrays.fill(arr, null);
            for (int i = 0; i < loopSize; i++) {
                ArrayList<Integer> l = new ArrayList<Integer>();
                ArrayListAux.insertRandomNumbers(l, SIZE, "Integer");
                arr[i] = l;
            }
            System.out.println("amansa2 -> " + arr[0].get(0));
            ArrayListAux.insertRandomNumbers(list, SIZE, "Integer");
            begin = System.currentTimeMillis();
            TemplatesAux.launchTimerThread();
            computation(arr, iter);
            System.out.println((System.currentTimeMillis()-begin)/1000.0);
        } catch (OutOfMemoryError e) {
            TemplatesAux.writeErrorInFile("t", "Out of memory error caught by the program.\n" + e.getMessage());
        } 
        catch (Exception e) {
            TemplatesAux.writeErrorInFile("t","Error caught by the program.\n"+e.getMessage());
        }

    }

}
