package org.apache.shardingsphere.infra.executor.exec;

import org.apache.calcite.rel.type.RelDataType;
import org.apache.shardingsphere.infra.executor.exec.meta.Row;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSProject;

public class ProjectExecutor extends AbstractExector implements Executor {
    
    private final Executor executor;
    
    private final RelDataType relDataType;
    
    public ProjectExecutor(Executor executor, RelDataType relDataType, final ExecContext execContext) {
        super(execContext);
        this.executor = executor;
        this.relDataType = relDataType;
    }
    
    @Override
    public QueryResultMetaData getMetaData() {
        return executor.getMetaData();
    }
    
    public static Executor build(SSProject rel, ExecutorBuilder executorBuilder) {
        Executor executor = executorBuilder.build(rel.getInput());
        return new ProjectExecutor(executor, rel.getRowType(), executorBuilder.getExecContext());
    }
    
    @Override
    public boolean moveNext() {
        return executor.moveNext();
    }
    
    @Override
    public Row current() {
        Row row = executor.current();
        return row;
    }
}
