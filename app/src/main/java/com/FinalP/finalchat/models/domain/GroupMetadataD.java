package com.FinalP.finalchat.models.domain;

import androidx.annotation.NonNull;

import java.util.Objects;

public class GroupMetadataD {
    public long createDate;

    public GroupMetadataD(long createDate) {
        this.createDate = createDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupMetadataD that = (GroupMetadataD) o;
        return createDate == that.createDate;
    }

    @Override
    public int hashCode() {
        return Objects.hash(createDate);
    }

    @NonNull
    @Override
    public String toString() {
        return "DialogMetadata{" +
                "createDate=" + createDate +
                '}';
    }
}
