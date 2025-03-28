package com.template;
import com.template.aux.DeepCopyUtil;
import com.template.aux.ArrayListAux;
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
        try {
            ArrayList<Integer> var0 = new ArrayList();
            ArrayListAux.insertRandomNumbers(var0, 150, "Integer");
            Integer var1 = 112;
            BenchmarkArgs[] arr = new BenchmarkArgs[20000];
            populateArray(arr, var0, var1);
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
                arrayList_add_java_lang_Object_(arr[iter].var0, arr[iter].var1);
                // add the && iter < loopSize
                // call fun to benchmark
                end = System.nanoTime();
                iter++;
            } 
            clearArr(arr);
            populateArray(arr, var0, var1);
            TemplatesAux.sendStartSignalToOrchestrator(args[0],iter);
            TemplatesAux.launchTimerThread();
            computation(arr, iter);
            // clear and restart the vars for real measurement
            // send start signal for measurement
            // call computation fun
        } catch (OutOfMemoryError e) {
            // catch errors
            // TemplatesAux.writeErrorInFile("BubbleSort"filename"", "Out of memory error caught by the program.\n" + e.getMessage());
            TemplatesAux.writeErrorInFile("ArrayList_add_java_lang_Object_6", "Out of memory error caught by the program:\n" + e.getMessage());
        } catch (Exception e) {
            // TemplatesAux.writeErrorInFile("BubbleSort"filename"","Error caught by the program.\n"+e.getMessage());
            TemplatesAux.writeErrorInFile("ArrayList_add_java_lang_Object_6", "Error caught by the program:\n" + e.getMessage());
        } finally {
            //TemplatesAux.sendStopSignalToOrchestrator(args[0]);
        }
    }

    static class BenchmarkArgs {
        public ArrayList<Integer> var0 = new ArrayList();

        public Integer var1 = 112;

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
        for (int i = 0;i < 20000;i++) {
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

    private String input3 = "112";
}
