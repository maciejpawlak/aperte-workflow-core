package org.aperteworkflow.files;

import org.aperteworkflow.files.dao.*;
import org.aperteworkflow.files.dao.config.FilesRepositoryConfigFactory;
import org.aperteworkflow.files.dao.config.FilesRepositoryConfigFactoryImpl;
import org.aperteworkflow.files.dao.config.FilesRepositoryStorageConfig;
import org.aperteworkflow.files.exceptions.DeleteFileException;
import org.aperteworkflow.files.exceptions.DownloadFileException;
import org.aperteworkflow.files.exceptions.UpdateDescriptionException;
import org.aperteworkflow.files.exceptions.UploadFileException;
import org.aperteworkflow.files.model.FileItemContent;
import org.aperteworkflow.files.model.IFilesRepositoryItem;
import org.hibernate.Session;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.IAttributesConsumer;
import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pl.net.bluesoft.util.lang.Formats.nvl;

/**
 * @author pwysocki@bluesoft.net.pl
 */
public class FilesRepositoryFacade implements IFilesRepositoryFacade {

    private static Logger logger = Logger.getLogger(FilesRepositoryFacade.class.getName());

    private FilesRepositoryConfigFactory configFactory;
    private Session customSession;

    public FilesRepositoryFacade() {
        this(null, new FilesRepositoryConfigFactoryImpl());
    }


    public FilesRepositoryFacade(Session customSession, FilesRepositoryConfigFactory configFactory) {
        this.customSession = customSession;
        this.configFactory = configFactory;
    }

    private FilesRepositoryItemDAO getFilesRepositoryItemDAO() {
        Session sessionToUse = getSession();
        return new FilesRepositoryItemDAOImpl(sessionToUse);
    }

    private Session getSession() {
        return customSession != null ? customSession : ProcessToolContext.Util.getThreadProcessToolContext().getHibernateSession();
    }

    private FilesRepositoryStorageDAO getFilesRepositoryStorageDAO() {
        return new FilesRepositoryStorageDAOImpl(getStorageConfig());
    }

    private FilesRepositoryStorageConfig getStorageConfig() {
        return configFactory.createFilesRepositoryStorageConfig();
    }

    @Override
    public IFilesRepositoryItem uploadFile(InputStream inputStream, String contentType, IAttributesConsumer filesConsumer,
										   String fileName, String fileDescription, String creatorLogin, FilesRepositoryAttributeFactory factory) throws UploadFileException {
		return uploadFile(inputStream, contentType, filesConsumer, fileName, fileDescription, creatorLogin, factory, null);
	}

    @Override
    public IFilesRepositoryItem uploadFile(InputStream inputStream, String contentType, IAttributesConsumer filesConsumer,
                                           String fileName, String fileDescription, String creatorLogin, FilesRepositoryAttributeFactory factory,
                                           Boolean sendWithMail) throws UploadFileException {
        return uploadFile(inputStream, contentType, filesConsumer, fileName, fileDescription, creatorLogin, factory, sendWithMail, null);
    }

	@Override
	public IFilesRepositoryItem uploadFile(InputStream inputStream, String contentType, IAttributesConsumer filesConsumer,
			String fileName, String fileDescription, String creatorLogin, FilesRepositoryAttributeFactory factory,
			Boolean sendWithMail, String groupId) throws UploadFileException {
        IFilesRepositoryItem result;
        String filePath = prepareFilePath(filesConsumer.getId(), fileName);
        try {
            getFilesRepositoryStorageDAO().uploadFileToStorage(inputStream, filePath);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error during uploading file", e);
            throw new UploadFileException("Cannot write file to storage", e);
        }
        result = getFilesRepositoryItemDAO().addItem(filesConsumer, fileName, fileDescription, filePath, contentType, creatorLogin, sendWithMail, groupId, factory);
        return result;
    }

	private String prepareFilePath(Long filesProviderId, String fileName) {
        return filesProviderId + File.separator + System.currentTimeMillis() + "_" + fileName;
    }

    @Override
    public void deleteFile(IAttributesProvider filesAttributeProvider, Long filesRepositoryItemId) throws DeleteFileException {
        IFilesRepositoryItem filesRepositoryItem = getFilesRepositoryItemDAO().getItemById(filesRepositoryItemId);
        if (filesRepositoryItem == null) {
            throw new DeleteFileException("File item with id=[" + filesRepositoryItemId + "] not found.");
        }
        getFilesRepositoryItemDAO().deleteById(filesAttributeProvider, filesRepositoryItemId);
		if (getFilesRepositoryItemDAO().hasAnyFileWithName(filesRepositoryItem.getRelativePath())) {
			return;
		}
        try {
            getFilesRepositoryStorageDAO().deleteFileFromStorage(new File(filesRepositoryItem.getRelativePath())); // sprawdzic czy nie ma innych odwolan na to!!!
        } catch (IOException e) {
            throw new DeleteFileException("File from path=[" + filesRepositoryItem.getRelativePath() + "].", e);
        }
    }

    @Override
    public FileItemContent downloadFile(Long filesRepositoryItemId) throws DownloadFileException {
        IFilesRepositoryItem filesRepositoryItem = getFilesRepositoryItemDAO().getItemById(filesRepositoryItemId);
        if (filesRepositoryItem == null) {
            throw new DownloadFileException("File item with id=[" + filesRepositoryItemId + "] not found.");
        }
        try {
            FileItemContent content = getFilesRepositoryStorageDAO().loadFileFromStorage(filesRepositoryItem.getRelativePath());
            content.setName(filesRepositoryItem.getName());
            content.setContentType(filesRepositoryItem.getContentType());
            return content;
        } catch (IOException e) {
            throw new DownloadFileException("File item download problem for filesRepositoryItemId=[" + filesRepositoryItemId + "].", e);
        }
    }

    @Override
    public Collection<? extends IFilesRepositoryItem> getFilesList(IAttributesProvider filesAttributeProvider) {
        return getFilesRepositoryItemDAO().getItemsFor(filesAttributeProvider);
    }

	@Override
	public Collection<? extends IFilesRepositoryItem> copy(List<IFilesRepositoryItem> files, IAttributesConsumer filesConsumer, FilesRepositoryAttributeFactory factory) {
		if (files == null || files.isEmpty()) {
			return Collections.emptyList();
		}

		List<? extends IFilesRepositoryItem> result = new ArrayList<IFilesRepositoryItem>();

		for (IFilesRepositoryItem file : files) {
			getFilesRepositoryItemDAO().addItem(filesConsumer, file.getName(), file.getDescription(), file.getRelativePath(),
					file.getContentType(), file.getCreatorLogin(), file.getSendWithMail(), file.getGroupId(), factory);
		}
		return result;
	}

	@Override
	public Collection<? extends IFilesRepositoryItem> getFilesList(IAttributesProvider filesAttributeProvider, FileListFilter filter) {
		Collection<? extends IFilesRepositoryItem> filesList = getFilesList(filesAttributeProvider);

		if (filter == null || filter == FileListFilter.ALL) {
			return filesList;
		}

		List<IFilesRepositoryItem> result = new ArrayList<IFilesRepositoryItem>();

		for (IFilesRepositoryItem item : filesList) {
			boolean isEmailAttachment = nvl(item.getSendWithMail(), false);

			if (filter == FileListFilter.ONLY_EMAIL_ATTACHMENTS && isEmailAttachment ||
				filter == FileListFilter.WITHOUT_EMAIL_ATTACHMENTS && !isEmailAttachment) {
				result.add(item);
			}
		}
		return result;
	}

	@Override
	public IFilesRepositoryItem getFileItem(Long id) {
		return getFilesRepositoryItemDAO().getItemById(id);
	}

	@Override
    public void updateDescription(Long filesRepositoryItemId, String fileDescription) throws UpdateDescriptionException {
        IFilesRepositoryItem filesRepositoryItem = getFilesRepositoryItemDAO().getItemById(filesRepositoryItemId);
        if (filesRepositoryItem == null) {
            throw new UpdateDescriptionException("File item with id=[" + filesRepositoryItemId + "] not found.");
        }
        getFilesRepositoryItemDAO().updateDescription(filesRepositoryItem, fileDescription);
    }

    @Override
    public void updateSendWithMail(Long filesRepositoryItemId, Boolean sendWithMail) {
        IFilesRepositoryItem filesRepositoryItem = getFilesRepositoryItemDAO().getItemById(filesRepositoryItemId);
        if (filesRepositoryItem == null) {
            throw new RuntimeException("File item with id=[" + filesRepositoryItemId + "] not found.");
        }
        getFilesRepositoryItemDAO().updateSendWithMail(filesRepositoryItem, sendWithMail);
    }

}
