/*****************************************************************
	Jay Waldron
	jaywaldron@gmail.com
	Apr 10, 2014
 *****************************************************************/

package portals;

import net.minidev.json.JSONObject;

public class PortalConfigHandler {

	private final JSONObject PORTAL;
	// required
	private String portalCode = "";
	private String url = "";
	private String user = "";
	private String pass = "";
	private String button = "";
	private int maxAttempts = 0;
	private int resetMinutes = 0;
	// optional
	private String firstClick = "";
	private boolean specializedModel = false;

	public PortalConfigHandler(JSONObject portal) {
		PORTAL = portal;

		parsePortalCode();
		parseURL();
		parseUserField();
		parsePassField();
		parseButtonField();
		parseMaxAttempts();
		parseResetMinutes();

		parseFirstClick();
		parseSpecializedModel();
	}

	public boolean isValidPortalDefinition() {
		return portalCode.length() > 0 &&
				url.length() > 0 &&
				user.length() > 0 &&
				pass.length() > 0 &&
				button.length() > 0 &&
				maxAttempts > 0 &&
				resetMinutes > 0;
	}

	public Portal createPortal() {
		return new Portal(portalCode, maxAttempts, resetMinutes, url, user, pass, button, firstClick, specializedModel);
	}

	private void parsePortalCode() {
		if (PORTAL.containsKey("portal_code")) {
			portalCode = (String) PORTAL.get("portal_code");
			if (!codeIsWellFormed(portalCode)) {
				portalCode = "";
			}
		}
	}

	private void parseURL() {
		if (PORTAL.containsKey("auth_url")) {
			url = (String) PORTAL.get("auth_url");
		}
	}

	private void parseUserField() {
		if (PORTAL.containsKey("user_field_xpath")) {
			user = (String) PORTAL.get("user_field_xpath");
		}
	}

	private void parsePassField() {
		if (PORTAL.containsKey("pass_field_xpath")) {
			pass = (String) PORTAL.get("pass_field_xpath");
		}
	}

	private void parseButtonField() {
		if (PORTAL.containsKey("submit_button_xpath")) {
			button = (String) PORTAL.get("submit_button_xpath");
		}
	}

	private void parseMaxAttempts() {
		if (PORTAL.containsKey("max_attempts")) {
			try {
				maxAttempts = Integer.parseInt((String) PORTAL.get("max_attempts"));
			} catch (Exception e) {};
		}
	}

	private void parseResetMinutes() {
		if (PORTAL.containsKey("attempts_reset_time")) {
			try {
				resetMinutes = Integer.parseInt((String) PORTAL.get("attempts_reset_time"));
			} catch (Exception e) {};
		}
	}

	private void parseFirstClick() {
		if (PORTAL.containsKey("first_click_xpath")) {
			firstClick = (String) PORTAL.get("first_click_xpath");
		}
	}

	private void parseSpecializedModel() {
		if (PORTAL.containsKey("specialized_model")) {
			specializedModel = ((String)PORTAL.get("specialized_model")).equals("true");
		}
	}

	/**
	 * Must be all alpha, no whitespace.
	 * @param portalCode
	 * @return
	 */
	public boolean codeIsWellFormed(String portalCode) {
		if (portalCode.isEmpty()) {
			return false;
		}
		for (char c : portalCode.toCharArray()) {
			if (!Character.isLetter(c)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(portalCode); sb.append(", ");
		sb.append(url); sb.append(", ");
		sb.append(user); sb.append(", ");
		sb.append(pass); sb.append(", ");
		sb.append(button); sb.append(", ");
		sb.append(maxAttempts); sb.append(", ");
		sb.append(resetMinutes); sb.append(", ");
		sb.append(firstClick); sb.append(", ");
		sb.append(specializedModel);
		return sb.toString();
	}

}
