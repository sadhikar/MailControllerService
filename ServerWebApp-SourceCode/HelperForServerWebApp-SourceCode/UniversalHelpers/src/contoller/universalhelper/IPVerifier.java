/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package contoller.universalhelper;

import java.sql.Connection;
import java.sql.ResultSet;


public class IPVerifier {
    
    private Connection dbConnection;
    private boolean ipAllowed=false;

    /**
     * Creates an instance of this object. Takes a Connection to a database
     * where the table containing the list of allowed IP addresses is stored,
     * and a query , idenfified by the name it's referenced in the properties
     * file , which is used by the class DBHandler.
     * @param dbConnection  A connection to the database where the table
     * containing the list of allowed ip addresses is present
     * @param verifQueryNameInPropFile  The name (the key) by which the query
     * to fetch the list of allowed ip address is referenced in the properties
     * file
     * @param ipAddress  The IP address of the requester
     * @throws ExceptionInInitializerError  When the ResultSet as a result of
     * executing the above mentioned query is empty, or when the constructor
     * of DBHandler used inside throws this Error
     */
    public IPVerifier(Connection dbConnection,
                      String verifQueryNameInPropFile, String ipAddress) {
        try {
            this.dbConnection = dbConnection;
            /*
             * Create an instance of the DBHandler object
             */
            DBHandler dbHandler = new DBHandler(dbConnection);
            /*
             * Call the fetch method of the DBHandler by passing the name of
             * the query , which fetches the list of allowed IP addresses from
             * the database.
             */
            ResultSet resultSet =
                    dbHandler.fetch(verifQueryNameInPropFile,ipAddress);
            /*
             * If ResultSet is empty throw an exception.
             */

            if ( resultSet == null ) {
                ipAllowed = false;
            } else {
                resultSet.next();
                if ( resultSet.getInt(1) == 0 ) {
                    ipAllowed = false;
                } else {
                    ipAllowed = true;
                }
            }

        } catch (Exception exc) {
            throw new ExceptionInInitializerError("An error occured " +
                    exc.getMessage());
        }

    }

    public boolean isIPAllowed() {
        return ipAllowed;
    }

}
