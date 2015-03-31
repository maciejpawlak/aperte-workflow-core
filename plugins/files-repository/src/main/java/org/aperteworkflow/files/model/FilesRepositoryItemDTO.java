package org.aperteworkflow.files.model;


import pl.net.bluesoft.util.lang.Formats;

/**
 * @author pwysocki@bluesoft.net.pl
 */
public class FilesRepositoryItemDTO {
    private String itemId;
    private Long processInstanceId;
    private String name;
    private String description;
    private String createDate;
    private String creatorLogin;
    private String version;

    public FilesRepositoryItemDTO(IFilesRepositoryItem frItem, Long processInstanceId) {
        setItemId(frItem.getItemId());
        setProcessInstanceId(processInstanceId);
        setName(frItem.getName());
        setDescription(frItem.getDescription());

        setCreateDate(Formats.formatFullDate(frItem.getCreateDate()));
        setCreatorLogin(frItem.getCreatorLogin());
        setVersion(frItem.getVersion());
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public Long getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(Long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getCreatorLogin() { return creatorLogin; }

    public void setCreatorLogin(String creatorLogin) { this.creatorLogin = creatorLogin; }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
