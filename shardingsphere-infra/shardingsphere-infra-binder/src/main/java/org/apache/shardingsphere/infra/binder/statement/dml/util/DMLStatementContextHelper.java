package org.apache.shardingsphere.infra.binder.statement.dml.util;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.CallStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.DeleteStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.database.DefaultSchema;

/**
 * DML statement context helper.
 */
public final class DMLStatementContextHelper {
    
    /**
     * Get schema name from DML statement context.
     * 
     * @param sqlStatementContext SQLStatementContext
     * @return schema name.
     */
    public static String getSchemaName(final SQLStatementContext sqlStatementContext) {
        if (sqlStatementContext instanceof CallStatementContext) {
            return ((CallStatementContext) sqlStatementContext).getSchemaName();
        }
        if (sqlStatementContext instanceof DeleteStatementContext) {
            return ((DeleteStatementContext) sqlStatementContext).getSchemaName();
        }
        if (sqlStatementContext instanceof InsertStatementContext) {
            return ((InsertStatementContext) sqlStatementContext).getSchemaName();
        }
        if (sqlStatementContext instanceof SelectStatementContext) {
            return ((SelectStatementContext) sqlStatementContext).getSchemaName();
        }
        if (sqlStatementContext instanceof UpdateStatementContext) {
            return ((UpdateStatementContext) sqlStatementContext).getSchemaName();
        }
        return DefaultSchema.LOGIC_NAME;
    }
}
