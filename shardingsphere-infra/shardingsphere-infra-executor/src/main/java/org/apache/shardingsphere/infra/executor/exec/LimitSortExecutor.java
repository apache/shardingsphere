package org.apache.shardingsphere.infra.executor.exec;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.calcite.rex.RexDynamicParam;
import org.apache.calcite.rex.RexLiteral;
import org.apache.calcite.rex.RexNode;
import org.apache.shardingsphere.infra.executor.exec.meta.Row;
import org.apache.shardingsphere.infra.executor.exec.tool.RowComparatorUtil;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSLimitSort;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Executor with offset and fetch, ordering is optional.
 */
public class LimitSortExecutor extends SortExecutor {
    
    @Getter(AccessLevel.PROTECTED)
    private final int offset;
    
    @Getter(AccessLevel.PROTECTED)
    private final int fetch;
    
    /**
     * the number of rows that has been fetched.
     */
    private int fetchedNum;
    
    public LimitSortExecutor(final Executor executor, final Comparator<Row> ordering, final int offset, final int fetch,
                             final ExecContext execContext) {
        super(executor, ordering, execContext);
        this.offset = offset;
        this.fetch = fetch;
    }
    
    /**
     * initialize input interator.
     * @return initialized iterator.
     */
    @Override
    protected Iterator<Row> initInputRowIterator() {
        Iterator<Row> inputRowIterator;
        if (getOrdering() != RowComparatorUtil.EMPTY) {
            inputRowIterator = super.initInputRowIterator();
        } else {
            inputRowIterator = new Iterator<Row>() {
                @Override
                public boolean hasNext() {
                    return getExecutor().moveNext();
                }
    
                @Override
                public Row next() {
                    return getExecutor().current();
                }
            };
        }
        skipOffsetRows(inputRowIterator);
        return inputRowIterator;
    }
    
    protected final void skipOffsetRows(final Iterator<Row> inputRowIterator) {
        int skipNum = 0;
        while (inputRowIterator.hasNext() && skipNum < offset) {
            inputRowIterator.next();
            skipNum++;
        }
    }
    
    @Override
    public final boolean executeMove() {
        if (super.executeMove() && fetchedNum < fetch) {
            fetchedNum++;
            return true;
        }
        return false;
    }
    
    /**
     * Build Executor from <code>LimitSortExecutor</code>.
     * @param limitSort <code>SSLimitSort</code> physical operator
     * @param executorBuilder executorBuilder
     * @return <code>LimitSortExecutor</code>
     */
    public static LimitSortExecutor build(final SSLimitSort limitSort, final ExecutorBuilder executorBuilder) {
        Executor input = executorBuilder.build(limitSort.getInput());
        Comparator<Row> ordering = RowComparatorUtil.convertCollationToRowComparator(limitSort.getCollation());
        int offset = 0;
        int fetch = Integer.MAX_VALUE;
        if (limitSort.offset != null) {
            offset = parseIntValueFromRexNode(limitSort.offset, executorBuilder.getExecContext().getParameters());
        }
        if (limitSort.fetch != null) {
            fetch = parseIntValueFromRexNode(limitSort.fetch, executorBuilder.getExecContext().getParameters());
        }
        int topNOffset = 0;
        if (ordering != RowComparatorUtil.EMPTY && offset <= topNOffset) {
            return new TopNExecutor(input, ordering, offset, fetch, executorBuilder.getExecContext());
        }
        return new LimitSortExecutor(input, ordering, offset, fetch, executorBuilder.getExecContext());
    }
    
    private static int parseIntValueFromRexNode(final RexNode rexNode, final List<Object> parameters) {
        if (rexNode instanceof RexDynamicParam) {
            RexDynamicParam param = (RexDynamicParam) rexNode;
            return (int) parameters.get(param.getIndex());
        } else {
            return RexLiteral.intValue(rexNode);
        }
    }
}
