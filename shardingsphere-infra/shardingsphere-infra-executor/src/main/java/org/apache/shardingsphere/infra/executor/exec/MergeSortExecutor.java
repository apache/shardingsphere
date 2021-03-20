package org.apache.shardingsphere.infra.executor.exec;

import com.google.common.collect.Iterables;
import org.apache.calcite.rex.RexDynamicParam;
import org.apache.calcite.rex.RexLiteral;
import org.apache.calcite.rex.RexNode;
import org.apache.shardingsphere.infra.executor.exec.meta.Row;
import org.apache.shardingsphere.infra.executor.exec.tool.RowComparatorUtil;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSMergeSort;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Executor with multi input Executors and read rows from these Executors useing merge sort algorithm.
 */
public class MergeSortExecutor extends AbstractExecutor {
    
    private List<Executor> executors;
    
    private long offset;
    
    private long fetch;
    
    private Comparator<Row> ordering;
    
    private Iterator<Row> mergeSortIterator;
    
    private long count;
    
    public MergeSortExecutor(final ExecContext execContext, final List<Executor> executors, final Comparator<Row> ordering,
                             final int offset, final int fetch) {
        super(execContext);
        this.executors = executors;
        this.ordering = ordering;
        this.offset = offset;
        this.fetch = fetch;
    }
    
    @Override
    protected void executeInit() {
        executors.forEach(Executor::init);
        this.mergeSortIterator = Iterables.mergeSorted(executors, ordering).iterator();
        
        for (int i = 0; i < offset; i++) {
            if (!moveNext()) {
                break;
            }
        }
        
    }
    
    @Override
    public boolean executeMove() {
        if (count >= fetch) {
            return false;
        }
        if (mergeSortIterator.hasNext()) {
            count++;
            replaceCurrent(mergeSortIterator.next());
            return true;
        } else {
            return false;
        }
    }
    
    @Override
    public QueryResultMetaData getMetaData() {
        return executors.get(0).getMetaData();
    }
    
    /**
     * Build an <code>Executor</code> instance using merge sort algorithm. 
     * @param rel <code>SSMergeSort</code> rational operator
     * @param executorBuilder see {@link ExecutorBuilder}
     * @return <code>MergeSortExecutor</code>
     */
    public static Executor build(final SSMergeSort rel, final ExecutorBuilder executorBuilder) {
        Executor executor = executorBuilder.build(rel.getInput());
        Comparator<Row> ordering = RowComparatorUtil.convertCollationToRowComparator(rel.collation);
        if (executor instanceof MultiExecutor) {
            ExecContext execContext = executorBuilder.getExecContext();
            int offset = resolveRexNodeValue(rel.offset, 0, execContext.getParameters(), Integer.class);
            int fetch = resolveRexNodeValue(rel.fetch, Integer.MAX_VALUE, execContext.getParameters(), Integer.class);
            return new MergeSortExecutor(execContext, ((MultiExecutor) executor).getExecutors(), ordering, offset, fetch);
        }
        return executor;
    }
    
    private static <T> T resolveRexNodeValue(final RexNode rexNode, final T defaultValue, final List<Object> parameters,
                                             final Class<T> clazz) {
        if (rexNode == null) {
            return defaultValue;
        }
        if (rexNode instanceof RexLiteral) {
            return ((RexLiteral) rexNode).getValueAs(clazz);
        } else if (rexNode instanceof RexDynamicParam) {
            RexDynamicParam rexDynamicParam = (RexDynamicParam) rexNode;
            int idx = rexDynamicParam.getIndex();
            Object val = parameters.get(idx);
            // TODO using a Data type conveter
            return clazz.cast(val);
        }
        return defaultValue;
    }
}
