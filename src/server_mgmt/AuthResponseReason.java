package server_mgmt;

public class AuthResponseReason {

	public static final String SUCCESSFUL = "successful authentication";
	public static final String FAIL = "authentication failed";
	public static final String INCORRECT_PORTAL = "invalid portal code";
	public static final String ILL_FORMED = "ill-formed request";
	public static final String TESTING = "server locked for testing";
	public static final String LOCKOUT = "too many login attempts";
}
