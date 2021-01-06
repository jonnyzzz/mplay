package com.jonnyzzz.mplay.agent.builder;

public interface TestInterfaceWithDefaultImpl {
    default <R> int fun(R r) {
        return r.hashCode();
    }
}
