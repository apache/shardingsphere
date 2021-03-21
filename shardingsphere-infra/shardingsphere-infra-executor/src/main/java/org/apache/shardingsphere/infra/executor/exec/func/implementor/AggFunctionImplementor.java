package org.apache.shardingsphere.infra.executor.exec.func.implementor;

import org.apache.calcite.adapter.enumerable.NullPolicy;
import org.apache.calcite.rel.core.AggregateCall;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.sql.SqlAggFunction;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.fun.SqlCountAggFunction;
import org.apache.calcite.sql.fun.SqlMinMaxAggFunction;
import org.apache.shardingsphere.infra.executor.exec.func.aggregate.AggregateBuiltinFunction;
import org.apache.shardingsphere.infra.executor.exec.func.aggregate.CountAggregateBuiltinFunction;
import org.apache.shardingsphere.infra.executor.exec.func.aggregate.MaxAggregateBuiltinFunction;

public class AggFunctionImplementor extends AbstractFunctionImplementor<AggregateCall, AggregateBuiltinFunction> {
    
    public AggFunctionImplementor(final NullPolicy nullPolicy) {
        super(nullPolicy);
    }
    
    @Override
    public final AggregateBuiltinFunction implement(final AggregateCall aggCall, final RelDataType[] argTypes) {
        SqlAggFunction sqlAggFunction = aggCall.getAggregation();
        if (sqlAggFunction instanceof SqlCountAggFunction) {
            return new CountAggregateBuiltinFunction(aggCall.getArgList(), aggCall.isDistinct());
        } else if (sqlAggFunction instanceof SqlMinMaxAggFunction) {
            if (sqlAggFunction.kind == SqlKind.MAX) {
                return new MaxAggregateBuiltinFunction(aggCall.getArgList(), aggCall.isDistinct());
            }
            // TODO
        }
        // TODO 
        throw new UnsupportedOperationException("unsupported aggregate call function: " + aggCall.getAggregation().getKind().name());
    }
}
