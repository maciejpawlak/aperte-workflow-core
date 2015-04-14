package pl.net.bluesoft.rnd.processtool.ui.basewidgets.controller;

import org.aperteworkflow.ui.help.datatable.JQueryDataTable;
import org.aperteworkflow.ui.help.datatable.JQueryDataTableColumn;
import org.aperteworkflow.ui.help.datatable.JQueryDataTableUtil;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.dao.impl.UserSubstitutionDAOImpl;
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

import java.io.IOException;
import java.lang.Long;import java.lang.String;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

import static pl.net.bluesoft.util.lang.DateUtil.beginOfDay;
import static pl.net.bluesoft.util.lang.DateUtil.endOfDay;
import static pl.net.bluesoft.util.lang.Formats.parseShortDate;

/**
 * 
 * Substitution operations controller for admin portlet
 * 
 * @author: mpawlak@bluesoft.net.pl
 */
@OsgiController(name = "substitutionsController")
public class SubstitutionsController implements IOsgiWebController {
	@Autowired
	protected IPortalUserSource portalUserSource;

	@Autowired
	protected ProcessToolRegistry processToolRegistry;

	@ControllerMethod(action = "loadSubstitutions")
	public GenericResultBean loadSubstitutions(final OsgiWebRequest invocation) {

		IProcessToolRequestContext requestContext = invocation.getProcessToolRequestContext();
		ProcessToolContext ctx = invocation.getProcessToolContext();

		JQueryDataTable dataTable = JQueryDataTableUtil.analyzeRequest(invocation.getRequest().getParameterMap());
		JQueryDataTableColumn sortingColumn = dataTable.getFirstSortingColumn();

		UserData user = requestContext.getUser();

		List<UserSubstitution> substitutionList;
        Long subtitiutionsCount = 0l;

		if (user.hasRole("Administrator")) {
            substitutionList = (List<UserSubstitution>) ctx
                    .getHibernateSession()
                    .createCriteria(UserSubstitution.class)
                    .addOrder(
                            sortingColumn.getSortedAsc() ? Order.asc(sortingColumn.getPropertyName()) : Order.desc(sortingColumn
                                    .getPropertyName())).setMaxResults(dataTable.getPageLength()).setFirstResult(dataTable.getPageOffset())
                    .list();

            subtitiutionsCount = (Long)ctx
                    .getHibernateSession()
                    .createCriteria(UserSubstitution.class)
                    .setProjection(Projections.rowCount())
                    .uniqueResult();
        }

		else {
            substitutionList = (List<UserSubstitution>) ctx
                    .getHibernateSession()
                    .createCriteria(UserSubstitution.class)
                    .add(Restrictions.or(Restrictions.eq("userLogin", user.getLogin()),
                            Restrictions.eq("userSubstituteLogin", user.getLogin())))
                    .addOrder(
                            sortingColumn.getSortedAsc() ? Order.asc(sortingColumn.getPropertyName()) : Order.desc(sortingColumn
                                    .getPropertyName())).setMaxResults(dataTable.getPageLength()).setFirstResult(dataTable.getPageOffset())
                    .list();

            subtitiutionsCount = (Long)ctx
                    .getHibernateSession()
                    .createCriteria(UserSubstitution.class)
                    .add(Restrictions.or(Restrictions.eq("userLogin", user.getLogin()),
                            Restrictions.eq("userSubstituteLogin", user.getLogin())))
                    .setProjection(Projections.rowCount())
                    .uniqueResult();
        }

		DataPagingBean<UserSubstitution> dataPagingBean = new DataPagingBean<UserSubstitution>(substitutionList, subtitiutionsCount.intValue(),
				dataTable.getDraw());

		return dataPagingBean;
	}

	@ControllerMethod(action = "deleteSubstitution")
	public GenericResultBean deleteSubtitution(final OsgiWebRequest invocation) {

		GenericResultBean result = new GenericResultBean();

		String substitutionId = invocation.getRequest().getParameter("substitutionId");

		IProcessToolRequestContext requestContext = invocation.getProcessToolRequestContext();
		ProcessToolContext ctx = invocation.getProcessToolContext();

        if(!invocation.getProcessToolRequestContext().getUser().hasRole("Administrator")) {

            UserSubstitution substitution = (UserSubstitution) ctx
                    .getHibernateSession()
                    .createCriteria(UserSubstitution.class)
                    .add(Restrictions.or(Restrictions.eq("userLogin", invocation.getProcessToolRequestContext().getUser().getLogin()),
                            Restrictions.eq("userSubstituteLogin", invocation.getProcessToolRequestContext().getUser().getLogin())))
                    .add(Restrictions.eq("id", Long.parseLong(substitutionId)))
                    .uniqueResult();

            if (substitution == null) {
                result.addError("SYSTEM", invocation.getProcessToolRequestContext().getMessageSource().getMessage("substitution.not.exists"));
                return result;
            }

            ctx.getUserSubstitutionDAO().delete(substitution);


        }
        else
        {
            ctx.getUserSubstitutionDAO().deleteById(Long.parseLong(substitutionId));
        }
        return result;


	}

	@ControllerMethod(action = "addOrEditSubstitution")
	public GenericResultBean addNewSubstitution(final OsgiWebRequest invocation) {
		GenericResultBean result = new GenericResultBean();

		HttpServletRequest request = invocation.getRequest();

		Long id = request.getParameter("SubstitutionId").isEmpty() ? null : Long.parseLong(request.getParameter("SubstitutionId"));
		String userLogin = request.getParameter("UserLogin");
		String userSubstituteLogin = request.getParameter("UserSubstituteLogin");
		Date dateFrom = beginOfDay(parseShortDate(request.getParameter("SubstitutingDateFrom")));
		Date dateTo = endOfDay(parseShortDate(request.getParameter("SubstitutingDateTo")));

		UserSubstitution userSubstitution = new UserSubstitution();

		userSubstitution.setId(id);
		userSubstitution.setUserLogin(userLogin);
		userSubstitution.setUserSubstituteLogin(userSubstituteLogin);

        userSubstitution.setDateFrom(beginOfDay(dateFrom));
        userSubstitution.setDateTo(endOfDay(dateTo));

		IProcessToolRequestContext requestContext = invocation.getProcessToolRequestContext();
		ProcessToolContext ctx = invocation.getProcessToolContext();

		ctx.getUserSubstitutionDAO().saveOrUpdate(userSubstitution);

		return result;
	}


    public static Date beginOfDay(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("Europe/Warsaw"));
        c.setTime(date);
        c.set(c.get(1), c.get(2), c.get(5), 0, 0, 0);
        c.set(14, 0);
        return c.getTime();
    }

    public static Date endOfDay(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("Europe/Warsaw"));
        c.setTime(date);
        c.set(c.get(1), c.get(2), c.get(5), 23, 59, 59);
        c.set(14, 999);
        return c.getTime();
    }


}
