package java_progs.aux;

import java.util.ArrayList;
import java.util.Random;

public class ArrayListAux {
    public static int min = 0;
    public static int max = 100_000;
    public static Random rand = new Random();

    @SuppressWarnings("unchecked")
    public static <T> ArrayList<T> insertRandomNumbers(ArrayList<T> list, int size, String type) {
            for (int i = 0; i < size; i++) {
                if (type.equals("Integer")) {
                   int randomNum = rand.nextInt(((Integer) max - (Integer) min) + 1) + (Integer) min;
                    list.add((T) Integer.valueOf(randomNum)); 
                } else if (type.equals("Double")){
                    double randomNum = rand.nextInt(((Integer) max - (Integer) min) + 1) + (Integer) min;
                    list.add((T) Double.valueOf(randomNum)); 
                }
                else if (type.equals("Float")){
                    float randomNum = rand.nextInt(((Integer) max - (Integer) min) + 1) + (Integer) min;
                    list.add((T) Float.valueOf(randomNum)); 
                }
                else if (type.equals("Long")){
                    long randomNum = rand.nextInt(((Integer) max - (Integer) min) + 1) + (Integer) min;
                    list.add((T) Long.valueOf(randomNum)); 
                }
                else if (type.equals("Character")){
                    char minChar = 'a';
                    char maxChar = 'z';
                    char randomChar = (char) (rand.nextInt((maxChar - minChar) + 1) + minChar);
                    list.add((T) Character.valueOf(randomChar));
                }
            }
        return list;
    }

    public static int getRandomNumber() {
        return rand.nextInt((max - min) + 1) + min;
    }

    public static int getRandomNumberHalved() {
        return rand.nextInt(((max/2) - min) + 1) + min;
    }

    public static Boolean areInputsFine(long inputSize, long memoryUsageOfType) {
        return true;
    }
    
}
