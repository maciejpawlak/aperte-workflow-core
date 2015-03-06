package pl.net.bluesoft.rnd.pt.ext.jbpm.service;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import pl.net.bluesoft.rnd.processtool.BasicSettings;

import java.io.File;
import java.io.InputStream;
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
        logger.log(Level.INFO, "[JBPM] Initializing default repository...");
        if(basePath == null){
            if(StringUtils.isNotEmpty(System.getProperty(JBPM_REPOSITORY_DIR))){
                this.basePath = System.getProperty(JBPM_REPOSITORY_DIR);
                logger.log(Level.INFO, "[JBPM] Base path set from system property to "+this.basePath);
            }else{
                this.basePath= DEFAULT_BASE_PATH;
                logger.log(Level.INFO, "[JBPM] Base path set from default path to "+this.basePath);
            }
        }else{
            this.basePath = basePath;
            logger.log(Level.INFO, "[JBPM] Base path set from database settings to "+this.basePath);
        }
		ensureBasePath();
	}

	@Override
	public List<byte[]> getAllResources(String type) {
		try {
            logger.log(Level.SEVERE, "[JBPM] Getting files from repository with type: "+type );
			File base = new File(basePath);
			Collection<File> files = FileUtils.listFiles(base, new String[] { type }, true);
			List<byte[]> result = new ArrayList<byte[]>(files.size());

			for (File file : files) {
                logger.log(Level.INFO, "[JBPM] Loading file to kseession: "+file.getPath()+"/"+file.getName());
				result.add(FileUtils.readFileToByteArray(file));
			}
			return result;
		}
		catch (Throwable e) {
            logger.log(Level.SEVERE, "[JBPM] Error during resource obitaining with type "+type, e);
		}
		return null;
	}

	@Override
	public byte[] getResource(String deploymentId, String resourceId) {
		try {
			File file = new File(getPath(deploymentId,resourceId));
			return FileUtils.readFileToByteArray(file);
		}
		catch (Throwable e) {
            logger.log(Level.SEVERE, "[JBPM] Error during resource obitaining with deploymentId= "+deploymentId+" and resourceId="+resourceId, e);
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
            logger.log(Level.INFO, "[JBPM] Addding new bpmn process to repository [resourceId="+resourceId+", deploymentId="+deploymentId+"]");
			File file = new File(getPath(deploymentId,resourceId));
			FileUtils.copyInputStreamToFile(definitionStream, file);
		} catch (Exception e) {
            logger.log(Level.SEVERE, "[JBPM] Error during resource adding with deploymentId= "+deploymentId+" and resourceId="+resourceId, e);
		}
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
