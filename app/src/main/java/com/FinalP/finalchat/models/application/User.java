package com.FinalP.finalchat.models.application;

import com.FinalP.finalchat.models.domain.UserD;

import java.io.Serializable;
import java.util.Date;

public class User extends UserD implements Serializable {
    public String id;
    public Date creationDate;
    public String username;

    public User(UserD userD) {
        this.name = userD.name;
        this.email = userD.email;
        this.username = userD.username;
        this.id = email.replaceAll(";", "").replaceAll("\\.", "").replaceAll("@", "");
        this.creationDate = new Date(userD.createDate);

    }
}
