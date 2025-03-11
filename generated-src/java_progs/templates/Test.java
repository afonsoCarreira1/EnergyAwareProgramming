package java_progs.templates;
import java.util.concurrent.CopyOnWriteArrayList;
public class Test {
    public static void main(String[] args) {
        CopyOnWriteArrayList myVar = new CopyOnWriteArrayList();
    }

    public static void testMethod(int arg0) {
        CopyOnWriteArrayList myVar = new CopyOnWriteArrayList();
        myVar.get(arg0);
    }
}
