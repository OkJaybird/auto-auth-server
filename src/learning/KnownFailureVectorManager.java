/*****************************************************************
	Jay Waldron
	jaywaldron@gmail.com
	Mar 8, 2014
 *****************************************************************/

package learning;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Container object that holds known failure vectors
 * @author jaywaldron
 *
 */
public class KnownFailureVectorManager {

	private static KnownFailureVectorManager manager;
	public static KnownFailureVectorManager getInstance() {
		if (manager==null) {
			manager = new KnownFailureVectorManager();
		}
		return manager;
	}
	private KnownFailureVectorManager(){}

	private ConcurrentHashMap<String, FeatureVector> map = new ConcurrentHashMap<>();

	/**
	 * Add FeatureVector to collection of known failures. Overwrites previous
	 * entries.
	 * 
	 * @param vector
	 *            FeatureVector to save as a baseline failure
	 */
	public void add(FeatureVector vector) {
		map.put(vector.getPortalCode(), vector);
	}

	/**
	 * Get known failure FeatureVector for a given portalCode
	 * 
	 * @param portalCode
	 * @return known failure FeatureVector
	 */
	public FeatureVector getVector(String portalCode) {
		return map.get(portalCode);
	}

}
