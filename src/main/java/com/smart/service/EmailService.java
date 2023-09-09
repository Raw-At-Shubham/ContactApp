package com.smart.service;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

@Service
public class EmailService {
public boolean sendEmail(String subject,String message,String to)
{
boolean f=false;
String from="rawat2118@gmail.com";
String host="smtp.gmail.com";
Properties properties=System.getProperties();
properties.put("mail.smtp.host",host);
properties.put("mail.smtp.port","465");
properties.put("mail.smtp.ssl.enable","true");
properties.put("mail.smtp.auth","true");

//Strp 1-Get session object
Session session=Session.getInstance(properties,new Authenticator() {
	@Override
	protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication("rawat2118@gmail.com", "yfmblhgzcjjroaya");
	}
});

//Step 2- Compose message using MimeMessage class
MimeMessage m = new MimeMessage(session);
try {
	m.setFrom(from);
	m.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
	m.setSubject(subject);
//	m.setText(message);
	m.setContent(message,"text/html");
	//Strp 3:Trasnsoprt message
	Transport.send(m);
	System.out.println("Email sent");
	f=true;
	
}
catch(Exception e)
{
e.printStackTrace();	
}
return f;
}
}
