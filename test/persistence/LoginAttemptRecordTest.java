/*****************************************************************
	Jay Waldron
	jaywaldron@gmail.com
	Apr 10, 2014
 *****************************************************************/

package persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import org.joda.time.DateTime;
import org.junit.Test;

public class LoginAttemptRecordTest {

	@Test
	public void testLoginAttemptRecord() throws InterruptedException {
		LoginAttemptRecord lar = new LoginAttemptRecord("test");
		DateTime dt = lar.getLoginAttemptTime();
		assertNotNull(dt);
		assertEquals("test", lar.getHASHED_PASSWORD());
		Thread.sleep(250);
		lar.updateLoginAttemptTime();
		assertNotEquals(dt, lar.getLoginAttemptTime());
	}

}
