/*****************************************************************
	Jay Waldron
	jaywaldron@gmail.com
	Apr 13, 2014
 *****************************************************************/

package util;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import util.Util.Priority;

public class UtilTest {

	@Test
	public void testNotifyAdmin() {
		Util.notifyAdmin("subject", new Exception(), Priority.LOW);
	}

	@Test
	public void testToByteArray() throws IOException {
		byte[] bytes = Util.toByteArray(this.getClass().getResourceAsStream("/portals.conf"));
		Assert.assertNotNull(bytes);
		bytes = Util.toByteArray(null);
		Assert.assertTrue(bytes.length==0);
	}

}
