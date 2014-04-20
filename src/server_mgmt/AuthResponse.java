package server_mgmt;

public class AuthResponse {

	private String username;
	private String portal_code;
	private boolean success;
	private String reason;

	public AuthResponse(String username, String portalCode, boolean success, String reason) {
		this.username = username;
		this.portal_code = portalCode;
		this.success = success;
		this.reason = reason;
	}

	public AuthResponse() {}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPortal_code() {
		return portal_code;
	}

	public void setPortalCode(String portalCode) {
		this.portal_code = portalCode;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(" username : ");sb.append(username);
		sb.append(" portal_code : ");sb.append(portal_code);
		sb.append(" success : ");sb.append(success);
		sb.append(" reason : ");sb.append(reason);
		return sb.toString();
	}

}
