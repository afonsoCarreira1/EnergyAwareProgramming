package com.template;

import java.util.ArrayList;

import com.fasterxml.jackson.core.type.TypeReference;
import com.template.aux.ArrayListAux;
import com.template.aux.DeepCopyUtil;

public class t {

    static class BenchmarkArgs {
        public ArrayList<Integer> var0 = new ArrayList();
        public int var1 = 0;
        public ArrayList<ArrayList<Integer>> var2 = new ArrayList();

        BenchmarkArgs(ArrayList<Integer> var0, int var1, ArrayList<ArrayList<Integer>> var2) {
            this.var0 = DeepCopyUtil.deepCopy(var0, new TypeReference<ArrayList<Integer>>(){});
            this.var1 = DeepCopyUtil.deepCopy(var1, new TypeReference<Integer>(){});
            this.var2 = DeepCopyUtil.deepCopy(var2, new TypeReference<ArrayList<ArrayList<Integer>>>(){});
        }
    }

    public static void main(String[] args) {
        ArrayList<ArrayList<Integer>> l = new ArrayList<>();
        ArrayList<Integer> l2 = new ArrayList<>();
        l2.add(1);
        l2.add(2);
        l2.add(3);
        l.add(l2);
        l.add(l2);
        l.add(l2);
        
        
        BenchmarkArgs b = new BenchmarkArgs(l2, 0, l);
        BenchmarkArgs b2 = new BenchmarkArgs(l2, 0, l);


        System.out.println("b -> "+b.var2.get(0));
        System.out.println("b2 -> "+b2.var2.get(0));
        b.var2.get(0).set(0, 69);
        System.out.println("b -> "+b.var2.get(0));
        System.out.println("b2 -> "+b2.var2.get(0));

    }
    
}
