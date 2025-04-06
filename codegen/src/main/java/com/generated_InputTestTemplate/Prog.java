package com.generated_InputTestTemplate;
import com.template.aux.DeepCopyUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.template.programsToBenchmark.*;
import com.template.aux.TemplatesAux;
import com.template.programsToBenchmark.Fibonacci;
import com.template.programsToBenchmark.Test;
// add imports
public class Prog {
    // static int SIZE = "size";
    // static int loopSize = "loopSize";
    // create fun to benchmark
    // create computation fun
    // add @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        int iter = 0;
        int in0 = Integer.valueOf(args[0]);

        try {
            Fibonacci var0 = new Fibonacci(new Test(in0));
            BenchmarkArgs[] arr = new BenchmarkArgs[150000];
            populateArray(arr, var0);
            TemplatesAux.sendStartSignalToOrchestrator(args[0]);
            TemplatesAux.launchTimerThread();
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
            TemplatesAux.writeErrorInFile("Fibonacci_fibonacci__", "Out of memory error caught by the program:\n" + e.getMessage());
        } catch (Exception e) {
            // TemplatesAux.writeErrorInFile("BubbleSort"filename"","Error caught by the program.\n"+e.getMessage());
            TemplatesAux.writeErrorInFile("Fibonacci_fibonacci__", "Error caught by the program:\n" + e.getMessage());
        } finally {
            TemplatesAux.sendStopSignalToOrchestrator(args[0], iter);
        }
    }

    static class BenchmarkArgs {
        public Fibonacci var0;

        BenchmarkArgs(Fibonacci var0) {
            this.var0 = DeepCopyUtil.deepCopy(var0, new TypeReference<Fibonacci>(){});
        }
    }

    private static void fibonacci_fibonacci__(Fibonacci var) {
        var.fibonacci();
    }

    private static int computation(BenchmarkArgs[] args, int iter) {
        int i = 0;
        while (!TemplatesAux.stop && i < iter) {
              fibonacci_fibonacci__(args[i].var0);
               i++;
        }
        return iter;
    }

    private static void populateArray(BenchmarkArgs[] arr, Fibonacci var0) {
        for (int i = 0;i < 150000;i++) {
          arr[i] = new BenchmarkArgs(var0);
        };
    }

    private String input1 = "ChangeValueHere1";
}
