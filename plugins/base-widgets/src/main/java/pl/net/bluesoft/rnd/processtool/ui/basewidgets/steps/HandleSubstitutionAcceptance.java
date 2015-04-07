package pl.net.bluesoft.rnd.processtool.ui.basewidgets.steps;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.BpmStep;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserSubstitution;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AperteDoc;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.rnd.util.StepUtil;
import pl.net.bluesoft.util.lang.exception.UtilityInvocationException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Logger;

import static pl.net.bluesoft.rnd.processtool.ProcessToolContext.Util.getThreadProcessToolContext;

/**
 * @author mpawlak@bluesoft.net.pl
 */
@AliasName(name = "HandleSubstitutionAcceptanceStep")
public class HandleSubstitutionAcceptance implements ProcessToolProcessStep
{
    private static final Logger logger = Logger.getLogger(HandleSubstitutionAcceptance.class.getName());

    @AutoWiredProperty(required = true)
    @AperteDoc(humanNameKey="substituting.user.label", descriptionKey="substituting.user.label")
    private String userSubstituteLoginAttributeName;

    @AutoWiredProperty(required = true)
    @AperteDoc(humanNameKey="substitute.user.label", descriptionKey="substitute.user.label")
    private String userLoginAttributeName;

    @AutoWiredProperty(required = true)
    @AperteDoc(humanNameKey="substituting.date.from.label", descriptionKey="substituting.date.from.label")
    private String dateFromAttributeName;

    @AutoWiredProperty(required = true)
    @AperteDoc(humanNameKey="substituting.date.to.label", descriptionKey="substituting.date.to.label")
    private String dateToAttributeName;

    @Override
    public String invoke(BpmStep step, Map params) throws Exception {
        ProcessInstance processInstance = step.getProcessInstance();
        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();


        String userLoginKey = StepUtil.extractVariable(userLoginAttributeName, ctx, processInstance);
        String userSubstituteLoginKey= StepUtil.extractVariable(userSubstituteLoginAttributeName, ctx, processInstance);
        String dateFromKey = StepUtil.extractVariable(dateFromAttributeName, ctx, processInstance);
        String dateToKey = StepUtil.extractVariable(dateToAttributeName, ctx, processInstance);

        UserSubstitution userSubstitution = new UserSubstitution();

        userSubstitution.setUserLogin(userLoginKey);
        userSubstitution.setDateFrom(beginOfDay(parseShortDate(dateFromKey)));
        userSubstitution.setDateTo(endOfDay(parseShortDate(dateToKey)));
        userSubstitution.setUserSubstituteLogin(userSubstituteLoginKey);

        getThreadProcessToolContext().getUserSubstitutionDAO().saveOrUpdate(userSubstitution);
        logger.warning("Added substitution for user " + userSubstitution.getUserLogin());
        return STATUS_OK;

    }

    public static Date parseShortDate(String val) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

        try {
            return sdf.parse(val);
        } catch (ParseException var3) {
            throw new UtilityInvocationException(var3);
        }
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
