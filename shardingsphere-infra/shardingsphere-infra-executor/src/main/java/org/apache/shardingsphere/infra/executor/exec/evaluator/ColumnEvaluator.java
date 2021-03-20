package org.apache.shardingsphere.infra.executor.exec.evaluator;

import org.apache.calcite.rel.type.RelDataType;
import org.apache.shardingsphere.infra.executor.exec.meta.Row;

public class ColumnEvaluator extends AbstractEvaluator {
    
    private final int idx;
    
    public ColumnEvaluator(final int idx, final RelDataType relDataType) {
        super(relDataType);
        this.idx = idx;
    }
    
    @Override
    public <T> T eval(final Row row) {
        return row.getColumnValue(idx + 1);
    }
}
