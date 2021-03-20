package org.apache.shardingsphere.infra.optimize.planner.rule;

import org.apache.calcite.plan.RelOptRuleCall;
import org.apache.calcite.plan.RelRule;
import org.apache.calcite.rel.logical.LogicalSort;
import org.apache.calcite.rel.rules.TransformationRule;
import org.apache.shardingsphere.infra.optimize.rel.logical.LogicalScan;

import java.util.function.Predicate;

public abstract class PushSortToScanRule extends RelRule<PushSortToScanRule.Config> implements TransformationRule {
    
    /**
     * Creates a RelRule.
     *
     * @param config config
     */
    protected PushSortToScanRule(final Config config) {
        super(config);
    }
    
    @Override
    public void onMatch(final RelOptRuleCall call) {
        LogicalScan logicalScan = pushdownSort(call.rel(0), call.rel(1));
        call.transformTo(logicalScan);
    }
    
    protected LogicalScan pushdownSort(final LogicalSort logicalSort, final LogicalScan logicalScan) {
        return logicalScan.pushdown(logicalSort);
    }
    
    public interface Config extends RelRule.Config {
    
        /**
         * Untility method.
         * @param sort sort
         * @param scan scan
         * @param predicate predicate before using rule
         * @param config config
         * @param <T> config type
         * @return Config
         */
        default <T extends Config> T withOperandFor(Class<? extends LogicalSort> sort,
                                                         Class<? extends LogicalScan> scan,
                                                         Predicate<LogicalScan> predicate,
                                                         Class<T> config) {
            return withOperandSupplier(b ->
                    b.operand(sort).oneInput(b2 ->
                            b2.operand(scan).predicate(predicate).anyInputs()))
                    .as(config);
        }
    }
}
