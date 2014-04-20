/*****************************************************************
	Jay Waldron
	jaywaldron@gmail.com
	Apr 13, 2014
 *****************************************************************/

package portals;

import net.minidev.json.JSONObject;

import org.junit.Assert;
import org.junit.Test;

public class PortalConfigHandlerTest {

	@Test
	public void test() {
		JSONObject json = new JSONObject();
		PortalConfigHandler conf = new PortalConfigHandler(json);
		Assert.assertFalse(conf.isValidPortalDefinition());
		Assert.assertNotNull(conf.toString());

		json.put("portal_code", "INVALID3");
		json.put("first_click_xpath", "firstClick");
		conf = new PortalConfigHandler(json);
		Assert.assertFalse(conf.isValidPortalDefinition());

		json = new JSONObject();
		json.put("portal_code", "");
		conf = new PortalConfigHandler(json);
		Assert.assertFalse(conf.isValidPortalDefinition());

	}

}
