package com.runner;
import com.template.aux.DeepCopyUtil;
import com.template.aux.ArrayListAux;
import com.fasterxml.jackson.core.type.TypeReference;
import com.template.aux.TemplatesAux;
import java.util.ArrayList;
import java.util.Collection;
// add imports
public class T {
    public static void main(String[] args) throws Exception {
        int iter = 0;
            ArrayList<Integer> var0 = new ArrayList();
            ArrayListAux.insertRandomNumbers(var0, (int) 622, "Integer");
            int var1 = 1;
            ArrayList<Integer> var2 = new ArrayList();
            ArrayListAux.insertRandomNumbers(var2, (int) 154, "Integer");
            BenchmarkArgs[] arr = new BenchmarkArgs[150000];
            populateArray(arr, var0, var1, var2);
            TemplatesAux.launchTimerThread(1100);
            iter = computation(arr, arr.length);
            System.out.println("iter -> "+iter);
    }

    static class BenchmarkArgs {
        public ArrayList<Integer> var0;

        public int var1;

        public ArrayList<Integer> var2;

        BenchmarkArgs(ArrayList<Integer> var0, int var1, ArrayList<Integer> var2) {
            this.var0 = DeepCopyUtil.deepCopy(var0, new TypeReference<ArrayList<Integer>>(){});
            this.var1 = DeepCopyUtil.deepCopy(var1, new TypeReference<Integer>(){});
            this.var2 = DeepCopyUtil.deepCopy(var2, new TypeReference<ArrayList<Integer>>(){});
        }
    }

    private static void arrayList_addAll_int_java_util_Collection_1243(ArrayList var, int arg0, Collection<?> arg1) {
        var.addAll(arg0, arg1);
    }

    private static int computation(BenchmarkArgs[] args, int iter) {
        int i = 0;
        while (!TemplatesAux.stop && i < iter) {
              arrayList_addAll_int_java_util_Collection_1243(args[i].var0, args[i].var1, args[i].var2);
               i++;
        }
        return iter;
    }

    private static void populateArray(BenchmarkArgs[] arr, ArrayList<Integer> var0, int var1, ArrayList<Integer> var2) {
        for (int i = 0;i < 150000;i++) {
          arr[i] = new BenchmarkArgs(var0, var1, var2);
        };
    }

    private String input1 = "622";

    private String input2 = "1";

    private String input3 = "154";
}
