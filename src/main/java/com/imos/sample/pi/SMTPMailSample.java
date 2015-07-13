/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.imos.sample.pi;

import java.io.File;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 *
 * @author Alok
 */
public class SMTPMailSample {

    private final Properties properties = new Properties();
    
    public void connect(String header) {
        // Get system properties
        Properties mailProperties = System.getProperties();

        // Setup mail server
        mailProperties.setProperty("mail.smtp.host", "smtp.gmail.com");
        mailProperties.setProperty("mail.smtp.user", "RaspberryPi");
        mailProperties.setProperty("mail.smtp.port", "587");
        mailProperties.setProperty("mail.smtp.auth", "true");
        mailProperties.setProperty("mail.smtp.starttls.enable", "true");

        // Get the default Session object.
        Session session = Session.getDefaultInstance(mailProperties);

        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            message.setFrom(new InternetAddress("alok.r.meher@gmail.com"));

            message.addRecipient(Message.RecipientType.TO, new InternetAddress("meher.ranjan12@gmail.com"));

            // Set Subject: header field
            message.setSubject("This is the Subject Line! " + header);

            // Now set the actual message
            message.setText("This is actual message");

            // Send message
            Transport transport = session.getTransport("smtp");
            transport.connect("smtp.gmail.com", "alok.r.meher@gmail.com", "gun1new*point");
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
            System.out.println("Sent message successfully....");
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }

    public void connect(String header, String videoFile, String timeFile) {

//        try {
//            properties.load(new FileReader("src/main/resources/mail.properties"));
//        } catch (IOException ex) {
//            Logger.getLogger(SMTPMailSample.class.getName()).log(Level.SEVERE, null, ex);
//        }
        // Get system properties
        Properties mailProperties = System.getProperties();

        // Setup mail server
//        mailProperties.setProperty("mail.smtp.host", getProperty(MAIL_SMTP_HOST));
//        mailProperties.setProperty("mail.smtp.user", getProperty(MAIL_SMTP_USER));
//        mailProperties.setProperty("mail.smtp.port", getProperty(MAIL_SMTP_PORT));
//        mailProperties.setProperty("mail.smtp.auth", getProperty(MAIL_SMTP_AUTH));
//        mailProperties.setProperty("mail.smtp.starttls.enable", getProperty(MAIL_SMTP_STARTLS_ENABLE));
        mailProperties.setProperty("mail.smtp.host", "smtp.gmail.com");
        mailProperties.setProperty("mail.smtp.user", "RaspberryPi");
        mailProperties.setProperty("mail.smtp.port", "587");
        mailProperties.setProperty("mail.smtp.auth", "true");
        mailProperties.setProperty("mail.smtp.starttls.enable", "true");

        // Get the default Session object.
        Session session = Session.getDefaultInstance(mailProperties);

        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field of the header.
            //message.setFrom(new InternetAddress(getProperty(FROM_MAIL_ID)));
            message.setFrom(new InternetAddress("alok.r.meher@gmail.com"));

            // Set To: header field of the header.
            //message.addRecipient(Message.RecipientType.TO, new InternetAddress(getProperty(TO_MAIL_ID)));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress("meher.ranjan12@gmail.com"));
//            message.addRecipient(Message.RecipientType.CC, new InternetAddress(to));

            // Set Subject: header field
            message.setSubject("This is the Subject Line! " + header);

            // Now set the actual message
            message.setText("This is actual message");

            attachement(message, videoFile, timeFile);

            // Send message
            Transport transport = session.getTransport("smtp");
            //transport.connect(getProperty(MAIL_SMTP_HOST), getProperty(USER_ID), getProperty(USER_PASSWORD));
            transport.connect("smtp.gmail.com", "alok.r.meher@gmail.com", "gun1new*point");
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
            System.out.println("Sent message successfully....");
        } catch (MessagingException mex) {
            mex.printStackTrace();
        } finally {
            File filess = new File("./");
            for (File file : filess.listFiles()) {
                if (file.getName().endsWith("h264")) {
                    System.out.println("file deleted : " + file.delete());
                }
                if (file.getName().endsWith("mp4")) {
                    file.delete();
                }
            }
            System.out.println("file deleted");
        }

//        System.out.println("done");
    }

    private String getProperty(String key) {

        return properties.getProperty(key);
    }

    private void attachement(MimeMessage message, String videoFile, String timeFile) throws MessagingException {
        // Create a multipart message for attachment
        Multipart multipart = new MimeMultipart();

        // Create the message body part
        BodyPart messageBodyPart = new MimeBodyPart();

        // Valid file location
        //String filename = "sample.txt";
        DataSource source = new FileDataSource(videoFile);
        messageBodyPart.setDataHandler(new DataHandler(source));
        //messageBodyPart.setFileName(source.getName() + getTimeWithDate());
        messageBodyPart.setFileName(source.getName());
        multipart.addBodyPart(messageBodyPart);

        messageBodyPart = new MimeBodyPart();
        source = new FileDataSource(timeFile);
        messageBodyPart.setDataHandler(new DataHandler(source));
        //messageBodyPart.setFileName(source.getName() + getTimeWithDate());
        messageBodyPart.setFileName(source.getName());
        multipart.addBodyPart(messageBodyPart);
        //multipart.addBodyPart(messageBodyPart);

        message.setContent(multipart);
    }

    private String getTimeWithDate() {
        Calendar cal = GregorianCalendar.getInstance();
        String separator = "-";
        StringBuilder builder = new StringBuilder();
        builder.append(separator);
        builder.append(cal.get(Calendar.HOUR_OF_DAY));
        builder.append(separator);
        builder.append(cal.get(Calendar.MINUTE));
        builder.append(separator);
        builder.append(cal.get(Calendar.SECOND));
        builder.append(separator);
        builder.append(cal.get(Calendar.DATE));
        builder.append(separator);
        builder.append(cal.get(Calendar.MONTH));
        builder.append(separator);
        builder.append(cal.get(Calendar.YEAR));
        builder.append(separator);

        return builder.toString();
    }
}
