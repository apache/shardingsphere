package org.apache.shardingsphere.shardingjdbc.jdbc.refreh;

import org.apache.shardingsphere.shardingjdbc.jdbc.core.context.ShardingRuntimeContext;
import org.apache.shardingsphere.sql.parser.binder.metadata.index.IndexMetaData;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.CreateIndexStatement;

/**
 * The type Create index statement meta data refresh.
 */
public final class CreateIndexStatementMetaDataRefresh implements SQLStatementMetaDataRefresh<CreateIndexStatement> {
   
    @Override
    public void refreshMetaData(final ShardingRuntimeContext shardingRuntimeContext, final SQLStatementContext<CreateIndexStatement> sqlStatementContext) {
        final CreateIndexStatement createIndexStatement = sqlStatementContext.getSqlStatement();
        if (null == createIndexStatement.getIndex()) {
            return;
        }
        String indexName = createIndexStatement.getIndex().getIdentifier().getValue();
        shardingRuntimeContext.getMetaData().getSchema().get(createIndexStatement.getTable().getTableName().getIdentifier().getValue()).getIndexes().put(indexName, new IndexMetaData(indexName));
    }
}
