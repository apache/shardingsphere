package org.apache.shardingsphere.shardingjdbc.jdbc.refreh;

import org.apache.shardingsphere.shardingjdbc.jdbc.core.context.ShardingRuntimeContext;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.AlterTableStatement;

import java.sql.SQLException;

/**
 * The type Alter table statement meta data refresh.
 */
public final class AlterTableStatementMetaDataRefresh extends AbstractTableStatementMetaData implements SQLStatementMetaDataRefresh<AlterTableStatement> {
    
    @Override
    public void refreshMetaData(final ShardingRuntimeContext shardingRuntimeContext, final SQLStatementContext<AlterTableStatement> sqlStatementContext) throws SQLException {
        String tableName = sqlStatementContext.getSqlStatement().getTable().getTableName().getIdentifier().getValue();
        shardingRuntimeContext.getMetaData().getSchema().put(tableName, loadTableMeta(tableName, shardingRuntimeContext));
    }
}
