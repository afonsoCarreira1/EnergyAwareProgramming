package java_progs.aux;

import java.util.ArrayList;
import java.util.Random;

public class ArrayListAux {
    public static int min = 0;
    public static int max = 100_000;
    public static Random rand = new Random();

    public static ArrayList<Integer> insertRandomNumbers(ArrayList<Integer> list,int size) {
        Random rand = new Random();
        for (int i = 0; i < size; i++) {
            int randomNum = rand.nextInt((max - min) + 1) + min;
            list.add(randomNum);
        }
        return list;
    }

    public static int getRandomNumber() {
        return rand.nextInt((max - min) + 1) + min;
    }

    public static int getRandomNumberHalved() {
        return rand.nextInt(((max/2) - min) + 1) + min;
    }
    
}
