/*****************************************************************
	Jay Waldron
	jaywaldron@gmail.com
	Apr 9, 2014
 *****************************************************************/

package learning;

import java.awt.Color;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.host.Window;
import com.gargoylesoftware.htmlunit.javascript.host.css.ComputedCSSStyleDeclaration;
import com.gargoylesoftware.htmlunit.javascript.host.html.HTMLElement;
import com.gargoylesoftware.htmlunit.util.Cookie;

/**
 * Object representation that handles the features used to classify web pages.
 * Uses relfection to communicate with the classification model (and the DB if a
 * specialized model has been requested in the configuration file), so order and
 * names of the fields matter. Do not alter unless modifying the model and DB as
 * well.
 * 
 * @author jaywaldron
 * 
 */
public class FeatureVector {
	// Text
	public int sentiment_score;
	public int length_page_text;
	public int length_title_text;
	public int length_url;
	public int num_prop_nouns;
	public int num_kw_login;
	public int num_kw_logout;
	public int num_kw_createaccount;
	public int num_kw_pass;
	public int num_kw_user;
	public int num_kw_forgot;
	public int num_kw_reset;
	public int num_kw_locked;
	public int num_kw_contactadmin;

	// Content
	public int num_divs;
	public int num_frames;
	public int num_forms;
	public int num_inputs;
	public int num_images;
	public int num_links;
	public int num_colors;
	public int num_text_sizes;
	public int num_text_families;
	public int has_red_text;
	public int pixel_area_images;

	// Structure
	public int response_code;
	public int num_cookies;
	public int num_cookie_domains;
	public int num_secure_cookies;

	// General
	public String classification;
	public String portalCode;

	/**
	 * Create a FeatureVector and parse features from the given parameters.
	 * 
	 * @param portalCode
	 *            the portal this FeatureVector corresponds to
	 * @param success
	 *            whether authentication was valid. This is not important if the
	 *            FeatureVector will later be classified.
	 * @param responsePage
	 *            the page returned after a login attempt
	 * @param webClient
	 *            the WebClient that handled web navigation to the responsePage
	 */
	public FeatureVector(String portalCode, boolean success, HtmlPage responsePage, WebClient webClient) {
		this.portalCode = portalCode;
		setClassification(success);
		parseData(success, responsePage, webClient);
	}

	/**
	 * Creates an empty FeatureVector with default field values
	 */
	public FeatureVector(){}

	/**
	 * Set classification as valid or invalid
	 * 
	 * @param success
	 *            was valid login attempt
	 */
	public void setClassification(boolean success) {
		classification = success ? "t" : "f";
	}

	/**
	 * get guaranteed read-only portal_code
	 * 
	 * @return portalCode
	 */
	public String getPortalCode() {
		return portalCode;
	}

	/**
	 * Parse the features for this FeatureVector
	 * 
	 * @param success
	 *            if the authentication was successful. Not important if
	 *            FeatureVector will later be classified.
	 * @param responsePage
	 *            the page returned after a login attempt
	 * @param webClient
	 *            the WebClient that handled web navigation to the responsePage
	 */
	private void parseData(boolean success, HtmlPage responsePage, WebClient webClient) {
		String pageText = responsePage.asText().toLowerCase();
		String pageSource = responsePage.asXml().toLowerCase();

		// Text
		sentiment_score = 0;

		String[] words = pageText.split("\\s+");
		length_page_text = words.length;

		String[] wordsTitle = responsePage.getTitleText().toLowerCase().split("\\s+");
		length_title_text = wordsTitle.length;

		length_url = responsePage.getUrl().toString().length();

		String[] rawWords = responsePage.asText().split("\\s+");
		for (String word : rawWords) {
			if (!word.equals("") && Character.isUpperCase(word.codePointAt(0))) {
				num_prop_nouns++;
			}
		}

		String[] sourceWords = pageSource.split("\\s+");
		countTextKeywords(sourceWords);
		countTextKeywords(wordsTitle);

		// Content
		num_divs = responsePage.getByXPath("//div").size();
		num_frames = responsePage.getByXPath("//frame").size();
		num_forms = responsePage.getByXPath("//form").size();
		List<?> inputs = responsePage.getByXPath("//input");
		num_inputs = inputs.size();
		List<?> images = responsePage.getByXPath("//img");
		num_images = images.size();
		num_links = responsePage.getByXPath("//a").size();

		for (Object imageObject : images) {
			HtmlImage image = (HtmlImage) imageObject;
			try {
				pixel_area_images += image.getHeight() * image.getWidth();
			} catch (IOException e) {}
		}

		HashSet<String> colors = new HashSet<>();
		HashSet<String> fonts = new HashSet<>();
		HashSet<String> textSizes = new HashSet<>();
		HtmlElement body = responsePage.getFirstByXPath("/html/body");
		for (HtmlElement e : body.getHtmlElementDescendants()) {
			if (e.isDisplayed()) {
				HTMLElement element = (HTMLElement) e.getScriptObject();
				ComputedCSSStyleDeclaration style = ((Window) webClient.getCurrentWindow().getScriptObject()).getComputedStyle(element, null);

				colors.add(style.getBackgroundColor().toLowerCase());
				String colorString = style.getColor().toLowerCase();
				colors.add(colorString);

				textSizes.add(style.getFontSize().toLowerCase());

				String[] fontsList = style.getFontFamily().split("[,\\s]+");
				fonts.add(fontsList[0].toLowerCase());

				Color c;
				if (colorString.startsWith("#")) {
					c = Color.decode(colorString);
				} else if (colorString.startsWith("rgb")) {
					c = parseStringRGB(colorString);
				} else { // a plain name
					c = Color.getColor(colorString, Color.BLACK);
				}
				if (c.getRed() >= 150 && c.getGreen() <= 60 && c.getBlue() <= 60) {
					has_red_text = 1;
				}
			}
		}
		num_colors = colors.size();
		num_text_sizes = textSizes.size();
		num_text_families = fonts.size();

		// Structure
		response_code = responsePage.getWebResponse().getStatusCode();
		CookieManager cookieManager = webClient.getCookieManager();
		num_cookies = cookieManager.getCookies().size();
		HashSet<String> cookieDomains = new HashSet<>();
		for (Cookie c : cookieManager.getCookies()) {
			cookieDomains.add(c.getDomain());
			if (c.isSecure()) {
				num_secure_cookies++;
			}
		}
		num_cookie_domains = cookieDomains.size();
	}

	/**
	 * Parse a RGB string to a Color
	 * 
	 * @param input
	 *            string representation of color: rgb(0,255, 25)
	 * @return color the string represents. If a parse error occurred,
	 *         Color.BLACK is returned instead.
	 */
	private Color parseStringRGB(String input) {
		Pattern c = Pattern.compile("rgb *\\( *([0-9]+), *([0-9]+), *([0-9]+) *\\)");
		Matcher m = c.matcher(input);
		if (m.matches()) {
			return new Color(Integer.valueOf(m.group(1)), Integer.valueOf(m.group(2)), Integer.valueOf(m.group(3)));
		}
		return Color.BLACK;
	}

	/**
	 * Count the number of keyword occurrences and increment the field counts
	 * when found. Indentification is accomplished using
	 * {@link String#contains(CharSequence)}, so heavily pre-processing the
	 * provided words is not necessary.
	 * 
	 * @param words
	 *            the tokenized words split by whitespace.
	 */
	private void countTextKeywords(String[] words) {
		for (int i=0; i< words.length; i++) {
			String s = words[i];
			if (s.contains("login")) {
				num_kw_login++;
			} else if (s.contains("logout")) {
				num_kw_logout++;
			} else if (i+1<words.length && s.contains("sign")) {
				if (words[i+1].contains("in")) {
					num_kw_login++;
				} else if (words[i+1].contains("out")) {
					num_kw_logout++;
				}
			} else if (s.contains("signin")) {
				num_kw_login++;
			} else if (s.contains("signout")) {
				num_kw_logout++;
			} else if (i+1<words.length && s.contains("create")) {
				if (words[i+1].contains("account") || i+2<words.length && words[i+2].contains("account")) {
					num_kw_createaccount++;
				}
			} else if (s.contains("password") || s.contains("pwd")) {
				num_kw_pass++;
			} else if (s.contains("username")) {
				num_kw_user++;
			} else if (i+1<words.length && s.contains("forgot")) {
				if (words[i+1].contains("pass") || words[i+1].contains("user") || i+2<words.length && (words[i+2].contains("pass") || words[i+2].contains("user"))) {
					num_kw_forgot++;
				}
			} else if (s.contains("reset")) {
				num_kw_reset++;
			}
			else if (i+1<words.length && s.contains("account")) {
				if (words[i+1].contains("locked") || i+2<words.length && words[i+2].contains("locked") || i+3<words.length && words[i+3].contains("locked")) {
					num_kw_locked++;
				} else if (words[i+1].contains("disabled") || i+2<words.length && words[i+2].contains("disabled") || i+3<words.length && words[i+3].contains("disabled")) {
					num_kw_locked++;
				}
			} else if (i+1<words.length && s.contains("contact")) {
				if (words[i+1].contains("admin") || i+2<words.length && words[i+2].contains("admin")) {
					num_kw_contactadmin++;
				}
			}
		}
	}

	/**
	 * Gets a new FeatureVector that has the known failure baseline for its
	 * portalCode subtracted from it.
	 * 
	 * @return new FeatureVector with its values as the differences between it
	 *         and a known failure baseline
	 */
	public FeatureVector getNewBaselineSubtractedVector() {
		FeatureVector newVector = new FeatureVector();
		FeatureVector baseline = KnownFailureVectorManager.getInstance().getVector(portalCode);

		newVector.portalCode = portalCode;
		newVector.classification = classification;
		for (Field f : this.getClass().getFields()) {
			try {
				if (f.getType().equals(int.class)) {
					int thisVal = (int) f.get(this);
					int otherVal = (int) baseline.getClass().getField(f.getName()).get(baseline);
					f.set(newVector, thisVal - otherVal);
				}
			} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
				e.printStackTrace();
			}
		}
		return newVector;
	}

	/**
	 * Converts this FeatureVector's fields to Instance format to be used by
	 * WEKA's classifier.
	 * 
	 * @param datasource
	 *            a sample datasource which the classifier is trained on. This
	 *            datasource format is set for the new Instance so that it is
	 *            compatible with the model.
	 * @return new Instance representation of this FeatureVector
	 */
	public Instance toInstance(Instances datasource) {
		Instance instance = new DenseInstance(this.getClass().getFields().length-1);
		instance.setDataset(datasource);
		int valIndex = 0;
		Field[] fields = this.getClass().getFields();
		for (Field f : fields) {
			if (valIndex < this.getClass().getFields().length-1) {
				try {
					if (f.getType().equals(int.class)) {
						instance.setValue(valIndex, (int)f.get(this));
					} else if (f.getType().equals(String.class)) {
						instance.setValue(valIndex, (String)f.get(this));
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
				valIndex++;
			}
		}
		return instance;
	}

	/**
	 * Gets the names of all fields to be used in a DB query
	 * 
	 * @return comma-separated field names
	 */
	public static String getFieldNamesForQuery() {
		FeatureVector v = new FeatureVector();
		return v.getFieldNamesForQueryHelper();
	}

	/**
	 * Gets the names of all fields to be used in a DB query
	 * 
	 * @return comma-separated field names
	 */
	private String getFieldNamesForQueryHelper() {
		@SuppressWarnings("unused")
		FeatureVector v = new FeatureVector();
		StringBuilder sb = new StringBuilder();
		int i=0;
		for (Field f : this.getClass().getFields()) {
			if (i < this.getClass().getFields().length-1) {
				try {
					sb.append(f.getName());
					if (i+1 < this.getClass().getFields().length-1) {
						sb.append(", ");
					}
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				}
			}
			i++;
		}
		return sb.toString();
	}

	/**
	 * Sets values for FeatureVector from an array of Objects. Only used when
	 * specialized model is requested.
	 * 
	 * @param arr
	 *            the field values packaged as an Object[]
	 */
	public void setValuesFromArray(Object[] arr) {
		int i = 0;
		for (Field f : this.getClass().getFields()) {
			if (i < this.getClass().getFields().length-1) {
				try {
					if (arr[i] instanceof Integer) {
						f.set(this, (int)arr[i]);
					}  else if (arr[i] instanceof String) {
						f.set(this, arr[i]);
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			i++;
		}
	}

}
