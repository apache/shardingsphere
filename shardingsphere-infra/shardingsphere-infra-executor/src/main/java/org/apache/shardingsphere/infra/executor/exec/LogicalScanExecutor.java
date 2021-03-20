package org.apache.shardingsphere.infra.executor.exec;

import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.executor.exec.storage.JdbcReader;
import org.apache.shardingsphere.infra.optimize.rel.logical.LogicalScan;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * <code>Executor</code> instance for relation operator <code>LogicalScan</code>.
 */
public class LogicalScanExecutor extends MultiExecutor {
    
    public LogicalScanExecutor(final List<Executor> executors, final ExecContext execContext) {
        super(executors, execContext);
    }
    
    /**
     * Build <code>Executor</code> instance for <code>LogicalScan</code>.
     * @param rel <code>LogicalScan</code> rational operator.
     * @param executorBuilder see {@link ExecutorBuilder}
     * @return <code>LogicalScanExecutor</code>
     */
    public static Executor build(final LogicalScan rel, final ExecutorBuilder executorBuilder) {
        ExecContext execContext = executorBuilder.getExecContext();
        try {
            Executor executor = JdbcReader.read(rel, execContext);
            if (executor instanceof MultiExecutor) {
                return new LogicalScanExecutor(((MultiExecutor) executor).getExecutors(), execContext);
            } else {
                return new LogicalScanExecutor(Collections.singletonList(executor), execContext);
            }
        } catch (SQLException ex) {
            throw new ShardingSphereException("Build Executor error for LogicalScan operator.", ex);
        }
        
    }
}
