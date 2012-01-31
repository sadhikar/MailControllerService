package contoller.universalhelper;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.dbcp.*;
import javax.sql.DataSource;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;


public class ConnectionManager {

    private static final String CONFIG_FILENAME = "db_config.xml";
    private static final Log LOG = LogFactory.getLog(ConnectionManager.class);
//    public static DataSource ds = null;
    public static Hashtable<String, DataSource> DS = new Hashtable<String, DataSource>();
    private List<Configuration> configurationBeans = new ArrayList<Configuration>();
//    private static GenericObjectPool _pool = null;
    public static Hashtable<String, GenericObjectPool> POOL = new Hashtable<String, GenericObjectPool>();
    public static boolean isConfigured = false;

    public ConnectionManager() {
        if (!isConfigured) {
            try {
                setDBConfiguration();
                for (Configuration config : configurationBeans) {
                    connectToDB(config);
                }
                isConfigured = true;
            } catch (IOException ex) {
                LOG.error("Exception :" + ex.getMessage());
            } catch (SAXException ex) {
                LOG.error("Exception :" + ex.getMessage());
            }
        }
    }

    /**
     *  @param config configuration from an XML file.
     */
//    public ConnectionManager(Configuration config) {
//        try {
//            connectToDB(config);
//        } catch (Exception e) {
//            LOG.error("Failed to construct ConnectionManager", e);
//
//        }
//    }
    /**
     *  destructor
     */
    @Override
    protected void finalize() {
        LOG.debug("Finalizing ConnectionManager");
        try {
            super.finalize();
        } catch (Throwable ex) {
            LOG.error("ConnectionManager finalize failed to disconnect from mysql : ", ex);
        }
    }

    /**
     *  connectToDB - Connect to the MySql DB!
     */
    private void connectToDB(Configuration config) {

        try {
            java.lang.Class.forName(config.getDriver()).newInstance();
        } catch (Exception e) {
            LOG.error("Error when attempting to obtain DB Driver: "
                    + config.getDriver() + " on " + new Date().toString(), e);
        }

        LOG.debug("Trying to connect to database...");
        try {
            ConnectionManager.DS.put(config.getName(), setupDataSource(
                    config.getName(),
                    config.getUri(),
                    config.getUser(),
                    config.getPassword(),
                    config.getPoolMinSize(),
                    config.getPoolMaxSize()));

//                    ConnectionManager.ds = setupDataSource(
//                    config.getUri(),
//                    config.getUser(),
//                    config.getPassword(),
//                    config.getPoolMinSize(),
//                    config.getPoolMaxSize());

            LOG.debug("Connection attempt to database succeeded.");
        } catch (Exception e) {
            LOG.error("Error when attempting to connect to DB ", e);
        }
    }

    /**
     *
     * @param connectURI - JDBC Connection URI
     * @param username - JDBC Connection username
     * @param password - JDBC Connection password
     * @param minIdle - Minimum number of idel connection in the connection pool
     * @param maxActive - Connection Pool Maximum Capacity (Size)
     * @throws Exception
     */
    public static DataSource setupDataSource(String name,
            String connectURI,
            String username,
            String password,
            int minIdle, int maxActive) throws Exception {
        //
        // First, we'll need a ObjectPool that serves as the
        // actual pool of connections.
        //
        // We'll use a GenericObjectPool instance, although
        // any ObjectPool implementation will suffice.
        //
        GenericObjectPool connectionPool = new GenericObjectPool(null);

        connectionPool.setMinIdle(minIdle);
        connectionPool.setMaxActive(maxActive);
        //CHECKING FOR STALE CONNECTIONS ON BORROW ONLY
        //validationQuery = "SELECT 1"
        //testOnBorrow = true
        //testOnReturn = true
        //testWhileIdle = true
        //timeBetweenEvictionRunsMillis=1000 * 60 * 15
        //numTestsPerEvictionRun=3
        //minEvictableIdleTimeMillis=1000 * 60 * 30
        connectionPool.setTestOnBorrow(true);
        connectionPool.setTimeBetweenEvictionRunsMillis(90000);
        connectionPool.setNumTestsPerEvictionRun(3);
        connectionPool.setMinEvictableIdleTimeMillis(90000);
        String validationQuery = "select 1";

        ConnectionManager.POOL.put(name, connectionPool);
        //ConnectionManager._pool = connectionPool;
        // we keep it for two reasons
        // #1 We need it for statistics/debugging
        // #2 PoolingDataSource does not have getPool()
        // method, for some obscure, weird reason.

        //
        // Next, we'll create a ConnectionFactory that the
        // pool will use to create Connections.
        // We'll use the DriverManagerConnectionFactory,
        // using the connect string from configuration
        //
        ConnectionFactory connectionFactory =
                new DriverManagerConnectionFactory(connectURI, username, password);

        //
        // Now we'll create the PoolableConnectionFactory, which wraps
        // the "real" Connections created by the ConnectionFactory with
        // the classes that implement the pooling functionality.
        //
        // You need the PoolableConnectionFactory as it will become the factory for all of the objects created
        // by the object pool. In the constructor of the PoolableConnectionfactory
        // the PoolableConnectionfactory instance sets the factory on the pool object passed in the constructor
        // ~stephen : can also be used as  new PoolableConnectionFactory( connectionFactory, connectionPool, null, null, false, true);
        // http://www.freshblurbs.com/jakarta-commons-dbcp-tutorial
        PoolableConnectionFactory poolableConnectionFactory =
                new PoolableConnectionFactory(
                connectionFactory, connectionPool, null, validationQuery, false, true);

        PoolingDataSource dataSource =
                new PoolingDataSource(connectionPool);

        return dataSource;
    }

    public static void printDriverStats(String dbname) throws Exception {
//        ObjectPool connectionPool = ConnectionManager._pool;
        ObjectPool connectionPool = ConnectionManager.POOL.get(dbname);
        LOG.info("NumActive: " + connectionPool.getNumActive());
        LOG.info("NumIdle: " + connectionPool.getNumIdle());
    }

    /**
     *  getNumLockedProcesses - gets the
     *  number of currently locked processes on the MySQL db
     *
     *  @return Number of locked processes
     */
    public int getNumLockedProcesses(String dbname) {
        int num_locked_connections = 0;
        Connection con = null;
        PreparedStatement p_stmt = null;
        ResultSet rs = null;
        try {
            //con = ConnectionManager.ds.getConnection();
            con = ConnectionManager.DS.get(dbname).getConnection();
            p_stmt = con.prepareStatement("SHOW PROCESSLIST");
            rs = p_stmt.executeQuery();
            while (rs.next()) {
                if (rs.getString("State")
                        != null && rs.getString("State").equals("Locked")) {
                    num_locked_connections++;
                }
            }
        } catch (Exception e) {
            LOG.debug("Failed to get get Locked Connections - Exception: " + e.toString());
        } finally {
            try {
                rs.close();
                p_stmt.close();
                con.close();
            } catch (java.sql.SQLException ex) {
                LOG.error(ex.toString());
            }
        }
        return num_locked_connections;
    }

    public void setDBConfiguration() throws IOException, SAXException {
        Digester digester = new Digester();

        digester.push(this);

        digester.addCallMethod("datasources/datasource", "addDataSource", 7);
        digester.addCallParam("datasources/datasource/name", 0);
        digester.addCallParam("datasources/datasource/driver", 1);
        digester.addCallParam("datasources/datasource/uri", 2);
        digester.addCallParam("datasources/datasource/username", 3);
        digester.addCallParam("datasources/datasource/password", 4);
        digester.addCallParam("datasources/datasource/poolMinSize", 5);
        digester.addCallParam("datasources/datasource/poolMaxSize", 6);

        InputStream is = ConnectionManager.class.getClassLoader().getResourceAsStream(CONFIG_FILENAME);
        digester.parse(is);
    }

    @SuppressWarnings("unchecked")
    public void addDataSource(String name,
            String driver,
            String uri,
            String username,
            String password,
            String poolMinSize,
            String poolMaxSize) {

        Configuration conf = new Configuration();
        conf.setName(name);
        conf.setDriver(driver);
        conf.setUri(uri);
        conf.setUser(username);
        conf.setPassword(password);
        conf.setPoolMinSize(Integer.valueOf(poolMinSize));
        conf.setPoolMaxSize(Integer.valueOf(poolMaxSize));

        configurationBeans.add(conf);
        LOG.info("DataSource added: " + conf.toString());
    }
//    public static void main(String[] args) throws IOException, SAXException, SQLException {
//        ConnectionManager manager = new ConnectionManager();
//        Connection connection = ConnectionManager.DS.get(DBEnums.REGISTER.toString()).getConnection();
//        PreparedStatement ps = connection.prepareStatement("select * from user_details");
//        ResultSet rs = ps.executeQuery();
//        while (rs.next()) {
//            System.out.println(rs.getString(2));
//        }
//        rs.close();
//        ps.close();
//        connection.close();
//    }
}
