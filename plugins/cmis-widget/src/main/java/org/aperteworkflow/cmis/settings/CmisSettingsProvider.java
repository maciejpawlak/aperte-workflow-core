package org.aperteworkflow.cmis.settings;

import pl.net.bluesoft.rnd.processtool.ISettingsProvider;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;

/**
 * Provider class for cmis settings
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class CmisSettingsProvider 
{
	/** Get url of the repository service */
	public static String getAtomRepostioryUrl(ISettingsProvider settingsProvider)
	{

		
		return settingsProvider.getSetting(CmisWidgetSettings.ATOM_REPOSITORY_URL);
	}
	
	/** Get username for atom repository serivce */
	public static String getAtomRepostioryUsername(ISettingsProvider settingsProvider)
	{

		
		return settingsProvider.getSetting(CmisWidgetSettings.ATOM_REPOSITORY_USERNAME);
	}
	
	/** Get password for atom repository serivce */
	public static String getAtomRepostioryPassword(ISettingsProvider settingsProvider)
	{

		
		return settingsProvider.getSetting(CmisWidgetSettings.ATOM_REPOSITORY_PASSWORD);
	}
	
	/** Get main folder name for atom repository serivce */
	public static String getAtomRepostioryMainFolderName(ISettingsProvider settingsProvider)
	{

		
		return settingsProvider.getSetting(CmisWidgetSettings.ATOM_REPOSTIORY_MAINFOLDER);
	}

}
