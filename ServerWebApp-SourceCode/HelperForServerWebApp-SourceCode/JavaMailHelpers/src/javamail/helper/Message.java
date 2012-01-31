/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package javamail.helper;

import java.io.Serializable;
import java.util.HashMap;

/**
 * The purpose of this class is to encapsulate the different components of an
 * e-mail message in order to simply the process of sending it to the server.
 * This class implements Serializable as it will be serialized over the
 * network
 *
 * 
 */
public class Message implements Serializable {
    private String[] recipientAddresses;
    private String[] carbonCopyAddresses;
    private String[] blindCarbonCopyAddresses;
    private String subject;
    private String body;
    private HashMap<Integer,String> fileIdToNameMap;


    /**
     * Creates an instance of this class.
     * @param recipientAddress  An array of String containing the e-mail
     * addresses of the intended recipients
     * @param carbonCopyAddresses  An array of String containing the list of
     * addresses to which this e-mail is to be carbon copied
     * @param blindCarbonCopyAddresses  An array of String containing the list
     * of addresses to which this e-mail is to be blind carbon copied
     * @param subject  The subject of the e-mail
     * @param body  The body of the e-mail
     */
    public Message( String[] recipientAddress,
                    String[] carbonCopyAddresses,
                    String[] blindCarbonCopyAddresses,
                    String subject,
                    String body,HashMap<Integer,String> fileIdToNameMap) {
        this.recipientAddresses = recipientAddress;
        this.carbonCopyAddresses = carbonCopyAddresses;
        this.blindCarbonCopyAddresses = blindCarbonCopyAddresses;
        this.subject = subject;
        this.body = body;
        this.fileIdToNameMap = fileIdToNameMap;
    }

    /**
     *
     * @return  String - The body of the e-mail
     */
    public String getBody() {
        return body;
    }

    /**
     *
     * @return  String - The subject of the e-mail
     */
    public String getSubject() {
        return subject;
    }

    /**
     *
     * @return  An array of String containing the e-mail Addresses of the
     * intended recipient.
     */
    public String[] getRecipients() {
        return recipientAddresses;
    }

    /**
     *
     * @return  An array of String containing the list of addresses to which
     * this e-mail is to be carbon copied
     */
    public String[] getCarbonCopyAddresses() {
        return carbonCopyAddresses;
    }

    /**
     *
     * @return  An array of String containing the list of addresses to which
     * this e-mail is to be blind carbon copied
     */
    public String[] getBlindCarbonCopyAddresses() {
        return blindCarbonCopyAddresses;
    }

    public HashMap<Integer, String> getFileIdToNameMap() {
        return fileIdToNameMap;
    }

}
