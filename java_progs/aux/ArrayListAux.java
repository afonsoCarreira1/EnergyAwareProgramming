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

    @SuppressWarnings("unchecked")
    public static <T> T getRandomValueOfType(String type){
        switch (type.toLowerCase()) {
            case "int":
                return (T) Integer.valueOf(rand.nextInt((max - min) + 1) + min);
            case "double":
                return (T) Double.valueOf(min + (max - min) * rand.nextDouble());
            case "float":
                return (T) Float.valueOf(min + (max - min) * rand.nextFloat());
            case "long":
                return (T) Long.valueOf(rand.nextLong(min, max + 1));
            case "boolean":
                return (T) Boolean.valueOf(rand.nextBoolean());
            case "short":
                return (T) Short.valueOf((short) (rand.nextInt((max - min) + 1) + min));
            case "integer":
                return (T) Integer.valueOf(rand.nextInt((max - min) + 1) + min);
            case "character":
                char minChar = 'a';
                char maxChar = 'z';
                char randomChar = (char) (rand.nextInt((maxChar - minChar) + 1) + minChar);
                return (T) Character.valueOf(randomChar);
            default:
                throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T getRandomValueOfTypeOutsideBounds(String type) {
        switch (type.toLowerCase()) {
            case "int":
            case "integer":
                return (T) Integer.valueOf(rand.nextBoolean() 
                    ? -(rand.nextInt(50_001))  // Generates values between -50,000 and -1
                    : rand.nextInt(50_001) + 100_001); // Generates values between 100,001 and 150,000
            case "double":
                return (T) Double.valueOf(rand.nextBoolean() 
                    ? -(rand.nextInt(50_001)) : rand.nextInt(50_001) + 100_001);
            case "float":
                return (T) Float.valueOf(rand.nextBoolean() 
                    ? -(rand.nextInt(50_001)) : rand.nextInt(50_001) + 100_001);
            case "long":
                return (T) Long.valueOf(rand.nextBoolean() 
                    ? -(rand.nextInt(50_001)) : rand.nextInt(50_001) + 100_001);
            case "boolean":
                return (T) Boolean.valueOf(rand.nextBoolean());
            case "short":
                return (T) Short.valueOf((short) (rand.nextBoolean() 
                    ? -(rand.nextInt(50_001)): rand.nextInt(50_001) + 100_001));
            case "character":
                char randomChar = (char) (rand.nextBoolean() 
                ? 'a' - (rand.nextInt(26) + 1) : 'z' + (rand.nextInt(26) + 1));  
                return (T) Character.valueOf(randomChar);
            default:
                throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }

    public static Boolean areInputsFine(long inputSize, long memoryUsageOfType) {
        return true;
    }
    
}
