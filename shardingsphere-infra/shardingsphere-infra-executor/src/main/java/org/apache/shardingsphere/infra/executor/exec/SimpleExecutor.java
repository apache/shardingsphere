package org.apache.shardingsphere.infra.executor.exec;

import org.apache.shardingsphere.infra.executor.exec.meta.Row;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;

/**
 * An simple Executor, if you need a mocked Executor, then this is.
 */
public final class SimpleExecutor extends AbstractExecutor {
    
    private final QueryResultMetaData metaData;
    
    private final Executor executor;
    
    private SimpleExecutor(final ExecContext execContext, final QueryResultMetaData metaData) {
        this(execContext, metaData, null);
    }
    
    private SimpleExecutor(final ExecContext execContext, final QueryResultMetaData metaData, final Executor executor) {
        super(execContext);
        this.metaData = metaData;
        this.executor = executor;
    }
    
    @Override
    public QueryResultMetaData getMetaData() {
        return metaData;
    }
    
    @Override
    public boolean executeMove() {
        if (executor != null) {
            return executor.moveNext();
        }
        return false;
    }
    
    @Override
    protected void executeInit() {
        if (executor != null) {
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
    
    /**
     * Build an mocked Executor.
     * @param execContext execution context
     * @param metaData metadata
     * @return <code>SimpleExecutor</code>
     */
    public static SimpleExecutor empty(final ExecContext execContext, final QueryResultMetaData metaData) {
        return new SimpleExecutor(execContext, metaData);
    }
}
