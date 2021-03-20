package org.apache.shardingsphere.infra.executor.exec;

/**
 * Abstract base class for <code>Executor</code> with a single input .
 *
 * <p>It is not required that single-input <code>Executor</code> use this
 * class as a base class. However, default implementations of methods make life
 * easier.
 */
public abstract class SingleExecutor extends AbstractExecutor {
    
    protected final Executor executor;
    
    public SingleExecutor(final Executor executor, final ExecContext execContext) {
        super(execContext);
        this.executor = executor;
    }
    
    @Override
    protected final void executeInit() {
        executor.init();
        doInit();
    }
    
    /**
     * do initialization for current <code>Executor</code> instance.
     */
    protected abstract void doInit();
}
