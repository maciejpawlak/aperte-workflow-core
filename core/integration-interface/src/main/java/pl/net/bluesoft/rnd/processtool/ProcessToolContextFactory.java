package pl.net.bluesoft.rnd.processtool;

import org.hibernate.SessionFactory;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;

/**
 * @author tlipski@bluesoft.net.pl
 */
public interface ProcessToolContextFactory {
    <T> T withProcessToolContext(ReturningProcessToolContextCallback<T> callback);
    <T> T withProcessToolContext(ReturningProcessToolContextCallback<T> callback, ExecutionType type);
    <T> T withExistingOrNewContext(ReturningProcessToolContextCallback<T> callback);
	ProcessToolRegistry getRegistry();
	void updateSessionFactory(SessionFactory sf);
	
	public enum ExecutionType {
		NO_TRANSACTION,
		NO_TRANSACTION_SYNCH,
		TRANSACTION,
		TRANSACTION_SYNCH
	}
}
