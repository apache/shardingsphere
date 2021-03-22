package org.apache.shardingsphere.infra.executor.exec;

import org.apache.calcite.rel.RelNode;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.optimize.rel.logical.LogicalScan;
import org.apache.shardingsphere.infra.route.context.RouteContext;

/**
 * <code>Executor</code> instance for relation operator <code>LogicalScan</code>.
 */
public class LogicalScanExecutor extends SingleExecutor {
    
    public LogicalScanExecutor(final Executor executor, final ExecContext execContext) {
        super(executor, execContext);
    }
    
    @Override
    protected void doInit() {
        
    }
    
    /**
     * Build <code>Executor</code> instance for <code>LogicalScan</code>.
     * @param logicalScan <code>LogicalScan</code> rational operator.
     * @param executorBuilder see {@link ExecutorBuilder}
     * @return <code>LogicalScanExecutor</code>
     */
    public static Executor build(final LogicalScan logicalScan, final ExecutorBuilder executorBuilder) {
        ExecContext execContext = executorBuilder.getExecContext();
        RouteContext routeContext = logicalScan.route();
        RelNode relNode = logicalScan.build();
        Executor executor = new JDBCQueryExecutor(relNode, routeContext, execContext);
        return new LogicalScanExecutor(executor, execContext);
    }
    
    @Override
    protected boolean executeMove() {
        return getExecutor().moveNext();
    }
    
    @Override
    public QueryResultMetaData getMetaData() {
        return getExecutor().getMetaData();
    }
}
