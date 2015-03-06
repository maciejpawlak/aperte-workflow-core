package pl.net.bluesoft.rnd.pt.ext.jbpm;

import bitronix.tm.TransactionManagerServices;
import bitronix.tm.jndi.BitronixInitialContextFactory;
import bitronix.tm.resource.jdbc.PoolingDataSource;
import junit.framework.TestCase;
import org.drools.KnowledgeBase;
import org.drools.SystemEventListenerFactory;
import org.drools.base.MapGlobalResolver;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.event.process.*;
import org.drools.impl.EnvironmentFactory;
import org.drools.io.ResourceFactory;
import org.drools.persistence.jpa.JPAKnowledgeService;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.NodeInstance;
import org.drools.runtime.process.ProcessInstance;
import org.jbpm.persistence.JpaProcessPersistenceContextManager;
import org.jbpm.process.audit.JPAWorkingMemoryDbLogger;
import org.jbpm.process.workitem.wsht.LocalHTWorkItemHandler;
import org.jbpm.task.event.TaskEventListener;
import org.jbpm.task.event.entity.TaskUserEvent;
import org.jbpm.task.identity.UserGroupCallbackManager;
import org.jbpm.task.service.TaskServiceSession;
import org.jbpm.task.service.local.LocalTaskService;
import org.jbpm.task.utils.OnErrorAction;
import org.junit.After;
import pl.net.bluesoft.rnd.pt.ext.jbpm.service.AwfUserCallback;
import pl.net.bluesoft.rnd.pt.ext.jbpm.service.DefaultJbpmRepository;
import pl.net.bluesoft.rnd.pt.ext.jbpm.service.JbpmRepository;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by mpawlak@bluesoft.net.pl on 2015-02-27.
 */
public class MigrationTest extends TestCase
{
    protected Logger log = Logger.getLogger(MigrationTest.class.getName());

    private static final int MAX_PROC_DEF_LENGTH = 1024;

    private EntityManagerFactory emf;
    private Environment env;
    private org.jbpm.task.service.TaskService taskService;
    private org.jbpm.task.TaskService client;
    private StatefulKnowledgeSession ksession;
    private JbpmRepository repository;
    private KnowledgeBase knowledgeBase;
    private BpmListener bpmListener;
    private PoolingDataSource ds1;
    private String repositoryDir;
    private int ksessionId = -1;

    public void testMigration() throws NamingException {

        repositoryDir = "E:\\Liferay\\liferay-portal-6.0.6-dpd\\tomcat-6.0.29\\jbpm-new";

        System.setProperty("bitronix.tm.jndi.userTransactionName", "btmTransactionManager");
        System.setProperty("java.naming.factory.initial", "bitronix.tm.jndi.BitronixInitialContextFactory");
        System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");
        System.setProperty("use.bitronix", "true");
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
        System.setProperty(Context.URL_PKG_PREFIXES,  "org.apache.naming");

        ds1 = new PoolingDataSource();
        ds1.setUniqueName("aperte-workflow-ds");
        ds1.setClassName("org.postgresql.xa.PGXADataSource");
        ds1.setMaxPoolSize(5);
        ds1.setAllowLocalTransactions(true);
        ds1.getDriverProperties().put("user", "dpd");
        ds1.getDriverProperties().put("password", "dpd");
        ds1.getDriverProperties().put("portNumber", "5432");
        ds1.getDriverProperties().put("databaseName", "dpd-jbpm-migration");
        ds1.getDriverProperties().put("serverName", "localhost");

        ds1.init();

        InitialContext ic = new InitialContext();

        ic.createSubcontext("java:");
        ic.createSubcontext("java:/comp");
        ic.createSubcontext("java:/comp/env");
        ic.createSubcontext("java:/comp/env/jdbc");

        ic.createSubcontext("java:comp");
        ic.createSubcontext("java:comp/env");
        ic.createSubcontext("java:comp/env/jdbc");

        ic.bind("aperte-workflow-ds", ds1);

        ic.bind("UserTransaction", TransactionManagerServices.getTransactionManager());
        ic.bind("java:comp/UserTransaction", TransactionManagerServices.getTransactionManager());
        ic.bind("java:/comp/UserTransaction", TransactionManagerServices.getTransactionManager());

        System.setProperty("org.aperteworkflow.datasource", "aperte-workflow-ds");

        log.log(Level.INFO, "Init bpm listerer...");
        bpmListener = new BpmListener();
        log.log(Level.INFO, "Init entiity manager...");
        initEntityManager();
        log.log(Level.INFO, "Init environment...");
        initEnvironment();
        log.log(Level.INFO, "Init client...");
        initClient();


        log.log(Level.INFO, "Loading ksession [id="+ksessionId+", repositoryPath="+repositoryDir+"]..." );
        StatefulKnowledgeSession session = getSession();
        TaskServiceSession taskServiceSession = taskService.createSession();

        log.log(Level.INFO, "Initialization done!");

        Map<String, Object> params = new HashMap<String, Object>();
        ProcessInstance processInstance = session.startProcess("Demand_Process_2", params);


    }

    @After
    public void tearDown() throws Exception {
        emf.close();
        ds1.close();
    }

    private void initEntityManager() {
        emf = Persistence.createEntityManagerFactory("org.jbpm.persistence.jpa");
    }

    private void initEnvironment() {
        env = EnvironmentFactory.newEnvironment();
        env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);
        env.set(EnvironmentName.TRANSACTION_MANAGER, TransactionManagerServices.getTransactionManager());
        env.set(EnvironmentName.GLOBALS, new MapGlobalResolver() );
    }

    private void initClient() {
        taskService = new org.jbpm.task.service.TaskService(emf, SystemEventListenerFactory.getSystemEventListener());
        UserGroupCallbackManager.getInstance().setCallback(new AwfUserCallback());

        LocalTaskService localTaskService = new LocalTaskService(taskService);
        localTaskService.setEnvironment(env);
        localTaskService.addEventListener(bpmListener);
        client = localTaskService;
    }


    private StatefulKnowledgeSession getSession() {
        if (ksession == null) {

            loadSession(ksessionId);

            ksessionId = ksession.getId();
        }
        return ksession;
    }

    private void loadSession(int sessionId) {
        KnowledgeBase kbase = getKnowledgeBase();

        if (sessionId == -1) {
            ksession = JPAKnowledgeService.newStatefulKnowledgeSession(kbase, null, env);
        } else {
            ksession = JPAKnowledgeService.loadStatefulKnowledgeSession(sessionId, kbase, null, env);
            //ksession.signalEvent("Trigger", null); // may be necessary for pushing processes after server restart
        }

        LocalHTWorkItemHandler handler = new LocalHTWorkItemHandler(client, ksession, OnErrorAction.LOG);
        handler.connect();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", handler);
        new JPAWorkingMemoryDbLogger(ksession);
        ksession.addEventListener(bpmListener);
    }

    private KnowledgeBase getKnowledgeBase() {

        if (knowledgeBase==null) {
            KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

            try {
                Thread.currentThread().getContextClassLoader().loadClass(JbpmStepAction.class.getName());
            } catch (ClassNotFoundException e) {
                log.warning("JbpmStepAction.class was not found");
            }

            if (getRepository() != null) {
                for (byte[] resource : getValidResources()) {
                    kbuilder.add(ResourceFactory.newByteArrayResource(resource), ResourceType.BPMN2);
                }
            }

            knowledgeBase = kbuilder.newKnowledgeBase();
        }
        return knowledgeBase;
    }

    public synchronized JbpmRepository getRepository() {
        if (repository == null) {
            repository = new DefaultJbpmRepository(repositoryDir);
        }
        return repository;
    }

    private List<byte[]> getValidResources() {
        List<byte[]> validResources = new ArrayList<byte[]>();
        for (byte[] resource : getRepository().getAllResources("bpmn")) {
            if (isValidResource(resource)) validResources.add(resource);
        }
        return validResources;
    }

    private boolean isValidResource(byte[] resource) {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newByteArrayResource(resource), ResourceType.BPMN2);
        boolean isOK = true;
        try {
            kbuilder.newKnowledgeBase();
        } catch (Exception e) {
            isOK = false;
            log.info("The following process definition contains errors and was not loaded:\n" + new String(resource).substring(0, Math.min(MAX_PROC_DEF_LENGTH, resource.length-1)) + "...");
        }
        return isOK;
    }

    private class BpmListener implements ProcessEventListener, TaskEventListener
    {

        @Override
        public void beforeProcessStarted(ProcessStartedEvent processStartedEvent) {

        }

        @Override
        public void afterProcessStarted(ProcessStartedEvent processStartedEvent) {

        }

        @Override
        public void beforeProcessCompleted(ProcessCompletedEvent processCompletedEvent) {

        }

        @Override
        public void afterProcessCompleted(ProcessCompletedEvent processCompletedEvent) {

        }

        @Override
        public void beforeNodeTriggered(ProcessNodeTriggeredEvent processNodeTriggeredEvent) {
            log.log(Level.INFO, "beforeNodeTriggered: "+processNodeTriggeredEvent.getNodeInstance().getNodeName());

            NodeInstance nodeInstance = processNodeTriggeredEvent.getNodeInstance();
        }

        @Override
        public void afterNodeTriggered(ProcessNodeTriggeredEvent processNodeTriggeredEvent) {

        }

        @Override
        public void beforeNodeLeft(ProcessNodeLeftEvent processNodeLeftEvent) {

        }

        @Override
        public void afterNodeLeft(ProcessNodeLeftEvent processNodeLeftEvent) {

        }

        @Override
        public void beforeVariableChanged(ProcessVariableChangedEvent processVariableChangedEvent) {

        }

        @Override
        public void afterVariableChanged(ProcessVariableChangedEvent processVariableChangedEvent) {

        }

        @Override
        public void taskCreated(TaskUserEvent taskUserEvent) {
            log.log(Level.INFO, "Task created: "+taskUserEvent.getTaskId());
        }

        @Override
        public void taskClaimed(TaskUserEvent taskUserEvent) {
            log.log(Level.INFO, "Task created: "+taskUserEvent.getTaskId());
        }

        @Override
        public void taskStarted(TaskUserEvent taskUserEvent) {
            log.log(Level.INFO, "Task started: "+taskUserEvent.getTaskId());
        }

        @Override
        public void taskStopped(TaskUserEvent taskUserEvent) {
            log.log(Level.INFO, "Task user event: "+taskUserEvent.getTaskId());
        }

        @Override
        public void taskReleased(TaskUserEvent taskUserEvent) {
            log.log(Level.INFO, "Task released: "+taskUserEvent.getTaskId());
        }

        @Override
        public void taskCompleted(TaskUserEvent taskUserEvent) {
            log.log(Level.INFO, "Task completed: "+taskUserEvent.getTaskId());
        }

        @Override
        public void taskFailed(TaskUserEvent taskUserEvent) {
            log.log(Level.INFO, "Task failed: "+taskUserEvent.getTaskId());
        }

        @Override
        public void taskSkipped(TaskUserEvent taskUserEvent) {
            log.log(Level.INFO, "Task skipped: "+taskUserEvent.getTaskId());
        }

        @Override
        public void taskForwarded(TaskUserEvent taskUserEvent) {
            log.log(Level.INFO, "Task forwarded: "+taskUserEvent.getTaskId());
        }
    }


}
