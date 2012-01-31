/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package client.sendmailfrompojo.exceptions;

/**
 * This exception is thrown from the SendMailPojo's sendMail method, if the
 * mail sending was failed due to some reasons
 * 
 * 
 */
public class SendingFailedException extends Exception {
    /**
     * @param message  The reason as to why the mail was not sent.
     */
    public SendingFailedException(String message) {
        super (message);
    }
}
