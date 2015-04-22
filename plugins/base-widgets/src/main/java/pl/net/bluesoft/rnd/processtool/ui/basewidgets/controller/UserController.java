package pl.net.bluesoft.rnd.processtool.ui.basewidgets.controller;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.usersource.IPortalUserSource;
import pl.net.bluesoft.rnd.processtool.web.controller.ControllerMethod;
import pl.net.bluesoft.rnd.processtool.web.controller.IOsgiWebController;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiController;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiWebRequest;
import pl.net.bluesoft.rnd.processtool.web.domain.GenericResultBean;

import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author: mpawlak@bluesoft.net.pl
 */
@OsgiController(name="usercontroller")
public class UserController  implements IOsgiWebController
{
    private final static Logger logger = Logger.getLogger(UserController.class.getName());

    @Autowired
    protected IPortalUserSource portalUserSource;

    @ControllerMethod(action="getAllUsers")
    public GenericResultBean getSyncStatus(final OsgiWebRequest invocation)
    {
        long t0 = System.currentTimeMillis();

        GenericResultBean result = new GenericResultBean();

        String pageLimit = invocation.getRequest().getParameter("page_limit");
        String queryTerm = invocation.getRequest().getParameter("q");

        if(StringUtils.isEmpty(queryTerm))
        {
            Collection<UserData> users =  portalUserSource.getAllUsers();

            result.setData(users);

            return result;
        }


        Collection<UserData> filtered = portalUserSource.findUsers(queryTerm);

        long t1= System.currentTimeMillis();

        logger.log(Level.INFO, "Search user: [1]: " + (t1 - t0) + "ms");
        result.setData(filtered);

        return result;
    }

    @ControllerMethod(action = "getUserByLogin")
    public GenericResultBean getUserByLogin(final OsgiWebRequest invocation) {
        GenericResultBean result = new GenericResultBean();

        String userLogin = invocation.getRequest().getParameter("userLogin");

        UserData user = portalUserSource.getUserByLogin(userLogin);

        result.setData(user);

        return result;
    }
}
