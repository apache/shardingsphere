package org.apache.shardingsphere.infra.executor.exec.func.aggregate;

import org.apache.calcite.sql.SqlKind;

import java.util.ArrayList;
import java.util.List;

public class CountAggregateBuiltinFunction extends AbstractAggregateBuiltinFunction<Long> {
    
    private long count;
    
    public CountAggregateBuiltinFunction(List<Integer> aggColumnIdx, boolean distinct) {
        super(aggColumnIdx, distinct);
    }
    
    @Override
    public void accumulate(final Object[] args) {
        if(args == null ) {
            // TODO handle nullable value
            return;
        }
        count++;
    }
    
    @Override
    public AggregateBuiltinFunction newFunc() {
        return new CountAggregateBuiltinFunction(aggColumnIdx, distinct);
    }
    
    @Override
    public Long getResult() {
        return count;
    }
    
    @Override
    public String getFunctionName() {
        return SqlKind.COUNT.name();
    }
    
    @Override
    public List<String[]> getArgTypeNames() {
        List<String[]> argTypeNames = new ArrayList<>();
        argTypeNames.add(new String[]{"int", "java.lang.Long"});
        argTypeNames.add(new String[]{"long", "java.lang.Long"});
        argTypeNames.add(new String[]{"java.lang.Long", "java.lang.Long"});
        argTypeNames.add(new String[]{"java.lang.Integer", "java.lang.Long"});
        argTypeNames.add(new String[]{"java.lang.String", "java.lang.Long"});
        argTypeNames.add(new String[]{"java.lang.Object", "java.lang.Long"});
        return argTypeNames;
    }
}
