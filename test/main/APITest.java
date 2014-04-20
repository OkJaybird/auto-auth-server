/*****************************************************************
	Jay Waldron
	jaywaldron@gmail.com
	Apr 13, 2014
 *****************************************************************/

package main;

import org.junit.Assert;
import org.junit.Test;

import portals.PortalConnector;
import portals.PortalManager;
import server_mgmt.AuthResponseReason;

public class APITest {

	@Test
	public void testHandleLoginRequest() throws InterruptedException {
		API api = new API();
		Assert.assertTrue(api.getRunningPortals().toString().contains("null"));
		Assert.assertTrue(api.handleLoginRequest("user","pass","badportal").toString().contains(AuthResponseReason.INCORRECT_PORTAL));
		Assert.assertTrue(api.handleLoginRequest(null,null,null).toString().contains(AuthResponseReason.ILL_FORMED));

		BackendInstance.getAttemptedLoginManager();
		Thread.sleep(15000);

		String resp = api.handleLoginRequest("user","pass","wustl").toString();
		Assert.assertTrue(api.getRunningPortals().toString().contains("wustl"));
		Assert.assertTrue(resp.contains(AuthResponseReason.SUCCESSFUL) || resp.contains(AuthResponseReason.FAIL));
		String lastResp = resp;
		resp = api.handleLoginRequest("user","pass","wustl").toString();
		Assert.assertTrue(resp.contains(lastResp));

		PortalConnector conn = new PortalConnector("user2", "pass2", PortalManager.getInstance().getPortalByName("wustl"));
		BackendInstance.getAttemptedLoginManager().storeLoginAttempt(conn, true);
		resp = api.handleLoginRequest("user2","pass2","wustl").toString();
		Assert.assertTrue(resp.contains(AuthResponseReason.SUCCESSFUL));
	}

}
