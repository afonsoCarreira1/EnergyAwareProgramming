package java_progs.templates.programsToBenchmark;

public class Fibonacci {

    int n;

    public Fibonacci(int n){this.n=n;}

    public int fibonacci() {return fibonacci(n);}
    
    private int fibonacci(int n) {
        if (n == 0)
            return 0;
        else if (n == 1)
            return 1;
        else
            return fibonacci(n - 1) + fibonacci(n - 2);
    }

    @Override
    public Fibonacci clone() {
        try {
            return (Fibonacci) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
