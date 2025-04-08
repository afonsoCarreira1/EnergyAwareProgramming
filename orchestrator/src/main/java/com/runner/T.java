package com.runner;
import com.template.aux.DeepCopyUtil;
import com.template.aux.ArrayListAux;
import com.fasterxml.jackson.core.type.TypeReference;
import com.template.aux.TemplatesAux;
import java.util.ArrayList;
import java.util.Collection;
import spoon.support.reflect.reference.CtTypeReferenceImpl;
// add imports
public class T {

    public static void main(String[] args) throws Exception {
        int iter = 0;
        ArrayList<Integer> var0 = new ArrayList();
        ArrayListAux.insertRandomNumbers(var0, 100000, "Integer");
        int var1 = 0;
        ArrayList<Integer> var2 = new ArrayList();
        ArrayListAux.insertRandomNumbers(var2, 1, "Integer");
        BenchmarkArgs[] arr = new BenchmarkArgs[20000];
        populateArray(arr, var0, var1, var2);
        TemplatesAux.launchTimerThread(1100);
        iter = computation(arr, arr.length);
    }

    static class BenchmarkArgs {
        public ArrayList<Integer> var0 = new ArrayList();

        public int var1 = 0;

        public ArrayList<Integer> var2 = new ArrayList();

        BenchmarkArgs(ArrayList<Integer> var0, int var1, ArrayList<Integer> var2) {
            this.var0 = DeepCopyUtil.deepCopy(var0, new TypeReference<ArrayList<Integer>>(){});
            this.var1 = DeepCopyUtil.deepCopy(var1, new TypeReference<Integer>(){});
            this.var2 = DeepCopyUtil.deepCopy(var2, new TypeReference<ArrayList<Integer>>(){});
        }
    }

    private static void arrayList_addAll_int_java_util_Collection_2400(ArrayList var, int arg0, Collection<?> arg1) {
        var.addAll(arg0, arg1);
    }

    private static int computation(BenchmarkArgs[] args, int iter) {
        int i = 0;
        while (!TemplatesAux.stop && i < iter) {
              arrayList_addAll_int_java_util_Collection_2400(args[i].var0, args[i].var1, args[i].var2);
               i++;
        };
        return i;
    }

    private static void populateArray(BenchmarkArgs[] arr, ArrayList<Integer> var0, int var1, ArrayList<Integer> var2) {
        for (int i = 0;i < 20000;i++) {
          arr[i] = new BenchmarkArgs(var0, var1, var2);
        };
    }

    private String input1 = "1";

    private String input3 = "0";

    private String input5 = "0";
}
