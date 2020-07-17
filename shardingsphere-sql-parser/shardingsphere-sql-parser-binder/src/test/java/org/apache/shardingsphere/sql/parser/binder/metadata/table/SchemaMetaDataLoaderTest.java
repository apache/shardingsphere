package org.apache.shardingsphere.sql.parser.binder.metadata.table;

import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaDataLoader;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.LinkedList;
import java.util.logging.Logger;

public final class SchemaMetaDataLoaderTest {
    private static int minIdleCount = 3;
    private static int maxIdleCount = 10;
    private static int currentIdleCount = 0;
    private static final LinkedList<Connection> connectionsPool = new LinkedList<Connection>();

    @Test
    public void assertloadAllTableNamesForOracle() throws SQLException {
        LiteDatasource dataSource = new LiteDatasource();
        SchemaMetaDataLoader.load(dataSource,5,"Oracle");
    }

    class LiteDatasource implements DataSource{
        private String url = "jdbc:oracle:thin:@192.168.1.1:1521/orcl";
        private String user = "test";
        private String password = "111111";
        public LiteDatasource(){
            try {
                for (int i = 0; i < SchemaMetaDataLoaderTest.maxIdleCount; i++) {
                    Class.forName("oracle.jdbc.OracleDriver");
                    Connection realConnection = DriverManager.getConnection(url, user, password);
                    SchemaMetaDataLoaderTest.connectionsPool.addLast(realConnection);
                    currentIdleCount++;
                }
            } catch (SQLException | ClassNotFoundException e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        @Override
        public Connection getConnection() throws SQLException {
            synchronized (connectionsPool) {
                try{
                    Class.forName("oracle.jdbc.OracleDriver");
                    if (currentIdleCount > 0) {
                        currentIdleCount--;
                        if (currentIdleCount < minIdleCount) {
                            Connection realConnection = DriverManager.getConnection(url, user, password);
                            SchemaMetaDataLoaderTest.connectionsPool.addLast(realConnection);
                            currentIdleCount++;
                        }
                        return SchemaMetaDataLoaderTest.connectionsPool.removeFirst();
                    }
                    Connection realConnection = DriverManager.getConnection(url, user, password);
                    return realConnection;
                }catch(Exception e){
                    e.printStackTrace();
                    return null;
                }
            }
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            return null;
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            return null;
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return false;
        }

        @Override
        public PrintWriter getLogWriter() throws SQLException {
            return null;
        }

        @Override
        public void setLogWriter(PrintWriter out) throws SQLException {

        }

        @Override
        public void setLoginTimeout(int seconds) throws SQLException {

        }

        @Override
        public int getLoginTimeout() throws SQLException {
            return 0;
        }

        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            return null;
        }
    }
}
