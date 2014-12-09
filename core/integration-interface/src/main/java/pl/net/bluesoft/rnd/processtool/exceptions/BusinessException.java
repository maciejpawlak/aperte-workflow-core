package pl.net.bluesoft.rnd.processtool.exceptions;

/**
 * Standard exception for business fail-overs
 * Created by mpawlak@bluesoft.net.pl on 2014-12-09.
 */
public class BusinessException extends RuntimeException {
    public BusinessException() {
    }

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public BusinessException(Throwable cause) {
        super(cause);
    }
}
