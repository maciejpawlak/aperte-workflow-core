package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.step;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.BpmStep;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessQueueConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessQueueRight;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.processsource.IProcessSource;
import pl.net.bluesoft.rnd.processtool.roles.IUserRolesManager;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.EmailSender;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.IBpmNotificationService;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.NotificationData;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.TemplateData;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;

@AliasName(name = "SendMailStep")
public class SendMailStep implements ProcessToolProcessStep {
    @AutoWiredProperty
    private String recipient;
    
    @AutoWiredProperty
    private String profileName = "Default";
    
    @AutoWiredProperty
    private String template;

	@AutoWiredProperty
	private String queueName;

	@Autowired
	private ProcessToolRegistry registry;

	@Autowired
	private IUserRolesManager userRolesManager;
    
    private final static Logger logger = Logger.getLogger(SendMailStep.class.getName());

    @Override
    public String invoke(BpmStep step, Map<String, String> params) throws Exception {
		try
		{
			IBpmNotificationService service = getRegistry().getRegisteredService(IBpmNotificationService.class);

			Set<UserData> recipients = new HashSet<UserData>();

			if(StringUtils.isNotEmpty(recipient)) {
				UserData user = findUser(recipient, step.getProcessInstance());
				if(user != null)
					recipients.add(user);
			}

			/** If queue name was given, add recipients with role from queue.role */
			if(StringUtils.isNotEmpty(queueName))
			{
				ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
				ProcessQueueConfig processQueueConfig = registry.getDataRegistry().getProcessDefinitionDAO(ctx.getHibernateSession()).getQueueConfig(queueName);

				if(processQueueConfig != null)
				{
					for(ProcessQueueRight processQueueRight: processQueueConfig.getRights())
					{
						String roleName = processQueueRight.getRoleName();
						recipients.addAll(userRolesManager.getUsersByRole(roleName));
					}
				}

			}


			for(UserData user: recipients) {

				TemplateData templateData = service.createTemplateData(template, Locale.getDefault());

				service.getTemplateDataProvider()
						.addProcessData(templateData, step.getProcessInstance())
						.addUserToNotifyData(templateData, user);

				NotificationData notificationData = new NotificationData()
						.setProfileName("Default")
						.setRecipient(user)
						.setTemplateData(templateData);


					EmailSender.sendEmail(service, notificationData);

			}
		} catch (Throwable e) {
			logger.log(Level.SEVERE, "Error sending email", e);
		}
        return STATUS_OK;
    }

	private UserData findUser(String recipient, ProcessInstance pi) {
		if (recipient == null) {
			return null;
		}
		recipient = recipient.trim();
		if(recipient.matches("#\\{.*\\}")){
        	String loginKey = recipient.replaceAll("#\\{(.*)\\}", "$1");
        	recipient = pi.getSimpleAttributeValue(loginKey);
    		if (recipient == null)
            {
                recipient = pi.getSimpleAttributeValue(loginKey);
                if(recipient == null)
                    return null;
    		}
        }
		return getRegistry().getUserSource().getUserByLogin(recipient);
	}
}

