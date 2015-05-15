package pl.net.bluesoft.rnd.processtool.ui.basewidgets.controller;

import org.aperteworkflow.admin.controller.CommentTime;
import org.aperteworkflow.ui.help.datatable.JQueryDataTable;
import org.aperteworkflow.ui.help.datatable.JQueryDataTableColumn;
import org.aperteworkflow.ui.help.datatable.JQueryDataTableUtil;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.UserSubstitution;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.usersource.IPortalUserSource;
import pl.net.bluesoft.rnd.processtool.web.controller.ControllerMethod;
import pl.net.bluesoft.rnd.processtool.web.controller.IOsgiWebController;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiController;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiWebRequest;
import pl.net.bluesoft.rnd.processtool.web.domain.DataPagingBean;
import pl.net.bluesoft.rnd.processtool.web.domain.GenericResultBean;
import pl.net.bluesoft.rnd.processtool.web.domain.IProcessToolRequestContext;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;

import static pl.net.bluesoft.util.lang.DateUtil.beginOfDay;
import static pl.net.bluesoft.util.lang.DateUtil.endOfDay;
import static pl.net.bluesoft.util.lang.Formats.parseShortDate;

/**
 * Created by Marcin Król on 2014-06-30.
 */
@OsgiController(name = "commentsController")
public class CommentsController implements IOsgiWebController {

    @ControllerMethod(action = "getCommentTime")
    public GenericResultBean getCommentTime(final OsgiWebRequest invocation) {

        GenericResultBean resultBean = new GenericResultBean();

        CommentTime commentTime = new CommentTime();
        Calendar calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("Europe/Warsaw"));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yy HH:mm");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Warsaw"));

        commentTime.setTime(calendar.getTime());
        commentTime.setFormattedTime(simpleDateFormat.format(calendar.getTime()));

        resultBean.setData(commentTime);
        return resultBean;
    }
}
