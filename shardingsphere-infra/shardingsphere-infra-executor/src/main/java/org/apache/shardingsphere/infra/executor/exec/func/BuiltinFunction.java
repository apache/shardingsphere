package org.apache.shardingsphere.infra.executor.exec.func;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface BuiltinFunction<T, R> {
    
    /**
     * Get function name.
     * @return function name
     */
    String getFunctionName();
    
    /**
     * Get argument type.
     * @return  argument type
     */
    List<String[]> getArgTypeNames();
    
    /**
     * Get function identity.
     * @return function identity
     */
    default List<FunctionIdentity> getFunctionIdentities() {
        List<FunctionIdentity> functionIdentities = new ArrayList<>();
        List<String[]> argTypeNames = getArgTypeNames();
        for (String[] argTypeName : argTypeNames) {
            functionIdentities.add(new FunctionIdentity(getFunctionName(), Arrays.asList(argTypeName)));
        }
        return functionIdentities;
    }
}
