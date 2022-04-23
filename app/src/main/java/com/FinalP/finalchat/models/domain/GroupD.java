package com.FinalP.finalchat.models.domain;

import java.util.Map;
import java.util.Objects;

public class GroupD {
    public GroupMetadataD metadata;
    public Map<String, MessageD> messages;

    public GroupD() {
    }

    public GroupD(GroupMetadataD metadata, Map<String, MessageD> messages) {
        this.metadata = metadata;
        this.messages = messages;
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

    @Override
    public String toString() {
        return "DialogD{" +
                "metadata=" + metadata +
                ", messages=" + messages +
                '}';
    }
}
