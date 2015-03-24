package pl.net.bluesoft.rnd.util;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;
import pl.net.bluesoft.rnd.processtool.web.controller.NoTransactionOsgiWebRequest;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiWebRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

/**
 * Created by mpawlak@bluesoft.net.pl on 2014-12-11.
 */
public class ControllerUtils {

    public static Boolean getBooleanParameter(NoTransactionOsgiWebRequest invocation, String name) {
        String value = invocation.getRequest().getParameter(name);
        if(StringUtils.isEmpty(value))
        {
            return null;
        }
        return Boolean.parseBoolean(value);
    }

    public static Integer getIntegerParameter(NoTransactionOsgiWebRequest invocation, String name) {
        String value = invocation.getRequest().getParameter(name);
        if(StringUtils.isEmpty(value))
        {
            return null;
        }
        return Integer.parseInt(value);
    }

    public static String getParameter(NoTransactionOsgiWebRequest invocation, String name) {
        return invocation.getRequest().getParameter(name);
    }

    public static <T extends Object> Collection<T> parseJsonList(NoTransactionOsgiWebRequest invocation, String parameterName, Class<T> clazz)
    {
        try
        {
            String jsonListString = getParameter(invocation, parameterName);

            ObjectMapper mapper = new ObjectMapper();
            JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, clazz);
            Collection<T> list = mapper.readValue(jsonListString, type);

            return list;
        }
        catch (Throwable e)
        {
            throw new RuntimeException("Problem during JSON parsing the list of "+clazz.getName());

        }
    }
}
