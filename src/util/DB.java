/*****************************************************************
	Jay Waldron
	jaywaldron@gmail.com
	Feb 16, 2014
 *****************************************************************/

package util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import learning.FeatureVector;
import weka.core.Instances;

public class DB {
	private static final String SPECIALIZED_TABLE_NAME = "custom_vectors";
	private static final String PROP_FILE = "/database.properties";
	private static final String HOST_KEY = "hostname";
	private static final String PORT_KEY = "port";
	private static final String DB_KEY = "dbname";
	private static final String USER_KEY = "user";
	private static final String PASS_KEY = "pass";
	private static Properties props = null;

	public static Instances getInstances(FeatureVector vector, Instances dataFormat) {
		if (props == null) {
			props = new DB().getPropertiesForDB(PROP_FILE);
		}
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		List<FeatureVector> vectors = new LinkedList<FeatureVector>();
		Connection conn = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			conn = DriverManager.getConnection (props.getProperty(HOST_KEY)+props.getProperty(DB_KEY), props.getProperty(USER_KEY), props.getProperty(PASS_KEY));
			String tableName = props.getProperty(DB_KEY)+"."+SPECIALIZED_TABLE_NAME;
			preparedStatement = conn.prepareStatement("SELECT "+FeatureVector.getFieldNamesForQuery()+" FROM "+ tableName +" WHERE portal_code = ?");
			preparedStatement.setString(1, vector.getPortalCode());

			resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				int cols = resultSet.getMetaData().getColumnCount();
				Object[] arr = new Object[cols];
				for(int i=0; i<cols; i++){
					arr[i] = resultSet.getObject(i+1);
				}
				FeatureVector v = new FeatureVector();
				v.setValuesFromArray(arr);
				vectors.add(v);
			}
			Instances instances = new Instances(dataFormat, vectors.size());
			for (FeatureVector v : vectors) {
				instances.add(v.toInstance(dataFormat));
			}
			return instances;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (conn != null) {
				try {
					conn.close ();
				} catch (Exception e) {}
			}
		}
	}

	public Properties getPropertiesForDB(String filename) {
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = this.getClass().getResourceAsStream(filename);
			prop.load(input);
			if (prop.containsKey(HOST_KEY) && prop.containsKey(PORT_KEY) && prop.containsKey(DB_KEY) && prop.containsKey(USER_KEY) && prop.containsKey(PASS_KEY)) {
				return prop;
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

}
