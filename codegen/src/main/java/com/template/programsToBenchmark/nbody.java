package com.template.programsToBenchmark;

public final class nbody {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);

        NBodySystem bodies = new NBodySystem();
        System.out.printf("%.9f\n", bodies.energy());
        for (int i=0; i<n; ++i)
           bodies.advance(0.01);
        System.out.printf("%.9f\n", bodies.energy());
    }
}