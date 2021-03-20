package org.apache.shardingsphere.infra.executor.exec;

import org.apache.shardingsphere.infra.executor.exec.meta.Row;
import org.apache.shardingsphere.infra.executor.exec.tool.RowComparatorUtil;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSSort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Executor to sort rows from input executor without offset and fetch. 
 * For sort with offset and fetch, @see {@link LimitSortExecutor}
 */
public class SortExecutor extends IteratorExecutor {
    
    protected final Comparator<Row> ordering;
    
    public SortExecutor(Executor executor, Comparator<Row> ordering, ExecContext execContext) {
        super(executor, execContext);
        this.ordering = ordering;
    }
    
    @Override
    public QueryResultMetaData getMetaData() {
        return executor.getMetaData();
    }
    
    @Override
    protected Iterator<Row> initInputRowIterator() {
        List<Row> inputRows = memSort();
        return inputRows.iterator();
    }
    
    protected List<Row> memSort() {
        List<Row> inputRows = new ArrayList<>();
        while(executor.moveNext()) {
            inputRows.add(executor.current());
        }
        Collections.sort(inputRows, ordering);
        return inputRows;
    }
    
    public static SortExecutor build(SSSort sort, ExecutorBuilder executorBuilder) {
        Executor input = executorBuilder.build(sort.getInput());
        Comparator<Row> ordering = RowComparatorUtil.convertCollationToRowComparator(sort.getCollation());
        return new SortExecutor(input, ordering, executorBuilder.getExecContext());
    }
}
