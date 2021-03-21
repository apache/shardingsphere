package org.apache.shardingsphere.infra.optimize.planner.rule;

import org.apache.calcite.plan.RelOptRuleCall;
import org.apache.calcite.plan.RelRule;
import org.apache.calcite.rel.logical.LogicalAggregate;
import org.apache.calcite.rel.rules.TransformationRule;
import org.apache.calcite.sql.SqlKind;
import org.apache.shardingsphere.infra.optimize.rel.logical.LogicalScan;

public final class PushAggToScanRule extends RelRule<PushAggToScanRule.Config> implements TransformationRule {
    
    /**
     * Creates a RelRule.
     *
     * @param config config
     */
    protected PushAggToScanRule(final Config config) {
        super(config);
    }
    
    @Override
    public void onMatch(final RelOptRuleCall call) {
        LogicalAggregate logicalAgg = call.rel(0);
        if (pushAgg(logicalAgg)) {
            return;
        }
        LogicalScan logicalScan = call.rel(1);
        logicalScan.pushdown(logicalAgg);
        call.transformTo(logicalScan);
    }
    
    private boolean pushAgg(final LogicalAggregate logicalAgg) {
        boolean containSingleValue = logicalAgg.getAggCallList().stream().anyMatch(aggCall -> aggCall.getAggregation().getKind() == SqlKind.SINGLE_VALUE);
        return !containSingleValue;
    }
    
    public interface Config extends RelRule.Config {
    
        Config DEFAULT = EMPTY.withOperandSupplier(b0 -> b0.operand(LogicalAggregate.class)
                .oneInput(b1 -> b1.operand(LogicalScan.class).anyInputs())).as(Config.class);
    
        @Override
        default PushAggToScanRule toRule() {
            return new PushAggToScanRule(this);
        }
    }
}
