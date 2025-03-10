package java_progs.templates;
// add imports
public class Template {
    // static int SIZE = "size";
    // static int loopSize = "loopSize";
    // create fun to benchmark
    // create computation fun
    // add @SuppressWarnings("unchecked")
    public static void main(java.lang.String[] args) throws java.io.IOException, java.lang.InterruptedException {
        try {
            // if fun to test is Static.fun() then just create multiple inputs
            // if fun is var.fun() then start by creating multiple vars and then multiple inputs
            // have a fun to get multiple lists or vars
            // do: var[i].fun(in[i].arg1,in[i].arg2,...) or do: Static.fun(in[i].arg1,in[i].arg2,...)
            // first iteration to get aproximate number of times the fun can run in a second
            long begin = java.lang.System.nanoTime();
            long end = begin;
            int iter = 0;
            /* 1s */
            while ((end - begin) < 1000000000) {
                // add the && iter < loopSize
                // call fun to benchmark
                end = java.lang.System.nanoTime();
                iter++;
            } 
            // clear and restart the vars for real measurement
            // send start signal for measurement
            java_progs.aux.TemplatesAux.sendStartSignalToOrchestrator(args[0], iter);
            java_progs.aux.TemplatesAux.launchTimerThread();
            // call computation fun
        } catch (java.lang.OutOfMemoryError e) {
            // catch errors
            // TemplatesAux.writeErrorInFile("BubbleSort"filename"", "Out of memory error caught by the program.\n" + e.getMessage());
        } catch (java.lang.Exception e) {
            // TemplatesAux.writeErrorInFile("BubbleSort"filename"","Error caught by the program.\n"+e.getMessage());
        } finally {
            java_progs.aux.TemplatesAux.sendStopSignalToOrchestrator(args[0]);
        }
    }

    public void injectedMethod() {
        System.out.println("Injected method executed!");
    }
}
