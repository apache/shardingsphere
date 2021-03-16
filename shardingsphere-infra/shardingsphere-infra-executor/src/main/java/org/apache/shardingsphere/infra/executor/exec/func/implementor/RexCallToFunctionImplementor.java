package org.apache.shardingsphere.infra.executor.exec.func.implementor;

import org.apache.calcite.rel.type.RelDataType;
import org.apache.shardingsphere.infra.executor.exec.func.BuiltinFunction;

public interface RexCallToFunctionImplementor<T, R extends BuiltinFunction> {
    
    R implement(T rexCall, RelDataType[] argTypes);
}
