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
    
    protected IteratorExecutor(final Executor executor, final ExecContext execContext) {
        super(executor, execContext);
    }
    
    @Override
    protected void doInit() {
        inputRowIterator = initInputRowIterator();
    }
    
    @Override
    public boolean executeMove() {
        if (inputRowIterator == null) {
            throw new ShardingSphereException("Uninitizlized Iterator");
        }
        if (inputRowIterator.hasNext()) {
            replaceCurrent(inputRowIterator.next());
            return true;
        }
        return false;
    }
    
    /**
     * Wrap current <code>Executor</code> to an Iterator.
     * @return Iterator instance.
     */
    protected abstract Iterator<Row> initInputRowIterator();
}
