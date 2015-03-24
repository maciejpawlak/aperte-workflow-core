package pl.net.bluesoft.rnd.processtool.web.controller;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.web.domain.IProcessToolRequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author: "mpawlak@bluesoft.net.pl"
 * @author: "pwysocki@bluesoft.net.pl"
 */
public class OsgiWebRequest extends NoTransactionOsgiWebRequest {

    private ProcessToolContext processToolContext;

    public ProcessToolContext getProcessToolContext() {
        return processToolContext;
    }

    public void setProcessToolContext(ProcessToolContext processToolContext) {
        this.processToolContext = processToolContext;
    }
}
