package org.apache.shardingsphere.infra.executor.exec.func.aggregate;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.shardingsphere.infra.executor.exec.func.BuiltinFunction;
import org.apache.shardingsphere.infra.executor.exec.meta.Row;

import java.util.List;

/**
 * BuiltinFunction form Aggregate operator.
 * @param <R> result type for aggregation result.
 */
public interface AggregateBuiltinFunction<R> extends BuiltinFunction<Object, R> {
    
    /**
     * aggregation method.
     * @param row row to be aggregate.
     */
    void aggregate(Row row);
    
    /**
     * copy this function instance.
     * @return a new aggregation BuiltinFunction.
     */
    AggregateBuiltinFunction newFunc();
    
    /**
     * Get the result for aggregation operator.
     * @return the result.
     */
    R getResult();
    
    @EqualsAndHashCode
    @Getter
    class GroupByKey {
        
        private final Object[] groupByVals;
        
        private final List<Integer> groupByColumnIdx;
        
        public GroupByKey(final List<Integer> groupByColumnIdx, final Object[] groupByVals) {
            this.groupByColumnIdx = groupByColumnIdx;
            this.groupByVals = groupByVals;
        }
        
        public int length() {
            return groupByColumnIdx.size();
        }
        
    }
    
}
