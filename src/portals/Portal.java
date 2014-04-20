/*****************************************************************
	Jay Waldron
	jaywaldron@gmail.com
	Mar 2, 2014
 *****************************************************************/

package portals;

import java.io.IOException;
import java.util.logging.Level;

import learning.FeatureVector;
import learning.KnownFailureVectorManager;
import learning.PortalResponseHandler;

import org.apache.http.auth.UsernamePasswordCredentials;
import org.joda.time.DateTime;
import org.joda.time.Minutes;

import util.Util;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.Cache;
import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.rits.cloning.Cloner;

public class Portal {

	private final String PORTAL_CODE;
	private final int MAX_ATTEMPTS_BEFORE_LOCKOUT;
	private final Minutes ATTEMPTS_RESET_TIME;
	private final String AUTH_URL;
	private final String FIRST_CLICK_XPATH;
	private final String USER_FIELD_XPATH;
	private final String PASS_FIELD_XPATH;
	private final String LOGIN_BUTTON_XPATH;
	private final boolean SPECIALIZED_MODEL;

	// caching
	private static boolean loggingDisabled = false;
	private static final int REFRESH_HOURS_THRESHOLD = 10;
	private CookieManager cookieManager;
	private Cache cache;
	private DateTime lastCacheUpdate;
	private boolean isActive = false;

	public Portal(String portalCode, int attemptsBeforeLockout, int minutesBeforeReset, String authURL, String userFieldXPath, String passFieldXPath, String loginButtonXPath, String firstClickXPath, boolean specializedModel) {
		PORTAL_CODE = portalCode;
		MAX_ATTEMPTS_BEFORE_LOCKOUT = attemptsBeforeLockout;
		ATTEMPTS_RESET_TIME = Minutes.minutes(minutesBeforeReset);
		AUTH_URL = authURL;
		FIRST_CLICK_XPATH = firstClickXPath;
		USER_FIELD_XPATH = userFieldXPath;
		PASS_FIELD_XPATH = passFieldXPath;
		LOGIN_BUTTON_XPATH = loginButtonXPath;
		SPECIALIZED_MODEL = specializedModel;
		genCacheFiles();
	}

	private boolean isValidResponse(HtmlPage responsePage, WebClient webclient) {
		return PortalResponseHandler.isValidResponse(PORTAL_CODE, responsePage, webclient, SPECIALIZED_MODEL);
	}

	public boolean authenticate(UsernamePasswordCredentials credentials, WebClient webclient) {
		HtmlPage responsePage = attemptLogin(credentials, webclient, false);
		if (responsePage == null) {
			return false;
		}
		return isValidResponse(responsePage, webclient);
	}

	private HtmlPage attemptLogin(UsernamePasswordCredentials credentials, WebClient webclient, boolean dummyLogin) {
		HtmlPage authPage = null;
		try {
			authPage = webclient.getPage(AUTH_URL);
			if (!FIRST_CLICK_XPATH.equals("")) {
				HtmlElement e = authPage.getFirstByXPath(FIRST_CLICK_XPATH);
				int attempts = 0;
				while (e==null) {
					if (attempts >= 5) {
						return null;
					}
					attempts++;
					webclient.waitForBackgroundJavaScript(1000);
					e = authPage.getFirstByXPath(FIRST_CLICK_XPATH);
				}
				authPage = e.click();
			}
			if (authPage==null) {
				return null;
			}
		} catch (Exception e) {
			Util.notifyAdmin("Error generating auth webpage for "+PORTAL_CODE, e, Util.Priority.MEDIUM);
		}

		HtmlInput userTextField = authPage.getFirstByXPath(USER_FIELD_XPATH);
		HtmlInput passTextField = authPage.getFirstByXPath(PASS_FIELD_XPATH);
		HtmlElement loginButton = authPage.getFirstByXPath(LOGIN_BUTTON_XPATH);
		int attempts = 0;
		while (userTextField==null || passTextField==null || loginButton==null) {
			if (attempts >= 5) {
				return null;
			}
			attempts++;
			webclient.waitForBackgroundJavaScript(1000);
			userTextField = authPage.getFirstByXPath(USER_FIELD_XPATH);
			passTextField = authPage.getFirstByXPath(PASS_FIELD_XPATH);
			loginButton = authPage.getFirstByXPath(LOGIN_BUTTON_XPATH);
		}
		userTextField.setValueAttribute(credentials.getUserName());
		passTextField.setValueAttribute(credentials.getPassword());
		HtmlPage responsePage = null;
		try {
			Page page = loginButton.click();
			webclient.waitForBackgroundJavaScript(3000);
			if (page instanceof HtmlPage) {
				responsePage = (HtmlPage)page;
			}
		} catch (IOException e) {
			Util.notifyAdmin("Error clicking button on auth webpage for: "+PORTAL_CODE, e, Util.Priority.HIGH);
		}

		if (dummyLogin && responsePage != null) {
			cookieManager = webclient.getCookieManager();
			cache = webclient.getCache();
			lastCacheUpdate = DateTime.now();
			FeatureVector vector = new FeatureVector(PORTAL_CODE, false, responsePage, webclient);
			vector.setClassification(false);
			KnownFailureVectorManager.getInstance().add(vector);
			System.out.println("saved dummy from "+PORTAL_CODE);
		}

		return responsePage;
	}

	protected synchronized void genCacheFiles() {
		new Thread() {
			@Override
			public void run() {
				HtmlPage response = attemptLogin(new UsernamePasswordCredentials("USERNAME","PASSWORD"), createNewWebClient(), true);
				if (response == null) {
					isActive = false;
					System.out.println("Couldn't create dummy login for "+PORTAL_CODE+". Trying once more...");
					response = attemptLogin(new UsernamePasswordCredentials("USERNAME","PASSWORD"), createNewWebClient(), true);
					if (response == null) {
						System.out.println("Couldn't create dummy login for "+PORTAL_CODE+". Marking Portal as inactive.");
						return;
					}
				}
				isActive = true;
			}
		}.start();
	}

	protected boolean needsCacheUpdate() {
		return getCache()==null || DateTime.now().minusHours(REFRESH_HOURS_THRESHOLD).isAfter(getLastCacheUpdateTime());
	}

	protected synchronized DateTime getLastCacheUpdateTime() {
		return lastCacheUpdate;
	}

	protected synchronized CookieManager getCookieManager() {
		return new Cloner().deepClone(cookieManager);
	}

	protected synchronized Cache getCache() {
		return new Cloner().deepClone(cache);
	}

	private static void disableWebPageErrors() {
		if (!loggingDisabled) {
			java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
			System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
			loggingDisabled = true;
		}
	}

	protected WebClient createConfiguredWebClient() {
		WebClient wc = createNewWebClient();
		wc.setCache(getCache());
		wc.setCookieManager(getCookieManager());
		return wc;
	}

	private WebClient createNewWebClient(){
		WebClient wc = new WebClient(BrowserVersion.CHROME_16);
		wc.getOptions().setRedirectEnabled(true);
		wc.getOptions().setJavaScriptEnabled(true);
		wc.getOptions().setPopupBlockerEnabled(false);
		wc.getOptions().setThrowExceptionOnFailingStatusCode(false);
		wc.getOptions().setThrowExceptionOnScriptError(false);
		wc.getOptions().setPrintContentOnFailingStatusCode(false);
		wc.getOptions().setUseInsecureSSL(true);
		wc.getOptions().setTimeout(30000);
		wc.getOptions().setActiveXNative(false);
		wc.getOptions().setAppletEnabled(false);
		wc.getOptions().setGeolocationEnabled(false);
		wc.getOptions().setCssEnabled(true);
		wc.setAjaxController(new NicelyResynchronizingAjaxController());
		disableWebPageErrors();
		return wc;
	}

	@Override
	public boolean equals(Object otherPortal) {
		if (otherPortal instanceof Portal) {
			return this.PORTAL_CODE.equals(((Portal)otherPortal).PORTAL_CODE);
		}
		return false;
	}

	public String getPORTAL_CODE() {
		return PORTAL_CODE;
	}

	public int getMAX_ATTEMPTS_BEFORE_LOCKOUT() {
		return MAX_ATTEMPTS_BEFORE_LOCKOUT;
	}

	public Minutes getATTEMPTS_RESET_TIME() {
		return ATTEMPTS_RESET_TIME;
	}

	public String getAUTH_URL() {
		return AUTH_URL;
	}

	public String getUSER_FIELD_XPATH() {
		return USER_FIELD_XPATH;
	}

	public String getPASS_FIELD_XPATH() {
		return PASS_FIELD_XPATH;
	}

	public String getLOGIN_BUTTON_XPATH() {
		return LOGIN_BUTTON_XPATH;
	}

	public boolean isActive() {
		return isActive;
	}


}
