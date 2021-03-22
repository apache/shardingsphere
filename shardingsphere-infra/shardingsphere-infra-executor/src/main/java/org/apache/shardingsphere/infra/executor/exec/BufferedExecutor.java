package org.apache.shardingsphere.infra.executor.exec;

import org.apache.shardingsphere.infra.executor.exec.meta.Row;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;

import java.util.ArrayList;
import java.util.List;

/**
 * This <code>Executor</code> is designed to buffer all rows from another Executor instance. This can be used in 
 * nested loop join, see {@link NestedLoopJoinExecutor}
 */
public final class BufferedExecutor extends SingleExecutor {
    
    private int idx;
    
    private final List<Row> rows;
    
    public BufferedExecutor(final Executor executor, final ExecContext execContext) {
        super(executor, execContext);
        rows = new ArrayList<>();
    }
    
    @Override
    protected void doInit() {
        bufferRows();
    }
    
    @Override
    public QueryResultMetaData getMetaData() {
        return getExecutor().getMetaData();
    }
    
    @Override
    public Row current() {
        return rows.get(idx - 1);
    }
    
    @Override
    public void reset() {
        idx = 0;
    }
    
    @Override
    public boolean executeMove() {
        return idx++ < rows.size();
    }
    
    private void bufferRows() {
        while (getExecutor().moveNext()) {
            Row row = getExecutor().current();
            rows.add(row);
        }
    }
}
