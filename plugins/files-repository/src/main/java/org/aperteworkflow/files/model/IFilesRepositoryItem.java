package org.aperteworkflow.files.model;

import java.util.Date;

/**
 * Created by mpawlak@bluesoft.net.pl on 2015-03-26.
 */
public interface IFilesRepositoryItem {
    String getItemId();
    String getName();
    String getDescription();
    Date getCreateDate();
    String getCreatorLogin();
    String getVersion();

}
