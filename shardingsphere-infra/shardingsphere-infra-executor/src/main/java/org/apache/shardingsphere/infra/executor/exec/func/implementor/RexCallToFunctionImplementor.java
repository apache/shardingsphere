package org.apache.shardingsphere.infra.executor.exec.func.implementor;

import org.apache.calcite.rel.type.RelDataType;
import org.apache.shardingsphere.infra.executor.exec.func.BuiltinFunction;

/**
 * Interface to convert Calcite functions call(e.g. {@link org.apache.calcite.rex.RexCall},
 * {@link org.apache.calcite.rel.core.AggregateCall}) to {@link BuiltinFunction} 
 * @param <T> Calcite function call
 * @param <R> implemented BuiltinFunction
 */
public interface RexCallToFunctionImplementor<T, R extends BuiltinFunction> {
    
    /**
     * Implement the sub-class of BuiltinFunction from parameter rexCall.
     * @param rexCall to be implemented
     * @param argTypes argument type for <code>BuiltinFunction</code>  
     * @return sub-class of <code>BuiltinFunction</code> instance
     */
    R implement(T rexCall, RelDataType[] argTypes);
}
