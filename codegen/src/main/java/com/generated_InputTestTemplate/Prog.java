package com.generated_InputTestTemplate;
import com.template.aux.DeepCopyUtil;
import com.template.aux.ArrayListAux;
import com.fasterxml.jackson.core.type.TypeReference;
import com.template.aux.TemplatesAux;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import spoon.support.reflect.reference.CtTypeReferenceImpl;
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
        Integer in1 = Integer.valueOf(args[1]);
        Integer in2 = Integer.valueOf(args[2]);
        try {
            CopyOnWriteArrayList<Integer> var0 = new CopyOnWriteArrayList();
            ArrayListAux.insertRandomNumbers(var0, in2, "Integer");
            int var1 = in0;
            CopyOnWriteArrayList<Integer> var2 = new CopyOnWriteArrayList();
            ArrayListAux.insertRandomNumbers(var2, in1, "Integer");
            BenchmarkArgs[] arr = new BenchmarkArgs[75000];
            populateArray(arr, var0, var1, var2);
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
        public CopyOnWriteArrayList<Integer> var0;

        public int var1;

        public CopyOnWriteArrayList<Integer> var2;

        BenchmarkArgs(CopyOnWriteArrayList<Integer> var0, int var1, CopyOnWriteArrayList<Integer> var2) {
            this.var0 = DeepCopyUtil.deepCopy(var0, new TypeReference<CopyOnWriteArrayList<Integer>>() {});
            this.var1 = DeepCopyUtil.deepCopy(var1, new TypeReference<Integer>() {});
            this.var2 = DeepCopyUtil.deepCopy(var2, new TypeReference<CopyOnWriteArrayList<Integer>>() {});
        }
    }

    private static void copyOnWriteArrayList_addAll_int_java_util_Collection_(CopyOnWriteArrayList var, int arg0, Collection<?> arg1) {
        var.addAll(arg0, arg1);
    }

    private static int computation(BenchmarkArgs[] args, int iter) {
        int i = 0;
        while ((!TemplatesAux.stop) && (i < iter)) {
            copyOnWriteArrayList_addAll_int_java_util_Collection_(args[i].var0, args[i].var1, args[i].var2);
            i++;
        } 
        return iter;
    }

    private static void populateArray(BenchmarkArgs[] arr, CopyOnWriteArrayList<Integer> var0, int var1, CopyOnWriteArrayList<Integer> var2) {
        for (int i = 0; i < 75000; i++) {
            arr[i] = new BenchmarkArgs(var0, var1, var2);
        }
    }

    private String input1 = "ChangeValueHere1";

    private String input2 = "ChangeValueHere2";

    private String input3 = "ChangeValueHere3";
}