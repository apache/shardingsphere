package org.apache.shardingsphere.infra.executor.exec.func;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface BuiltinFunction<T, R> {
    
    String getFunctionName();
    
    List<String[]> getArgTypeNames();
    
    default List<FunctionIdentity> getFunctionIdentities() {
        List<FunctionIdentity> functionIdentities = new ArrayList<>();
        List<String[]> argTypeNames = getArgTypeNames();
        for(String[] argTypeName : argTypeNames) {
            functionIdentities.add(new FunctionIdentity(getFunctionName(), Arrays.asList(argTypeName)));
        }
        return functionIdentities;
    }
}
