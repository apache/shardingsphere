package io.shardingjdbc.example.proxy;

import io.shardingjdbc.example.proxy.repository.RawJdbcRepository;
import org.apache.commons.dbcp.BasicDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;

/*
 * Please make sure start sharding-proxy before you run this example.
 */
public final class ServerMain {
    
    public static void main(String[] args) throws SQLException {
        RawJdbcRepository rawJdbcRepository = new RawJdbcRepository(createDataSource());
        rawJdbcRepository.demo();
    }
    
    private static DataSource createDataSource() {
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName(com.mysql.jdbc.Driver.class.getName());
        result.setUrl("jdbc:mysql://localhost:3307/sharding_db?useServerPrepStmts=true");
        result.setUsername("root");
        result.setPassword("");
        return result;
    }
}
