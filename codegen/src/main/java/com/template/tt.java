package com.template;
import com.template.aux.DeepCopyUtil;
import com.template.aux.CollectionAux;
import com.fasterxml.jackson.core.type.TypeReference;
import com.template.aux.TemplatesAux;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
// add imports
public class tt {
    // static int SIZE = "size";
    // static int loopSize = "loopSize";
    // create fun to benchmark
    // create computation fun
    // add @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        int iter = 0;
        Double in0 = Double.valueOf(args[0]);
        Double in1 = Double.valueOf(args[1]);

        try {
            CopyOnWriteArrayList<Double> var0 = new CopyOnWriteArrayList();
            CollectionAux.insertRandomNumbers(var0, in1, "Double");
            CopyOnWriteArrayList<Double> var1 = new CopyOnWriteArrayList();
            CollectionAux.insertRandomNumbers(var1, in0, "Double");
            BenchmarkArgs[] arr = new BenchmarkArgs[75000];
            populateArray(arr, var0, var1);
            TemplatesAux.sendStartSignalToOrchestrator(args[0]);
            TemplatesAux.launchTimerThread(1100);

            iter = computation(arr, arr.length);
            // if fun to test is Static.fun() then just create multiple inputs
            // if fun is var.fun() then start by creating multiple vars and then multiple inputs
            // have a fun to get multiple lists or vars
            // do: var[i].fun(in[i].arg1,in[i].arg2,...) or do: Static.fun(in[i].arg1,in[i].arg2,...)
            // first iteration to get aproximate number of times the fun can run in a second
            // long begin = System.nanoTime();
            // long end = begin;
            // int iterr = 0;
            // while (end - begin < 1000000000/* 1s */) {//add the && iter < loopSize
            // call fun to benchmark
            // end = System.nanoTime();
            // iter++;
            // }
            // clear and restart the vars for real measurement
            // send start signal for measurement
            // call computation fun
        } catch (OutOfMemoryError e) {
            // catch errors
            // TemplatesAux.writeErrorInFile("BubbleSort"filename"", "Out of memory error caught by the program.\n" + e.getMessage());
            TemplatesAux.writeErrorInFile("CopyOnWriteArrayList_removeAll_java_util_Collection_", "Out of memory error caught by the program:\n" + e.getMessage());
        } catch (Exception e) {
            // TemplatesAux.writeErrorInFile("BubbleSort"filename"","Error caught by the program.\n"+e.getMessage());
            TemplatesAux.writeErrorInFile("CopyOnWriteArrayList_removeAll_java_util_Collection_", "Error caught by the program:\n" + e.getMessage());
        } finally {
            TemplatesAux.sendStopSignalToOrchestrator(args[0], iter);
        }
    }

    static class BenchmarkArgs {
        public CopyOnWriteArrayList<Double> var0;

        public CopyOnWriteArrayList<Double> var1;

        BenchmarkArgs(CopyOnWriteArrayList<Double> var0, CopyOnWriteArrayList<Double> var1) {
            this.var0 = DeepCopyUtil.deepCopy(var0, new TypeReference<CopyOnWriteArrayList<Double>>(){});
            this.var1 = DeepCopyUtil.deepCopy(var1, new TypeReference<CopyOnWriteArrayList<Double>>(){});
        }
    }

    private static void copyOnWriteArrayList_removeAll_java_util_Collection_(CopyOnWriteArrayList var, Collection<?> arg0) {
        var.removeAll(arg0);
    }

    private static int computation(BenchmarkArgs[] args, int iter) {
        int i = 0;
        while (!TemplatesAux.stop && i < iter) {
              copyOnWriteArrayList_removeAll_java_util_Collection_(args[i].var0, args[i].var1);
               i++;
        }
        return iter;
    }

    private static void populateArray(BenchmarkArgs[] arr, CopyOnWriteArrayList<Double> var0, CopyOnWriteArrayList<Double> var1) {
        for (int i = 0;i < 75000;i++) {
          arr[i] = new BenchmarkArgs(var0, var1);
        };
    }

    private String input1 = "ChangeValueHere1";

    private String input2 = "ChangeValueHere2";
}
