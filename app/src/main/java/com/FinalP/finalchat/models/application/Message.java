package com.FinalP.finalchat.models.application;

import com.FinalP.finalchat.models.domain.MessageD;

import java.util.Date;

public class Message extends MessageD {
    public String id;
    public User from;
    public User to;
    public Date creationDate;

    public Message(MessageD messageD, User from, User to, String id) {
        this.creationDate = new Date(messageD.createDate);
        this.createDate = messageD.createDate;
        this.from = from;
        this.to = to;
        this.id = id;

        text = messageD.text;
        fromID = messageD.fromID;
    }
}
