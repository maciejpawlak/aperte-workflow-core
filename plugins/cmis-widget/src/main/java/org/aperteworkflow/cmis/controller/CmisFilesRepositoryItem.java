package org.aperteworkflow.cmis.controller;

import org.aperteworkflow.files.model.IFilesRepositoryItem;

import java.util.Date;

/**
 * Created by mpawlak@bluesoft.net.pl on 2015-03-26.
 */
public class CmisFilesRepositoryItem implements IFilesRepositoryItem
{
    private String name;
    private String itemId;
    private String description;
    private Date createDate;
    private String creatorLogin;
    private String version;


    @Override
    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Override
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String getCreatorLogin() {
        return creatorLogin;
    }


    public void setCreatorLogin(String creatorLogin) {
        this.creatorLogin = creatorLogin;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
