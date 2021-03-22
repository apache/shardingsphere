package org.apache.shardingsphere.infra.executor.exec;

import org.apache.calcite.rex.RexProgram;
import org.apache.shardingsphere.infra.executor.exec.evaluator.Evaluator;
import org.apache.shardingsphere.infra.executor.exec.evaluator.RexEvaluatorConverter;
import org.apache.shardingsphere.infra.executor.exec.meta.Row;
import org.apache.shardingsphere.infra.executor.exec.tool.MetaDataConverter;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSCalc;

import java.util.List;

public final class CalcExecutor extends SingleExecutor {
    
    private Evaluator conditionEvaluator;
    
    private List<Evaluator> projectEvaluators;
    
    private QueryResultMetaData metaData;
    
    public CalcExecutor(final Executor input, final QueryResultMetaData metaData, final List<Evaluator> projectEvaluators,
                        final Evaluator conditionEvaluator, final ExecContext execContext) {
        super(input, execContext);
        this.metaData = metaData;
        this.projectEvaluators = projectEvaluators;
        this.conditionEvaluator = conditionEvaluator;
    }
    
    @Override
    protected void doInit() {
        
    }
    
    @Override
    public QueryResultMetaData getMetaData() {
        return this.metaData;
    }
    
    @Override
    public boolean executeMove() {
        if (conditionEvaluator != null) {
            while (true) {
                if (!getExecutor().moveNext()) {
                    return false;
                }
                Row row = getExecutor().current();
                if (conditionEvaluator.eval(row)) {
                    return true;
                }
            }
        } else {
            return getExecutor().moveNext();
        }
    }
    
    @Override
    public Row current() {
        Row row = getExecutor().current();
        Object[] vals = new Object[projectEvaluators.size()];
        int idx = 0;
        for (Evaluator evaluator : projectEvaluators) {
            vals[idx++] = evaluator.eval(row);
        }
        return new Row(vals);
    }
    
    /**
     * Build Executor from <code>SSCalc</code>.
     * @param calc <code>SSCalc</code> physical operator
     * @param executorBuilder executorBuilder
     * @return <code>CalcExecutor</code>
     */
    public static CalcExecutor build(final SSCalc calc, final ExecutorBuilder executorBuilder) {
        Executor input = executorBuilder.build(calc.getInput());
        RexProgram program = calc.getProgram();
        Evaluator conditionEvaluator = RexEvaluatorConverter.translateCondition(program, executorBuilder.getExecContext());
        List<Evaluator> projectEvaluators = RexEvaluatorConverter.translateProjects(program, executorBuilder.getExecContext());
        return new CalcExecutor(input, MetaDataConverter.buildMetaData(calc), projectEvaluators, conditionEvaluator, 
                executorBuilder.getExecContext());
    }
}
