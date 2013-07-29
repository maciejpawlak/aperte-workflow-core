package pl.net.bluesoft.rnd.pt.ext.jbpm;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import pl.net.bluesoft.rnd.processtool.BasicSettings;
import pl.net.bluesoft.rnd.processtool.IProcessToolSettings;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolSessionFactory;
import pl.net.bluesoft.rnd.processtool.bpm.exception.ProcessToolException;
import pl.net.bluesoft.rnd.processtool.dao.*;
import pl.net.bluesoft.rnd.processtool.dict.GlobalDictionaryProvider;
import pl.net.bluesoft.rnd.processtool.dict.ProcessDictionaryRegistry;
import pl.net.bluesoft.rnd.processtool.hibernate.HibernateBean;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.UserDataBean;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessToolSequence;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessToolSetting;
import pl.net.bluesoft.util.eventbus.EventBusManager;
import pl.net.bluesoft.util.lang.Formats;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;

/**
 * Context replacement for Spring library
 *
 * @author tlipski@bluesoft.net.pl
 */
public class ProcessToolContextImpl implements ProcessToolContext { 
    private Session hibernateSession;
    private ProcessDictionaryRegistry processDictionaryRegistry;

    private Map<Class<? extends HibernateBean>, HibernateBean> daoCache = new HashMap<Class<? extends HibernateBean>, HibernateBean>();

    private Boolean closed = false;

    public ProcessToolContextImpl(Session hibernateSession) {
        this.hibernateSession = hibernateSession;
    }

    private synchronized void verifyContextOpen() {
        if (closed) {
            throw new ProcessToolException("Context already closed!");
        }
    }

    public void init() {
    }

    @Override
	public boolean isActive()
    {
    	/* Check hibernate session */
    	if(!hibernateSession.isOpen())
    		return false;
    	
        return !closed;
    }

    @Override
    public ProcessDictionaryRegistry getProcessDictionaryRegistry() {
        if (processDictionaryRegistry == null) {
            processDictionaryRegistry = new ProcessDictionaryRegistry();
            processDictionaryRegistry.addDictionaryProvider("db", (GlobalDictionaryProvider)getProcessDictionaryDAO());
        }
        return processDictionaryRegistry;
    }

    @SuppressWarnings("unchecked")
	private <T extends HibernateBean> T getHibernateDAO(Class<T> daoClass) {
        verifyContextOpen();
        if (!daoCache.containsKey(daoClass)) {
            T dao = null;
            if (ProcessDictionaryDAO.class.equals(daoClass)) {
                dao = (T) getRegistry().getProcessDictionaryDAO(hibernateSession);
            } else if (ProcessInstanceDAO.class.equals(daoClass)) {
                dao = (T) getRegistry().getProcessInstanceDAO(hibernateSession);
            } else if (ProcessInstanceFilterDAO.class.equals(daoClass)) {
                dao = (T) getRegistry().getProcessInstanceFilterDAO(hibernateSession);
            } else if (ProcessDefinitionDAO.class.equals(daoClass)) {
                dao = (T) getRegistry().getProcessDefinitionDAO(hibernateSession);
            } else if (UserSubstitutionDAO.class.equals(daoClass)) {
                dao = (T) getRegistry().getUserSubstitutionDAO(hibernateSession);
            }else if (ProcessInstanceSimpleAttributeDAO.class.equals(daoClass)) {
                dao = (T) getRegistry().getProcessInstanceSimpleAttributeDAO(hibernateSession);
            }else if (ProcessStateActionDAO.class.equals(daoClass)) {
                dao = (T) getRegistry().getProcessStateAction(hibernateSession);
            }
            if (dao != null) {
                daoCache.put(daoClass, dao);
            }
        }
        return (T) daoCache.get(daoClass);
    }

    public ProcessDictionaryDAO getProcessDictionaryDAO() {
        return getHibernateDAO(ProcessDictionaryDAO.class);
    }

    @Override
    public ProcessInstanceDAO getProcessInstanceDAO() {
        return getHibernateDAO(ProcessInstanceDAO.class);
    }

    @Override
    public ProcessInstanceFilterDAO getProcessInstanceFilterDAO() {
        return getHibernateDAO(ProcessInstanceFilterDAO.class);
    }

    @Override
    public ProcessDefinitionDAO getProcessDefinitionDAO() {
        return getHibernateDAO(ProcessDefinitionDAO.class);
    }

    @Override
    public UserSubstitutionDAO getUserSubstitutionDAO() {
        return getHibernateDAO(UserSubstitutionDAO.class);
    }

    @Override
	public ProcessInstanceSimpleAttributeDAO getProcessInstanceSimpleAttributeDAO() {
		return getHibernateDAO(ProcessInstanceSimpleAttributeDAO.class);
	}
    
    @Override
	public ProcessStateActionDAO getProcessStateActionDAO() {
		return getHibernateDAO(ProcessStateActionDAO.class);
	}


    @Override
    public Session getHibernateSession() {
        verifyContextOpen();
        return hibernateSession;
    }

    @Override
    public ProcessToolSessionFactory getProcessToolSessionFactory() {
        return getRegistry().getProcessToolSessionFactory();
    }

    @Override
    public EventBusManager getEventBusManager() {
        return getRegistry().getEventBusManager();
    }

    @Override
    public String getSetting(IProcessToolSettings key) {
        verifyContextOpen();
        ProcessToolSetting setting = (ProcessToolSetting) hibernateSession.createCriteria(ProcessToolSetting.class)
                .add(Restrictions.eq("key", key.toString())).uniqueResult();
        return setting != null ? setting.getValue() : null;
    }

    @Override
    public void setSetting(IProcessToolSettings key, String value) {
        verifyContextOpen();
        List list = hibernateSession.createCriteria(ProcessToolSetting.class).add(Restrictions.eq("key", key.toString())).list();
        ProcessToolSetting setting;
        if (list.isEmpty()) {
            setting = new ProcessToolSetting();
            setting.setKey(key.toString());
        } else {
            setting = (ProcessToolSetting) list.get(0);
        }
        setting.setValue(value);
        hibernateSession.saveOrUpdate(setting);
    }

    @Override
    public long getNextValue(String processDefinitionName, String sequenceName) 
    {
    	/* Create new session to handle atomic operations with for update lock. There is no 
    	 * possibility to create new transaction inside another in the same session 
    	 * 
    	 * If one creates for update query in current hibernateSession there is 
    	 * possibility to make deadlock
    	 */
    	Session newValueSession = hibernateSession.getSessionFactory().openSession();
    	Transaction tx = newValueSession.beginTransaction();

    	verifyContextOpen();
    	
    	String queryString = 
    			"select seq.* from pt_sequence seq where seq.name = :sequenceName " +
    			(processDefinitionName != null ? "and seq.processdefinitionname = :processDefinitionName " : "")+
    			"for update";
    	
    	SQLQuery query =
    			newValueSession.createSQLQuery(queryString);
    	
    	query.setParameter("sequenceName", (String)sequenceName);
    	
    	if(processDefinitionName != null)
    		query.setParameter("processDefinitionName", (String)processDefinitionName);
    	
    	query.addEntity("seq", ProcessToolSequence.class);
    	
    	ProcessToolSequence seq = (ProcessToolSequence)query.uniqueResult();
    	
    	if(seq == null)
    	{
            seq = new ProcessToolSequence();
            seq.setProcessDefinitionName(processDefinitionName);
            seq.setName(sequenceName);
            seq.setValue(0);
    	}
    	seq.setValue(seq.getValue() + 1);
    	
    	
    	newValueSession.saveOrUpdate(seq);
    	newValueSession.flush();
    	tx.commit();
    	newValueSession.close();
        return seq.getValue();
    }

    @Override
    public long getNextValue(ProcessInstance processInstance, String sequenceName) {
        return getNextValue(processInstance.getDefinitionName(), sequenceName);
    }

    @Override
    public long getNextValue(String sequenceName) {
        return getNextValue((String) null, sequenceName);
    }

    @Override
    public void updateContext(ProcessInstance processInstance) {
    }

	@Override
	public void close() {
		this.closed = true;
	}
}
