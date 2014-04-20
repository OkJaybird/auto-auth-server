package main;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import persistence.AttemptedLoginManager.LoginAdvisorCodes;
import portals.PortalConnector;
import portals.PortalManager;
import server_mgmt.AuthRequest;
import server_mgmt.AuthResponse;
import server_mgmt.AuthResponseReason;
import server_mgmt.RunningPortals;

/**
 * The Server's front-end. Has two connection points: /api and /portals
 * 
 * @author jaywaldron
 * 
 */
@Path("/")
public class API {

	/**
	 * Handles an AuthRequest, attempts to login if necessary, and responds to
	 * the client who made the request.
	 * 
	 * @param username
	 *            the username to login with at the portal
	 * @param password
	 *            the password to login with at the portal
	 * @param portalCode
	 *            the portal code defined in portals.conf which specifies where
	 *            to attempt login
	 * @return AuthResponse-formatted response which notifies client of
	 *         successful login or not
	 */
	@POST
	@Path("/login")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public AuthResponse handleLoginRequest(@FormParam("username") String username, @FormParam("password") String password, @FormParam("portal_code") String portalCode) {
		AuthRequest request = new AuthRequest(username, password, portalCode);

		// catch ill-formed requests
		if (request==null || !request.hasValidFields()) {
			return new AuthResponse("unknown", "unknown", false, AuthResponseReason.ILL_FORMED);
		}

		// catch invalid portals
		if (!PortalManager.getInstance().portalIsActive(request.getPortalCode())) {
			return new AuthResponse(request.getUsername(), request.getPortalCode(), false, AuthResponseReason.INCORRECT_PORTAL);
		}

		// process a valid request
		boolean allow = false;
		PortalConnector portalConnector = new PortalConnector(request.getUsername(), request.getPassword(), PortalManager.getInstance().getPortalByName(request.getPortalCode().toLowerCase()));
		LoginAdvisorCodes loginCode = BackendInstance.getAttemptedLoginManager().adviseLogin(portalConnector);
		if (LoginAdvisorCodes.SKIP_LOGIN_AUTH_SUCCESS == loginCode) {
			allow = true;
		} else if (LoginAdvisorCodes.SKIP_LOGIN_AUTH_FAILURE == loginCode) {
			allow = false;
		} else { // attempt login
			allow = portalConnector.authenticate();
			BackendInstance.getAttemptedLoginManager().storeLoginAttempt(portalConnector, allow);
		}
		return new AuthResponse(request.getUsername(), request.getPortalCode(), allow, allow ? AuthResponseReason.SUCCESSFUL : AuthResponseReason.FAIL);
	}

	/**
	 * Gets a list of currently active portals. If a portal is still working on
	 * being initialized, it will not be shown until its ready.
	 * 
	 * @return JSON-formatted list of portals
	 */
	@GET
	@Path("/portals")
	@Produces(MediaType.APPLICATION_JSON)
	public RunningPortals getRunningPortals() {
		RunningPortals portals = new RunningPortals();
		portals.addAllPortals(PortalManager.getInstance().getActivePortals());
		return portals;
	}

}
