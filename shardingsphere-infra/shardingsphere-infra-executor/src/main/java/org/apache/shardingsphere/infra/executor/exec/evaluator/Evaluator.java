package org.apache.shardingsphere.infra.executor.exec.evaluator;

import org.apache.calcite.rel.type.RelDataType;
import org.apache.shardingsphere.infra.executor.exec.meta.Row;

public interface Evaluator {
    
    /**
     * Evaluate a row.
     * @param row input row
     * @param <T> return data type parameter
     * @return a value instance.
     */
    <T> T eval(Row row);
    
    /**
     * Get return type.
     * @return  data type
     */
    RelDataType getRetType();
    
}
