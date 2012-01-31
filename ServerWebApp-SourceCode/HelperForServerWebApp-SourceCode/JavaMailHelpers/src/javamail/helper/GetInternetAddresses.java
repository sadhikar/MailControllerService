/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package javamail.helper;

import java.util.ArrayList;
import javax.mail.internet.InternetAddress;

/**
 * The purpose of this class is to get a valid list of InternetAddress(es) given
 * an array of String containing e-mail addresses/
 * 
 */
public class GetInternetAddresses {
    /*
     * This would store the list of valid e-mail addresses
     */
    private ArrayList<InternetAddress> internetAddresses = 
            new ArrayList<InternetAddress>();
    /**
     * Creates an instance of this class.
     * @param addresses  An array of String containing the e-mail addresses
     */
    public GetInternetAddresses(String[] addresses) {
        /*
         * for each element in the string array address[], try to get it as
         * an InternetAddress. If the address is invalid, proceed with the next
         * address in the array
         */
        for (String address: addresses) {
            try {
                InternetAddress internetAddress =
                        new InternetAddress(address);
                internetAddresses.add(internetAddress);
            } catch (Exception exc) {
            }
        }
    }

    /**
     *
     * @return  An array of type InternetAddress containing only the list
     * of valid e-mail IDs from the String[] array passed on to the constructor
     * of this class.
     */
    public InternetAddress[] getAddresses() {

        InternetAddress[] internetAddressesArray =
                new InternetAddress[internetAddresses.size()];
        int count = 0;
        for (InternetAddress address : internetAddresses) {
            internetAddressesArray[count++] = address;
        }
        return internetAddressesArray;
        //return (InternetAddress[])internetAddresses.toArray();
    }

}
