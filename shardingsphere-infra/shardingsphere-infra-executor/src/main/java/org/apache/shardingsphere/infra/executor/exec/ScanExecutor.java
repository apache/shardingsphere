package org.apache.shardingsphere.infra.executor.exec;

import org.apache.calcite.rel.RelNode;
import org.apache.shardingsphere.infra.executor.exec.meta.Row;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSScan;
import org.apache.shardingsphere.infra.route.context.RouteContext;

/**
 * <code>Executor</code> instance for relation operator <code>LogicalScan</code>.
 */
public final class ScanExecutor extends SingleExecutor {
    
    public ScanExecutor(final Executor executor, final ExecContext execContext) {
        super(executor, execContext);
    }
    
    @Override
    protected void doInit() {
        
    }
    
    /**
     * Build <code>Executor</code> instance for <code>LogicalScan</code>.
     * @param scan <code>LogicalScan</code> rational operator.
     * @param executorBuilder see {@link ExecutorBuilder}
     * @return <code>LogicalScanExecutor</code>
     */
    public static Executor build(final SSScan scan, final ExecutorBuilder executorBuilder) {
        ExecContext execContext = executorBuilder.getExecContext();
        RouteContext routeContext = scan.route();
        RelNode relNode = scan.getPushdownRelNode();
        Executor executor = new JDBCQueryExecutor(relNode, routeContext, execContext);
        return new ScanExecutor(executor, execContext);
    }
    
    @Override
    protected boolean executeMove() {
        return getExecutor().moveNext();
    }
    
    @Override
    public Row current() {
        return getExecutor().current();
    }
    
    @Override
    public QueryResultMetaData getMetaData() {
        return getExecutor().getMetaData();
    }
}
