package org.apache.shardingsphere.infra.executor.exec.func.implementor;

import org.apache.calcite.rel.type.RelDataType;
import org.apache.shardingsphere.infra.executor.exec.func.BuiltinFunction;

public interface RexCallToFunctionImplementor<T, R extends BuiltinFunction> {
    
    /**
     * Implement the sub-class of BuiltinFunction from parameter rexCall.
     * @param rexCall to be implemented
     * @param argTypes argument type for <code>BuiltinFunction</code>  
     * @return sub-class of <code>BuiltinFunction</code> instance
     */
    R implement(T rexCall, RelDataType[] argTypes);
}
