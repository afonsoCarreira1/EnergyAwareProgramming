package com.template;
import com.template.aux.DeepCopyUtil;
import com.template.aux.CollectionAux;
import com.fasterxml.jackson.core.type.TypeReference;
import com.template.aux.TemplatesAux;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Stack;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

public class T {

    public static void main(String[] args) throws Exception {
        int iter = 0;
        long st = 0;
        long et = 0;
        try {
            ArrayList<Integer> var0 = new ArrayList();
            CollectionAux.insertRandomNumbers(var0, (int) 407, "Integer");
            int var1 = 1;
            ArrayList<Integer> var2 = new ArrayList();
            CollectionAux.insertRandomNumbers(var2, (int) 597, "Integer");
            BenchmarkArgs[] arr = new BenchmarkArgs[75000];
            populateArray(arr, var0, var1, var2);
            TemplatesAux.launchTimerThread(1100);
            st = System.currentTimeMillis() ;
            iter = computation(arr, arr.length);
            et = System.currentTimeMillis() ;
        } catch (OutOfMemoryError e) {
            System.out.println("out of mem");
        } catch (Exception e) {
            System.out.println("Exception");
        } finally {
            System.out.println("stop -> "+iter);
            System.out.println("time taken "+(et-st) +"ms");
            LinkedList ll = new LinkedList<>();
            CopyOnWriteArrayList cpl = new CopyOnWriteArrayList<>();
            ArrayList l = new ArrayList<>();
            Vector v = new Vector<>();
            Stack s = new Stack<>();
            ll.removeIf(null);
            cpl.removeIf(null);
            l.removeIf(null);
            v.removeIf(null);
            s.removeIf(null);
        }
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

    private static void arrayList_addAll_int_java_util_Collection_1200(ArrayList var, int arg0, Collection<?> arg1) {
        var.addAll(arg0, arg1);
    }

    private static int computation(BenchmarkArgs[] args, int iter) {
        int i = 0;
        while (!TemplatesAux.stop && i < iter) {
              arrayList_addAll_int_java_util_Collection_1200(args[i].var0, args[i].var1, args[i].var2);
               i++;
        }
        return iter;
    }

    private static void populateArray(BenchmarkArgs[] arr, ArrayList<Integer> var0, int var1, ArrayList<Integer> var2) {
        for (int i = 0;i < 75000;i++) {
          arr[i] = new BenchmarkArgs(var0, var1, var2);
        };
    }

    private String input1 = "407";

    private String input2 = "1";

    private String input3 = "597";
}
