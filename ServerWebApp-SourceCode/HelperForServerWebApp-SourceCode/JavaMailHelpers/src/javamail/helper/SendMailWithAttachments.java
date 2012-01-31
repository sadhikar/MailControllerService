package javamail.helper;

import com.sun.corba.se.spi.servicecontext.SendingContextServiceContext;
import com.sun.mail.smtp.SMTPSendFailedException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.Multipart;
import javax.mail.SendFailedException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * The purpose of this class is to Send an e-mail given the necessary
 * information. This is run as a separate thread in order to prevent the caller
 * from getting affected because of any delays in the mail sending operation
 *
 * 
 */
public class SendMailWithAttachments extends Thread {

    private String sender;
    private String[] recipientAddresses;
    private String[] carbonCopyAddresses;
    private String[] blindCarbonCopyAddresses;
    private String subject;
    private String text;
    private HashMap<Integer, String> fileIdToNameMap;
    private String directoryPathForAttachments = null;
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
    public SendMailWithAttachments(String mailServerPropertiesFileName,
            String sender,
            String[] recipientAddresses,
            String[] carbonCopyAddresses,
            String[] blindCarbonCopyAddresses,
            String subject,
            String text,
            HashMap<Integer, String> fileIdToNameMap) {


        InputStream mailServerPropertiesFile = null;
        Properties logFileProperty = null;


        this.sender = sender;
        this.recipientAddresses = recipientAddresses;
        this.carbonCopyAddresses = carbonCopyAddresses;
        this.blindCarbonCopyAddresses = blindCarbonCopyAddresses;
        this.subject = subject;
        this.text = text;
        this.fileIdToNameMap = fileIdToNameMap;
        logFileProperty = new Properties();

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
            System.out.println("inside this 1");
            throw new ExceptionInInitializerError("Error occured while " +
                    "reading " + mailServerPropertiesFileName +
                    exc.getMessage());
        }

        try {
            InputStream in = this.getClass().getClassLoader().getResourceAsStream("logfile.properties");
            logFileProperty.load(in);
            directoryPathForAttachments = logFileProperty.getProperty("attachments_directory_path");
        } catch (Exception ex) {
            /*
             * Occurs mostly when the properties file wasn't found in the
             * classpath
             */
            System.out.println("inside this 1");
            throw new ExceptionInInitializerError("Error occured while " +
                    "reading " + mailServerPropertiesFileName +
                    ex.getMessage());
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
        Multipart parts = new MimeMultipart();
        MimeBodyPart bodyWithAttachments = null;
        bodyWithAttachments = new MimeBodyPart();
        try {
            bodyWithAttachments.setText(text);
            parts.addBodyPart(bodyWithAttachments);
        } catch (MessagingException ex) {
            Logger.getLogger(SendMailWithAttachments.class.getName()).log(Level.SEVERE, null, ex);
        }

        /*
         * Add attachments to the mail.
         */
        Set<Integer> fileIdSet = fileIdToNameMap.keySet();
        Iterator iterate = fileIdSet.iterator();
        while (iterate.hasNext()) {

            int fileId = (Integer) iterate.next();

            try {

                FileDataSource fileDataSource = new FileDataSource(directoryPathForAttachments + fileId);
                bodyWithAttachments = new MimeBodyPart();
                bodyWithAttachments.setDataHandler(new DataHandler(fileDataSource));
                bodyWithAttachments.setFileName((String) fileIdToNameMap.get(fileId));
                System.out.println("attached!!!");
                parts.addBodyPart(bodyWithAttachments);

            } catch (MessagingException ex) {
                Logger.getLogger(SendMailWithAttachments.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                System.out.println(ex + "below message");
            }
        }


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
            simpleMessage.setContent(parts);
            //simpleMessage.setText(text);
            Transport.send(simpleMessage);
        }catch(SendFailedException exc){
            try {
                Transport.send(simpleMessage, exc.getValidUnsentAddresses());
            } catch (MessagingException ex) {
                Logger.getLogger(SendMailWithAttachments.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (MessagingException exc) {
            exc.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String mailServerPropertyFileName = new String("mailserver.properties");
        String sender = new String("chennai.ignite@tcs.com");
        String subject = new String("testing");
        String receiver[] = new String[]{"shrikant.adhikarla@tcs.com"};
        String text = new String("i don know???");
        HashMap<Integer, String> fileMap = new HashMap<Integer, String>();
        fileMap.put(20, "sample.jpg");
        SendMailWithAttachments send = new SendMailWithAttachments(mailServerPropertyFileName, sender, receiver, null, null, subject, text, fileMap);
        send.start();
    }
}
