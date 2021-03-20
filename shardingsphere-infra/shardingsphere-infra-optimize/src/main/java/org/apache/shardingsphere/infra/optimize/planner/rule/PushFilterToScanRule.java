package org.apache.shardingsphere.infra.optimize.planner.rule;

import org.apache.calcite.plan.RelOptRuleCall;
import org.apache.calcite.plan.RelRule;
import org.apache.calcite.rel.logical.LogicalFilter;
import org.apache.calcite.rel.rules.TransformationRule;
import org.apache.shardingsphere.infra.optimize.rel.logical.LogicalScan;

/**
 * Pushdown LogicalFilter to LogicalScan.
 */
public class PushFilterToScanRule extends RelRule<PushFilterToScanRule.Config> implements TransformationRule {
    
    public PushFilterToScanRule(final Config config) {
        super(config);
    }
    
    @Override
    public void onMatch(final RelOptRuleCall call) {
        LogicalFilter logicalFilter = call.rel(0);
        LogicalScan logicalScan = call.rel(1);
        logicalScan.pushdown(logicalFilter);
        call.transformTo(logicalScan);
    }
    
    public interface Config extends RelRule.Config {
        Config DEFAULT = EMPTY.withOperandSupplier(b0 -> b0.operand(LogicalFilter.class).inputs(b1 -> b1.operand(LogicalScan.class).anyInputs())).as(Config.class);
    
        @Override
        default PushFilterToScanRule toRule() {
            return new PushFilterToScanRule(this);
        }
    }
}
