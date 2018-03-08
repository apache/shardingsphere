package io.shardingjdbc.example.server;

import io.shardingjdbc.example.server.repository.RawJdbcRepository;
import io.shardingjdbc.server.ShardingJDBCServer;
import org.apache.commons.dbcp.BasicDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;

public final class ServerMain {
    
    public static void main(String[] args) throws InterruptedException, SQLException {
//        startServer();
        RawJdbcRepository rawJdbcRepository = new RawJdbcRepository(createDataSource());
        rawJdbcRepository.demo();
    }
    
    private static void startServer() throws InterruptedException {
        new Thread() {
            
            @Override
            public void run() {
                ShardingJDBCServer server = new ShardingJDBCServer();
                try {
                    server.start(3307);
                } catch (final InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }.start();
        Thread.sleep(1000L);
    }
    
    public static DataSource createDataSource() {
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName(com.mysql.jdbc.Driver.class.getName());
        result.setUrl("jdbc:mysql://localhost:3307/demo_ds");
        result.setUsername("root");
        result.setPassword("");
        return result;
    }
}
