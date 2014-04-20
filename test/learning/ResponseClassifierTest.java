/*****************************************************************
	Jay Waldron
	jaywaldron@gmail.com
	Apr 12, 2014
 *****************************************************************/

package learning;

import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.Assert;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class ResponseClassifierTest {

	@Test
	public void test() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		final WebClient webClient = createNewWebClient();

		final HtmlPage pageGoogle = webClient.getPage("https://www.google.com/");
		FeatureVector vectorG = new FeatureVector("google", true, pageGoogle, webClient);
		KnownFailureVectorManager.getInstance().add(vectorG);

		final HtmlPage pageTwitter = webClient.getPage("https://twitter.com/");
		FeatureVector vectorT = new FeatureVector("twitter", true, pageTwitter, webClient);
		KnownFailureVectorManager.getInstance().add(vectorT);

		Assert.assertFalse(PortalResponseHandler.isValidResponse("google", pageGoogle, webClient, false));
		Assert.assertFalse(PortalResponseHandler.isValidResponse("twitter", pageGoogle, webClient, true));
		Assert.assertFalse(PortalResponseHandler.isValidResponse("twitter", pageGoogle, webClient, true));

		webClient.closeAllWindows();
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
		return wc;
	}

}
