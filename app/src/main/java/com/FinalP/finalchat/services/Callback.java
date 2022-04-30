package com.FinalP.finalchat.services;

import com.FinalP.finalchat.models.application.User;

@FunctionalInterface
public interface Callback<T> {
    void call(User arg);
}
