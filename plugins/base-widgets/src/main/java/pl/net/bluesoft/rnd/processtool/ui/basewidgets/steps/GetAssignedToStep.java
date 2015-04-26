package pl.net.bluesoft.rnd.processtool.ui.basewidgets.steps;


import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.*;
import pl.net.bluesoft.rnd.processtool.roles.IUserRolesManager;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;

import java.util.*;
import java.util.logging.Logger;

import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;

/**
 * Get last assigned person to task with given name
 */
@AliasName(name = "GetAssignedToStep")
public class GetAssignedToStep implements ProcessToolProcessStep {
	
	@AutoWiredProperty
	private String stepName;

    @AutoWiredProperty
    private String attributeKey;

    @AutoWiredProperty
    private String required = "false";

    @Autowired
    private IUserRolesManager userRolesManager;

	private final static Logger logger = Logger.getLogger(GetAssignedToStep.class.getName());

    @Override
    public String invoke(BpmStep step, Map<String, String> params) throws Exception
    {
        if(stepName == null)
            return STATUS_ERROR;

        if(attributeKey == null)
            return STATUS_ERROR;

    	ProcessInstance pi = step.getProcessInstance();
        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();

        //TODO future architecture refactor
        ProcessToolBpmSession bpmSession = getRegistry().getProcessToolSessionFactory().createAutoSession();


        BpmTask task = bpmSession.getLastHistoryTaskByName(Long.parseLong(pi.getInternalId()), stepName);

        if(task == null) {
            String controllerName = getControllerFromLogs(pi);

            if (StringUtils.isEmpty(controllerName))
            {
                UserData financialController = userRolesManager.getFirstUserWithRole("FINANCIAL_CONTROLLER");


                if(financialController == null && Boolean.parseBoolean(required))
                    throw new RuntimeException("No task with given step name: " + stepName);
                else
                    pi.setSimpleAttribute(attributeKey, financialController.getLogin());
            }
            else
                pi.setSimpleAttribute(attributeKey, controllerName);
        }
        else
            pi.setSimpleAttribute(attributeKey, task.getAssignee());

    	return STATUS_OK;
    }

    private String getControllerFromLogs(ProcessInstance pi)
    {
        List<ProcessInstanceLog> logs = new ArrayList<ProcessInstanceLog>(pi.getProcessLogs());
        Collections.sort(logs, new Comparator<ProcessInstanceLog>() {
            @Override
            public int compare(ProcessInstanceLog o1, ProcessInstanceLog o2) {
                return o1.getEntryDate().compareTo(o2.getEntryDate());
            }
        });
        for(ProcessInstanceLog log: pi.getProcessLogs())
        {
            if(StringUtils.isEmpty(log.getLogValue()))
                continue;

            if(log.getLogValue().equals("analyst_controller_acceptance_reject") ||
                log.getLogValue().equals("analyst_controller_acceptance_accept"))
                return log.getUserLogin();
        }

        return null;
    }
}
