package org.apache.shardingsphere.infra.executor.exec.func.aggregate;


import org.apache.shardingsphere.infra.executor.exec.meta.Row;

import java.util.List;

public abstract class AbstractAggregateBuiltinFunction<R> implements AggregateBuiltinFunction<R> {
    
    protected final List<Integer> aggColumnIdx;
    
    protected final boolean distinct;
    
    private List<Integer> groupByColumnIdx;
    
    
    public AbstractAggregateBuiltinFunction(List<Integer> aggColumnIdx, boolean distinct) {
        this.aggColumnIdx = aggColumnIdx;
        this.distinct = distinct;
    }
    
    @Override
    public void aggregate(Row row) {
        
        Object[] values = getColumnVals(row, aggColumnIdx);
        accumulate(values);
    }
    
    public abstract void accumulate(Object[] args);
    
    @Override
    public void setGroupByColumns(final List<Integer> groupByColumns) {
        this.groupByColumnIdx = groupByColumns;
    }
    
    private Object[] getColumnVals(Row row, List<Integer> columnIdx) {
        Object[] columnVals = new Object[columnIdx.size()];
        int idx = 0;
        for(Integer groupByIdx : columnIdx) {
            columnVals[idx++] = row.getColumnValue(groupByIdx);
        }
        return columnVals;
    }
    
}
