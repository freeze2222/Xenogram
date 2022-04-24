package com.FinalP.finalchat.listeners;

public abstract class SimpleListener<T> {
    public void onValue(T value){}

    public void onValueReg(String val,String val2){}

    public void onException() {}
    public void onDatabaseError() {}
}

