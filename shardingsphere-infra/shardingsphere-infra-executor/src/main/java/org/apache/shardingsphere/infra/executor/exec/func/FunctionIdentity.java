package org.apache.shardingsphere.infra.executor.exec.func;

import lombok.Getter;

import java.util.List;
import java.util.Objects;

@Getter
public class FunctionIdentity {
    
    private String funcName;
    
    private List<String> argTypeNames;
    
    public FunctionIdentity(final String funcName, final List<String> argTypeNames) {
        this.funcName = funcName;
        this.argTypeNames = argTypeNames;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final FunctionIdentity that = (FunctionIdentity) o;
        return Objects.equals(funcName, that.funcName) && Objects.equals(argTypeNames, that.argTypeNames);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(funcName, argTypeNames);
    }
}
