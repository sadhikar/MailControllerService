package contoller.universalhelper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class DBConnectionManager {

    private static final Log LOG = LogFactory.getLog(DBConnectionManager.class);

    public static void init() {
        final ConnectionManager connectionManager = new ConnectionManager();      
    }    

    public static Connection getConnection() throws SQLException {
        if (!ConnectionManager.isConfigured) {
            init();
        }
        return ConnectionManager.DS.get("emaildb").getConnection();
    }

    public static void closeResultSet(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException ex) {
            LOG.error(ex);
        }
    }

    public static void closeStatement(Statement stmt) {
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException ex) {
            LOG.error(ex);
        }
    }

    public static void closeConnection(Connection connection) {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException ex) {
            LOG.error(ex);
        }
    }
//    public static void main(String[] args) {
//        try {
//            System.out.println(DBConnectionManager.getSSOConnection());
//        } catch (SQLException ex) {
//            Logger.getLogger(DBConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
}
