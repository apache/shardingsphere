package org.apache.shardingsphere.infra.optimize.planner.rule;

import org.apache.calcite.plan.Convention;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.convert.ConverterRule;
import org.apache.calcite.rel.logical.LogicalJoin;
import org.apache.shardingsphere.infra.optimize.planner.ShardingSphereConvention;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSNestedLoopJoin;

import java.util.ArrayList;
import java.util.List;

public class SSNestedLoopJoinConverterRule extends ConverterRule {
    
    public static final Config DEFAULT_CONFIG = Config.INSTANCE
            .withConversion(LogicalJoin.class, Convention.NONE,
                    ShardingSphereConvention.INSTANCE, SSNestedLoopJoinConverterRule.class.getName())
            .withRuleFactory(SSNestedLoopJoinConverterRule::new);
    
    protected SSNestedLoopJoinConverterRule(final Config config) {
        super(config);
    }
    
    @Override
    public RelNode convert(final RelNode rel) {
        LogicalJoin join = (LogicalJoin) rel;
        List<RelNode> newInputs = new ArrayList<>();
        for (RelNode input : join.getInputs()) {
            if (!(input.getConvention() instanceof ShardingSphereConvention)) {
                input = convert(input, input.getTraitSet().replace(ShardingSphereConvention.INSTANCE));
            }
            newInputs.add(input);
        }
        final RelNode left = newInputs.get(0);
        final RelNode right = newInputs.get(1);
        return SSNestedLoopJoin.create(left, right, join.getCondition(), join.getVariablesSet(), join.getJoinType());
    }
}
