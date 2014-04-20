/*****************************************************************
	Jay Waldron
	jaywaldron@gmail.com
	Mar 2, 2014
 *****************************************************************/

package learning;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Handles determining response page authentication success
 * @author jaywaldron
 *
 */
public class PortalResponseHandler {

	/**
	 * Determine if a response page is valid
	 * 
	 * @param portalCode
	 *            the portal this page corresponds to
	 * @param responsePage
	 *            the page returned after a login attempt
	 * @param responseClient
	 *            the WebClient that handled web navigation to the responsePage
	 * @param specializedModel
	 *            true if using specialized model for this portal is desired,
	 *            false if using the general model
	 * @return classifier-determined authentication success
	 */
	public static boolean isValidResponse(String portalCode, HtmlPage responsePage, WebClient responseClient, boolean specializedModel) {
		FeatureVector vectorRawValues = new FeatureVector(portalCode, false, responsePage, responseClient);
		if (specializedModel) {
			return ResponseClassifier.getInstance().classifyWithSpecializedClassifier(vectorRawValues);
		} else {
			FeatureVector vectorSubtracted = vectorRawValues.getNewBaselineSubtractedVector();
			return ResponseClassifier.getInstance().classify(vectorSubtracted);
		}
	}

}
