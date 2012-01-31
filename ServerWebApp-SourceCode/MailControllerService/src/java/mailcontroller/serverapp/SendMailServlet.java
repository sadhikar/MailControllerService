/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mailcontroller.serverapp;

import com.tcs.igrid.javamail.helper.Message;
import com.tcs.igrid.javamail.helper.SendMail;
import com.tcs.igrid.universal.helper.DBConnectionManager;
import com.tcs.igrid.universal.helper.DBHandler;
import com.tcs.igrid.universal.helper.IPVerifier;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class SendMailServlet extends HttpServlet {

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {


        Connection connection = null;
        ObjectInputStream input = null;
        String statusMessage = "Mail sending failed.";
        String senderIdentifier = request.getParameter("identifier");
        String sender = null;
        SendMailServlet object = new SendMailServlet();

        try {

            input = new ObjectInputStream(request.getInputStream());
            /* Read the Message object passed by the caller to the
             * InputStream
             */
            Message message = (Message) input.readObject();

            /*
             * Get the mail id of the sender reading from "mailnameownership.properties"
             *
             */

            sender = object.getMailSender(senderIdentifier);

            /*
             * Get the other encapsulated information contained in the Message
             * object
             */
            String[] recipientAddresses = message.getRecipients();
            String[] carbonCopyAddresses = message.getCarbonCopyAddresses();
            String[] blindCarbonCopyAddresses =
                    message.getBlindCarbonCopyAddresses();
            String subject = message.getSubject();
            String bodyOfMessage = message.getBody();

            /*
             * Create a String representation of the array of e-mail IDs
             */
            String toList = "";
            for (String address : recipientAddresses) {
                toList += address + " ";
            }

            /*
             * Create a String representation of the array of e-mail IDs
             */
            String ccList = "";
            if (carbonCopyAddresses != null) {
                for (String address : carbonCopyAddresses) {
                    ccList += address + " ";
                }
            }

            /*
             * Create a String representation of the array of e-mail IDs
             */
            String bccList = "";
            if (blindCarbonCopyAddresses != null) {
                for (String address : blindCarbonCopyAddresses) {
                    bccList += address + " ";
                }
            }

            /* Init-param mailserver_properties_file holds the name of the
             * properties file which in turn contains information about the
             * mail server.
             */
            String mailServerPropertiesFileName =
                    getInitParameter("mailserver_properties_filename");
            /* Init-param dbhandler_properties_filename holds the name of the
             * properties file which contains the list of queries that
             * the dbHandler would execute
             */
            String dbHandlerPropertiesFileName =
                    getInitParameter("dbhandler_properties_filename");
            /*
             * Specifies the name (the key), by which the query used to log the
             * mail sending attempt, is referenced  in the properties file
             * */
            String loggerExecuteQueryName =
                    getInitParameter("logger_execute_query_name");
            /*
             * Specifies the name (the key), by which the query that's used to
             * fetch the list of allowed ip addresses, is referenced in the
             * properties file
             * */
            String ipVerificationQueryName =
                    getInitParameter("ip_verification_query_name");

            /*
             * Create a new database connection. By default, the class
             * DBConnectionManager will attempt to find information from
             * the properties file sqlconnection.properties
             */
            connection = DBConnectionManager.getConnection();

            /*
             * Get the ip address of the machine from which this servlet is
             * being called. This is the ip address of the requester.
             */
            String requesterAddress = request.getRemoteAddr();

            /*
             * Create a new instance of IPVerifier with the database
             * connection created earlier and the name by which the ip
             * address fetch query is referenced in the properties file
             */
            IPVerifier verifier = new IPVerifier(connection,
                    ipVerificationQueryName,
                    requesterAddress);

            /*
             * check if the requester's ip is in the list of allowed ip
             * addresses
             */
            boolean senderAllowed = verifier.isIPAllowed();
            /*
             * create an instance of DBHandler by passing the database
             * connection obtained earlier along with the name of the
             * properties file, which containes a list of queries
             */

            DBHandler dbHandler = new DBHandler(connection,
                    dbHandlerPropertiesFileName);
            /*
             * Attempt to log the mail sending attempt in the database. The
             * query to be executed is referenced by the key name of the query
             * which is present in the properties file.
             */
            boolean logged = dbHandler.execute(loggerExecuteQueryName,
                    toList,
                    ccList,
                    bccList,
                    subject,
                    bodyOfMessage,
                    requesterAddress,
                    ((senderAllowed == true)
                    ? "Success"
                    : "Failed"));
            /*
             * Send the e-mail if the requester's ip is in the list of allowed
             * IPs
             */
            if (senderAllowed) {
                /*
                 * Kick start the thread which sends the e-mail.
                 */
                new SendMail(mailServerPropertiesFileName,
                        sender,
                        recipientAddresses,
                        carbonCopyAddresses,
                        blindCarbonCopyAddresses,
                        subject,
                        bodyOfMessage).start();
                /*
                 * Update the status accordingly
                 */
                statusMessage = "Mail sent";
            }

        } catch (Exception exc) {
            System.out.println(exc);
            /*
             * Log if any exception has occured
             */
        } finally {

            /*
             * Close any open stream and database connections.
             */
            try {
                if (input != null) {
                    input.close();
                }
            } catch (Exception exc) {
            }

            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception e) {
            }
        }

        /*
         * Write back the response to the requester
         */
        PrintWriter output = null;
        try {
            output = response.getWriter();
            output.println(statusMessage);
            output.close();
        } catch (Exception exc) {
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
            } catch (Exception exc) {
            }
        }

    }

    private String getMailSender(String senderIdentifier) {
        String sender = null;
        try {
            Properties prop = new Properties();
            InputStream in = this.getClass().getClassLoader().getResourceAsStream("mailnameownership.properties");
            prop.load(in);
            if (senderIdentifier == null) {
                sender = prop.getProperty("default");
            } else {
                sender = prop.getProperty(senderIdentifier, prop.getProperty("default"));
            }
        } catch (IOException ex) {
            Logger.getLogger(SendMailServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sender;
    }
}
