/*****************************************************************
	Jay Waldron
	jaywaldron@gmail.com
	Apr 10, 2014
 *****************************************************************/

package server_mgmt;

import org.junit.Assert;
import org.junit.Test;

public class AuthResponseTest {

	@Test
	public void testAuthResponse() {
		AuthResponse resp = new AuthResponse();
		Assert.assertNotNull(resp);
		resp = new AuthResponse("user","wustl",false,AuthResponseReason.FAIL);

		Assert.assertEquals("user", resp.getUsername());
		Assert.assertEquals("wustl", resp.getPortal_code());
		Assert.assertFalse(resp.isSuccess());
		Assert.assertEquals(AuthResponseReason.FAIL, resp.getReason());

		resp.setUsername("u");
		resp.setPortalCode("wu");
		resp.setSuccess(true);
		resp.setReason(AuthResponseReason.SUCCESSFUL);
		Assert.assertEquals("u", resp.getUsername());
		Assert.assertEquals("wu", resp.getPortal_code());
		Assert.assertTrue(resp.isSuccess());
		Assert.assertEquals(AuthResponseReason.SUCCESSFUL, resp.getReason());

		Assert.assertNotNull(resp.toString());
	}

}
