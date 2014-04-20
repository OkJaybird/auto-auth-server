/*****************************************************************
	Jay Waldron
	jaywaldron@gmail.com
	Apr 10, 2014
 *****************************************************************/

package persistence;

import org.junit.Assert;
import org.junit.Test;

import persistence.AttemptedLoginManager.LoginAdvisorCodes;
import portals.PortalConnector;
import portals.PortalManager;

public class AttemptedLoginManagerTest {

	@Test
	public void testAttemptedLoginManagerPruningRemoval() throws InterruptedException {
		AttemptedLoginManager manager = new AttemptedLoginManager(500, 0);

		PortalConnector connectorFailure = new PortalConnector("user","wrongpass",PortalManager.getInstance().getPortalByName("wustl"));
		manager.storeLoginAttempt(connectorFailure, false);
		Assert.assertEquals(LoginAdvisorCodes.SKIP_LOGIN_AUTH_FAILURE, manager.adviseLogin(connectorFailure));

		PortalConnector connectorUnsure = new PortalConnector("user","unsurepass",PortalManager.getInstance().getPortalByName("wustl"));
		Assert.assertEquals(LoginAdvisorCodes.ATTEMPT_LOGIN, manager.adviseLogin(connectorUnsure));

		PortalConnector connector = new PortalConnector("user","pass",PortalManager.getInstance().getPortalByName("wustl"));
		manager.storeLoginAttempt(connector, true);
		Assert.assertEquals(LoginAdvisorCodes.SKIP_LOGIN_AUTH_SUCCESS, manager.adviseLogin(connector));

		Thread.sleep(3000);
	}

	@Test
	public void testAttemptedLoginManagerPruningRemaining() throws InterruptedException {
		AttemptedLoginManager manager = new AttemptedLoginManager(500, 1);

		PortalConnector connectorFailure = new PortalConnector("user","wrongpass",PortalManager.getInstance().getPortalByName("wustl"));
		manager.storeLoginAttempt(connectorFailure, false);
		Assert.assertEquals(LoginAdvisorCodes.SKIP_LOGIN_AUTH_FAILURE, manager.adviseLogin(connectorFailure));

		PortalConnector connectorUnsure = new PortalConnector("user","unsurepass",PortalManager.getInstance().getPortalByName("wustl"));
		Assert.assertEquals(LoginAdvisorCodes.ATTEMPT_LOGIN, manager.adviseLogin(connectorUnsure));

		PortalConnector connector = new PortalConnector("user","pass",PortalManager.getInstance().getPortalByName("wustl"));
		manager.storeLoginAttempt(connector, true);
		Assert.assertEquals(LoginAdvisorCodes.SKIP_LOGIN_AUTH_SUCCESS, manager.adviseLogin(connector));

		Thread.sleep(3000);
	}

	@Test
	public void testAdviseLoginAndStoreLoginAttempt() throws InterruptedException {
		AttemptedLoginManager manager = new AttemptedLoginManager();
		PortalManager.getInstance().loadPortalsFromConfigFile();

		// null test
		PortalConnector connector = new PortalConnector("user","pass",PortalManager.getInstance().getPortalByName("wustl"));
		Assert.assertEquals(LoginAdvisorCodes.ATTEMPT_LOGIN, manager.adviseLogin(connector));

		// failure
		PortalConnector connectorFailure = new PortalConnector("user","wrongpass",PortalManager.getInstance().getPortalByName("wustl"));
		manager.storeLoginAttempt(connectorFailure, false);
		Assert.assertEquals(LoginAdvisorCodes.SKIP_LOGIN_AUTH_FAILURE, manager.adviseLogin(connectorFailure));

		// attempt login
		PortalConnector connectorUnsure = new PortalConnector("user","unsurepass",PortalManager.getInstance().getPortalByName("wustl"));
		Assert.assertEquals(LoginAdvisorCodes.ATTEMPT_LOGIN, manager.adviseLogin(connectorUnsure));

		// success
		manager.storeLoginAttempt(connector, true);
		Assert.assertEquals(LoginAdvisorCodes.SKIP_LOGIN_AUTH_SUCCESS, manager.adviseLogin(connector));
	}
}
