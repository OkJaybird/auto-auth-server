/*****************************************************************
	Jay Waldron
	jaywaldron@gmail.com
	Mar 2, 2014
 *****************************************************************/

package portals;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import util.Util;

public class PortalManager {

	private final ConcurrentHashMap<String, Portal> PORTALS_MAP = new ConcurrentHashMap<>();

	private static PortalManager portalManager;
	private PortalManager() {}
	public static PortalManager getInstance() {
		if (portalManager == null) {
			portalManager = new PortalManager();
		}
		return portalManager;
	}

	public boolean loadPortalsFromConfigFile() {
		try {
			byte[] encoded = Util.toByteArray(this.getClass().getResourceAsStream("/portals.conf"));
			String jsonString = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
			JSONObject jsonObject = (JSONObject) JSONValue.parse(jsonString);
			JSONArray portalsArray = (JSONArray) jsonObject.get("portals");

			for (Object portalO : portalsArray) {
				JSONObject portal = (JSONObject) portalO;
				PortalConfigHandler portalConf = new PortalConfigHandler(portal);
				if (portalConf.isValidPortalDefinition()) {
					addPortal(portalConf.createPortal());
				} else {
					System.out.println("Portal definition is ill-formed. Cannot create Portal.");
				}
			}
		} catch (IOException e) {
			System.out.println("Unable to parse json file portals.conf.");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private boolean addPortal(Portal portal) {
		Portal existingMapping = PORTALS_MAP.putIfAbsent(portal.getPORTAL_CODE(), portal);
		if (existingMapping==null) {
			return true;
		}
		return false;
	}

	public boolean portalIsActive(String portalCode) {
		return PORTALS_MAP.containsKey(portalCode) && PORTALS_MAP.get(portalCode).isActive();
	}

	public Portal getPortalByName(String portalCode) {
		return PORTALS_MAP.get(portalCode);
	}

	public List<String> getActivePortals() {
		List<String> activePortalCodes = new LinkedList<>();
		for (Portal p: PORTALS_MAP.values()) {
			if (p.isActive()) {
				activePortalCodes.add(p.getPORTAL_CODE());
			}
		}
		return activePortalCodes;
	}

}
