package org.apache.shardingsphere.infra.executor.exec.func;

public interface EvalBuiltinFunction<T, R> extends BuiltinFunction<T, R> {
    
    R apply(T[] args);
}
