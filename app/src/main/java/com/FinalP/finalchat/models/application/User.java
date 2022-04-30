package com.FinalP.finalchat.models.application;

import com.FinalP.finalchat.models.domain.UserD;

import java.io.Serializable;
import java.util.Date;

public class User extends UserD implements Serializable {
    public String id;
    public Date creationDate;
    public String avatar;

    public User(UserD userD) {
        this.avatar=userD.avatar;
        this.name = userD.name;
        this.email = userD.email;
        this.id = email.replaceAll(";", "").replaceAll("\\.", "").replaceAll("@", "");
        this.creationDate = new Date(userD.createDate);

    }
}
