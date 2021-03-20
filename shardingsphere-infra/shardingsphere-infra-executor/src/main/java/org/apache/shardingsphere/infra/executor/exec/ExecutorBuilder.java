package org.apache.shardingsphere.infra.executor.exec;


import lombok.Getter;
import org.apache.calcite.rel.RelNode;
import org.apache.shardingsphere.infra.optimize.rel.logical.LogicalScan;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSCalc;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSHashAggregate;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSLimitSort;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSMergeSort;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSNestedLoopJoin;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSRel;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSSort;

@Getter
public class ExecutorBuilder {
    
    private final ExecContext execContext;
    
    public ExecutorBuilder(ExecContext execContext) {
        this.execContext = execContext;
    }
    
    public Executor build(RelNode rel) {
        Executor executor;
        if(rel instanceof LogicalScan) {
            executor = LogicalScanExecutor.build((LogicalScan) rel, this);
        } else if(rel instanceof SSMergeSort) {
            executor = MergeSortExecutor.build((SSMergeSort) rel, this);
        } else if(rel instanceof SSNestedLoopJoin) {
            executor = NestedLoopJoinExecutor.build((SSNestedLoopJoin)rel, this);
        } else if(rel instanceof SSCalc) {
            executor = CalcExecutor.build((SSCalc) rel, this);
        } else if(rel instanceof SSHashAggregate) {
            executor = HashAggregateExecutor.build((SSHashAggregate)rel, this);
        } else if(rel instanceof SSSort) {
            executor = SortExecutor.build((SSSort) rel, this);
        } else if(rel instanceof SSLimitSort) {
            executor = LimitSortExecutor.build((SSLimitSort) rel, this);
        } else {
            throw new UnsupportedOperationException("unsupported physic operator " + rel.getClass().getName());
        }
        
        return executor;
    }
    
    public static Executor build(ExecContext execContext, RelNode rel) {
        if(!(rel instanceof SSRel)) {
           throw new UnsupportedOperationException("unsupported physic plan: " + rel.getClass());
        }
        return new ExecutorBuilder(execContext).build(rel);
    }
}
