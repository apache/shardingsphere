package org.apache.shardingsphere.shardingjdbc.jdbc.refreh;

import org.apache.shardingsphere.shardingjdbc.jdbc.core.context.ShardingRuntimeContext;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.DropTableStatement;

/**
 * The type Drop table statement meta data refresh.
 */
public final class DropTableStatementMetaDataRefresh implements SQLStatementMetaDataRefresh<DropTableStatement> {
    
    @Override
    public void refreshMetaData(final ShardingRuntimeContext shardingRuntimeContext, final SQLStatementContext<DropTableStatement> sqlStatementContext) {
        for (SimpleTableSegment each : sqlStatementContext.getSqlStatement().getTables()) {
            shardingRuntimeContext.getMetaData().getSchema().remove(each.getTableName().getIdentifier().getValue());
        }
    }
    
}
