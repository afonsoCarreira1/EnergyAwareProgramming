import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import java_progs.aux.ArrayListAux;
import java_progs.aux.TemplatesAux;

public class t {

    public static void main(String[] args) {
        ArrayList<Integer> l = createInputRange(5,3,1);
        l.add(1,1);
        l.addAll(1,new ArrayList<>());
        System.out.println(l);
    }

    private static ArrayList<Integer> createInputRange(int initialvalue, double factor, int exponent){
            Set<Integer> numberSet = new HashSet<>();
            Random random = new Random();
            int max_value = initialvalue * 100_000;
            while (initialvalue < max_value) {
                int min = initialvalue;
                int max = initialvalue*10;
                double nums = Math.pow(factor, exponent);
                for (int j = 0; j < nums; j++) {
                    int num = min + random.nextInt(max - min + 1);
                    numberSet.add(num);
                }
                initialvalue = initialvalue*10;
                exponent++;
            }
            return new ArrayList<>(numberSet);
        }

}
