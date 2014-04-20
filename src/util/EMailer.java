/*****************************************************************
	Jay Waldron
	jaywaldron@gmail.com
	2013
*****************************************************************/
package util;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.joda.time.DateTime;

/**
 * @author Jay Waldron
 * @date Jul 21, 2013
 */
public class EMailer {

	private final String FROM;
	private final String TO;
	private final String SUBJECT;
	private final String MESSAGE;
	
	public EMailer(String fromAddress, String toAddress, String subject, String message){
		FROM = fromAddress;
		TO = toAddress;
		SUBJECT = subject;
		MESSAGE = message;
	}
	
	public void send(){
		Properties props = new Properties();
		props.put("mail.smtp.host", "localhost");
		Session session = Session.getInstance(props);
		try {
		    Message msg = new MimeMessage(session);
		    msg.setFrom(new InternetAddress(FROM));
		    InternetAddress[] address = {new InternetAddress(TO)};
		    msg.setRecipients(Message.RecipientType.TO, address);
		    msg.setSubject(SUBJECT);
		    msg.setSentDate(DateTime.now().toDate());
		    msg.setText(MESSAGE);
		    Transport.send(msg);
		}
		catch (MessagingException mex) {
		    mex.printStackTrace();
		}
	}
}
