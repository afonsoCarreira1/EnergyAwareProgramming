package com.template.aux;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;

public class ArrayListAux {
    public static int min = 0;
    public static int max = 100_000;
    public static Random rand = new Random(42);

    private static int getSize(Object size) {
        if (size instanceof Integer) {
            return (Integer) size;
        } else if (size instanceof Double) {
            return ((Double) size).intValue();
        } else if (size instanceof Float) {
            return ((Float) size).intValue();
        } else if (size instanceof Long) {
            return ((Long) size).intValue();
        } else if (size instanceof Short) {
            return ((Short) size).intValue();
        } else if (size instanceof Byte) {
            return ((Byte) size).intValue();
        } else {
            if (size == null) throw new IllegalArgumentException("Unsupported type for getSize null");
            return (int) size;
        }
    }
    

    @SuppressWarnings("unchecked")
    public static <T> List<T> insertRandomNumbers(List<T> list, Object sizeT, String type) {
        int size = getSize(sizeT);
        if (type.equals("Integer")) {
            for (int i = 0; i < (Integer) size; i++) {
                int randomNum = rand.nextInt(((Integer) max - (Integer) min) + 1) + (Integer) min;
                list.add((T) Integer.valueOf(randomNum));
            }
        }else if (type.equals("Short")) {
            for (int i = 0; i < size; i++) {
                short randomNum = (short) (rand.nextInt((Short.MAX_VALUE - min) + 1) + min);
                list.add((T) Short.valueOf(randomNum));
            }
        } else if (type.equals("Double")) {
            for (int i = 0; i < size; i++) {
                double randomNum = rand.nextInt(((Integer) max - (Integer) min) + 1) + (Integer) min;
                list.add((T) Double.valueOf(randomNum));
            }
        } else if (type.equals("Float")) {
            for (int i = 0; i < size; i++) {
                float randomNum = rand.nextInt(((Integer) max - (Integer) min) + 1) + (Integer) min;
                list.add((T) Float.valueOf(randomNum));
            }
        } else if (type.equals("Long")) {
            for (int i = 0; i < size; i++) {
                long randomNum = rand.nextInt(((Integer) max - (Integer) min) + 1) + (Integer) min;
                list.add((T) Long.valueOf(randomNum));
            }
        } else if (type.equals("Character")) {
            char minChar = 'a';
            char maxChar = 'z';
            for (int i = 0; i < (Integer) size; i++) {
                char randomChar = (char) (rand.nextInt((maxChar - minChar) + 1) + minChar);
                list.add((T) Character.valueOf(randomChar));
            }
        }
        return list;
    }


    @SuppressWarnings("unchecked")
    public static <T> Set<T> insertRandomNumbers(Set<T> set, int size, String type) {
            for (int i = 0; i < size; i++) {
                if (type.equals("Integer")) {
                   int randomNum = rand.nextInt(((Integer) max - (Integer) min) + 1) + (Integer) min;
                   set.add((T) Integer.valueOf(randomNum)); 
                } else if (type.equals("Double")){
                    double randomNum = rand.nextInt(((Integer) max - (Integer) min) + 1) + (Integer) min;
                    set.add((T) Double.valueOf(randomNum)); 
                }
                else if (type.equals("Float")){
                    float randomNum = rand.nextInt(((Integer) max - (Integer) min) + 1) + (Integer) min;
                    set.add((T) Float.valueOf(randomNum)); 
                }
                else if (type.equals("Long")){
                    long randomNum = rand.nextInt(((Integer) max - (Integer) min) + 1) + (Integer) min;
                    set.add((T) Long.valueOf(randomNum)); 
                }
                else if (type.equals("Character")){
                    char minChar = 'a';
                    char maxChar = 'z';
                    char randomChar = (char) (rand.nextInt((maxChar - minChar) + 1) + minChar);
                    set.add((T) Character.valueOf(randomChar));
                }
            }
        return set;
    }

    //public static <T> void populateArrayObject(T[] arr, Supplier<T> sup) {
    //    for (int i = 0; i < arr.length; i++) {
    //        arr[i] = sup.get();
    //    }
    //}

    public static <T> void populateArrayPrimitive(T[] arr, Supplier<T> valueSupplier) {
        for (int i = 0; i < arr.length; i++) {
            arr[i] = valueSupplier.get();
        }
    }

    public static int getRandomIndex(int listSize) {
        return rand.nextInt(listSize);
    }

    public static int getRandomIndexHalved(int listSize) {
        int n = rand.nextInt((listSize-1)/2);  
        return n < 0 || n >= listSize ? 0 : n; 
    }

    public static int getRandomIndexQuartered(int listSize) {
        int n = rand.nextInt((listSize-1)/4);  
        return n < 0 || n >= listSize ? 0 : n;
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

    @SuppressWarnings("unchecked")
    public static <T> T getDefaultValues(String type){
        switch (type.toLowerCase()) {
            case "int":
                return (T) Integer.valueOf(0);
            case "double":
                return (T) Double.valueOf(0.0);
            case "float":
                return (T) Float.valueOf(0f);
            case "long":
                return (T) Long.valueOf(0);
            case "boolean":
                return (T) Boolean.valueOf(false);
            case "short":
                return (T) Short.valueOf("0");
            case "integer":
                return (T) Integer.valueOf(0);
            case "character":
                return (T) Character.valueOf('a');
            default:
                throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }

    public static Boolean areInputsFine(long inputSize, long memoryUsageOfType) {
        return true;
    }
    
}
