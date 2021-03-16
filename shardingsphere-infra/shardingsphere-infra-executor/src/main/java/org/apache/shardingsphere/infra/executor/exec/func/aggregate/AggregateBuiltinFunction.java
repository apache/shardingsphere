package org.apache.shardingsphere.infra.executor.exec.func.aggregate;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.shardingsphere.infra.executor.exec.func.BuiltinFunction;
import org.apache.shardingsphere.infra.executor.exec.meta.Row;

import java.util.List;

public interface AggregateBuiltinFunction<R> extends BuiltinFunction<Object, R> {
    
    void aggregate(Row row);
    
    void setGroupByColumns(List<Integer> groupByColumns);
    
    AggregateBuiltinFunction newFunc();
    
    R getResult();
    
    @EqualsAndHashCode
    @Getter
    class GroupByKey {
        
        private Object[] groupByVals;
        
        private List<Integer> groupByColumnIdx;
        
        public GroupByKey(List<Integer> groupByColumnIdx, Object[] groupByVals) {
            this.groupByColumnIdx = groupByColumnIdx;
            this.groupByVals = groupByVals;
        }
        
        public int length() {
            return groupByColumnIdx.size();
        }
        
    }
    
}
