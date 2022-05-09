package com.FinalP.finalchat.services;

@FunctionalInterface
public interface Callback<T> {
    void call(T arg);
}
