import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import java_progs.aux.ArrayListAux;
import java_progs.aux.TemplatesAux;

public class t {
    static int SIZE = 200_000_000;
    static int loopSize = 1;

    
    public static void insertRandomNumbers(List<Integer> list, int size, int min, int max) {
        // Pre-allocate memory to prevent resizing
        ((ArrayList<Integer>) list).ensureCapacity(size);

        // Use fast ThreadLocalRandom for single-threaded random number generation
        ThreadLocalRandom rand = ThreadLocalRandom.current();

        for (int i = 0; i < size; i++) {
            list.add(rand.nextInt(min, max + 1));
        }

    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws IOException, InterruptedException {
        
        int iter = 0;
        ArrayList<Integer> list  = new ArrayList <Integer>();
        ArrayList<Integer>[] lists  = (ArrayList<Integer>[]) new ArrayList[loopSize];
        for (int i = 0; i < loopSize; i++) {
            ArrayList<Integer> l  = new ArrayList <Integer>();
            long st = System.currentTimeMillis();
            //ArrayListAux.insertRandomNumbers(l,SIZE,"Integer");

            insertRandomNumbers(l, SIZE, 0, 100_000);
            // Step 2: Convert to other List implementations
            List<Integer> linkedList = new LinkedList<>(l);
            //insertRandomNumbers(l, SIZE, 0, 100_000);
            //List<Integer> range = IntStream.rangeClosed(0, 100_000)
            //.boxed().collect(Collectors.toList());   
            long et = System.currentTimeMillis();
            double duration = (et-st)/1000.0;
            System.out.println(duration);
            //ArrayListAux.insertRandomNumbers(l,SIZE,"Integer");
            //for (int j = 0; j < SIZE; j++) {
            //    l.add(j);
            //}
            lists[i] = l;
        }
            
        
    }
    
}
