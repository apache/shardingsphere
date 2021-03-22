package org.apache.shardingsphere.infra.executor.exec;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.infra.executor.exec.meta.Row;

import java.util.Iterator;

/**
 * Base class for {@link Executor} of rational operator.
 */
public abstract class AbstractExecutor implements Executor {
    
    /**
     * context for current {@link Executor}.
     */
    @Getter(AccessLevel.PROTECTED)
    private final ExecContext execContext;
    
    private volatile boolean inited;
    
    private Row current;
    
    public AbstractExecutor(final ExecContext execContext) {
        this.execContext = execContext;
    }
    
    /**
     * move to next row.
     * @return true, if the next row exist, or false
     */
    @Override
    public final boolean moveNext() {
        init();
        return executeMove();
    }
    
    /**
     * default implementation for {@link Executor#reset()}.
     */
    @Override
    public void reset() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * default implementation for {@link Executor#close()}.
     */
    @Override
    public void close() {
        
    }
    
    /**
     * Get current Row.
     * @return current Row.
     */
    @Override
    public Row current() {
        return current;
    }
    
    /**
     * replace the current row reference, so sub-class do not need to override {@link #current()} method.
     * @param row the row to replace current.
     */
    protected void replaceCurrent(final Row row) {
        current = row;
    }
    
    /**
     * execute initialization for this executor.
     */
    protected abstract void executeInit();
    
    /**
     * move to the next row of this executor.
     * @return true if the next row exist, else false
     */
    protected abstract boolean executeMove();
    
    /**
     * Decorate this Executor to Iterator.
     * @return Iterator.
     */
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
    public final boolean isInited() {
        return inited;
    }
    
    @Override
    public final void init() {
        if (inited) {
            return;
        }
        synchronized (this) {
            if (inited) {
                return;
            }
            executeInit();
            inited = true;
        }
       
    }
}
