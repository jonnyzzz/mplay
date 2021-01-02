package com.jonnyzzz.mplay.agent;

public class TestClassWithStaticInit {
    static {
        System.out.println("This is static init");
    }

    public TestClassWithStaticInit() {
        System.out.println("This is constructor");
    }

    public final void method() {
        System.out.println("This is method");
    }
}
