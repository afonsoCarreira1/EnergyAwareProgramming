import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import java_progs.aux.ArrayListAux;

public class t {

    static class BenchmarkArgs {
        public ArrayList var0 = new ArrayList();

        public int var1 = 80;

        public ArrayList var2 = new ArrayList();

        BenchmarkArgs(ArrayList var0, Integer var1, ArrayList var2) {
            this.var0 = (ArrayList) var0.clone();
            this.var1 = var1;
            this.var2 = var2;
        }
    }

    public static void main(String[] args) {
            ArrayList<Integer> var0 = new ArrayList<>();
            var0.add(1);
            var0.add(2);
            var0.add(3);
            int var1 = 80;
            ArrayList<Integer> var2 = new ArrayList<>();
            var2.add(3);
            var2.add(2);
            var2.add(1);
            BenchmarkArgs[] arr = new BenchmarkArgs[150];
            for (int i = 0;i < 3;i++) {
                arr[i] = new BenchmarkArgs(var0, var1, var2);
            };
            System.out.println(arr[0].var0);
            System.out.println(arr[1].var0);
            arr[0].var0.set(0, 69);
            System.out.println(arr[0].var0);
            System.out.println(arr[1].var0);

            System.out.println("---------------------------------");
            
            System.out.println(arr[0].var1);
            System.out.println(arr[1].var1);
            arr[0].var1 = 120;
            System.out.println(arr[0].var1);
            System.out.println(arr[1].var1);
            
    }
}
