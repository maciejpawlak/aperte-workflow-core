package pl.net.bluesoft.rnd.pt.ext.jbpm.service;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import pl.net.bluesoft.rnd.processtool.BasicSettings;
import pl.net.bluesoft.rnd.processtool.bpm.diagram.Node;
import pl.net.bluesoft.rnd.processtool.bpm.diagram.ProcessDiagram;
import pl.net.bluesoft.rnd.pt.ext.jbpm.ProcessDiagramParser;


import javax.xml.parsers.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DefaultJbpmRepository implements JbpmRepository {

    private static final Logger logger = Logger.getLogger(DefaultJbpmRepository.class.getName());

    private static final String DEFAULT_BASE_PATH = ".." + File.separator + ".." + File.separator + "jbpm" + File.separator + "repository";

    private static final String JBPM_REPOSITORY_DIR = "jbpm.repository.dir";

    private String basePath;

    public DefaultJbpmRepository(String basePath){
        if(basePath == null){
            if(System.getProperty(JBPM_REPOSITORY_DIR)!=null){
                this.basePath = System.getProperty(JBPM_REPOSITORY_DIR);
            }else{
                this.basePath= DEFAULT_BASE_PATH;
            }
        }else{
            this.basePath = basePath;
        }
		ensureBasePath();
	}

	@Override
	public List<byte[]> getAllResources(String type) {
		List<byte[]> result;
		try {
			File base = new File(basePath);
			Collection<File> files = FileUtils.listFiles(base, new String[] { type }, true);
			result = new ArrayList<byte[]>(files.size());
			for (File file : files) {
				result.add(FileUtils.readFileToByteArray(file));
			}
			checkForDuplicates(files);
			return result;
		}
		catch (Exception e) {
			throw new RuntimeException(e.getMessage(),e);
		}
	}

	@Override
	public byte[] getResource(String deploymentId, String resourceId) {
		try {
			File file = new File(getPath(deploymentId,resourceId));
			return FileUtils.readFileToByteArray(file);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String addResource(String resourceId, InputStream definitionStream) {
		String deploymentId = getDeploymentId();
		addResource(deploymentId, resourceId, definitionStream);
		return deploymentId;
	}

	@Override
	public void addResource(String deploymentId, String resourceId, InputStream definitionStream) {
		try {
			File file = new File(getPath(deploymentId,resourceId));
			FileUtils.copyInputStreamToFile(definitionStream, file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void checkForDuplicates(Collection<File> files) throws Exception {
		List<String> processIds = new ArrayList<String>();

		for (File file : files){
			processIds.add(getProcessIdFromFile(file));
		}

		for(int i = 0 ; i < processIds.size(); i++){
			for(int j = i+1 ; j < processIds.size(); j++){
				if(processIds.get(i).equals(processIds.get(j))){
					throw new RuntimeException("duplicate process ID: "+processIds.get(i)+" in the path: "+ basePath);
				}
			}
		}
	}

	private String getProcessIdFromFile(File file){

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = null;
		String processId = null;
		try {
			db = dbf.newDocumentBuilder();
			org.w3c.dom.Document document = db.parse(file);
			NodeList nodeList = document.getElementsByTagName("process");
			org.w3c.dom.Node processNode = nodeList.item(0);
			org.w3c.dom.NamedNodeMap processAttributes = processNode.getAttributes();
			org.w3c.dom.Node idAttribute = processAttributes.getNamedItem("id");
			processId = idAttribute.getNodeValue();

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return processId;
	}

	private String getDeploymentId() {
		return String.valueOf(System.currentTimeMillis());
	}
	
	private String getPath(String deploymentId, String resourceId) {
		return basePath + File.separator + deploymentId + File.separator + resourceId;
	}

	private void ensureBasePath() {
		if (basePath != null) {
			new File(basePath).mkdirs();
		}

        logger.log(Level.INFO, "[JBPM] Setting base path to "+basePath);
	}
}
