package com.runner;
import com.template.aux.DeepCopyUtil;
import com.template.aux.ArrayListAux;
import com.fasterxml.jackson.core.type.TypeReference;
import com.template.aux.TemplatesAux;
import java.util.ArrayList;
import spoon.support.reflect.reference.CtTypeReferenceImpl;
// add imports
public class t {
    // static int SIZE = "size";
    // static int loopSize = "loopSize";
    // create fun to benchmark
    // create computation fun
    // add @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        
            ArrayList<Integer> var0 = new ArrayList();
            ArrayListAux.insertRandomNumbers(var0, 150, "Integer");
            int var1 = 96;
            Integer var2 = 78;
            BenchmarkArgs[] arr = new BenchmarkArgs[20000];
            populateArray(arr, var0, var1, var2);
            // if fun to test is Static.fun() then just create multiple inputs
            // if fun is var.fun() then start by creating multiple vars and then multiple inputs
            // have a fun to get multiple lists or vars
            // do: var[i].fun(in[i].arg1,in[i].arg2,...) or do: Static.fun(in[i].arg1,in[i].arg2,...)
            // first iteration to get aproximate number of times the fun can run in a second
            long begin = System.nanoTime();
            long end = begin;
            int iter = 0;
            /* 1s */
            while (end - begin < 1000000000 && iter < arr.length) {
                arrayList_add_int_java_lang_Object_1(arr[iter].var0, arr[iter].var1, arr[iter].var2);
                // add the && iter < loopSize
                // call fun to benchmark
                end = System.nanoTime();
                iter++;
            } 
            clearArr(arr);
            populateArray(arr, var0, var1, var2);
            TemplatesAux.launchTimerThread();
            computation(arr, iter);
            // clear and restart the vars for real measurement
            // send start signal for measurement
            // call computation fun
        
    }

    static class BenchmarkArgs {
        public ArrayList<Integer> var0 = new ArrayList();

        public int var1 = 96;

        public Integer var2 = 78;

        BenchmarkArgs(ArrayList<Integer> var0, int var1, Integer var2) {
            this.var0 = DeepCopyUtil.deepCopy(var0, new TypeReference<ArrayList<Integer>>(){});
            this.var1 = DeepCopyUtil.deepCopy(var1, new TypeReference<Integer>(){});
            this.var2 = DeepCopyUtil.deepCopy(var2, new TypeReference<Integer>(){});
        }
    }

    private static void arrayList_add_int_java_lang_Object_1(ArrayList var, int arg0, Integer arg1) {
        var.add(arg0, arg1);
    }

    private static void computation(BenchmarkArgs[] args, int iter) {
        int i = 0;
        while (!TemplatesAux.stop && i < iter) {
              arrayList_add_int_java_lang_Object_1(args[i].var0, args[i].var1, args[i].var2);
               i++;
        };
    }

    private static void populateArray(BenchmarkArgs[] arr, ArrayList<Integer> var0, int var1, Integer var2) {
        for (int i = 0;i < 20000;i++) {
          arr[i] = new BenchmarkArgs(var0, var1, var2);
        };
    }

    private static void clearArr(BenchmarkArgs[] arr) {
        for (int i = 0; i < arr.length; i++) {
          arr[i] = null;
        }
        System.gc();
    }

    private String input1 = "150";

    private String input3 = "96";

    private String input5 = "78";
}
