package org.apache.shardingsphere.infra.executor.exec.func.aggregate;

import org.apache.calcite.sql.SqlKind;

import java.util.List;

public final class MaxAggregateBuiltinFunction extends AbstractAggregateBuiltinFunction {
    
    public MaxAggregateBuiltinFunction(final List aggColumnIdx, final boolean distinct) {
        super(aggColumnIdx, distinct);
    }
    
    @Override
    public void accumulate(final Object[] args) {
        // args[0];
        // use comparable to determine how to compare 
    }
    
    @Override
    public AggregateBuiltinFunction newFunc() {
        return null;
    }
    
    @Override
    public Object getResult() {
        return null;
    }
    
    @Override
    public String getFunctionName() {
        return SqlKind.MAX.name();
    }
    
    @Override
    public List<String[]> getArgTypeNames() {
        return null;
    }
}
