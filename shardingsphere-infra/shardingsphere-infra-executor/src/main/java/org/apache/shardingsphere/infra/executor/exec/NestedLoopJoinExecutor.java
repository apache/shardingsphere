package org.apache.shardingsphere.infra.executor.exec;

import org.apache.calcite.rel.core.JoinRelType;
import org.apache.shardingsphere.infra.executor.exec.evaluator.Evaluator;
import org.apache.shardingsphere.infra.executor.exec.evaluator.RexEvaluatorConverter;
import org.apache.shardingsphere.infra.executor.exec.meta.Row;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSNestedLoopJoin;

/**
 * Nested loop join implementation, reference to the <code>EnumerableDefaults#nestedLoopJoinOptimized</code> of calcite.
 */
public class NestedLoopJoinExecutor extends AbstractJoinExecutor {
    
    private Executor innerBuffer = null;
    private boolean outerMatch = false; // whether the outerValue has matched an innerValue
    private Row outerValue;
    private Row innerValue;
    private int state = 0; // 0 moving outer, 1 moving inner
    
    public NestedLoopJoinExecutor(Executor outer, Executor inner, JoinRelType joinType, Evaluator joinEvaluator, final ExecContext execContext) {
        super(outer, inner, joinType, joinEvaluator, execContext);
    }
    
    public static Executor build(final SSNestedLoopJoin join, final ExecutorBuilder executorBuilder) {
        Executor outer = executorBuilder.build(join.getOuter());
        Executor inner = executorBuilder.build(join.getInner());
        Evaluator joinEvaluator = RexEvaluatorConverter.translateCondition(join.getCondition(), executorBuilder.getExecContext());
        return new NestedLoopJoinExecutor(outer, inner, join.getJoinType(), joinEvaluator, executorBuilder.getExecContext());
    }
    
    @Override
    public boolean moveNext() {
        init();
        while (true) {
            switch (state) {
                case 0:
                    // move outer
                    if (!outer.moveNext()) {
                        return false;
                    }
                    outerValue = outer.current();
                    innerBuffer.reset();
                    outerMatch = false;
                    state = 1;
                    continue;
                case 1:
                    // move inner
                    if (innerBuffer.moveNext()) {
                        innerValue = innerBuffer.current();
                        if (joinEvaluator.eval(newJoinRow(outerValue, innerValue))) {
                            outerMatch = true;
                            switch (joinType) {
                                case ANTI: // try next outer row
                                    state = 0;
                                    continue;
                                case SEMI: // return result, and try next outer row
                                    state = 0;
                                    return true;
                                case INNER:
                                case LEFT: // INNER and LEFT just return result
                                    return true;
                            }
                        } // else (predicate returned false) continue: move inner
                    } else { // innerEnumerator is over
                        state = 0;
                        innerValue = null;
                        if (!outerMatch
                                && (joinType == JoinRelType.LEFT || joinType == JoinRelType.ANTI)) {
                            // No match detected: outerValue is a result for LEFT / ANTI join
                            return true;
                        }
                    }
            }
        }
    }
    
    @Override
    protected void executeInit() {
        super.executeInit();
        
        innerBuffer = new BufferedExecutor(execContext, inner);
        innerBuffer.init();
    }
    
    @Override
    public Row current() {
        return newJoinRow(outerValue, innerValue);
    }
}
