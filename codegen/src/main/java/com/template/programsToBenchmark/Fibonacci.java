package com.template.programsToBenchmark;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Fibonacci {

    //public static int fibonacci(int n) {
    //    if (n==0) return 0;
    //    else if (n==1) return 1;
    //    else return fibonacci(n-1)+fibonacci(n+1);
    //}

    //int n;
    Test t;
    @JsonCreator
    public Fibonacci(@JsonProperty("t")Test t){this.t = t;}
    
    public int fibonacci() {return fibonacci(t.n);}
    
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
