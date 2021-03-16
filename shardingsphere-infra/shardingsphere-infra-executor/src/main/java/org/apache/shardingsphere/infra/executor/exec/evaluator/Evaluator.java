package org.apache.shardingsphere.infra.executor.exec.evaluator;

import org.apache.calcite.rel.type.RelDataType;
import org.apache.shardingsphere.infra.executor.exec.meta.Row;

public interface Evaluator {
    
    <T> T eval(Row row);
    
    RelDataType getRetType();
    
}
