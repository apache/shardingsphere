package org.apache.shardingsphere.infra.optimize.planner.rule;

import org.apache.calcite.plan.RelOptRuleCall;
import org.apache.calcite.plan.RelRule;
import org.apache.calcite.rel.logical.LogicalProject;
import org.apache.calcite.rel.rules.TransformationRule;
import org.apache.shardingsphere.infra.optimize.rel.logical.LogicalScan;

/**
 * Pushdown Project to LogicalScan
 */
public class PushProjectToScanRule extends RelRule<PushProjectToScanRule.Config> implements TransformationRule {
    
    public static PushProjectToScanRule INSTANCE = Config.DEFAULT.toRule();
    
    /**
     * Creates a RelRule.
     *
     * @param config
     */
    protected PushProjectToScanRule(final Config config) {
        super(config);
    }
    
    @Override
    public void onMatch(final RelOptRuleCall call) {
        LogicalProject logicalProject = call.rel(0);
        LogicalScan logicalScan = call.rel(1);
        logicalScan.pushdown(logicalProject);
        call.transformTo(logicalScan);
    }
    
    public interface Config extends RelRule.Config {
        Config DEFAULT = EMPTY.withOperandSupplier(b0 -> b0.operand(LogicalProject.class)
                .oneInput(b1 -> b1.operand(LogicalScan.class).anyInputs())).as(Config.class);
    
        @Override
        default PushProjectToScanRule toRule() {
            return new PushProjectToScanRule(this);
        }
    }
}
