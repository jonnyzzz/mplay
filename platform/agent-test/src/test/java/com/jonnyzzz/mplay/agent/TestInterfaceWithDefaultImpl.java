package com.jonnyzzz.mplay.agent;

public interface TestInterfaceWithDefaultImpl {
    default <R> int fun(R r) {
        return r.hashCode();
    }
}
