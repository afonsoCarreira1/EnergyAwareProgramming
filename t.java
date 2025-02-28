import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class t {
    public static void main(String[] args) {
        createInputRange();
    }
    private static void createInputRange(){
            Set<Integer> numberSet = new HashSet<>();
            Random random = new Random();
            int initialvalue = 5;
            int factor = 2;
            int exponent = 1;
            while (initialvalue < 500_000) {
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

            //while (numberSet.size() < count) {
            //    int num = min + random.nextInt(max - min + 1);
            //    numberSet.add(num);
            //}
            List<Integer> numbers = new ArrayList<>(numberSet);
            numbers.sort(null);
            for (int num : numbers) {
                System.out.println(num);
            }
            System.out.println(numberSet.size());
        }
    
}
