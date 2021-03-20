package org.apache.shardingsphere.infra.executor.exec;

public abstract class SingleExecutor extends AbstractExecutor {
    
    protected final Executor executor;
    
    public SingleExecutor(Executor executor, ExecContext execContext) {
        super(execContext);
        this.executor = executor;
    }
    
    @Override
    protected final void executeInit() {
        executor.init();
        executeInitCurrent();
    }
    
    protected abstract void executeInitCurrent();
}
