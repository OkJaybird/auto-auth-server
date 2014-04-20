/*****************************************************************
	Jay Waldron
	jaywaldron@gmail.com
	Apr 10, 2014
 *****************************************************************/

package persistence;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import org.junit.Assert;
import org.junit.Test;

public class PasswordHashTest {

	@Test
	public void testPasswordHash() throws NoSuchAlgorithmException, InvalidKeySpecException {
		String hash1 = PasswordHash.createHash("password");
		String hash2 = PasswordHash.createHash("password");
		Assert.assertNotEquals(hash1, hash2);
		Assert.assertTrue(PasswordHash.validatePassword("password", hash1));
		Assert.assertTrue(PasswordHash.validatePassword("password", hash2));
		Assert.assertFalse(PasswordHash.validatePassword("password2", hash1));
	}

}
