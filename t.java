import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import java_progs.aux.ArrayListAux;
import java_progs.aux.TemplatesAux;

public class t {

    public static void main(String[] args) {
        ArrayList<Integer>[] arr = (ArrayList<Integer>[]) new ArrayList[15_000];
        for (int i = 0; i < arr.length; i++) {
            ArrayList<Integer> l = new ArrayList<>();
            l.add(1);
            arr[i] = l;
        }
        long begin = System.nanoTime();
        long t = 1000000000/1000;
            long end = begin;
            int iter = 0;
            while (end - begin < t) {
                arr[iter].size();
                end = System.nanoTime();
                iter++;
            }
            System.out.println(iter);
    }
}
