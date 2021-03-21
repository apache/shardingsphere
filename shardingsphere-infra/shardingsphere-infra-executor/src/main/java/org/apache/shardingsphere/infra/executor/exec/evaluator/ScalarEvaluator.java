package org.apache.shardingsphere.infra.executor.exec.evaluator;

import org.apache.calcite.rel.type.RelDataType;
import org.apache.shardingsphere.infra.executor.exec.func.BuiltinFunction;
import org.apache.shardingsphere.infra.executor.exec.func.EvalBuiltinFunction;
import org.apache.shardingsphere.infra.executor.exec.meta.Row;

public final class ScalarEvaluator extends AbstractEvaluator implements Evaluator {
    
    private Evaluator[] evaluatorArgs;
    
    private EvalBuiltinFunction function;
    
    protected ScalarEvaluator(final Evaluator[] evaluatorArgs, final BuiltinFunction function, final RelDataType retType) {
        super(retType);
        this.evaluatorArgs = evaluatorArgs;
        this.function = (EvalBuiltinFunction) function;
    }
    
    @Override
    public Object eval(final Row row) {
        Object[] args = new Object[evaluatorArgs.length];
        for (int i = 0; i < evaluatorArgs.length; i++) {
            Evaluator evaluator = evaluatorArgs[i];
            args[i] = evaluator.eval(row);
        }
        return function.apply(args);
    }
}
