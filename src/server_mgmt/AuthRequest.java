package server_mgmt;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthRequest {

	private String username;
	private String password;
	private String portal_code;

	public AuthRequest(String u, String p, String code) {
		username = u;
		password = p;
		if (code != null) {
			code = code.toLowerCase();
		}
		portal_code = code;
	}

	public AuthRequest() {}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPortalCode() {
		return portal_code;
	}

	public void setPortalCode(String portalCode) {
		if (portalCode != null) {
			portalCode = portalCode.toLowerCase();
		}
		portal_code = portalCode;
	}

	public boolean hasValidFields() {
		return !hasNullField() && !hasEmptyField();
	}

	private boolean hasNullField() {
		return username==null || password==null || portal_code==null;
	}

	private boolean hasEmptyField() {
		return username.trim().length()<=0 || password.trim().length()<=0 || portal_code.trim().length()<=0;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("username : "); sb.append(username);
		sb.append(" password : "); sb.append(password);
		sb.append(" portal_code : "); sb.append(portal_code);
		return sb.toString();
	}

}
