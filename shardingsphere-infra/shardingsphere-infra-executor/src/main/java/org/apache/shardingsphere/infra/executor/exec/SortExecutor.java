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
 * TODO to be refactor to extend a single executor 
 * executor to sort rows from input executor without offset and fetch. 
 * @see {@link LimitSortExecutor}
 */
public class SortExecutor extends AbstractExector {
    
    protected final Executor executor;
    
    protected final Comparator<Row> ordering;
    
    Iterator<Row> inputRowIterator;
    
    public SortExecutor(Executor executor, Comparator<Row> ordering, ExecContext execContext) {
        super(execContext);
        this.executor = executor;
        this.ordering = ordering;
    }
    
    @Override
    public QueryResultMetaData getMetaData() {
        return executor.getMetaData();
    }
    
    @Override
    protected void executeInit() {
        executor.init();
        memSort();
    }
    
    protected void memSort() {
        List<Row> inputRows = new ArrayList<>();
        while(executor.moveNext()) {
            inputRows.add(executor.current());
        }
        Collections.sort(inputRows, ordering);
        inputRowIterator = inputRows.iterator();
    }
    
    @Override
    public boolean moveNext() {
        return inputRowIterator.hasNext();
    }
    
    @Override
    public Row current() {
        return inputRowIterator.next();
    }
    
    public static SortExecutor build(SSSort sort, ExecutorBuilder executorBuilder) {
        Executor input = executorBuilder.build(sort.getInput());
        Comparator<Row> ordering = RowComparatorUtil.convertCollationToRowComparator(sort.getCollation());
        return new SortExecutor(input, ordering, executorBuilder.getExecContext());
    }
}
