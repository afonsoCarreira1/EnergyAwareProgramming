package com.template;

import com.template.aux.DeepCopyUtil;
import com.template.aux.ArrayListAux;
import com.fasterxml.jackson.core.type.TypeReference;
import com.template.aux.TemplatesAux;
import java.util.Collection;
import java.util.LinkedList;
import spoon.support.reflect.reference.CtTypeReferenceImpl;
public class t {
    public static void main(String[] args) throws Exception {
        int iter = 0;
        int in0 = 0;
        Short in1 = 1;
        Short in2 = 1;
        try {
            LinkedList<Short> var0 = new LinkedList();
            ArrayListAux.insertRandomNumbers(var0, in1, "Short");
            int var1 = in0;
            LinkedList<Short> var2 = new LinkedList();
            ArrayListAux.insertRandomNumbers(var2, in2, "Short");
            BenchmarkArgs[] arr = new BenchmarkArgs[75000];
            populateArray(arr, var0, var1, var2);
            TemplatesAux.launchTimerThread(1100);
            iter = computation(arr, arr.length);
        } catch (OutOfMemoryError e) {
            System.out.println("error1 -> "+e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("error2 -> "+e.getMessage());
            System.exit(2);
        }
        System.out.println("aki");
        System.exit(0);
    }

    static class BenchmarkArgs {
        public LinkedList<Short> var0;

        public int var1;

        public LinkedList<Short> var2;

        BenchmarkArgs(LinkedList<Short> var0, int var1, LinkedList<Short> var2) {
            this.var0 = DeepCopyUtil.deepCopy(var0, new TypeReference<LinkedList<Short>>() {});
            this.var1 = DeepCopyUtil.deepCopy(var1, new TypeReference<Integer>() {});
            this.var2 = DeepCopyUtil.deepCopy(var2, new TypeReference<LinkedList<Short>>() {});
        }
    }

    private static void linkedList_addAll_int_java_util_Collection_(LinkedList var, int arg0, Collection<?> arg1) {
        var.addAll(arg0, arg1);
    }

    private static int computation(BenchmarkArgs[] args, int iter) {
        int i = 0;
        while ((!TemplatesAux.stop) && (i < iter)) {
            linkedList_addAll_int_java_util_Collection_(args[i].var0, args[i].var1, args[i].var2);
            i++;
        } 
        return iter;
    }

    private static void populateArray(BenchmarkArgs[] arr, LinkedList<Short> var0, int var1, LinkedList<Short> var2) {
        for (int i = 0; i < 75000; i++) {
            arr[i] = new BenchmarkArgs(var0, var1, var2);
        }
    }

    private String input1 = "ChangeValueHere1";

    private String input2 = "ChangeValueHere2";

    private String input3 = "ChangeValueHere3";
}