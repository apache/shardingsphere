package org.apache.shardingsphere.infra.executor.exec;

import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.executor.exec.meta.Row;
import org.apache.shardingsphere.infra.executor.exec.storage.JdbcReader;
import org.apache.shardingsphere.infra.optimize.rel.logical.LogicalScan;

import java.util.Collections;
import java.util.List;

public class LogicalScanExecutor extends MultiExecutor {
    
    /**
     * operator from optimizer
     */
    private LogicalScan logicalScan;
    
    public LogicalScanExecutor(LogicalScan logicalScan, ExecContext execContext, List<Executor> executors) {
        super(execContext, executors);
        this.logicalScan = logicalScan;
    }
    
    public static Executor build(LogicalScan rel, ExecutorBuilder executorBuilder) {
        ExecContext execContext = executorBuilder.getExecContext();
        try {
            Executor executor = JdbcReader.read(rel, execContext);
            if(executor instanceof MultiExecutor) {
                return new LogicalScanExecutor(rel, execContext, ((MultiExecutor)executor).getExecutors());
            } else {
                return new LogicalScanExecutor(rel, execContext, Collections.singletonList(executor));
            }
        } catch (Exception t) {
            throw new ShardingSphereException("execute error", t);
        }
        
    }
    
    @Override
    public Row current() {
        return super.current();
    }
    
    @Override
    public boolean moveNext() {
        if(!isInited()) {
            init();
        }
        return super.moveNext();
    }
    
    
}
