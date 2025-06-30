
public class BinaryTrees_loops_int_int_406 {
    public static boolean stop = false;
    public static void main(String[] args) throws Exception {
        int iter = 0;

            BenchmarkArgs[] arr = new BenchmarkArgs[100000];
            populateArray(arr);
            iter = computation(arr, arr.length);
        
    }

    static class BenchmarkArgs {
        public int var0;

        public int var1;

        BenchmarkArgs() {
            this.var0 = 0;
            this.var1 = 10;
        }
    }

    private static void binaryTrees_loops_int_int_406(int iterations, int depth) {
        BinaryTrees.loops(iterations, depth);
    }

    private static int computation(BenchmarkArgs[] args, int iter) {
        int i = 0;
        while (!stop && i < iter) {
              binaryTrees_loops_int_int_406(args[i].var0, args[i].var1);
               i++;
        }
        if (i == 0) return 1;
        return i;
    }

    private static void populateArray(BenchmarkArgs[] arr) {
        for (int i = 0;i < 100000;i++) {
          arr[i] = new BenchmarkArgs();
        };
    }

    private String input1 = "0";

    private String input2 = "10";
}
