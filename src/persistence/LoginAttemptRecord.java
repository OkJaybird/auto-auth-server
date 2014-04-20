/*****************************************************************
	Jay Waldron
	jaywaldron@gmail.com
	Mar 2, 2014
 *****************************************************************/

package persistence;

import org.joda.time.DateTime;

public class LoginAttemptRecord {

	private DateTime loginAttemptTime;
	private final String HASHED_PASSWORD;

	public LoginAttemptRecord(String hashedPassword) {
		loginAttemptTime = DateTime.now();
		HASHED_PASSWORD = hashedPassword;
	}

	protected synchronized void updateLoginAttemptTime() {
		loginAttemptTime = DateTime.now();
	}

	protected synchronized DateTime getLoginAttemptTime() {
		return loginAttemptTime;
	}

	protected String getHASHED_PASSWORD() {
		return HASHED_PASSWORD;
	}


}
