package org.apache.shardingsphere.infra.executor.exec.func.implementor;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.calcite.adapter.enumerable.NullPolicy;
import org.apache.shardingsphere.infra.executor.exec.func.BuiltinFunction;
import org.apache.shardingsphere.infra.executor.exec.func.FunctionIdentity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractFunctionImplementor<T, R extends BuiltinFunction> implements RexCallToFunctionImplementor<T, R> {
    
    @Getter(AccessLevel.PROTECTED)
    private final Map<FunctionIdentity, BuiltinFunction> functionMap = new HashMap<>();
    
    @Getter(AccessLevel.PROTECTED)
    private final NullPolicy nullPolicy;
    
    protected AbstractFunctionImplementor(final NullPolicy nullPolicy) {
        this.nullPolicy = nullPolicy;
    }
    
    protected final void registerFunction(final BuiltinFunction builtinFunction) {
        List<FunctionIdentity> functionIdentities = builtinFunction.getFunctionIdentities();
        for (FunctionIdentity functionIdentity : functionIdentities) {
            functionMap.put(functionIdentity, builtinFunction);
        }
    }

}
