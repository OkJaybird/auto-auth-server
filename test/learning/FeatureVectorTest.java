/*****************************************************************
	Jay Waldron
	jaywaldron@gmail.com
	Apr 12, 2014
 *****************************************************************/

package learning;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;

import org.junit.Assert;
import org.junit.Test;

import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class FeatureVectorTest {

	@Test
	public void testFeatureVector() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		FeatureVector vector = new FeatureVector();
		Assert.assertNotNull(vector);
		Assert.assertEquals(0, vector.length_url);

		final WebClient webClient = createNewWebClient();
		final HtmlPage pageGoogle = webClient.getPage("https://www.google.com/");
		vector = new FeatureVector("google", false, pageGoogle, webClient);
		Assert.assertEquals("google", vector.getPortalCode());
		webClient.closeAllWindows();

		final HtmlPage pageTwitter = webClient.getPage("https://twitter.com/");
		vector = new FeatureVector("twitter", true, pageTwitter, webClient);
		webClient.closeAllWindows();

		final HtmlPage pageW = webClient.getPage("http://en.wikipedia.org/wiki/Help:Using_colours");
		vector = new FeatureVector("wiki", true, pageW, webClient);
		webClient.closeAllWindows();

		Assert.assertNotNull(FeatureVector.getFieldNamesForQuery());
	}

	@Test
	public void testGetNewBaselineSubtractedVector() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		final WebClient webClient = createNewWebClient();
		final HtmlPage pageGoogle = webClient.getPage("https://www.google.com/");
		FeatureVector vector = new FeatureVector("google", false, pageGoogle, webClient);
		KnownFailureVectorManager.getInstance().add(vector);

		FeatureVector subtracted = vector.getNewBaselineSubtractedVector();
		Assert.assertEquals(0, subtracted.num_cookies);
	}

	@Test
	public void testSetValuesFromArray() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		final WebClient webClient = createNewWebClient();
		final HtmlPage pageGoogle = webClient.getPage("https://www.linkedin.com/");
		FeatureVector vector = new FeatureVector("linkedin", false, pageGoogle, webClient);
		Object[] arr = new Object[vector.getClass().getFields().length];
		for (int i=0; i<arr.length-2; i++) {
			arr[i] = 1;
		}
		arr[arr.length-2] = "f";
		arr[arr.length-1] = "linkedin";
		vector.setValuesFromArray(arr);
		Assert.assertEquals(1, vector.length_url);

		// to instances
		BufferedReader reader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/data.arff")));
		ArffReader arff = new ArffReader(reader);
		Instances classifierDataset = arff.getData();
		Assert.assertNotNull(vector.toInstance(classifierDataset));
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
