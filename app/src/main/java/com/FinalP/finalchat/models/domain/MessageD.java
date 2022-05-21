package com.FinalP.finalchat.models.domain;

import androidx.annotation.NonNull;

import java.util.Objects;

public class MessageD {
    public String text;

    public String fromID;
    public long createDate;

    public MessageD() {
    }

    public MessageD(String text, String fromID, long createDate) {
        this.text = text;
        this.fromID = fromID;
        this.createDate = createDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageD message = (MessageD) o;
        return createDate == message.createDate && Objects.equals(text, message.text) && Objects.equals(fromID, message.fromID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, fromID, createDate);
    }

    @NonNull
    @Override
    public String toString() {
        return "Message{" +
                "text='" + text + '\'' +
                ", fromID='" + fromID + '\'' +
                ", createDate=" + createDate +
                '}';
    }
}
