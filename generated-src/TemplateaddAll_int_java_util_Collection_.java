import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java_progs.aux.TemplatesAux;
// add imports
public class TemplateaddAll_int_java_util_Collection_ {
    // static int SIZE = "size";
    // static int loopSize = "loopSize";
    // create fun to benchmark
    // create computation fun
    // add @SuppressWarnings("unchecked")
    public static void main(String[] args) throws IOException, InterruptedException {
        try {
            // if fun to test is Static.fun() then just create multiple inputs
            // if fun is var.fun() then start by creating multiple vars and then multiple inputs
            // have a fun to get multiple lists or vars
            // do: var[i].fun(in[i].arg1,in[i].arg2,...) or do: Static.fun(in[i].arg1,in[i].arg2,...)
            // first iteration to get aproximate number of times the fun can run in a second
            long begin = System.nanoTime();
            long end = begin;
            int iter = 0;
            /* 1s */
            while ((end - begin) < 1000000000) {
                // add the && iter < loopSize
                // call fun to benchmark
                end = System.nanoTime();
                iter++;
            } 
            // clear and restart the vars for real measurement
            // send start signal for measurement
            TemplatesAux.sendStartSignalToOrchestrator(args[0], iter);
            TemplatesAux.launchTimerThread();
            // call computation fun
        } catch (OutOfMemoryError e) {
            // catch errors
            // TemplatesAux.writeErrorInFile("BubbleSort"filename"", "Out of memory error caught by the program.\n" + e.getMessage());
        } catch (Exception e) {
            // TemplatesAux.writeErrorInFile("BubbleSort"filename"","Error caught by the program.\n"+e.getMessage());
        } finally {
            TemplatesAux.sendStopSignalToOrchestrator(args[0]);
        }
    }

    private static void computation(ArrayList var, int arg0, Collection<?> arg1) {
        var.addAll(arg0, arg1);
    }
}
