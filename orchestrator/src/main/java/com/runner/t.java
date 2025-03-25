package com.runner;
import com.template.aux.DeepCopyUtil;
import com.template.aux.ArrayListAux;
import com.template.aux.TemplatesAux;
import java.util.ArrayList;
import spoon.support.reflect.reference.CtTypeReferenceImpl;
// add imports
public class t {
    static int size =20000;
    // static int SIZE = "size";
    // static int loopSize = "loopSize";
    // create fun to benchmark
    // create computation fun
    // add @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        
            ArrayList<Integer> var0 = new ArrayList();
            ArrayListAux.insertRandomNumbers(var0, 150, "Integer");
            Integer var1 = 147;
            BenchmarkArgs[] arr = new BenchmarkArgs[size];
            populateArray(arr, var0, var1);
            System.out.println("aki1");
            // if fun to test is Static.fun() then just create multiple inputs
            // if fun is var.fun() then start by creating multiple vars and then multiple inputs
            // have a fun to get multiple lists or vars
            // do: var[i].fun(in[i].arg1,in[i].arg2,...) or do: Static.fun(in[i].arg1,in[i].arg2,...)
            // first iteration to get aproximate number of times the fun can run in a second
            long begin = System.nanoTime();
            long end = begin;
            int iter = 0;
            /* 1s */
            while ((end - begin) < 1000000000 && iter< size) {
                arrayList_add_java_lang_Object_(arr[iter].var0, arr[iter].var1);
                // add the && iter < loopSize
                // call fun to benchmark
                end = System.nanoTime();
                iter++;
            } 
            System.out.println("aki2");
            clearArr(arr);
            System.out.println("aki3");
            populateArray(arr, var0, var1);
            System.out.println("aki4");
            TemplatesAux.launchTimerThread();
            computation(arr, iter);
            System.out.println("aki5");
            // clear and restart the vars for real measurement
            // send start signal for measurement
            // call computation fun
        
    }

    static class BenchmarkArgs {
        public ArrayList<Integer> var0 = new ArrayList();

        public Integer var1 = 147;

        BenchmarkArgs(ArrayList<Integer> var0, Integer var1) {
            this.var0 = DeepCopyUtil.deepCopy(var0);
            this.var1 = DeepCopyUtil.deepCopy(var1);
        }
    }

    private static void arrayList_add_java_lang_Object_(ArrayList var, Integer arg0) {
        var.add(arg0);
    }

    private static void computation(BenchmarkArgs[] args, int iter) {
        int i = 0;
        while (!TemplatesAux.stop && i < iter) {
              arrayList_add_java_lang_Object_(args[i].var0, args[i].var1);
               i++;
        };
    }

    private static void populateArray(BenchmarkArgs[] arr, ArrayList<Integer> var0, Integer var1) {
        for (int i = 0;i < size;i++) {
          arr[i] = new BenchmarkArgs(var0, var1);
        };
    }

    private static void clearArr(BenchmarkArgs[] arr) {
        for (int i = 0; i < arr.length; i++) {
          arr[i] = null;
        }
        System.gc();
    }

    private String input1 = "150";

    private String input3 = "147";
}
