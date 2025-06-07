package com.template;
import com.template.aux.CollectionAux;
import com.template.aux.TemplatesAux;
import java.util.HashMap;
public class G {
    public static void main(String[] args) throws Exception {
        int iter = 0;
        try {
            BenchmarkArgs[] arr = new BenchmarkArgs[1];
            populateArray(arr);
            TemplatesAux.sendStartSignalToOrchestrator(args[0]);
            TemplatesAux.launchTimerThread(1100);
            iter = computation(arr, arr.length);
        } catch (OutOfMemoryError e) {
            TemplatesAux.writeErrorInFile("HashMap_put_java_lang_Object_java_lang_Object_", "Out of memory error caught by the program:\n" + e.getMessage());
        } catch (Exception e) {
            TemplatesAux.writeErrorInFile("HashMap_put_java_lang_Object_java_lang_Object_", "Error caught by the program:\n" + e.getMessage());
        } finally {
            TemplatesAux.sendStopSignalToOrchestrator(args[0], iter);
        }
    }

    static class BenchmarkArgs {
        public HashMap<Integer, Integer> var0;

        public Integer var1;

        public Integer var2;

        BenchmarkArgs() {
            this.var0 = new HashMap();
            CollectionAux.insertRandomNumbers(var0, "ChangeValueHere1_changetypehere", "changetypehere");
            this.var1 = 1;
            this.var2 = 1;
        }
    }

    private static void hashMap_put_java_lang_Object_java_lang_Object_(HashMap var, Integer arg0, Integer arg1) {
        var.put(arg0, arg1);
    }

    private static int computation(BenchmarkArgs[] args, int iter) {
        int i = 0;
        while (!TemplatesAux.stop && i < iter) {
              hashMap_put_java_lang_Object_java_lang_Object_(args[i].var0, args[i].var1, args[i].var2);
               i++;
        }
        return iter;
    }

    private static void populateArray(BenchmarkArgs[] arr) {
        for (int i = 0;i < 1;i++) {
          arr[i] = new BenchmarkArgs();
        };
    }

    private String input1 = "ChangeValueHere1";

    private String input2 = "ChangeValueHere2";

    private String input3 = "ChangeValueHere3";
}
