package com.template.programsToBenchmark;

public class Fibonacci {

    //public static int fibonacci(int n) {
    //    if (n==0) return 0;
    //    else if (n==1) return 1;
    //    else return fibonacci(n-1)+fibonacci(n+1);
    //}

    //int n;
    Test t;
    public Fibonacci(Test t){this.t = t;}
    
    public int fibonacci() {return fibonacci(t.n);}
    
    private int fibonacci(int n) {
        if (n == 0)
            return 0;
        else if (n == 1)
            return 1;
        else
            return fibonacci(n - 1) + fibonacci(n - 2);
    }
}