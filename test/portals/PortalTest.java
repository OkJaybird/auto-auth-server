/*****************************************************************
	Jay Waldron
	jaywaldron@gmail.com
	Apr 10, 2014
 *****************************************************************/

package portals;

import org.joda.time.Minutes;
import org.junit.Assert;
import org.junit.Test;

public class PortalTest {

	@Test
	public void testPortal() throws InterruptedException {
		String portalCode = "twitter";
		String url = "https://twitter.com";
		String user = "//*[@id=\"signin-email\"]";
		String pass = "//*[@id=\"signin-password\"]";
		String button = "//*[@id=\"front-container\"]/div[2]/div[2]/form/table/tbody/tr/td[2]/button";
		int maxAttempts = 100;
		int resetMinutes = 60;
		String firstClick = "";
		boolean specializedModel = false;
		Portal portal = new Portal(portalCode, maxAttempts, resetMinutes, url, user, pass, button, firstClick, specializedModel);

		Thread.sleep(15000);
		Assert.assertTrue(portal.isActive());
	}

	@Test
	public void testGoodPortal() throws InterruptedException {
		String portalCode = "twitter";
		String url = "https://twitter.com";
		String user = "//*[@id=\"signin-email\"]";
		String pass = "//*[@id=\"signin-password\"]";
		String button = "//*[@id=\"front-container\"]/div[2]/div[2]/form/table/tbody/tr/td[2]/button";
		int maxAttempts = 100;
		int resetMinutes = 60;
		String firstClick = "//*[@id=\"doc\"]/div[1]/div[2]/div/div/ul/li/a/span";
		boolean specializedModel = false;

		Portal portal = new Portal(portalCode, maxAttempts, resetMinutes, url, user, pass, button, firstClick, specializedModel);
		Portal portalSame = new Portal(portalCode, maxAttempts, resetMinutes, "invalid", user, pass, button, firstClick, specializedModel);
		Portal portalDiff = new Portal("diff", maxAttempts, resetMinutes, "invalid", user, pass, button, firstClick, specializedModel);

		Thread.sleep(12000);
		Assert.assertTrue(portal.isActive());
		Assert.assertEquals(Minutes.minutes(60), portal.getATTEMPTS_RESET_TIME());
		Assert.assertEquals(url, portal.getAUTH_URL());
		Assert.assertEquals(user, portal.getUSER_FIELD_XPATH());
		Assert.assertEquals(pass, portal.getPASS_FIELD_XPATH());
		Assert.assertEquals(button, portal.getLOGIN_BUTTON_XPATH());

		Assert.assertTrue(portal.equals(portalSame));
		Assert.assertFalse(portal.equals(portalDiff));
	}

	@Test
	public void testBadPortal1() throws InterruptedException {
		String portalCode = "twitter";
		String url = "https://twitter.com";
		String user = "//*[@id=\"signin-email\"]";
		String pass = "//*[@id=\"SIGNIN-password\"]";
		String button = "//*[@id=\"front-container\"]/div[2]/div[2]/form/table/tbody/tr/td[2]/button";
		int maxAttempts = 100;
		int resetMinutes = 60;
		String firstClick = "";
		boolean specializedModel = false;
		Portal portal = new Portal(portalCode, maxAttempts, resetMinutes, url, user, pass, button, firstClick, specializedModel);

		Thread.sleep(10000);
		Assert.assertFalse(portal.isActive());
	}

	@Test
	public void testBadPortal2() throws InterruptedException {
		String portalCode = "twitter";
		String url = "https://twitter.com";
		String user = "//*[@id=\"signin-email\"]";
		String pass = "//*[@id=\"signin-password\"]";
		String button = "//*[@id=\"front-container\"]/div[2]/div[2]/form/table/tbody/tr/td[2]/button";
		int maxAttempts = 100;
		int resetMinutes = 60;
		String firstClick = "invalidFirstClick";
		boolean specializedModel = false;
		Portal portal = new Portal(portalCode, maxAttempts, resetMinutes, url, user, pass, button, firstClick, specializedModel);

		Thread.sleep(10000);
		Assert.assertFalse(portal.isActive());
	}

	//	@Test
	//	public void testAuthenticate() {
	//		fail("Not yet implemented");
	//	}
	//
	//	@Test
	//	public void testGenCacheFiles() {
	//		fail("Not yet implemented");
	//	}
	//
	//	@Test
	//	public void testNeedsCacheUpdate() {
	//		fail("Not yet implemented");
	//	}
	//
	//	@Test
	//	public void testGetLastCacheUpdateTime() {
	//		fail("Not yet implemented");
	//	}
	//
	//	@Test
	//	public void testGetCookieManager() {
	//		fail("Not yet implemented");
	//	}
	//
	//	@Test
	//	public void testGetCache() {
	//		fail("Not yet implemented");
	//	}
	//
	//	@Test
	//	public void testCreateConfiguredWebClient() {
	//		fail("Not yet implemented");
	//	}
	//
	//	@Test
	//	public void testEqualsObject() {
	//		fail("Not yet implemented");
	//	}
	//
	//	@Test
	//	public void testGetPORTAL_CODE() {
	//		fail("Not yet implemented");
	//	}
	//
	//	@Test
	//	public void testGetMAX_ATTEMPTS_BEFORE_LOCKOUT() {
	//		fail("Not yet implemented");
	//	}
	//
	//	@Test
	//	public void testGetATTEMPTS_RESET_TIME() {
	//		fail("Not yet implemented");
	//	}
	//
	//	@Test
	//	public void testGetAUTH_URL() {
	//		fail("Not yet implemented");
	//	}
	//
	//	@Test
	//	public void testGetUSER_FIELD_XPATH() {
	//		fail("Not yet implemented");
	//	}
	//
	//	@Test
	//	public void testGetPASS_FIELD_XPATH() {
	//		fail("Not yet implemented");
	//	}
	//
	//	@Test
	//	public void testGetLOGIN_BUTTON_XPATH() {
	//		fail("Not yet implemented");
	//	}
	//
	//	@Test
	//	public void testIsActive() {
	//		fail("Not yet implemented");
	//	}

}
