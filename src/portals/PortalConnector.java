/*****************************************************************
	Jay Waldron
	jaywaldron@gmail.com
	Mar 2, 2014
 *****************************************************************/

package portals;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import org.apache.http.auth.UsernamePasswordCredentials;

import persistence.PasswordHash;

import com.gargoylesoftware.htmlunit.WebClient;

public class PortalConnector {

	private final UsernamePasswordCredentials CREDENTIALS;
	private final Portal PORTAL;
	private String hashedPass = "";

	public PortalConnector(String user, String pass, Portal portal) {
		CREDENTIALS = new UsernamePasswordCredentials(user, pass);
		PORTAL = portal;
	}

	public boolean authenticate() {
		if (PORTAL.needsCacheUpdate()) {
			PORTAL.genCacheFiles();
		}
		WebClient webClient = PORTAL.createConfiguredWebClient();
		return PORTAL.authenticate(getCREDENTIALS(), webClient);
	}

	public UsernamePasswordCredentials getCREDENTIALS() {
		return CREDENTIALS;
	}

	public String getPORTAL_CODE() {
		return PORTAL.getPORTAL_CODE();
	}

	public String getHashedPass() {
		if (hashedPass.equals("")) {
			try {
				hashedPass = PasswordHash.createHash(CREDENTIALS.getPassword());
			} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
				e.printStackTrace();
			}
		}
		return hashedPass;
	}


}
