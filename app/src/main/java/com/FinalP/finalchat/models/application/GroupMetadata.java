package com.FinalP.finalchat.models.application;

import com.FinalP.finalchat.models.domain.GroupMetadataD;

import java.util.Date;

public class GroupMetadata extends GroupMetadataD {
    public String id;
    public Date creationDate;

    public GroupMetadata(GroupMetadataD metadataD, String id) {
        this.creationDate = new Date(metadataD.createDate);
        this.createDate = metadataD.createDate;
        this.id = id;
    }
}
