package org.apache.shardingsphere.shardingjdbc.jdbc.refreh;

import org.apache.shardingsphere.shardingjdbc.jdbc.core.context.ShardingRuntimeContext;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetaData;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.DropIndexStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The type Drop index statement meta data refresh.
 */
public final class DropIndexStatementMetaDataRefresh implements SQLStatementMetaDataRefresh<DropIndexStatement> {
   
    @Override
    public void refreshMetaData(final ShardingRuntimeContext shardingRuntimeContext, final SQLStatementContext<DropIndexStatement> sqlStatementContext) {
        final DropIndexStatement dropIndexStatement = sqlStatementContext.getSqlStatement();
        Collection<String> indexNames = getIndexNames(dropIndexStatement);
        TableMetaData tableMetaData = shardingRuntimeContext.getMetaData().getSchema().get(dropIndexStatement.getTable().getTableName().getIdentifier().getValue());
        if (null != dropIndexStatement.getTable()) {
            for (String each : indexNames) {
                tableMetaData.getIndexes().remove(each);
            }
        }
        for (String each : indexNames) {
            if (findLogicTableName(shardingRuntimeContext.getMetaData().getSchema(), each).isPresent()) {
                tableMetaData.getIndexes().remove(each);
            }
        }
    }
    
    private Collection<String> getIndexNames(final DropIndexStatement dropIndexStatement) {
        return dropIndexStatement.getIndexes().stream().map(each -> each.getIdentifier().getValue()).collect(Collectors.toCollection(LinkedList::new));
    }
    
    private Optional<String> findLogicTableName(final SchemaMetaData schemaMetaData, final String logicIndexName) {
        return schemaMetaData.getAllTableNames().stream().filter(each -> schemaMetaData.get(each).getIndexes().containsKey(logicIndexName)).findFirst();
    }
}
