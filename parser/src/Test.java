import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.LinkedList;
import java.util.HashMap;
import java.util.Map;


public class Test {

    public static void main(String[] args) {
        HashMap<String, Map<String, Object>> methods = ASTFeatureExtractor.getFeatures("file");
        Map<String, Object> methodfeatures = methods.get("TestFile.t()");
        System.out.println(methodfeatures);
    }

    //@SuppressWarnings("unused")
    //private static void insertInEnd(int n) {
    //    ArrayList<Integer> list = new ArrayList<>();
    //    list.add(n);
    //}

    /*public static void main(String[] args) {
        //ArrayList<Integer> l = new ArrayList<Integer>();
        //LinkedList<Integer> ll = new LinkedList<>();
        //HashMap<String,Integer> hm = new HashMap<>();
        //hm.put("f", 10);
        //l.add(10);
        //l.add(15);
        //l.get(0);
        //ll.add(10);
        moreComplexFun(0);
        justToFillMainWithMoreFeatures();
        //TestObject t = new TestObject();
        //t.yes();
        //t.no();
    }

    //public static void f(){}

    public static void moreComplexFun(int n) {
        int sum = 0;
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= i; j++) {
                if (j % 2 == 0) {
                    sum += i * j;
                } else {
                    sum += j + i;
                }
            }
        }
        System.out.println("Result: " + sum);
    }

    public static void justToFillMainWithMoreFeatures(){
        if (2 > 0) {
            System.out.println("WOWOW");
        }else {System.out.println("OWOWO");}
    }*/
    
}
