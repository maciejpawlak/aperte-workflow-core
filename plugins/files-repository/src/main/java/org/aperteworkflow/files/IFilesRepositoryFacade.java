package org.aperteworkflow.files;

import org.aperteworkflow.files.exceptions.DeleteFileException;
import org.aperteworkflow.files.exceptions.DownloadFileException;
import org.aperteworkflow.files.exceptions.UpdateDescriptionException;
import org.aperteworkflow.files.exceptions.UploadFileException;
import org.aperteworkflow.files.model.FileItemContent;
import org.aperteworkflow.files.model.FilesRepositoryItem;
import org.aperteworkflow.files.model.IFilesRepositoryItem;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

/**
 * @author pwysocki@bluesoft.net.pl
 */
public interface IFilesRepositoryFacade {
    IFilesRepositoryItem uploadFile(InputStream inputStream, String contentType, Long processInstanceId, String fileName, String fileDescription, String creatorLogin) throws UploadFileException;

    void deleteFile(Long processInstanceId, String filesRepositoryItemId) throws DeleteFileException;

    FileItemContent downloadFile(Long processInstanceId, String fileId) throws DownloadFileException;

    Collection<IFilesRepositoryItem> getFilesList(Long processInstanceId);

    void updateDescription(Long processInstanceId, String filesRepositoryItemId, String fileDescription) throws UpdateDescriptionException;
}
