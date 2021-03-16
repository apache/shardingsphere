package org.apache.shardingsphere.infra.executor.exec.evaluator;

import org.apache.calcite.rel.type.RelDataType;

public abstract class AbstractEvaluator implements Evaluator {
    
    private RelDataType retType;
    
    protected AbstractEvaluator(RelDataType retType) {
        this.retType = retType;
    }
    
    @Override
    public RelDataType getRetType() {
        return retType;
    }
}
