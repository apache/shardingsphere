package org.apache.shardingsphere.infra.executor.exec;

import org.apache.calcite.rex.RexProgram;
import org.apache.shardingsphere.infra.executor.exec.evaluator.Evaluator;
import org.apache.shardingsphere.infra.executor.exec.evaluator.RexEvaluatorConverter;
import org.apache.shardingsphere.infra.executor.exec.meta.Row;
import org.apache.shardingsphere.infra.executor.exec.tool.MetaDataConverter;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSCalc;

import java.util.List;

public class CalcExecutor extends AbstractExector {
    
    private Evaluator conditionEvaluator;
    
    private List<Evaluator> projectEvaluators;
    
    private Executor executor;
    
    private QueryResultMetaData metaData;
    
    
    public CalcExecutor(Executor input, QueryResultMetaData metaData, List<Evaluator> projectEvaluators, 
                        Evaluator conditionEvaluator, final ExecContext execContext) {
        super(execContext);
        this.executor = input;
        this.metaData = metaData;
        this.projectEvaluators = projectEvaluators;
        this.conditionEvaluator = conditionEvaluator;
    }
    
    @Override
    public QueryResultMetaData getMetaData() {
        return this.metaData;
    }
    
    @Override
    protected void executeInit() {
        executor.init();
    }
    
    @Override
    public boolean moveNext() {
        if(conditionEvaluator != null) {
            while(true) {
                if(!executor.moveNext()) {
                    return false;
                }
                Row row = executor.current();
                if(conditionEvaluator.eval(row)) {
                    return true;
                }
            }
        } else {
            return executor.moveNext();
        }
    }
    
    @Override
    public Row current() {
        Row row = executor.current();
        Object[] vals = new Object[projectEvaluators.size()];
        int idx = 0;
        for(Evaluator evaluator : projectEvaluators) {
            vals[idx++] = evaluator.eval(row);
        }
        return new Row(vals);
    }
    
    public static CalcExecutor build(SSCalc calc, ExecutorBuilder executorBuilder) {
        Executor input = executorBuilder.build(calc.getInput());
        RexProgram program = calc.getProgram();
        Evaluator conditionEvaluator = RexEvaluatorConverter.translateCondition(program, executorBuilder.getExecContext());
        List<Evaluator> projectEvaluators = RexEvaluatorConverter.translateProjects(program, executorBuilder.getExecContext());
        return new CalcExecutor(input, MetaDataConverter.buildMetaData(calc), projectEvaluators, conditionEvaluator, 
                executorBuilder.getExecContext());
    }
}
