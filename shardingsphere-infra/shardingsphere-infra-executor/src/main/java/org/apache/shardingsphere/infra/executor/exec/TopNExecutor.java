package org.apache.shardingsphere.infra.executor.exec;

import org.apache.shardingsphere.infra.executor.exec.meta.Row;

import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

/**
 * Executor that keep only top n elements in the heap.
 */
public class TopNExecutor extends LimitSortExecutor {
    
    private final PriorityQueue<Row> heap;
    
    public TopNExecutor(final Executor executor, final Comparator<Row> ordering, final int offset, final int fetch,
                        final ExecContext execContext) {
        super(executor, ordering, offset, fetch, execContext);
        heap = new PriorityQueue<>(fetch, ordering);
    }
    
    @Override
    protected final Iterator<Row> initInputRowIterator() {
        while (executor.moveNext()) {
            if (heap.size() > (fetch + offset)) {
                heap.poll();
            }
            Row row = executor.current();
            heap.add(row);
        }
        Iterator<Row> inputRowIterator = heap.iterator();
        skipOffsetRows(inputRowIterator);
        return inputRowIterator;
    }
}
