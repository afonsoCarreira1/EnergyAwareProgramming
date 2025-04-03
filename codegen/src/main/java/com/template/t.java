package com.template;
import com.template.aux.DeepCopyUtil;
import com.template.aux.ArrayListAux;
import com.fasterxml.jackson.core.type.TypeReference;
import com.template.aux.TemplatesAux;
import java.util.concurrent.CopyOnWriteArrayList;
import spoon.support.reflect.reference.CtTypeReferenceImpl;
// add imports
public class t {

    public static void main(String[] args) throws Exception {
            //CopyOnWriteArrayList<Character> var0 = new CopyOnWriteArrayList();
            //ArrayListAux.insertRandomNumbers(var0, 245, "Character");
            //int var1 = 71;
            //BenchmarkArgs[] arr = new BenchmarkArgs[150_000];
            //populateArray(arr, var0, var1);
            //long begin = System.nanoTime();
            //long end = begin;
            //int iter = 0;
            ///* 1s */
            //while (end - begin < 1000000000 && iter < arr.length) {
            //    copyOnWriteArrayList_get_int_6234(arr[iter].var0, arr[iter].var1);
            //    end = System.nanoTime();
            //    iter++;
            //} 
            //clearArr(arr);
            //populateArray(arr, var0, var1);
            //TemplatesAux.launchTimerThread();
            //long st = System.currentTimeMillis();
            //computation(arr, iter);
            //long et = System.currentTimeMillis();
            //long dif = et-st;
            //System.out.println("time dif -> "+dif+"ms");
            CopyOnWriteArrayList<Integer> l = new CopyOnWriteArrayList();
            ArrayListAux.insertRandomNumbers(l, 245, "Integer");
            TemplatesAux.launchTimerThread();
            long st = System.currentTimeMillis();
            int i = 0;
            while (!TemplatesAux.stop) {
                l.get(0);
                i++;
            }
            long et = System.currentTimeMillis();
            long dif = et-st;
            System.out.println("time dif -> "+dif+"ms\nNumber of operations = "+i);

        
    }

    static class BenchmarkArgs {
        public CopyOnWriteArrayList<Character> var0 = new CopyOnWriteArrayList();

        public int var1 = 71;

        BenchmarkArgs(CopyOnWriteArrayList<Character> var0, int var1) {
            this.var0 = DeepCopyUtil.deepCopy(var0, new TypeReference<CopyOnWriteArrayList<Character>>(){});
            this.var1 = DeepCopyUtil.deepCopy(var1, new TypeReference<Integer>(){});
        }
    }

    private static void copyOnWriteArrayList_get_int_6234(CopyOnWriteArrayList var, int arg0) {
        var.get(arg0);
    }

    private static void computation(BenchmarkArgs[] args, int iter) {
        int i = 0;
        while (!TemplatesAux.stop && i < iter) {
              copyOnWriteArrayList_get_int_6234(args[i].var0, args[i].var1);
               i++;
        };
    }

    private static void populateArray(BenchmarkArgs[] arr, CopyOnWriteArrayList<Character> var0, int var1) {
        for (int i = 0;i < 150000;i++) {
          arr[i] = new BenchmarkArgs(var0, var1);
        };
    }

    private static void clearArr(BenchmarkArgs[] arr) {
        for (int i = 0; i < arr.length; i++) {
          arr[i] = null;
        }
        System.gc();
    }

    private String input1 = "245";

    private String input3 = "71";
}
