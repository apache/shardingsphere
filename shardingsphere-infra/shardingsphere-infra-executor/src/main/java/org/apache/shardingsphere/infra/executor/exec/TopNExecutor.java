package org.apache.shardingsphere.infra.executor.exec;

import org.apache.shardingsphere.infra.executor.exec.meta.Row;

import java.util.Comparator;
import java.util.PriorityQueue;

public class TopNExecutor extends LimitSortExecutor {
    
    private final PriorityQueue<Row> heap;
    
    public TopNExecutor(final Executor executor, final Comparator<Row> ordering, final int offset, final int fetch,
                        final ExecContext execContext) {
        super(executor, ordering, offset, fetch, execContext);
        heap = new PriorityQueue<>(fetch, ordering);
    }
    
    @Override
    protected void executeInit() {
        executor.init();
        while(executor.moveNext()) {
            if(heap.size() > (fetch + offset)) {
                heap.poll();
            }
            Row row = executor.current();
            heap.add(row);
        }
        inputRowIterator = heap.iterator();
        skipOffsetRows();
    }
    
    @Override
    public boolean moveNext() {
        this.init();
        return inputRowIterator.hasNext();
    }
    
}
