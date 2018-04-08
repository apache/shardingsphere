package io.shardingjdbc.example.proxy;

import io.shardingjdbc.example.proxy.repository.RawJdbcRepository;
import io.shardingjdbc.proxy.frontend.ShardingProxy;
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
                ShardingProxy server = new ShardingProxy();
                try {
                    server.start(3307);
                } catch (final InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }.start();
        Thread.sleep(1000L);
    }
    
    private static DataSource createDataSource() {
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName(com.mysql.jdbc.Driver.class.getName());
        result.setUrl("jdbc:mysql://localhost:3307/demo_ds");
//        result.setUrl("jdbc:mysql://localhost:3307/demo_ds?useServerPrepStmts=true");
        result.setUsername("root");
        result.setPassword("");
        return result;
    }
}
