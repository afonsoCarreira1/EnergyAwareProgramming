package com.generated_InputTestTemplate;
import com.template.aux.DeepCopyUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.template.programsToBenchmark.*;
import com.template.aux.TemplatesAux;
import com.template.programsToBenchmark.TT;
public class Prog_TT_doNothing__ {
    public static void main(String[] args) throws Exception {
        int iter = 0;
        Long in0 = Long.valueOf(args[0]);
        try {
            TT var0 = new TT(in0);
            BenchmarkArgs[] arr = new BenchmarkArgs[150000];
            populateArray(arr, var0);
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
        public TT var0;

        BenchmarkArgs(TT var0) {
            this.var0 = DeepCopyUtil.deepCopy(var0, new TypeReference<TT>() {});
        }
    }

    private static void TT_doNothing__(TT var) {
        var.doNothing();
    }

    private static int computation(BenchmarkArgs[] args, int iter) {
        int i = 0;
        while ((!TemplatesAux.stop) && (i < iter)) {
            TT_doNothing__(args[i].var0);
            i++;
        } 
        return iter;
    }

    private static void populateArray(BenchmarkArgs[] arr, TT var0) {
        for (int i = 0; i < 150000; i++) {
            arr[i] = new BenchmarkArgs(var0);
        }
    }

    private String input1 = "ChangeValueHere1";
}