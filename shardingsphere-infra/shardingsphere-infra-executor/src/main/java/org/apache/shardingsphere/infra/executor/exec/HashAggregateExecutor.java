package org.apache.shardingsphere.infra.executor.exec;

import org.apache.calcite.rel.core.AggregateCall;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.util.ImmutableBitSet;
import org.apache.shardingsphere.infra.executor.exec.func.BuiltinFunctionTable;
import org.apache.shardingsphere.infra.executor.exec.func.aggregate.AggregateBuiltinFunction;
import org.apache.shardingsphere.infra.executor.exec.func.aggregate.AggregateBuiltinFunction.GroupByKey;
import org.apache.shardingsphere.infra.executor.exec.func.implementor.AggFunctionImplementor;
import org.apache.shardingsphere.infra.executor.exec.meta.Row;
import org.apache.shardingsphere.infra.executor.exec.tool.MetaDataConverter;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSHashAggregate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Aggregate operator implementation Executor using hash .
 */
public final class HashAggregateExecutor extends SingleExecutor {
    
    private List<Integer> groupByColumnIdx;
    
    private List<AggregateBuiltinFunction> aggFuncs;
    
    private QueryResultMetaData metaData;
    
    private Map<GroupByKey, List<AggregateBuiltinFunction>> groupByKeyAccumulators = new HashMap<>();
    
    private final int columnNum;
    
    private Iterator<Entry<GroupByKey, List<AggregateBuiltinFunction>>> lookup;
    
    public HashAggregateExecutor(final Executor input, final QueryResultMetaData metaData, final List<Integer> groupByColumnIdx,
                                 final List<AggregateBuiltinFunction> aggFuncs, final ExecContext execContext) {
        super(input, execContext);
        this.metaData = metaData;
        this.groupByColumnIdx = groupByColumnIdx;
        this.aggFuncs = aggFuncs;
        this.columnNum = groupByColumnIdx.size() + aggFuncs.size();
    }
    
    @Override
    public QueryResultMetaData getMetaData() {
        return metaData;
    }
    
    @Override
    public boolean executeMove() {
        if (!lookup.hasNext()) {
            return false;
        }
        Entry<GroupByKey, List<AggregateBuiltinFunction>> entry = lookup.next();
        Object[] rowVals = new Object[columnNum];
        GroupByKey groupByKey = entry.getKey();
        System.arraycopy(groupByKey.getGroupByVals(), 0, rowVals, 0, groupByKey.length());
        int idx = groupByKey.length();
        List<AggregateBuiltinFunction> aggFuncs = entry.getValue();
        for (AggregateBuiltinFunction aggFunc : aggFuncs) {
            rowVals[idx++] = aggFunc.getResult();
        }
        replaceCurrent(new Row(rowVals));
        return true;
    }
    
    @Override
    protected void doInit() {
        while (getExecutor().moveNext()) {
            Row row = getExecutor().current();
            aggregate(row);
        }
        lookup = groupByKeyAccumulators.entrySet().iterator();
    }
    
    private void aggregate(final Row row) {
        Object[] groupByVals = new Object[groupByColumnIdx.size()];
        int idx = 0;
        for (Integer groupByIdx : groupByColumnIdx) {
            groupByVals[idx++] = row.getColumnValue(groupByIdx + 1);
        }
        GroupByKey groupByKey = new GroupByKey(groupByColumnIdx, groupByVals);
        if (!groupByKeyAccumulators.containsKey(groupByKey)) {
            List<AggregateBuiltinFunction> newAggFuncs = new ArrayList<>(aggFuncs.size());
            for (AggregateBuiltinFunction aggFunc : aggFuncs) {
                newAggFuncs.add(aggFunc.newFunc());
            }
            groupByKeyAccumulators.put(groupByKey, newAggFuncs);
        }
        
        List<AggregateBuiltinFunction> groupAggFuncs = groupByKeyAccumulators.get(groupByKey);
        for (AggregateBuiltinFunction aggFunc : groupAggFuncs) {
            aggFunc.aggregate(row);
        }
        
    }
    
    /**
     * Build Executor from <code>SSHashAggregate</code>.
     * @param aggregate <code>SSHashAggregate</code> physical operator
     * @param executorBuilder executorBuilder
     * @return <code>HashAggregateExecutor</code>
     */
    public static HashAggregateExecutor build(final SSHashAggregate aggregate, final ExecutorBuilder executorBuilder) {
        Executor input = executorBuilder.build(aggregate.getInput());
        ImmutableBitSet groupBy = aggregate.getGroupSet();
    
        List<AggregateCall> aggCalls = aggregate.getAggCallList();
        List<AggregateBuiltinFunction> aggFuncs = new ArrayList<>();
        for (AggregateCall aggCall : aggCalls) {
            AggFunctionImplementor aggImp = BuiltinFunctionTable.INSTANCE.get(aggCall.getAggregation());
            AggregateBuiltinFunction aggFunction = aggImp.implement(aggCall, new RelDataType[]{aggCall.getType()});
            aggFuncs.add(aggFunction);
        }
        
        return new HashAggregateExecutor(input, MetaDataConverter.buildMetaData(aggregate), groupBy.asList(), aggFuncs, executorBuilder.getExecContext());
    } 
}
