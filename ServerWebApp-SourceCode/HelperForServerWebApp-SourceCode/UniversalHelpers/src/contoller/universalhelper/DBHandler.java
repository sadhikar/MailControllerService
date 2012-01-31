/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package contoller.universalhelper;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The purpose of this class is to help with the execution of SQL statements.
 * It provides two methods <bold>fetch()</bold> which executes select queries 
 * and returns a ResultSet object and </bold>execute()</bold> which is to
 * execute other DML statements
 * 
 * 
 */
public class DBHandler {
    private String queryString;
    private Properties dbHandlerProperties;
    private Connection connection;

    /**
     * Creates an instance of this class, assuming the existance of a properties
     * file with the name queries.properties in the classpath
     * @param connection  A database connection
     */
    public DBHandler(Connection connection) {
        this(connection,"queries.properties");
    } //end constructor

    /**
     * Creates an instance of this class given the database connection and the
     * properties file containing the list of queries
     * @param connection  A database connection
     * @param dbHandlerPropertiesFileName  Name of the properties file containing
     * the queries.
     * @throws ExceptionInInitializerError  If there was a problem in loading the
     * properties file. 
     */
    public DBHandler(Connection connection,
                     String dbHandlerPropertiesFileName) {
        
        this.connection = connection;
        /*
         * Attempt to read the properties file.
         */
        InputStream loggerPropertiesFile = this.getClass().getClassLoader()
                .getResourceAsStream(dbHandlerPropertiesFileName);
        try {
            dbHandlerProperties = new Properties();
            dbHandlerProperties.load(loggerPropertiesFile);
            loggerPropertiesFile.close();
        } catch (Exception exc) {
            /*
             * Log attempt failure.
             */
            throw new ExceptionInInitializerError("Error occured while " +
                    "attempting to read " + dbHandlerPropertiesFileName  +
                    exc.getMessage());
        } //end try

    } //end constructor


    public String replaceSQLMetaCharacters(String inputString) {
        String replacement = inputString.replaceAll("\"", "\\\\\"");
        replacement = replacement.replaceAll("\'", "\\\\\'");
        replacement = replacement.replaceAll("--", "__");
        return replacement;
    }

//    private static Object[] replaceSQLMetaCharacters(Object... arguments) {
//        if (arguments == null) return null;
//        Object[] objArray = new Object[arguments.length];
//        int count=0;
//        for (Object obj: arguments) {
//            if (obj == null) continue;
//            String param = (String)obj;
//            String replacement = param.replaceAll("\"", "\\\\\"");
//            replacement = replacement.replaceAll("\'", "\\\\\'");
//            replacement = replacement.replaceAll("--", "__");
//            objArray[count++] = replacement;
//        }
//        return objArray;
//    }
    /**
     * Executes a DELETE, INSERT or UPDATE query and returns the status.
     * It takes the name (the key) of the query string as an argument and
     * attempts to fetch the query from the properties file. The query should
     * be formatted as per the String formatter.
     * 
     * @param queryStringNameInPropertiesFile   The name (Key) of the query
     * string as referenced in the properties file. 
     * @param queryParameters  Var-args, essentially the data that
     * corresponds to the format query string.
     * @return  <bold>true</bold> If the execution was successful
     *          <bold>false</bold> If the execution failed.
     */
    public boolean execute(String queryStringNameInPropertiesFile,
                           Object... queryParameters) {
/*        Object[] replacedMetaQueryParams = queryParameters;
        if (checkAndReplaceMetaChar) {
             replacedMetaQueryParams =
                     replaceSQLMetaCharacters(queryParameters);
        }*/
        try {
            queryString = dbHandlerProperties.getProperty(
                                              queryStringNameInPropertiesFile);
//            if (replacedMetaQueryParams != null )
                queryString = String.format(queryString,queryParameters);
//                replacedMetaQueryParams = null;
            PreparedStatement statement = null;
            statement = this.connection.prepareStatement(queryString);
            int updateCount = statement.executeUpdate(queryString);
            statement = null;
            return (updateCount > 0);
        } catch (Exception exc) {
            return false;
        } //end try
    } //end execute()

    /**
     * Returns a java.sql.ResultSet object after executing the query present
     * in the properties file, referenced by the name (key) in the argument.
     *
     * @param queryStringNameInPropertiesFile  The name (the key) by which the
     * select query to be executed is referenced in the property file.
     * @return  A <bold>ResultSet</bold> object when the execution was
     * successful
     *          <bold>null</bold> if the execution failed or when no matching
     * result was found.
     */
    public ResultSet fetch(String queryStringNameInPropertiesFile,
                           Object... queryParameters) {
/*        Object[] replacedMetaQueryParams =
                replaceSQLMetaCharacters(queryParameters);*/
        try {
            queryString = dbHandlerProperties.getProperty(
                                              queryStringNameInPropertiesFile);
                queryString = String.format(queryString, queryParameters);
            PreparedStatement statement = null;
            statement = this.connection.prepareStatement(queryString);
            return statement.executeQuery();
        } catch (Exception exc) {
            return null;
        } //end try
    } //end fetch()

    public ResultSet executeAndGetKeys(String queryStringNameInPropertiesFile,
                                       Object... queryParameters){
        PreparedStatement statement = null;
        try{
            queryString = dbHandlerProperties.getProperty(
                                               queryStringNameInPropertiesFile);
            queryString = String.format(queryString, queryParameters);
            
            statement = this.connection.prepareStatement(queryString,Statement.RETURN_GENERATED_KEYS);
            int updateCount = statement.executeUpdate(queryString);            
            if(updateCount > 0)
              return statement.getGeneratedKeys();
            else
              throw new RuntimeException("Insertion Failed.");            
        }
        catch(Exception exception){
            System.out.println(exception);
            return null;
        }
        finally{
            statement = null;
        }
    }

    public static void main(String[] args) {
        DBHandler db =null;
        try {
            System.out.println(DBConnectionManager.getConnection());
        } catch (SQLException ex) {
            Logger.getLogger(DBHandler.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
    

}
