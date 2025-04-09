package com.generated_InputTestTemplate;
import com.template.aux.DeepCopyUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.template.programsToBenchmark.*;
import com.template.aux.TemplatesAux;
import com.template.programsToBenchmark.TestTwoInputs;
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
        int in1 = Integer.valueOf(args[1]);
        try {
            TestTwoInputs var0 = new TestTwoInputs(in1, in0);
            BenchmarkArgs[] arr = new BenchmarkArgs[150000];
            populateArray(arr, var0);
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
            System.out.println(e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(2);
        }
        System.out.println("aki");
        System.exit(0);
    }

    static class BenchmarkArgs {
        public TestTwoInputs var0;

        BenchmarkArgs(TestTwoInputs var0) {
            this.var0 = DeepCopyUtil.deepCopy(var0, new TypeReference<TestTwoInputs>() {});
        }
    }

    private static void testTwoInputs_doNothing__(TestTwoInputs var) {
        var.doNothing();
    }

    private static int computation(BenchmarkArgs[] args, int iter) {
        int i = 0;
        while ((!TemplatesAux.stop) && (i < iter)) {
            testTwoInputs_doNothing__(args[i].var0);
            i++;
        } 
        return iter;
    }

    private static void populateArray(BenchmarkArgs[] arr, TestTwoInputs var0) {
        for (int i = 0; i < 150000; i++) {
            arr[i] = new BenchmarkArgs(var0);
        }
    }

    private String input1 = "ChangeValueHere1";

    private String input2 = "ChangeValueHere2";
}