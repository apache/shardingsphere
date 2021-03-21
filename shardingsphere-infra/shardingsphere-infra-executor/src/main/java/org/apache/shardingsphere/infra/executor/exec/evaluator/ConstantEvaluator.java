package org.apache.shardingsphere.infra.executor.exec.evaluator;

import org.apache.calcite.rel.type.RelDataType;
import org.apache.shardingsphere.infra.executor.exec.meta.Row;

public class ConstantEvaluator extends AbstractEvaluator {
    
    private Object value;
    
    private Class<?> clazz;
    
    protected ConstantEvaluator(final Object value, final RelDataType retType) {
        super(retType);
        this.value = value;
    }
    
    @Override
    public final <T> T eval(final Row row) {
        return (T) value;
    }
}
