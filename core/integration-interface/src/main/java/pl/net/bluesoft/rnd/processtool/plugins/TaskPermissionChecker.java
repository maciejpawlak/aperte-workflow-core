package pl.net.bluesoft.rnd.processtool.plugins;

import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.UserData;

import java.util.Collection;

/**
 * Created by mpawluczuk on 2015-02-11.
 */
public interface TaskPermissionChecker {
	Boolean hasPermission(UserData user, Collection<String> userQueues, BpmTask task);
}
