package org.apache.shardingsphere.shardingjdbc.jdbc.refreh;

import org.apache.shardingsphere.shardingjdbc.jdbc.core.context.ShardingRuntimeContext;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;

import java.sql.SQLException;

/**
 * The interface SQL statement meta data refresh.
 *
 * @param <T> the type parameter
 */
public interface SQLStatementMetaDataRefresh<T extends SQLStatement> {
    
    /**
     * Refresh meta data.
     *
     * @param shardingRuntimeContext the sharding runtime context
     * @param sqlStatementContext       the sql statement context
     * @throws SQLException the sql exception
     */
    void refreshMetaData(ShardingRuntimeContext shardingRuntimeContext, SQLStatementContext<T> sqlStatementContext) throws SQLException;
}
