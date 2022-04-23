package com.FinalP.finalchat.models.domain;

import java.util.Objects;

public class MessageD {
    public String text;
    public boolean isRead;
    public boolean isSent;
    public String fromID;
    public long createDate;

    public MessageD() {
    }

    public MessageD(String text, boolean isRead, boolean isSent, String fromID, long createDate) {
        this.text = text;
        this.isRead = isRead;
        this.isSent = isSent;
        this.fromID = fromID;
        this.createDate = createDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageD message = (MessageD) o;
        return isRead == message.isRead && isSent == message.isSent && createDate == message.createDate && Objects.equals(text, message.text) && Objects.equals(fromID, message.fromID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, isRead, isSent, fromID, createDate);
    }

    @Override
    public String toString() {
        return "Message{" +
                "text='" + text + '\'' +
                ", isRead=" + isRead +
                ", isSent=" + isSent +
                ", fromID='" + fromID + '\'' +
                ", createDate=" + createDate +
                '}';
    }
}
