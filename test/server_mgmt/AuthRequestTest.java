/*****************************************************************
	Jay Waldron
	jaywaldron@gmail.com
	Apr 10, 2014
 *****************************************************************/

package server_mgmt;

import org.junit.Assert;
import org.junit.Test;

public class AuthRequestTest {

	@Test
	public void testAuthRequest() {
		AuthRequest req = new AuthRequest();
		Assert.assertNotNull(req);
		req = new AuthRequest("user","pass","WUSTL");
		Assert.assertNotNull(req);
		Assert.assertEquals("user", req.getUsername());
		Assert.assertEquals("pass", req.getPassword());
		Assert.assertEquals("wustl", req.getPortalCode());
	}

	@Test
	public void testAuthRequestSetVals() {
		AuthRequest req = new AuthRequest();
		req.setUsername("u");
		req.setPassword("p");
		req.setPortalCode("WU");
		Assert.assertEquals("u", req.getUsername());
		Assert.assertEquals("p", req.getPassword());
		Assert.assertEquals("wu", req.getPortalCode());

		req = new AuthRequest("user","pass",null);
		Assert.assertNull(req.getPortalCode());
		Assert.assertNotNull(req.toString());

		req.setPortalCode(null);
		Assert.assertNull(req.getPortalCode());
	}

	@Test
	public void testAuthRequestValidationChecks() {
		AuthRequest req = new AuthRequest();
		Assert.assertFalse(req.hasValidFields());
		req.setUsername("");
		req.setPassword("");
		Assert.assertFalse(req.hasValidFields());
		req.setPortalCode("");
		Assert.assertFalse(req.hasValidFields());

		req = new AuthRequest("user","pass","WUSTL");
		Assert.assertTrue(req.hasValidFields());
	}

}
