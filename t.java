

import java.io.IOException;
import java.util.ArrayList;

import java_progs.aux.ArrayListAux;
import java_progs.aux.TemplatesAux;

public class t {

    static int SIZE = 1000;
    static int loopSize = 1_000_000;

    private static Boolean containsAllElemRandomArrayList(ArrayList<Integer> list, ArrayList<Integer> list2) {
        return list.containsAll(list2);
    }

    private static void computation(ArrayList<Integer> list, ArrayList<Integer>[] lists, int iter) {
        int i = 0;
        while (!TemplatesAux.stop && i < iter) {
            containsAllElemRandomArrayList(list, lists[i]);
            i++;
        }
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws IOException, InterruptedException {
        try {
            ArrayList<Integer> list = new ArrayList<Integer>(SIZE * loopSize);
            ArrayList<Integer>[] lists = new ArrayList[loopSize];
            for (int i = 0; i < loopSize; i++) {
                ArrayList<Integer> l = new ArrayList<Integer>(SIZE);
                ArrayListAux.insertRandomNumbers(l, SIZE, "Integer");
                lists[i] = l;
            }
            ArrayListAux.insertRandomNumbers(list, SIZE, "Integer");
            long begin = System.nanoTime();
            long end = begin;
            int iter = 0;
            while (end - begin < 1000000000/* 1s */ && iter < loopSize) {
                containsAllElemRandomArrayList(list, lists[iter]);
                end = System.nanoTime();
                iter++;
            }
            //TemplatesAux.sendStartSignalToOrchestrator(args[0],iter);
            TemplatesAux.launchTimerThread();
            computation(list, lists, iter);
        } catch (OutOfMemoryError e) {
            TemplatesAux.writeErrorInFile("ArrayListContainsAllElemRandomMem","Out of memory error caught by the program.\n" + e.getMessage());
        } catch (Exception e) {
            TemplatesAux.writeErrorInFile("ArrayListContainsAllElemRandomExpt",
                    "Error caught by th program.\n" + e.getMessage());
        } finally {
            //TemplatesAux.sendStopSignalToOrchestrator(args[0]);
        }

    }

}
