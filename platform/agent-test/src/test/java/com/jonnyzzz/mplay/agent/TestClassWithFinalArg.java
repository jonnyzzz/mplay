package com.jonnyzzz.mplay.agent;

public class TestClassWithFinalArg {
    static {
        System.out.println("This is static init");
    }

    public TestClassWithFinalArg() {
        System.out.println("This is constructor");
    }

    public final void method(final byte b) {
        System.out.println("This is method: " + b);
    }
}
