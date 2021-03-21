package org.apache.shardingsphere.infra.executor.exec.func.binary;

import org.apache.shardingsphere.infra.executor.exec.func.BuiltinFunction;
import org.apache.shardingsphere.infra.executor.exec.func.EvalBuiltinFunction;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

public abstract class BinaryBuiltinFunction<T, R> implements EvalBuiltinFunction<T, R> {
    
    @Override
    public final R apply(final T[] args) {
        if (args == null || args.length < 2) {
            throw new IllegalArgumentException();
        }
        // TODO handle null values with nullPolicy if possible
        return apply(args[0], args[1]);
    }
    
    /**
     * parameters should not be null. 
     * @param t1 the first operand
     * @param t2 the second operand
     * @return the result of the function.
     */
    public abstract R apply(T t1, T t2);
    
    /**
     * Default implementation of {@link BuiltinFunction#getArgTypeNames()}.
     * @return A list of type names
     */
    @Override
    public List<String[]> getArgTypeNames() {
        String typeName = ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0].getTypeName();
        List<String[]> argTypeNames = new ArrayList<>();
        argTypeNames.add(new String[]{typeName, typeName});
        return argTypeNames;
    }
}
