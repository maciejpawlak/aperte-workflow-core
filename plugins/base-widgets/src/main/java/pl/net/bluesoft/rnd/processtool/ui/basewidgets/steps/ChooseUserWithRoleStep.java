package pl.net.bluesoft.rnd.processtool.ui.basewidgets.steps;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.exceptions.BusinessException;
import pl.net.bluesoft.rnd.processtool.model.BpmStep;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.roles.IUserRolesManager;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author: "mpawlak@bluesoft.net.pl"
 */
@AliasName(name = "ChooseUserWithRoleStep")
public class ChooseUserWithRoleStep implements ProcessToolProcessStep
{
    private final static Logger logger = Logger.getLogger(ChooseUserWithRoleStep.class.getName());

    @AutoWiredProperty
    private String roleName;

    @AutoWiredProperty
    private String assignePropertyName;

    @AutoWiredProperty
    private String expectedCount;

    @Autowired
    private IUserRolesManager userRolesManager;

    @Override
    public String invoke(BpmStep bpmStep, Map<String, String> params) throws Exception
    {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        ProcessInstance processInstance = bpmStep.getProcessInstance();
        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();

        Collection<UserData> users = userRolesManager.getUsersByRole(roleName);

        if(users.isEmpty())
            throw new BusinessException("No user with role: "+roleName);
        else if(users.size() > 1)
            throw new BusinessException("There are "+users.size()+" users ["+getUsersNames(users)+"] with role "+roleName);

        UserData user = users.iterator().next();

        logger.log(Level.INFO, "User with role "+roleName+" selected: "+user.getLogin());

        if(user == null)
            throw new RuntimeException("No user with role: "+roleName);

        processInstance.setSimpleAttribute(assignePropertyName, user.getLogin());

        return STATUS_OK;
    }

    private String getUsersNames(Collection<UserData> users)
    {
        Set<String> userLogins = new HashSet<String>();
        for(UserData user: users)
            userLogins.add(user.getLogin());

        return StringUtils.join(userLogins, ",");
    }
}
