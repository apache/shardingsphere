package org.apache.shardingsphere.infra.executor.exec;

import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.executor.exec.meta.Row;

import java.util.Iterator;

/**
 * Base class for {@link Executor} of rational operator.
 */
public abstract class AbstractExecutor implements Executor {
    
    /**
     * context for current {@link Executor}
     */
    protected final ExecContext execContext;
    
    private volatile boolean inited;
    
    private Row current;
    
    public AbstractExecutor(ExecContext execContext) {
        this.execContext = execContext;
    }
    
    @Override
    public boolean moveNext() {
        init();
        return executeMove();
    }
    
    @Override
    public void reset() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void close() {
        
    }
    
    @Override
    public Row current() {
        return current;
    }
    
    /**
     * replace the current row reference, so sub-class do not need to override {@link #current()} method
     * @param row
     */
    protected void replaceCurrent(Row row) {
        current = row;
    }
    
    /**
     * execute initialization for this executor
     */
    protected abstract void executeInit();
    
    /**
     * move to the next row of this executor
     * @return true if the next row exist, else false
     */
    protected abstract boolean executeMove();
    
    @Override
    public Iterator<Row> iterator() {
        return new Iterator<Row>() {
            @Override
            public boolean hasNext() {
                return AbstractExecutor.this.moveNext();
            }
    
            @Override
            public Row next() {
                return AbstractExecutor.this.current();
            }
        };
    }
    
    @Override
    public boolean isInited() {
        return inited;
    }
    
    public final void init() {
        if(inited) {
            return;
        }
        synchronized (this) {
            if(inited) {
                return;
            }
            try {
                executeInit();
            } catch (Exception t) {
                throw new ShardingSphereException("execute init failed for executor: " + this.getClass().getName(), t);
            }
            inited = true;
        }
       
    }
}
