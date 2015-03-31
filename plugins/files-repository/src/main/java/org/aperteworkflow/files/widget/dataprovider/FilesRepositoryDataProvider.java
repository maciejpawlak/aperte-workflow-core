package org.aperteworkflow.files.widget.dataprovider;

import org.aperteworkflow.files.IFilesRepositoryFacade;
import org.aperteworkflow.files.model.FilesRepositoryItemDTO;
import org.aperteworkflow.files.model.IFilesRepositoryItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.ui.widgets.IWidgetDataProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author pwysocki@bluesoft.net.pl
 */
public class FilesRepositoryDataProvider implements IWidgetDataProvider {
    private static final String PROCESS_INSTANCE_FILES_PARAMETER = "processInstanceFiles";

    private static final String FILES_PARAMETER = "files";

    @Autowired(required = false)
    protected IFilesRepositoryFacade filesRepoFacade;


    @Override
    public Map<String, Object> getData(IAttributesProvider provider, Map<String, Object> baseViewData) {
        if(filesRepoFacade == null)
            SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        Map<String, Object> data = new HashMap<String, Object>();

        ProcessInstance processInstance = provider.getProcessInstance();
        if (processInstance != null)
            data.put(PROCESS_INSTANCE_FILES_PARAMETER, getFiles(processInstance.getId()));
        else
            data.put(FILES_PARAMETER, getFiles(provider.getId()));

        return data;
    }

    private Collection<FilesRepositoryItemDTO> getFiles(Long id)
    {
        Collection<FilesRepositoryItemDTO> dtos = new ArrayList<FilesRepositoryItemDTO>();
        Collection<IFilesRepositoryItem> items =  filesRepoFacade.getFilesList(id);
        for(IFilesRepositoryItem item: items)
            dtos.add(new FilesRepositoryItemDTO(item, id));

        return dtos;
    }

}
