package main;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import persistence.AttemptedLoginManager;
import portals.PortalManager;

/**
 * Specialized ServletContextListener that creates an AttemptedLoginManager for
 * global-use and loads/initilizes the PortalManager upon startup.
 * 
 * @author jaywaldron
 * 
 */
public class BackendInstance implements ServletContextListener {

	private static AttemptedLoginManager ATTEMPTED_LOGIN_MANAGER = null;

	public static AttemptedLoginManager getAttemptedLoginManager() {
		if (ATTEMPTED_LOGIN_MANAGER == null) {
			init();
		}
		return ATTEMPTED_LOGIN_MANAGER;
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		init();
	}

	private static void init() {
		ATTEMPTED_LOGIN_MANAGER = new AttemptedLoginManager();
		PortalManager.getInstance().loadPortalsFromConfigFile();
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {}

}
