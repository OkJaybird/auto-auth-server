/*****************************************************************
	Jay Waldron
	jaywaldron@gmail.com
	2013
 *****************************************************************/
package util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author Jay Waldron
 * @date Jul 22, 2013
 */
public class Util {

	public enum Priority {
		LOW, MEDIUM, HIGH
	}

	public enum CONFIG_FLAGS {
		ADMIN_EMAIL, JAVA_COMMAND
	}

	private final static String ADMIN_EMAIL = "user@domain.com";

	public static void notifyAdmin(String subject, String message, Priority priority){
		subject = "["+priority+" PRIORITY] Auto-Auth-Server: "+subject;
		// Removed for elastic beanstalk compatibility, but kept here for reference. Currently just prints to log.
		// new EMailer("Auto-Auth-Server@wustl.edu", ADMIN_EMAIL, subject, message).send();
		System.out.println(subject+": send to "+ADMIN_EMAIL);
		System.out.println();
	}

	/**
	 * Helper method for notifyAdmin to handle thrown errors
	 * @param subject
	 * @param exceptions
	 * @param priority
	 */
	public static void notifyAdmin(String subject, Exception exceptions, Priority priority){
		StringWriter stringWriter = new StringWriter();
		exceptions.printStackTrace(new PrintWriter(stringWriter));
		notifyAdmin(subject, stringWriter.toString(), priority);
	}

	public static byte[] toByteArray(InputStream is) throws IOException {
		if (is==null) {
			return new byte[0];
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int reads = is.read();
		while(reads != -1){
			baos.write(reads);
			reads = is.read();
		}
		return baos.toByteArray();
	}
}
