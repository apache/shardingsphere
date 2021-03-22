package org.apache.shardingsphere.infra.executor.exec;

import org.apache.shardingsphere.infra.executor.exec.meta.Row;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;

import java.util.Collection;
import java.util.Iterator;

public final class CollectionBaseExecutor extends AbstractExecutor {
    
    private final Iterator<Row> rowIterator;
    
    private QueryResultMetaData metaData;
    
    public CollectionBaseExecutor(Collection<Row> rows, QueryResultMetaData metaData, final ExecContext execContext) {
        super(execContext);
        this.metaData = metaData;
        this.rowIterator = rows.iterator();
    }
    
    @Override
    protected void executeInit() {
        
    }
    
    @Override
    protected boolean executeMove() {
        if(rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if(row == null) {
                return false;
            }
            replaceCurrent(row);
            return true;
        }
        return false;
    }
    
    @Override
    public QueryResultMetaData getMetaData() {
        return metaData;
    }
}
