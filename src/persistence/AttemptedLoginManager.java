/*****************************************************************
	Jay Waldron
	jaywaldron@gmail.com
	Mar 2, 2014
 *****************************************************************/

package persistence;

import java.util.concurrent.ConcurrentHashMap;

import org.joda.time.Minutes;

import portals.PortalConnector;

/**
 * Handles login attempts for all global requests. Stores old successful &
 * unsuccessful attempts for 5 minutes before purging (successful attempts will
 * be held 5 minutes from their last access), and will advise login according to
 * currently held attempts.
 * 
 * @author jaywaldron
 * 
 */
public class AttemptedLoginManager {

	public static enum LoginAdvisorCodes {
		SKIP_LOGIN_AUTH_SUCCESS, SKIP_LOGIN_AUTH_FAILURE, ATTEMPT_LOGIN
	}

	private static final int PRUNING_TIMEOUT = 60000;
	private static final Minutes HOLD_TIME = Minutes.minutes(5);
	private ConcurrentHashMap<String,UserLoginAttempts> loginAttemptsMap = new ConcurrentHashMap<>();

	/**
	 * Creates a new AttemptedLoginManager and starts its pruning process with
	 * default values: run every minute & remove entries older than 5 minutes.
	 */
	public AttemptedLoginManager() {
		pruneAttemptedLoginRecords(PRUNING_TIMEOUT, HOLD_TIME);
	}

	/**
	 * Creates a new AttemptedLoginManager and starts its pruning process with
	 * specified values. Used only for testing.
	 * 
	 * @param pruningTimeout
	 *            milliseconds to run pruning process after
	 * @param holdMinutes
	 *            minutes to hold onto old records
	 */
	protected AttemptedLoginManager(int pruningTimeout, int holdMinutes) {
		pruneAttemptedLoginRecords(pruningTimeout, Minutes.minutes(holdMinutes));
	}

	/**
	 * Starts an ongoing thread that prunes the records for ones that are old
	 * enough to discard.
	 * 
	 * @param pruningTimeoutMS
	 *            run the pruning thread every this many milliseconds
	 * @param holdTime
	 *            how many Minutes to hold on to each record for
	 */
	private void pruneAttemptedLoginRecords(final int pruningTimeoutMS, final Minutes holdTime) {
		new Thread(
				new Runnable() {
					@Override
					public void run() {
						while (true){
							for (String key : loginAttemptsMap.keySet()) {
								UserLoginAttempts userLoginAttempts = loginAttemptsMap.get(key);
								boolean del = userLoginAttempts.prune(holdTime);
								if (del) {
									loginAttemptsMap.remove(key);
								} else {
									loginAttemptsMap.put(key, userLoginAttempts);
								}
							}

							try {
								Thread.sleep(pruningTimeoutMS);
							} catch (InterruptedException e) {}
						}
					}
				}).start();
	}

	/**
	 * Advise whether or not to attempt logging in at a site or if the login
	 * information is already cached here
	 * 
	 * @param portalConnector
	 *            PortalConnector that wishes to connect if required
	 * @return one of the defined LoginAdvisorCodes which tells if a valid
	 *         record exists, invalid record exists, or there is no record for
	 *         this portal/user/password combination
	 */
	public LoginAdvisorCodes adviseLogin(PortalConnector portalConnector) {
		String key = getPortalConnectorKey(portalConnector);
		UserLoginAttempts userLoginAttempts = loginAttemptsMap.get(key);
		if (userLoginAttempts == null) {
			return LoginAdvisorCodes.ATTEMPT_LOGIN;
		}
		if (userLoginAttempts.isValidPassword(portalConnector.getCREDENTIALS().getPassword())) {
			userLoginAttempts.updateSuccessTime();
			loginAttemptsMap.put(key, userLoginAttempts);
			return LoginAdvisorCodes.SKIP_LOGIN_AUTH_SUCCESS;
		}
		if (userLoginAttempts.isPreviouslySeenInvalidPassword(portalConnector.getCREDENTIALS().getPassword())) {
			return LoginAdvisorCodes.SKIP_LOGIN_AUTH_FAILURE;
		}
		return LoginAdvisorCodes.ATTEMPT_LOGIN;
	}

	/**
	 * Store information from a PortalConnector here with whether or not it was
	 * successful for logging in
	 * 
	 * @param portalConnector
	 *            PortalConnector that has previously connected
	 * @param wasSuccessful
	 *            true if the authentication from the PortalConnector was
	 *            successful, false if not.
	 */
	public void storeLoginAttempt(PortalConnector portalConnector, boolean wasSuccessful) {
		String key = getPortalConnectorKey(portalConnector);
		UserLoginAttempts userLoginAttempts = loginAttemptsMap.get(key);
		if (userLoginAttempts == null) {
			userLoginAttempts = new UserLoginAttempts(portalConnector.getPORTAL_CODE());
		}
		if (wasSuccessful) {
			userLoginAttempts.setSuccessfulAttempt(portalConnector.getHashedPass());
		} else {
			userLoginAttempts.saveFailureAttempt(portalConnector.getHashedPass());
		}
		loginAttemptsMap.put(key, userLoginAttempts);
	}

	/**
	 * Create the key for interacting with the AttemptedLoginManager's storage
	 * mechanism from a PortalConnector's information.
	 * 
	 * @param portalConnector
	 *            PortalConnector with the required info
	 * @return key for AttemptedLoginManager access for this record
	 */
	private String getPortalConnectorKey(PortalConnector portalConnector) {
		return portalConnector.getCREDENTIALS().getUserName() + "@" + portalConnector.getPORTAL_CODE();
	}

}
