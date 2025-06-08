package com.generated_InputTestTemplate;
import com.template.programsToBenchmark.*;
import com.template.aux.TemplatesAux;
import com.template.programsToBenchmark.Fibonacci;
public class Prog_TestClass_m_com_template_programsToBenchmark_Fibonacci_ {
    static int in0;

    public static void main(String[] args) throws Exception {
        int iter = 0;
        in0 = Integer.valueOf(args[0]);
        try {
            BenchmarkArgs[] arr = new BenchmarkArgs[150000];
            populateArray(arr);
            TemplatesAux.launchTimerThread(1100);
            iter = computation(arr, arr.length);
        } catch (OutOfMemoryError e) {
            System.out.println(e.getMessage());
            System.exit(50);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(51);
        }
        System.out.println("aki");
        System.exit(0);
    }

    static class BenchmarkArgs {
        public Fibonacci var0;

        BenchmarkArgs() {
            this.var0 = new Fibonacci(new Test(in0));
        }
    }

    private static void testClass_m_com_template_programsToBenchmark_Fibonacci_(Fibonacci fib) {
        TestClass.m(fib);
    }

    private static int computation(BenchmarkArgs[] args, int iter) {
        int i = 0;
        while ((!TemplatesAux.stop) && (i < iter)) {
            testClass_m_com_template_programsToBenchmark_Fibonacci_(args[i].var0);
            i++;
        } 
        return iter;
    }

    private static void populateArray(BenchmarkArgs[] arr) {
        for (int i = 0; i < 150000; i++) {
            arr[i] = new BenchmarkArgs();
        }
    }

    private String input1 = "ChangeValueHere1";
}