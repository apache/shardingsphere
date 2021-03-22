package org.apache.shardingsphere.infra.executor.exec.func.aggregate;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.infra.executor.exec.meta.Row;

import java.util.List;

public abstract class AbstractAggregateBuiltinFunction<R> implements AggregateBuiltinFunction<R> {
    
    @Getter(AccessLevel.PROTECTED)
    private final List<Integer> aggColumnIdx;
    
    @Getter(AccessLevel.PROTECTED)
    private final boolean distinct;
    
    public AbstractAggregateBuiltinFunction(final List<Integer> aggColumnIdx, final boolean distinct) {
        this.aggColumnIdx = aggColumnIdx;
        this.distinct = distinct;
    }
    
    @Override
    public final void aggregate(final Row row) {
        Object[] values = getColumnVals(row, aggColumnIdx);
        accumulate(values);
    }
    
    /**
     * accumulating method for aggregate operator.
     * @param args args for accumulating.
     */
    public abstract void accumulate(Object[] args);
    
    private Object[] getColumnVals(final Row row, final List<Integer> columnIdx) {
        Object[] columnVals = new Object[columnIdx.size()];
        int idx = 0;
        for (Integer groupByIdx : columnIdx) {
            columnVals[idx++] = row.getColumnValue(groupByIdx);
        }
        return columnVals;
    }
    
}
