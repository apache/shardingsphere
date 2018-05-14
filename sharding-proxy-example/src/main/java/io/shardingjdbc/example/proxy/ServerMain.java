package io.shardingjdbc.example.proxy;

import io.shardingjdbc.example.proxy.repository.RawJdbcRepository;
import org.apache.commons.dbcp.BasicDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;

/*
 * Please make sure sharding-proxy is running before you run this example.
 */
public final class ServerMain {
    
    private static final String PROXY_IP = "localhost";
    
    private static final int PROXY_PORT = 3307;
    
    public static void main(String[] args) throws SQLException {
        RawJdbcRepository rawJdbcRepository = new RawJdbcRepository(createDataSource());
        rawJdbcRepository.demo();
    }
    
    private static DataSource createDataSource() {
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName(com.mysql.jdbc.Driver.class.getName());
        result.setUrl(String.format("jdbc:mysql://%s:%d/sharding_db?useServerPrepStmts=true&cachePrepStmts=true", PROXY_IP, PROXY_PORT));
        result.setUsername("root");
        result.setPassword("");
        return result;
    }
}
