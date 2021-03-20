package org.apache.shardingsphere.infra.executor.exec.func;

public interface EvalBuiltinFunction<T, R> extends BuiltinFunction<T, R> {
    
    /**
     * Evaluate arguments as a result.
     * @param args arguments
     * @return the result
     */
    R apply(T[] args);
}
