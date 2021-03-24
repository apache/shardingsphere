package org.apache.shardingsphere.infra.executor.exec;

import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.ExecutorJDBCManager;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExecutorJDBCManagerImpl implements ExecutorJDBCManager {
    
    private Map<String, DataSource> dataSourceMap;
    
    public ExecutorJDBCManagerImpl(Map<String, DataSource> dataSourceMap) {
        this.dataSourceMap = dataSourceMap;
    }
    
    @Override
    public List<Connection> getConnections(final String dataSourceName, final int connectionSize, final ConnectionMode connectionMode) throws SQLException {
        DataSource dataSource = dataSourceMap.get(dataSourceName);
        List<Connection> connections = new ArrayList<>();
        for(int i = 0; i < connectionSize; i++) {
            connections.add(dataSource.getConnection());
        }
        return connections;
    }
    
    @Override
    public Statement createStorageResource(final Connection connection, final ConnectionMode connectionMode, final StatementOption option) throws SQLException {
        return connection.createStatement(option.getResultSetType(), option.getResultSetConcurrency(), option.getResultSetHoldability());
    }
    
    @Override
    public PreparedStatement createStorageResource(final String sql, final List<Object> parameters, final Connection connection, final ConnectionMode connectionMode, final StatementOption option) throws SQLException {
        return option.isReturnGeneratedKeys() ? connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
                : connection.prepareStatement(sql, option.getResultSetType(), option.getResultSetConcurrency(), option.getResultSetHoldability());
    }
}
