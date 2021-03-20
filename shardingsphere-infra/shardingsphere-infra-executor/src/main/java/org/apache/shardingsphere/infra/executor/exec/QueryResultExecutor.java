package org.apache.shardingsphere.infra.executor.exec;

import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.executor.exec.meta.Row;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;

import java.sql.SQLException;

/**
 * Wrapper <code>QueryResult</code> as <code>Executor</code>.
 */
public class QueryResultExecutor extends AbstractExecutor {
    
    private QueryResult queryResult;
    
    public QueryResultExecutor(final QueryResult queryResult, final ExecContext execContext) {
        super(execContext);
        this.queryResult = queryResult;
    }
    
    @Override
    protected void executeInit() {
        
    }
    
    @Override
    public boolean executeMove() {
        try {
            return queryResult.next();
        } catch (SQLException sqlException) {
            throw new ShardingSphereException("move next error", sqlException);
        }
    }
    
    @Override
    public Row current() {
        QueryResultMetaData metaData = this.getMetaData();
        try {
            int columnCount = metaData.getColumnCount();
            Object[] rowVal = new Object[columnCount];
            for (int i = 0; i < rowVal.length; i++) {
                rowVal[i] = queryResult.getValue(i + 1, Object.class);
            }
            return new Row(rowVal);
        } catch (SQLException t) {
            throw new ShardingSphereException("load row error", t);
        }
    }
    
    @Override
    public QueryResultMetaData getMetaData() {
        return queryResult.getMetaData();
    }
}
