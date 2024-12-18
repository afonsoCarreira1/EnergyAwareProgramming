public class Test {

    public static void main(String[] args) {
        moreComplexFun(0);
        justToFillMainWithMoreFeatures();
    }

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
    }
    
}
