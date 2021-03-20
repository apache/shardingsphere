package org.apache.shardingsphere.infra.executor.exec;

import org.apache.calcite.rex.RexDynamicParam;
import org.apache.calcite.rex.RexLiteral;
import org.apache.calcite.rex.RexNode;
import org.apache.shardingsphere.infra.executor.exec.meta.Row;
import org.apache.shardingsphere.infra.executor.exec.tool.RowComparatorUtil;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSLimitSort;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static org.apache.shardingsphere.infra.executor.exec.tool.RowComparatorUtil.EMPTY;

/**
 * Executor with offset and fetch, ordering is optional.
 */
public class LimitSortExecutor extends SortExecutor {
    
    protected final int offset;
    
    protected final int fetch;
    
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
    
    @Override
    protected Iterator<Row> initInputRowIterator() {
        Iterator<Row> inputRowIterator;
        if(ordering == EMPTY) {
            inputRowIterator = super.initInputRowIterator();
        } else {
            inputRowIterator = new Iterator<Row>() {
                @Override
                public boolean hasNext() {
                    return executor.moveNext();
                }
    
                @Override
                public Row next() {
                    return executor.current();
                }
            };
        }
        skipOffsetRows(inputRowIterator);
        return inputRowIterator;
    }
    
    protected void skipOffsetRows(Iterator<Row> inputRowIterator) {
        int skipNum = 0;
        while(inputRowIterator.hasNext() && skipNum < offset) {
            inputRowIterator.next();
            skipNum++;
        }
    }
    
    @Override
    public boolean executeMove() {
        if(super.moveNext() && fetchedNum < fetch) {
            fetchedNum++;
            return true;
        }
        return false;
    }
    
    public static LimitSortExecutor build(SSLimitSort limitSort, ExecutorBuilder executorBuilder) {
        Executor input = executorBuilder.build(limitSort.getInput());
        Comparator<Row> ordering = RowComparatorUtil.convertCollationToRowComparator(limitSort.getCollation());
        int offset = 0;
        int fetch = Integer.MAX_VALUE;
        if (limitSort.offset != null) {
            offset = parseIntValueFromRexNode(limitSort.offset, executorBuilder.getExecContext().getParameters());
        }
        if(limitSort.fetch != null) {
            fetch = parseIntValueFromRexNode(limitSort.fetch, executorBuilder.getExecContext().getParameters());
        }
        int topNOffset = 0;
        if(ordering != EMPTY && offset <= topNOffset) {
            return new TopNExecutor(input, ordering, offset, fetch, executorBuilder.getExecContext());
        }
        return new LimitSortExecutor(input, ordering, offset, fetch, executorBuilder.getExecContext());
    }
    
    private static int parseIntValueFromRexNode(RexNode rexNode, List<Object> parameters) {
        if(rexNode instanceof RexDynamicParam) {
            RexDynamicParam param = (RexDynamicParam) rexNode;
            return (int)parameters.get(param.getIndex());
        } else {
            return RexLiteral.intValue(rexNode);
        }
    }
}
