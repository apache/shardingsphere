package org.apache.shardingsphere.traffic.executor;

import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Traffic executor.
 */
public interface TrafficExecutor extends AutoCloseable {
    
    /**
     * Execute query.
     * 
     * @param logicSQL logic SQL
     * @param dataSourceConfig dataSource config
     * @return result set
     * @throws SQLException SQL exception
     */
    ResultSet executeQuery(LogicSQL logicSQL, DataSourceConfiguration dataSourceConfig) throws SQLException;
    
    /**
     * Execute update.
     * 
     * @param logicSQL logic SQL
     * @param dataSourceConfig dataSource config
     * @return update count
     * @throws SQLException SQL exception
     */
    int executeUpdate(LogicSQL logicSQL, DataSourceConfiguration dataSourceConfig) throws SQLException;
    
    /**
     * Execute.
     * 
     * @param logicSQL logic SQL
     * @param dataSourceConfig dataSource config
     * @return whether execute success or not
     */
    boolean execute(LogicSQL logicSQL, DataSourceConfiguration dataSourceConfig);
    
    /**
     * Get result set.
     *
     * @return result set
     * @throws SQLException SQL exception
     */
    ResultSet getResultSet() throws SQLException;
    
    @Override
    void close() throws SQLException;
}
