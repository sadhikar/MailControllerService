package javamail.helper;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * The purpose of this class is to Send an e-mail given the necessary
 * information. This is run as a separate thread in order to prevent the caller
 * from getting affected because of any delays in the mail sending operation
 *
 *
 */
public class SendMail extends Thread {

    private String sender;
    private String[] recipientAddresses;
    private String[] carbonCopyAddresses;
    private String[] blindCarbonCopyAddresses;
    private String subject;
    private String text;
    private Properties mailServerInfo;

    /**
     * Creates an instance of this thread.
     * @param mailServerPropertiesFileName  The name of the properties file
     * containing information about the mail server
     * @param sender  The reply-to address the recipient would see
     * @param recipient  The e-mail address of the recipient
     * @param subject  The subject of the e-mail
     * @param text  The main content of the e-mail
     * @throws ExceptionInInitializerError  When unable to load the necessary
     * information from the properties file.
     */
    public SendMail(String mailServerPropertiesFileName,
            String sender,
            String[] recipientAddresses,
            String[] carbonCopyAddresses,
            String[] blindCarbonCopyAddresses,
            String subject,
            String text) {

        InputStream mailServerPropertiesFile = null;
        this.sender = sender;
        this.recipientAddresses = recipientAddresses;
        this.carbonCopyAddresses = carbonCopyAddresses;
        this.blindCarbonCopyAddresses = blindCarbonCopyAddresses;
        this.subject = subject;
        this.text = text;

        mailServerInfo = new Properties();
        /*
         * Attempts to load the properties file specified by the constructor
         * parameter from the classpath
         */
        try {
            mailServerPropertiesFile = this.getClass().getClassLoader().getResourceAsStream(mailServerPropertiesFileName);
            mailServerInfo.load(mailServerPropertiesFile);
        } catch (Exception exc) {
            /*
             * Occurs mostly when the properties file wasn't found in the
             * classpath
             */
            throw new ExceptionInInitializerError("Error occured while " +
                    "reading " + mailServerPropertiesFileName +
                    exc.getMessage());
        }

    }

    /*
     * This method is called internally by the start method when this program
     * is run as a thread. 
     */
    @Override
    public void run() {
        send();
    }

    /*
     * The purpose of this method is to send the e-mail. It reads information
     * about the mail server from the properties file. 
     */
    private void send() {
        InternetAddress fromAddress = null;
        InternetAddress[] recipientEMailAddresses;
        InternetAddress[] carbonCopyEMailAddresses;
        InternetAddress[] blindCarbonCopyEmailAddresses;
        Session mailSession = Session.getDefaultInstance(mailServerInfo);
        Message simpleMessage = new MimeMessage(mailSession);
        try {
            fromAddress = new InternetAddress(sender);
            simpleMessage.setFrom(fromAddress);

            try {
                recipientEMailAddresses =
                        new GetInternetAddresses(recipientAddresses).getAddresses();
                simpleMessage.setRecipients(RecipientType.TO,
                        recipientEMailAddresses);
            } catch (MessagingException exc) {
                throw new RuntimeException(exc.getMessage());
            }

            try {
                if (carbonCopyAddresses != null) {
                    carbonCopyEMailAddresses =
                            new GetInternetAddresses(carbonCopyAddresses).getAddresses();
                    simpleMessage.setRecipients(RecipientType.CC,
                            carbonCopyEMailAddresses);
                }
            } catch (MessagingException exc) {
                throw new RuntimeException(exc.getMessage());
            }

            try {
                if (blindCarbonCopyAddresses != null) {
                    blindCarbonCopyEmailAddresses =
                            new GetInternetAddresses(blindCarbonCopyAddresses).getAddresses();
                    simpleMessage.setRecipients(RecipientType.BCC,
                            blindCarbonCopyEmailAddresses);
                }
            } catch (MessagingException exc) {
                throw new RuntimeException(exc.getMessage());
            }

            simpleMessage.setSubject(subject);
            simpleMessage.setText(text);
            Transport.send(simpleMessage);
        } catch (SendFailedException exc) {
            try {
                Transport.send(simpleMessage, exc.getValidUnsentAddresses());
            } catch (MessagingException ex) {
                Logger.getLogger(SendMailWithAttachments.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (MessagingException exc) {
        }
    }
}
