package org.apache.shardingsphere.infra.executor.exec;

import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.executor.exec.meta.Row;

import java.util.Iterator;

public abstract class AbstractExector implements Executor {
    
    protected final ExecContext execContext;
    
    private volatile boolean inited;
    
    public AbstractExector(ExecContext execContext) {
        this.execContext = execContext;
    }
    
    @Override
    public boolean moveNext() {
        return false;
    }
    
    @Override
    public void reset() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void close() {
        
    }
    
    protected void executeInit() {
        
    }
    
    @Override
    public Iterator<Row> iterator() {
        return new Iterator<Row>() {
            @Override
            public boolean hasNext() {
                return AbstractExector.this.moveNext();
            }
    
            @Override
            public Row next() {
                return AbstractExector.this.current();
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
