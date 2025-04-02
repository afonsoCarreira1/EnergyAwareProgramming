package com.runner;
import com.template.aux.DeepCopyUtil;
import com.template.aux.ArrayListAux;
import com.fasterxml.jackson.core.type.TypeReference;
import com.template.aux.TemplatesAux;
import java.util.concurrent.CopyOnWriteArrayList;
import spoon.support.reflect.reference.CtTypeReferenceImpl;

public class CopyOnWriteArrayList_add_int_java_lang_Object_2401 {
    static int size = 100;
    public static void main(String[] args) throws Exception {
        
            CopyOnWriteArrayList<Integer> var0 = new CopyOnWriteArrayList();
            ArrayListAux.insertRandomNumbers(var0, 150, "Integer");
            System.out.println("first arrayNum fill");
            int var1 = 1;
            Integer var2 = 29761;
            BenchmarkArgs[] arr = new BenchmarkArgs[size];
            populateArray(arr, var0, var1, var2);
            System.out.println("first populate");
            long begin = System.nanoTime();
            long end = begin;
            int iter = 0;
            /* 1s */
            while (end - begin < 1000000000 && iter < arr.length) {
                copyOnWriteArrayList_add_int_java_lang_Object_2401(arr[iter].var0, arr[iter].var1, arr[iter].var2);
                // add the && iter < loopSize
                // call fun to benchmark
                end = System.nanoTime();
                iter++;
            } 
            System.out.println("first iteration");
            clearArr(arr);
            System.out.println("clear");
            populateArray(arr, var0, var1, var2);
            System.out.println("2nd populate");
            TemplatesAux.launchTimerThread();
            computation(arr, iter);
            System.out.println("computation");
            // clear and restart the vars for real measurement
            // send start signal for measurement
            // call computation fun
        
    }

    static class BenchmarkArgs {
        public CopyOnWriteArrayList<Integer> var0 = new CopyOnWriteArrayList();

        public int var1 = 731;

        public Integer var2 = 29761;

        BenchmarkArgs(CopyOnWriteArrayList<Integer> var0, int var1, Integer var2) {
            this.var0 = DeepCopyUtil.deepCopy(var0, new TypeReference<CopyOnWriteArrayList<Integer>>(){});
            this.var1 = DeepCopyUtil.deepCopy(var1, new TypeReference<Integer>(){});
            this.var2 = DeepCopyUtil.deepCopy(var2, new TypeReference<Integer>(){});
        }
    }

    private static void copyOnWriteArrayList_add_int_java_lang_Object_2401(CopyOnWriteArrayList var, int arg0, Integer arg1) {
        var.add(arg0, arg1);
    }

    private static void computation(BenchmarkArgs[] args, int iter) {
        int i = 0;
        while (!TemplatesAux.stop && i < iter) {
              copyOnWriteArrayList_add_int_java_lang_Object_2401(args[i].var0, args[i].var1, args[i].var2);
               i++;
        };
    }

    private static void populateArray(BenchmarkArgs[] arr, CopyOnWriteArrayList<Integer> var0, int var1, Integer var2) {
        for (int i = 0;i < size;i++) {
            System.out.println("going to deepcopy "+i);   
            arr[i] = new BenchmarkArgs(var0, var1, var2);
            System.out.println("finished deepcopy "+i); 
        }
    }

    private static void clearArr(BenchmarkArgs[] arr) {
        for (int i = 0; i < arr.length; i++) {
          arr[i] = null;
        }
        System.gc();
    }

    private String input1 = "33186";

    private String input3 = "731";

    private String input5 = "29761";
}
