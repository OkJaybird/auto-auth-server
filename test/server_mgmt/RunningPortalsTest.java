/*****************************************************************
	Jay Waldron
	jaywaldron@gmail.com
	Apr 10, 2014
 *****************************************************************/

package server_mgmt;

import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class RunningPortalsTest {

	@Test
	public void testRunningPortals() {
		RunningPortals portals = new RunningPortals();
		Assert.assertNotNull(portals);
		List<String> portalCodes = new LinkedList<>();
		portalCodes.add("wustl");
		portalCodes.add("wu");
		portals = new RunningPortals(portalCodes);
		Assert.assertNotNull(portals);
		Assert.assertEquals(portalCodes, portals.getPortals());
		Assert.assertNotNull(portals.toString());
	}

	@Test
	public void testRunningPortalsLiveAdding() {
		List<String> portalCodes = new LinkedList<>();
		portalCodes.add("wustl");
		portalCodes.add("wu");

		RunningPortals portals = new RunningPortals();
		portals.addAllPortals(portalCodes);
		Assert.assertNotNull(portals.getPortals());
	}

}
