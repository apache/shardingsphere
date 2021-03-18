package org.apache.shardingsphere.infra.executor.exec;

import org.apache.shardingsphere.infra.executor.exec.meta.Row;
import org.apache.shardingsphere.infra.executor.exec.tool.RowComparatorUtil;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSLimitSort;

import java.util.Comparator;

public class LimitSortExecutor extends SortExecutor {
    
    public LimitSortExecutor(Executor executor, Comparator<Row> ordering , ExecContext execContext) {
        super(executor, ordering, execContext);
    }
    
    @Override
    public QueryResultMetaData getMetaData() {
        return executor.getMetaData();
    }
    
    @Override
    public Row current() {
        return null;
    }
    
    public static LimitSortExecutor build(SSLimitSort limitSort, ExecutorBuilder executorBuilder) {
        Executor input = executorBuilder.build(limitSort.getInput());
        Comparator<Row> ordering = RowComparatorUtil.convertCollationToRowComparator(limitSort.getCollation());
        return new LimitSortExecutor(input, ordering, executorBuilder.getExecContext());
    }
}
