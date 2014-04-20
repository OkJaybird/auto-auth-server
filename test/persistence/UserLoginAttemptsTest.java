/*****************************************************************
	Jay Waldron
	jaywaldron@gmail.com
	Apr 10, 2014
 *****************************************************************/

package persistence;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import org.joda.time.Minutes;
import org.junit.Assert;
import org.junit.Test;

import portals.PortalManager;

public class UserLoginAttemptsTest {

	@Test
	public void testUserLoginAttempts() throws NoSuchAlgorithmException, InvalidKeySpecException {
		// setup
		UserLoginAttempts attempts = new UserLoginAttempts("wustl");
		PortalManager.getInstance().loadPortalsFromConfigFile();
		attempts = new UserLoginAttempts("wustl");

		// null values
		Assert.assertFalse(attempts.isValidPassword("password"));
		Assert.assertFalse(attempts.isPreviouslySeenInvalidPassword("password"));

		// failure values
		String pHash = PasswordHash.createHash("p");
		attempts.saveFailureAttempt(pHash);
		Assert.assertTrue(attempts.isPreviouslySeenInvalidPassword("p"));
		Assert.assertFalse(attempts.isPreviouslySeenInvalidPassword("pass"));

		// success
		String passwordHash = PasswordHash.createHash("password");
		attempts.setSuccessfulAttempt(passwordHash);
		attempts.updateSuccessTime();
		Assert.assertTrue(attempts.isValidPassword("password"));

		// multiple failures & lockout scenario
		String p1Hash = PasswordHash.createHash("p1"); attempts.saveFailureAttempt(p1Hash);
		String p2Hash = PasswordHash.createHash("p2"); attempts.saveFailureAttempt(p2Hash);
		String p3Hash = PasswordHash.createHash("p3"); attempts.saveFailureAttempt(p3Hash);
		String p4Hash = PasswordHash.createHash("p4"); attempts.saveFailureAttempt(p4Hash);
		String p5Hash = PasswordHash.createHash("p5"); attempts.saveFailureAttempt(p5Hash);
		Assert.assertTrue(attempts.isPreviouslySeenInvalidPassword("pass"));
	}

	@Test
	public void testUserLoginAttemptsPrune() throws NoSuchAlgorithmException, InvalidKeySpecException, InterruptedException {
		PortalManager.getInstance().loadPortalsFromConfigFile();
		UserLoginAttempts attempts = new UserLoginAttempts("wustl");
		Assert.assertTrue(attempts.prune(Minutes.ZERO));

		String passwordHash = PasswordHash.createHash("password");
		attempts.setSuccessfulAttempt(passwordHash);
		String p1Hash = PasswordHash.createHash("p1");
		attempts.saveFailureAttempt(p1Hash);
		Thread.sleep(100);
		Assert.assertTrue(attempts.prune(Minutes.ZERO));
	}

}
