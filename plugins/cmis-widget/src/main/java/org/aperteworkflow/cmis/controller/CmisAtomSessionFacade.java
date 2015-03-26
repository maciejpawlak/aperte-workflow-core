package org.aperteworkflow.cmis.controller;

import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.ExtensionLevel;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.commons.io.IOUtils;
import org.aperteworkflow.cmis.settings.CmisSettingsProvider;
import org.aperteworkflow.cmis.widget.DocumentContentStream;
import org.aperteworkflow.files.IFilesRepositoryFacade;
import org.aperteworkflow.files.exceptions.DeleteFileException;
import org.aperteworkflow.files.exceptions.DownloadFileException;
import org.aperteworkflow.files.exceptions.UpdateDescriptionException;
import org.aperteworkflow.files.exceptions.UploadFileException;
import org.aperteworkflow.files.model.FileItemContent;
import org.aperteworkflow.files.model.FilesRepositoryItem;
import org.aperteworkflow.files.model.IFilesRepositoryItem;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import org.springframework.beans.factory.annotation.Autowired;
import pl.net.bluesoft.rnd.processtool.ISettingsProvider;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.dao.ProcessInstanceDAO;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.util.lang.StringUtil;

/**
 * @author tlipski@bluesoft.net.pl
 * @author mpawlak@bluesoft.net.pl
 */
public class CmisAtomSessionFacade implements IFilesRepositoryFacade
{
    private static Logger logger = Logger.getLogger(CmisAtomSessionFacade.class.getName());

	/** Connection timeout in miliseconds */
	private static final String CONNECTION_TIMEOUT_MS = "3000";
	

	private Session session;

    @Autowired
    private ISettingsProvider settingsProvider;
	
	/** Get folder for the given process instance */
	public Folder getFolderForProcessInstance(ProcessInstance processInstance)
	{
		ProcessInstance mainProcess = processInstance;
		
		/* If process is subprocess, get the parent */
		if(processInstance.isSubprocess())
			mainProcess = processInstance.getParent();
		
		String folderName = "PT_"+mainProcess.getId();
		Folder mainFolder = getMainFolder();
		Folder processFolder = createFolderIfNecessary(folderName, mainFolder.getPath());
		
		return processFolder;
	}
	


	public Folder getFolderById(String id) throws CmisObjectNotFoundException
	{
		return (Folder) getCmisSession().getObject(session.createObjectId(id));
	}
	
	/** Find document in given folder by it's name */
	private Document getDocumentByName(Folder folder, String documentName)
	{
		for(CmisObject folderObject: folder.getChildren())
			if(folderObject instanceof Document)
			{
				Document folderDocument = (Document)folderObject;
				if(folderDocument.getName().equals(documentName))
					return folderDocument;
			}
		
		/* No document in given folder was found, return null */
		return null;
	}

	/** Uploads document to the given folder. If document with given name already exists, its content is
	 * overwrite. Otherwise, the new document is created
	 * 
	 * @param filename name of the file
	 * @param folder folder where file will be stored
	 * @param MIMEType MIME type of the document
	 * @param newProperties custom properties
	 * @return new document or updated existing one
	 */
	public Document uploadDocument(String filename, Folder folder, InputStream inputStream, String MIMEType, Map<String, Object> newProperties)
	{
        try {
		/* Create new content stream */
            ContentStream contentStream = new DocumentContentStream(inputStream, MIMEType, filename);
		
		/* Get document from repostitory */
            Document document = getDocumentByName(folder, filename);
		
		/* Document already exists, update it's content */
            if (document != null) {

                Document pwc = (Document) getCmisSession().getObject(document.checkOut());
                pwc.checkIn(true, newProperties, contentStream, "");
            }
		/* Create new one */
            else {
                HashMap<String, Object> properties = new HashMap<String, Object>();
                properties.put(PropertyIds.NAME, filename);
                properties.put(PropertyIds.OBJECT_TYPE_ID,  "cmis:document");

                if (newProperties != null) {
                    properties.putAll(newProperties);
                }

                document = folder.createDocument(properties, contentStream, VersioningState.MAJOR);
            }

            return document;
        }
        catch(Throwable ex)
        {
            logger.log(Level.SEVERE, "Problem during file upload using cmis", ex);
            throw new RuntimeException(ex);
        }
	}
	

	
	
	public Folder getMainFolder()
	{
		return this.getCmisSession().getRootFolder();
	}
	
	public Folder createFolderIfNecessary(String name, String parentPath) 
	{
		try
		{
			Folder folder = (Folder) getObjectByPath(parentPath +
					                                         (parentPath.equals("/") ? "" : "/") +
					                                         name);
			
			return folder;
		}
		catch(CmisObjectNotFoundException ex)
		{
			Folder parent;
			if (parentPath.equals("/") || parentPath.equals("")) {
				parent = getCmisSession().getRootFolder();
			}
			else {
				parent = (Folder) getObjectByPath(parentPath);
			}
			if (parent == null) {
				String[] toks = parentPath.split("/");
				StringBuilder path = new StringBuilder("/");
				for (String t : toks) {
					if (!StringUtil.hasText(t)) continue;
					parent = createFolderIfNecessary(t, path.toString());
					if (path.length() > 1) {
						path.append("/").append(t);
					}
					else {
						path.append(t);
					}
				}
			}
			Map<String, String> props = new HashMap<String, String>();
			props.put(PropertyIds.NAME, name);
			props.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
			Folder folder = parent.createFolder(props);

			return folder;
		}
	}

	public CmisObject getObjectByPath(String path) throws CmisObjectNotFoundException
	{
		return session.getObjectByPath(path);
	}
	
	public CmisObject getObject(ObjectId objectId) 
	{
		return session.getObject(objectId);
	}
	
	private Session createCmisSession() throws CmisConnectionException
	{

		SessionFactory cmisSessionFactory = SessionFactoryImpl.newInstance();
		
		//SessionFactory cmisSessionFactory = SessionFactoryImpl.newInstance();
		Map<String, String> parameter = new HashMap<String, String>();
		
		
		/* Get settings from database */
		String repositoryUser = CmisSettingsProvider.getAtomRepostioryUsername(settingsProvider);
		String repositoryPassword = CmisSettingsProvider.getAtomRepostioryPassword(settingsProvider);
		String repositoryAtomUrl = CmisSettingsProvider.getAtomRepostioryUrl(settingsProvider);
		//String repositoryUser = CmisSettingsProvider.getAtomRepostioryUsername();

		// user credentials
		parameter.put(SessionParameter.USER, repositoryUser);
		parameter.put(SessionParameter.PASSWORD, repositoryPassword);

		// connection settings
		parameter.put(SessionParameter.ATOMPUB_URL, repositoryAtomUrl);
		parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
		parameter.put(SessionParameter.CONNECT_TIMEOUT, CONNECTION_TIMEOUT_MS);
		

		List<Repository> repositories = cmisSessionFactory.getRepositories(parameter);
		
		if(repositories.isEmpty())
			throw new RuntimeException("Repository system do not have any repository configured!");
		
		parameter.put(SessionParameter.REPOSITORY_ID, repositories.get(0).getId());
		
		return cmisSessionFactory.createSession(parameter);

	}

    @Override
    public IFilesRepositoryItem uploadFile(InputStream inputStream, String contentType, Long processInstanceId, String fileName, String fileDescription, String creatorLogin) throws UploadFileException {


        ProcessInstance processInstance = getProcessInstanceDAO().getProcessInstance(processInstanceId);

        Folder folder = getFolderForProcessInstance(processInstance);

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.DESCRIPTION, fileDescription);
        properties.put(PropertyIds.CREATED_BY, creatorLogin);
        properties.put(PropertyIds.CREATION_DATE, new GregorianCalendar().getTime());
        Document document = uploadDocument(fileName, folder, inputStream, contentType, properties);


        return convert(document);
    }

    @Override
    public void deleteFile(Long processInstanceId, String filesRepositoryItemId) throws DeleteFileException {

        Document document = getDocument(processInstanceId, filesRepositoryItemId);
        if(document != null)
            document.delete(true);

    }

    @Override
    public FileItemContent downloadFile(Long processInstanceId, String fileId) throws DownloadFileException
    {
        try {
            Document document = getDocument(processInstanceId, fileId);

            FileItemContent content = new FileItemContent();
            content.setContentType(document.getContentStreamMimeType());
            content.setName(document.getName());
            content.setBytes(IOUtils.toByteArray(document.getContentStream().getStream()));
            content.setExtension("");
            content.setFilename(document.getName());

            return content;
        }
        catch(Throwable ex)
        {
            logger.log(Level.SEVERE, "Problem during file upload using cmis", ex);
            throw new DownloadFileException("Problem during file upload using cmis");
        }


    }

    @Override
    public Collection<IFilesRepositoryItem> getFilesList(Long processInstanceId) {
        ProcessInstance processInstance = getProcessInstanceDAO().getProcessInstance(processInstanceId);
        Folder folder = getFolderForProcessInstance(processInstance);

        Collection<IFilesRepositoryItem> items = new ArrayList<IFilesRepositoryItem>();
        for(CmisObject cmisObject: folder.getChildren())
            if(cmisObject instanceof Document)
                items.add(convert((Document)cmisObject));

        return items;
    }

    @Override
    public void updateDescription(Long processInstanceId, String filesRepositoryItemId, String fileDescription) throws UpdateDescriptionException {

        Map<String, String> properties = new HashMap<String, String>();
        properties.put(PropertyIds.DESCRIPTION, fileDescription);

        Document document = getDocument(processInstanceId, filesRepositoryItemId);
        if(document != null)
            document.updateProperties(properties);
    }

    private Document getDocument(Long processInstanceId, String filesRepositoryItemId)
    {
        ProcessInstance processInstance = getProcessInstanceDAO().getProcessInstance(processInstanceId);
        Folder folder = getFolderForProcessInstance(processInstance);

        for(CmisObject cmisObject: folder.getChildren())
            if(cmisObject instanceof Document)
                if(cmisObject.getId().equals(filesRepositoryItemId))
                    return (Document)cmisObject;

        return null;
    }

    private ProcessInstanceDAO getProcessInstanceDAO() {
        return ProcessToolContext.Util.getThreadProcessToolContext().getProcessInstanceDAO();
    }

    private IFilesRepositoryItem convert(Document document)
    {
        CmisFilesRepositoryItem filesRepositoryItem = new CmisFilesRepositoryItem();
        filesRepositoryItem.setName(document.getName());
        filesRepositoryItem.setCreateDate(document.getLastModificationDate().getTime());
        filesRepositoryItem.setCreatorLogin(document.getCreatedBy());
        filesRepositoryItem.setDescription(document.getDescription());
        filesRepositoryItem.setItemId(document.getId());
        filesRepositoryItem.setVersion(document.getVersionLabel());

        return filesRepositoryItem;
    }

    private Session getCmisSession()
    {
        if(session == null)
            session = createCmisSession();
        return session;
    }
}
