package server_mgmt;

import java.util.ArrayList;
import java.util.List;

public class RunningPortals {

	private List<String> portals;

	public RunningPortals(List<String> portals) {
		this.portals = portals;
	}

	public RunningPortals(){}

	public List<String> getPortals() {
		return portals;
	}

	public void addAllPortals(List<String> portals) {
		for (String portalCode : portals) {
			addPortal(portalCode);
		}
	}

	public void addPortal(String p) {
		if (portals == null) {
			portals = new ArrayList<String>();
		}
		portals.add(p);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("portals : ");
		sb.append(portals);
		return sb.toString();
	}


}
