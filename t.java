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
            java.lang.Object[] argsArr = new java.lang.Object[]{ var0, var1, var2 };
            Object[][] arr = new Object[2][argsArr.length];
            //for (int i = 0; i < argsArr.length-1; i++) {
                //arr[i] = Arrays.copyOf(argsArr, argsArr.length);
                //arr[i] = new Object[]{args};
                //for (int j = 0; j < argsArr.length-1; j++) {
                //    if (argsArr[i] instanceof Collection) {
                //        arr[i][j] = new ArrayList<>((ArrayList<Integer>) argsArr[i]);
                //    } else {
                //        arr[i][j] = argsArr[j];
                //    }
                //} 
            //}
            for (int i = 0; i < arr.length; i++) {
                arr[i] = new Object[argsArr.length];
                for (int j = 0; j < argsArr.length; j++) {
                    if (argsArr[j] instanceof Collection) {
                        arr[i][j] = new ArrayList<>((ArrayList<Integer>) argsArr[j]);
                    } else {
                        arr[i][j] = argsArr[j];
                    }
                }
            }
            System.out.println(arr[0][0]);
            System.out.println(arr[1][0]);
            ArrayList<Integer> list = (ArrayList<Integer>) arr[0][0];
            list.set(0,69);
            System.out.println(arr[0][0]);
            System.out.println(arr[1][0]);

            System.out.println("--------------------");
            System.out.println(arr[0][1]);
            System.out.println(arr[1][1]);
            arr[0][1] = 10;
            System.out.println(arr[0][1]);
            System.out.println(arr[1][1]);

            System.out.println("--------------------");
            System.out.println(arr[0][2]);
            System.out.println(arr[1][2]);
            
    }
}
