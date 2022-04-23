package com.FinalP.finalchat.models.application;

import com.FinalP.finalchat.models.domain.GroupD;

import java.util.ArrayList;

public class Group extends GroupD {
    public String id;
    public User from;
    public User to;
    public ArrayList<com.FinalP.finalchat.models.application.Message> messages;
    public com.FinalP.finalchat.models.application.GroupMetadata metadata;

    public Group(User from, User to, ArrayList<com.FinalP.finalchat.models.application.Message> messages, com.FinalP.finalchat.models.application.GroupMetadata metadata, String id) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.messages = messages;
        this.metadata = metadata;
    }
}
