package org.apache.shardingsphere.infra.executor.exec;

import org.apache.shardingsphere.infra.executor.exec.meta.Row;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;

public class SimpleExecutor extends AbstractExector {
    
    private final QueryResultMetaData metaData;
    
    private final Executor executor;
    
    private SimpleExecutor(ExecContext execContext, QueryResultMetaData metaData) {
        this(execContext, metaData, null);
    }
    
    private SimpleExecutor(ExecContext execContext, QueryResultMetaData metaData, Executor executor) {
        super(execContext);
        this.metaData = metaData;
        this.executor = executor;
    }
    
    
    @Override
    public QueryResultMetaData getMetaData() {
        return metaData;
    }
    
    @Override
    public boolean moveNext() {
        if(executor != null) {
            return executor.moveNext();
        }
        return false;
    }
    
    @Override
    protected void executeInit() {
        if(executor != null) {
            executor.init();
        }
    }
    
    @Override
    public Row current() {
        if (executor != null) {
            return executor.current();
        }
        return null;
    }
    
    public static SimpleExecutor empty(ExecContext execContext, QueryResultMetaData metaData) {
        return new SimpleExecutor(execContext, metaData);
    }
}
