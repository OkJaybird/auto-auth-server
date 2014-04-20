/*****************************************************************
	Jay Waldron
	jaywaldron@gmail.com
	Mar 3, 2014
 *****************************************************************/

package learning;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.concurrent.ConcurrentHashMap;

import util.DB;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;
import weka.filters.unsupervised.attribute.Remove;

/**
 * Handles the actual model configuration elements
 * @author jaywaldron
 *
 */
public class ResponseClassifier {

	private ConcurrentHashMap<String, FilteredClassifier> specializedMap;
	private FilteredClassifier classifier;
	private Instances classifierDataset;

	private static ResponseClassifier responseClassifier;
	public static ResponseClassifier getInstance() {
		if (responseClassifier == null) {
			responseClassifier = new ResponseClassifier();
		}
		return responseClassifier;
	}
	private ResponseClassifier() {
		loadModel();
	}

	/**
	 * Loads the general-case model
	 */
	private synchronized void loadModel() {
		try {
			deserializeModel();
			loadDatasetFormat();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Loads the pre-trained model
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void deserializeModel() throws IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(this.getClass().getResourceAsStream("/rf.model"));
		classifier = (FilteredClassifier) ois.readObject();
		ois.close();
	}

	/**
	 * Loads the pre-defined dataset format used to train the model
	 * 
	 * @throws IOException
	 */
	private void loadDatasetFormat() throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/data.arff")));
		ArffReader arff = new ArffReader(reader);
		classifierDataset = arff.getData();
		classifierDataset.setClassIndex(classifierDataset.numAttributes() - 1);
	}

	/**
	 * Classify a FeatureVector using the general model
	 * 
	 * @param vector
	 *            FeatureVector to be classified (with baseline-subtracted field
	 *            values)
	 * @return the classifier's determined authentication success
	 */
	public synchronized boolean classify(FeatureVector vector) {
		vector.setClassification(false);
		Instance instance = vector.toInstance(classifierDataset);
		instance.setClassMissing();

		boolean success = false;
		try {
			double classLabel = classifier.classifyInstance(instance);
			String classValue = instance.classAttribute().value((int)classLabel);
			if (classValue.equals("t")) {
				success = true;
			}
		} catch (Exception e) {
			System.out.println("Error classifying: "+instance);
			e.printStackTrace();
		}
		return success;
	}

	/**
	 * Classify a FeatureVector using a portalCode-specific model
	 * 
	 * @param vector
	 *            FeatureVector to be classified (with raw field values, not
	 *            baseline-subtracted)
	 * @return the classifier's determined authentication success
	 */
	public synchronized boolean classifyWithSpecializedClassifier(FeatureVector vector) {
		if (specializedMap == null) {
			specializedMap = new ConcurrentHashMap<String, FilteredClassifier>();
		}
		FilteredClassifier model = specializedMap.get(vector.getPortalCode());
		if (model == null) {
			model = generateSpecializedModel(vector);
			specializedMap.put(vector.getPortalCode(), model);
		}
		if (model == null) {
			FeatureVector vectorSubtracted = vector.getNewBaselineSubtractedVector();
			return classify(vectorSubtracted);
		}

		vector.setClassification(false);
		Instance instance = vector.toInstance(classifierDataset);
		instance.setClassMissing();

		boolean success = false;
		try {
			double classLabel = model.classifyInstance(instance);
			String classValue = instance.classAttribute().value((int)classLabel);
			if (classValue.equals("t")) {
				success = true;
			}
		} catch (Exception e) {
			System.out.println("Error classifying in specialized model: "+instance);
			e.printStackTrace();
		}
		return success;
	}

	/**
	 * Generates a specialized model for the given FeatureVector's portal
	 * 
	 * @param vector
	 *            FeatureVector who's portal a specialized model should be
	 *            created for
	 * @return specialized model for the FeatureVector's portal
	 */
	public synchronized FilteredClassifier generateSpecializedModel(FeatureVector vector) {
		Instances data = DB.getInstances(vector, classifierDataset);
		data.setClassIndex(data.numAttributes()-1);
		J48 j48 = new J48();
		j48.setUnpruned(true);
		j48.setMinNumObj(1);
		Remove rm = new Remove();
		rm.setAttributeIndices("1-26,28");
		FilteredClassifier fc = new FilteredClassifier();
		fc.setFilter(rm);
		fc.setClassifier(j48);
		try {
			fc.buildClassifier(data);
		} catch (Exception e) {
			System.out.println("unable to build specialized classifier.");
			e.printStackTrace();
		}
		return fc;
	}

}
