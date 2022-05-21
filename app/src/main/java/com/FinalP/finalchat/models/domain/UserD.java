package com.FinalP.finalchat.models.domain;

import androidx.annotation.NonNull;

public class UserD {
    public String name;
    public String email;
    public long createDate;
    public String id;
    public boolean isActive;

    public UserD() {//required
    }
    public UserD(long createDate, String email, String name) {
        this.name = name;
        this.createDate = createDate;
        this.email = email;
        this.id = email.replaceAll(";", "").replaceAll("\\.", "").replaceAll("@", "");
    }

    @NonNull
    @Override
    public String toString() {
        return "UserD{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", createDate=" + createDate +
                '}';
    }
}
