package pl.net.bluesoft.rnd.processtool.exceptions;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Standard exception for business fail-overs
 * Created by mpawlak@bluesoft.net.pl on 2014-12-09.
 */
public class BusinessException extends RuntimeException
{
    private String[] parameters;

    public BusinessException() {
    }

    public BusinessException(String message, String ... params) {
        super(message);

        parameters = params;
    }

    public String[] getParameters()
    {
        return parameters;
    }



    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public BusinessException(Throwable cause) {
        super(cause);
    }
}
