package com.FinalP.finalchat.listeners;

import com.google.firebase.database.DatabaseError;

public abstract class SimpleListener<T> {
    public void onValue(T value){};
    public void onValueReg(String val,String val2){};
    public void onException(Exception e) {}
    public void onDatabaseError(DatabaseError e) {}
}

