/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package client.sendmailfrompojo;

import client.sendmailfrompojo.exceptions.SendingFailedException;
import com.tcs.igrid.javamail.helper.Message;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The purpose of this class is to establish a connection to the web application
 * which would send e-mails given the necessary parameters and get back the
 * response from it.
 * 
 * 
 */
public class SendMailPojo {

    private String servletURL;
    private String[] recipientAddresses;
    private String[] carbonCopyAddresses;
    private String[] blindCarbonCopyAddresses;
    private String subject;
    private String body;
    private String[] filepaths=null;
    private HashMap<Integer,String> fileIdToNameMap = new HashMap<Integer, String>();
    private String uploadUrl =null;
    private float sizeThreshold=0;

    /**
     *  It creates the instance of the SendMailPojo class taking neccessary inputs as
     *  arguments.
     *
     *  @param servletUrl is the url of the web application which supports mail sending service.
     *                    This url is given in mailServer properties file by the name "mail_server_url" given with pojo.
     *
     *  @param recipientAddresses is an array of email-address of the mail recepients.
     *
     *  @param carbonCopyAddresses is an array of email-address, receiving a carbon copy.
     *
     *  @param blindCarbonCopyAddresses is an array of email-address, receiving a blind carbon copy.
     *
     *  @param subject is the subject of the mail being sent.
     *
     *  @param body is the message that the mail has to carry.
     *
     *  @param attachments is an array of complete filepaths for the attachments to be sent.
     *
     */

    public SendMailPojo(String servletUrl,
            String[] recipientAddresses,
            String[] carbonCopyAddresses,
            String[] blindCarbonCopyAddresses,
            String subject,
            String body, String[] attachments) {
        this.servletURL = servletUrl;
        this.recipientAddresses = recipientAddresses;
        this.carbonCopyAddresses = carbonCopyAddresses;
        this.blindCarbonCopyAddresses = blindCarbonCopyAddresses;
        this.subject = subject;
        this.body = body;
        this.filepaths = attachments;
        Properties uploadProperty = new Properties();        
        try {
            InputStream in = this.getClass().getClassLoader().getResourceAsStream("mailserver.properties");
            uploadProperty.load(in);
            uploadUrl = uploadProperty.getProperty("upload_file_url");
            sizeThreshold = Float.parseFloat(uploadProperty.getProperty("attachment_size_limit"));
        } catch (IOException ex) {
            Logger.getLogger(SendMailPojo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *  It creates the instance of the SendMailPojo class taking neccessary inputs as
     *  arguments.
     *
     *  @param servletUrl is the url of the web application which supports mail sending service.
     *                    This url is given in mailServer properties file by the name "mail_server_url" given with pojo.
     *
     *  @param recipientAddress is the email-address of the mail recepient.
     *
     *  @param carbonCopyAddress is the email-address, receiving a carbon copy.
     *
     *  @param blindCarbonCopyAddress is the email-address, receiving a blind carbon copy.
     *
     *  @param subject is the subject of the mail being sent.
     *
     *  @param body is the message that the mail has to carry.
     *
     *  @param attachment is the complete filepath for the attachment to be sent.
     *
     */
    public SendMailPojo(String servletUrl,
            String recipientAddress,
            String carbonCopyAddress,
            String blindCarbonCopyAddress,
            String subject,
            String body,
            String attachment) {
        this.servletURL = servletUrl;
        this.recipientAddresses = new String[]{recipientAddress};
        this.carbonCopyAddresses = new String[]{carbonCopyAddress};
        this.blindCarbonCopyAddresses = new String[]{blindCarbonCopyAddress};
        this.subject = subject;
        this.body = body;
        if(attachment!=null){
        this.filepaths = new String[]{attachment};
        }
        Properties uploadProperty = new Properties();
        try {
            InputStream in = this.getClass().getClassLoader().getResourceAsStream("mailserver.properties");
            uploadProperty.load(in);
            uploadUrl = uploadProperty.getProperty("upload_file_url");
            sizeThreshold = Float.parseFloat(uploadProperty.getProperty("attachment_size_limit"));
        } catch (IOException ex) {
            Logger.getLogger(SendMailPojo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void uploadFilesOnServer(String filename) {
        DataOutputStream dataStream = null;
        String exsistingFileName = filename;
        String responseMessage;
        int fileId=0;

        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";

        int bytesRead, bytesAvailable;
        byte[] buffer;
        BufferedReader in=null;

        
        URLConnection connection = HttpsConnection.getConnection(uploadUrl);

        connection.setRequestProperty("Connection", "Keep-Alive");

        connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
        try {
            dataStream = new DataOutputStream(connection.getOutputStream());

            dataStream.writeBytes(twoHyphens + boundary + lineEnd);
            dataStream.writeBytes("Content-Disposition: form-data; name=\"uploadFile\";" + " filename=\"" + exsistingFileName + "\"" + lineEnd);
            dataStream.writeBytes(lineEnd);

            FileInputStream fileInputStream = new FileInputStream(new File(exsistingFileName));


            // create a buffer of maximum size

            bytesAvailable = fileInputStream.available();


            buffer = new byte[bytesAvailable];

            // read file and write it into form...

            bytesRead = fileInputStream.read(buffer, 0, bytesAvailable);

            while (bytesRead > 0) {

                dataStream.write(buffer, 0, bytesAvailable);
                bytesAvailable = fileInputStream.available();

                bytesRead = fileInputStream.read(buffer, 0, bytesAvailable);
            }

            // send multipart form data necesssary after file data...

            dataStream.writeBytes(lineEnd);
            dataStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // close streams

            fileInputStream.close();
            dataStream.flush();
            dataStream.close();
            in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            responseMessage = in.readLine();
            in.close();
            System.out.println("done!!!! "+responseMessage);
            fileId =Integer.parseInt(responseMessage.substring(responseMessage.lastIndexOf("|")+1));
            fileIdToNameMap.put(fileId,filename.substring(filename.lastIndexOf("/")+1));
            
        } catch (Exception ex) {
            System.out.println(ex + "here");
        }

    }

    /**
     * Attempts to establish a connection to the servlet specified in the
     * argument of the constructor of this class and passes the necessary
     * parameters to it. This method does not return any value but throws an
     * exception if the message sending failed.
     *
     * @throws java.net.MalformedURLException  If the URL to the e-mail sending
     * servlet specified in the constructor is a malformed URL
     * @throws com.tcs.igrid.sendmailfrompojo.exceptions.SendingFailedException
     * If the mail sending was failed due to some other reasons such as
     * the ip address of the caller is not authorized, etc...
     */

    public void sendMail() throws MalformedURLException,
            SendingFailedException,RuntimeException {

        String status = "Failed";
        ObjectOutputStream out = null;
        BufferedReader in = null;

        try {
            /*
             * Uploading files on the server.
             */
            fileIdToNameMap = new HashMap<Integer, String>();
            if (filepaths != null) {
                System.out.println("filepaths="+filepaths);
                validatingFileAttachments();
                int fileCountTrack = 0;
                while (fileCountTrack < filepaths.length) {
                    uploadFilesOnServer(filepaths[fileCountTrack]);
                    fileCountTrack++;
                }
            }

            URLConnection connection = HttpsConnection.getConnection(servletURL);
            /*
             * Set the content type of the Http request to octet-stream as
             * the e-mail message is going to be sent as an Object
             */
            connection.setRequestProperty("Content-Type",
                    "application/octet-stream");

            /*
             * Encapsulate the different components of the message in a single
             * entity called message
             */
            Message message = new Message(recipientAddresses,
                    carbonCopyAddresses,
                    blindCarbonCopyAddresses,
                    subject,
                    body,fileIdToNameMap);

            /*
             * Get an ObjectOutputStream and send the message to the output
             */
            out = new ObjectOutputStream(connection.getOutputStream());
            out.writeObject(message);
            out.flush();
            out.close();
            /*
             * Read the response from the servlet
             */
            in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            status = in.readLine();
            in.close();

        }catch(SendingFailedException exc){
            status = "Size of attachments exceeds threshold.";
        }
        catch (Exception exc) {
            Logger.getLogger(SendMailPojo.class.getName()).log(Level.SEVERE, null, exc);
        }
        finally {
            /*
             * Close any connection left open.
             */
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (Exception exc) {
            } //end catch
        } //end finally
        if (!status.trim().toLowerCase().equals("mail sent")) {
            throw new SendingFailedException(status);
        }
    } // end sendMail

    public static void main(String[] args) {

        String servletUrl = "https://172.20.50.246/MailServerWebApp03/sendmailwithattachment.do";
        String recepientAddress[] = new String[]{"shrikant.adhikarla@tcs.com"};
        String subject = "check it out!!!";
        String body = "testing!!!! \n\n";
        String filepath[] = new String[]{"C://from/sample.JPG"};
        //SendMailPojo sender = new SendMailPojo(servletUrl,recepientAddress,null,null,subject,body,filepath);
        SendMailPojo sender = new SendMailPojo(servletUrl, recepientAddress,null,null,subject,body,null);
        try {
            sender.sendMail();
            //sender.sendMail();
        } catch (MalformedURLException ex) {
            Logger.getLogger(SendMailPojo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SendingFailedException ex) {
            Logger.getLogger(SendMailPojo.class.getName()).log(Level.SEVERE, null, ex);
        }
        //SendMailPojo.uploadFilesOnServer("C://GoogleGearsSetup.exe");


    }

    private void validatingFileAttachments() throws SendingFailedException {
        File filesArray[] = new File[filepaths.length];
        float totalSize=0;
        int tracker=0;
        while(tracker<filepaths.length){
            filesArray[tracker] = new File(filepaths[tracker]);
            totalSize = totalSize + ((float)filesArray[tracker].length()/(1024*1024));
            tracker++;
        }        
        if(totalSize>sizeThreshold){
            throw new SendingFailedException("File Size Exceeds Threshold");
        }
    }
} //end class

