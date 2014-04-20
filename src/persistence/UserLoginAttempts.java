/*****************************************************************
	Jay Waldron
	jaywaldron@gmail.com
	Mar 2, 2014
 *****************************************************************/

package persistence;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.CopyOnWriteArrayList;

import org.joda.time.DateTime;
import org.joda.time.Minutes;

import portals.Portal;
import portals.PortalManager;

public class UserLoginAttempts {

	private final String PORTAL_CODE;
	private final int MAX_FAILURES;

	private LoginAttemptRecord successfulAttempt;
	private CopyOnWriteArrayList<LoginAttemptRecord> failureAttempts;

	public UserLoginAttempts(String portalCode) {
		PORTAL_CODE = portalCode;
		failureAttempts = new CopyOnWriteArrayList<>();
		Portal portal = PortalManager.getInstance().getPortalByName(PORTAL_CODE);
		if (portal == null) {
			MAX_FAILURES = 5;
		} else {
			MAX_FAILURES = portal.getMAX_ATTEMPTS_BEFORE_LOCKOUT()-1;
		}
	}

	protected boolean isValidPassword(String plainPassword) {
		return getSuccessfulAttempt() != null && passwordMatches(plainPassword, successfulAttempt.getHASHED_PASSWORD());
	}

	protected void updateSuccessTime() {
		getSuccessfulAttempt().updateLoginAttemptTime();
	}

	private boolean passwordMatches(String plainPass, String hashedPass) {
		try {
			return PasswordHash.validatePassword(plainPass, hashedPass);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
			return false;
		}
	}

	protected boolean isPreviouslySeenInvalidPassword(String plainPass) {
		if (failureAttempts.size() >= MAX_FAILURES) {
			return true;
		}
		for (LoginAttemptRecord record : failureAttempts) {
			if (passwordMatches(plainPass, record.getHASHED_PASSWORD())) {
				return true;
			}
		}
		return false;
	}

	protected synchronized void setSuccessfulAttempt(String hashedPass) {
		successfulAttempt = new LoginAttemptRecord(hashedPass);
	}

	protected synchronized LoginAttemptRecord getSuccessfulAttempt() {
		return successfulAttempt;
	}

	protected void saveFailureAttempt(String hashedPass) {
		failureAttempts.add(new LoginAttemptRecord(hashedPass));
	}

	protected synchronized boolean prune(Minutes minutesThresh) {
		if (successfulAttempt != null && successfulAttempt.getLoginAttemptTime().plus(minutesThresh).isBefore(DateTime.now())) {
			successfulAttempt = null;
		}
		Deque<Integer> removeIndexes = new ArrayDeque<Integer>();
		for (int i=0; i<failureAttempts.size(); i++) {
			if (failureAttempts.get(i).getLoginAttemptTime().plus(minutesThresh).isBefore(DateTime.now())) {
				removeIndexes.push(i);
			}
		}
		int i;
		while (!removeIndexes.isEmpty()) {
			i = removeIndexes.pop();
			failureAttempts.remove(i);
		}
		return successfulAttempt==null && failureAttempts.isEmpty();
	}

}
