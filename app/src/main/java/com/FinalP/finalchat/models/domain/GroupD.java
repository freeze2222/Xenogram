package com.FinalP.finalchat.models.domain;

import androidx.annotation.NonNull;

import java.util.Map;
import java.util.Objects;

public class GroupD {
    public GroupMetadataD metadata;
    public int unread;
    public boolean isActiveUser1;
    public boolean isActiveUser2;
    public String unread_property;
    public Map<String, MessageD> messages;

    public GroupD(GroupMetadataD metadata, Map<String, MessageD> messages,int unread,String unread_property,boolean isActiveUser1,boolean isActiveUser2) {
        this.metadata = metadata;
        this.messages = messages;
        this.isActiveUser1 = isActiveUser1;
        this.isActiveUser2 = isActiveUser2;
        this.unread=unread;
        this.unread_property=unread_property;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupD groupD = (GroupD) o;
        return Objects.equals(metadata, groupD.metadata) && Objects.equals(messages, groupD.messages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metadata, messages);
    }

}
