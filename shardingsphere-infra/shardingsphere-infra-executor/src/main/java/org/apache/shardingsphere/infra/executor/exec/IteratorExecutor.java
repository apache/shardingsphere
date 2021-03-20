package org.apache.shardingsphere.infra.executor.exec;

import lombok.Setter;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.executor.exec.meta.Row;

import java.util.Iterator;

/**
 * Executor that wrap an Iterator to Executor interface.
 */
abstract class IteratorExecutor extends SingleExecutor {
    
    @Setter
    private Iterator<Row> inputRowIterator;
    
    public IteratorExecutor(Executor executor, ExecContext execContext) {
        super(executor, execContext);
    }
    
    @Override
    protected void executeInitCurrent() {
        inputRowIterator = initInputRowIterator();
    }
    
    @Override
    public boolean executeMove() {
        if(inputRowIterator == null) {
            throw new ShardingSphereException("Uninitizlized Iterator");
        }
        if(inputRowIterator.hasNext()) {
            replaceCurrent(inputRowIterator.next());
            return true;
        }
        return false;
    }
    
    protected abstract Iterator<Row> initInputRowIterator();
}
