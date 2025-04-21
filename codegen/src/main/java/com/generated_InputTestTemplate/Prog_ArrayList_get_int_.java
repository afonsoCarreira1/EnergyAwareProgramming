package com.generated_InputTestTemplate;
import com.template.aux.DeepCopyUtil;
import com.template.aux.ArrayListAux;
import com.fasterxml.jackson.core.type.TypeReference;
import com.template.aux.TemplatesAux;
import java.util.ArrayList;
// add imports
public class Prog_ArrayList_get_int_ {
    // static int SIZE = "size";
    // static int loopSize = "loopSize";
    // create fun to benchmark
    // create computation fun
    // add @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        int iter = 0;
        int in0 = Integer.valueOf(args[0]);
        Integer in1 = Integer.valueOf(args[1]);
        try {
            ArrayList<Integer> var0 = new ArrayList();
            ArrayListAux.insertRandomNumbers(var0, ((int) (in1)), "Integer");
            int var1 = in0;
            BenchmarkArgs[] arr = new BenchmarkArgs[75000];
            populateArray(arr, var0, var1);
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
            System.exit(50);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(51);
        }
        System.out.println("aki");
        System.exit(0);
    }

    static class BenchmarkArgs {
        public ArrayList<Integer> var0;

        public int var1;

        BenchmarkArgs(ArrayList<Integer> var0, int var1) {
            this.var0 = DeepCopyUtil.deepCopy(var0, new TypeReference<ArrayList<Integer>>() {});
            this.var1 = DeepCopyUtil.deepCopy(var1, new TypeReference<Integer>() {});
        }
    }

    private static void arrayList_get_int_(ArrayList var, int arg0) {
        var.get(arg0);
    }

    private static int computation(BenchmarkArgs[] args, int iter) {
        int i = 0;
        while ((!TemplatesAux.stop) && (i < iter)) {
            arrayList_get_int_(args[i].var0, args[i].var1);
            i++;
        } 
        return iter;
    }

    private static void populateArray(BenchmarkArgs[] arr, ArrayList<Integer> var0, int var1) {
        for (int i = 0; i < 75000; i++) {
            arr[i] = new BenchmarkArgs(var0, var1);
        }
    }

    private String input1 = "ChangeValueHere1";

    private String input2 = "ChangeValueHere2";
}