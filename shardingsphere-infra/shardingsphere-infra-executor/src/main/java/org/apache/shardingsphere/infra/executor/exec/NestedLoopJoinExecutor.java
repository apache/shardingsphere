package org.apache.shardingsphere.infra.executor.exec;

import org.apache.calcite.rel.core.JoinRelType;
import org.apache.shardingsphere.infra.executor.exec.evaluator.Evaluator;
import org.apache.shardingsphere.infra.executor.exec.evaluator.RexEvaluatorConverter;
import org.apache.shardingsphere.infra.executor.exec.meta.Row;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSNestedLoopJoin;

/**
 * Nested loop join implementation, reference to the <code>EnumerableDefaults#nestedLoopJoinOptimized</code> of calcite.
 */
public final class NestedLoopJoinExecutor extends AbstractJoinExecutor {
    
    private Executor innerBuffer;
    
    /**
     * whether the outerValue has matched an innerValue.
     */
    private boolean outerMatch;
    
    private Row outerValue;
    
    private Row innerValue;
    
    /**
     * 0 moving outer, 1 moving inner.
     */
    private int state;
    
    public NestedLoopJoinExecutor(final Executor outer, final Executor inner, final JoinRelType joinType, 
                                  final Evaluator joinEvaluator, final ExecContext execContext) {
        super(outer, inner, joinType, joinEvaluator, execContext);
    }
    
    @Override
    public boolean executeMove() {
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
                default:
                    // move inner
                    if (innerBuffer.moveNext()) {
                        innerValue = innerBuffer.current();
                        if (joinEvaluator.eval(newJoinRow(outerValue, innerValue))) {
                            outerMatch = true; 
                            switch (joinType) {
                                // try next outer row
                                case ANTI: 
                                    state = 0; 
                                    continue;
                                // return result, and try next outer row
                                case SEMI: 
                                    state = 0; 
                                    return true;
                                case INNER:
                                // INNER and LEFT just return result
                                case LEFT: 
                                    return true;
                                default:
                                    
                            }
                        } 
                    } else { 
                        // innerEnumerator is over
                        state = 0; 
                        innerValue = null;
                        if (!outerMatch && (joinType == JoinRelType.LEFT || joinType == JoinRelType.ANTI)) {
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
        innerBuffer = new BufferedExecutor(inner, execContext);
    }
    
    @Override
    public Row current() {
        return newJoinRow(outerValue, innerValue);
    }
    
    /**
     * Build an <code>Executor</code> instance using nested loop join algorithm. 
     * @param join <code>SSNestedLoopJoin</code> rational operator
     * @param executorBuilder see {@link ExecutorBuilder}
     * @return <code>NestedLoopJoinExecutor</code>
     */
    public static Executor build(final SSNestedLoopJoin join, final ExecutorBuilder executorBuilder) {
        Executor outer = executorBuilder.build(join.getOuter());
        Executor inner = executorBuilder.build(join.getInner());
        Evaluator joinEvaluator = RexEvaluatorConverter.translateCondition(join.getCondition(), executorBuilder.getExecContext());
        return new NestedLoopJoinExecutor(outer, inner, join.getJoinType(), joinEvaluator, executorBuilder.getExecContext());
    }
}
