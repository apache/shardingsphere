package org.apache.shardingsphere.infra.executor.exec;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.infra.executor.exec.meta.Row;
import org.apache.shardingsphere.infra.executor.exec.tool.RowComparatorUtil;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSSort;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Executor to sort rows from input executor without offset and fetch. 
 * For sort with offset and fetch, @see {@link LimitSortExecutor}
 */
public class SortExecutor extends IteratorExecutor {
    
    @Getter(AccessLevel.PROTECTED)
    private final Comparator<Row> ordering;
    
    public SortExecutor(final Executor executor, final Comparator<Row> ordering, final ExecContext execContext) {
        super(executor, execContext);
        this.ordering = ordering;
    }
    
    @Override
    public final QueryResultMetaData getMetaData() {
        return getExecutor().getMetaData();
    }
    
    /**
     * initialize input interator.
     * @return initialized iterator.
     */
    @Override
    protected Iterator<Row> initInputRowIterator() {
        List<Row> inputRows = memSort();
        return inputRows.iterator();
    }
    
    /**
     * Sort rows of input Executor in memory.
     * @return sorted list
     */
    protected List<Row> memSort() {
        List<Row> inputRows = new ArrayList<>();
        while (getExecutor().moveNext()) {
            inputRows.add(getExecutor().current());
        }
        inputRows.sort(ordering);
        return inputRows;
    }
    
    /**
     * Build <code>SortExecutor</code> instance.
     * @param sort <code>SSSort</code>
     * @param executorBuilder <code>ExecutorBuilder</code>
     * @return <code>SortExecutor</code>
     */
    public static SortExecutor build(final SSSort sort, final ExecutorBuilder executorBuilder) {
        Executor input = executorBuilder.build(sort.getInput());
        Comparator<Row> ordering = RowComparatorUtil.convertCollationToRowComparator(sort.getCollation());
        return new SortExecutor(input, ordering, executorBuilder.getExecContext());
    }
}
