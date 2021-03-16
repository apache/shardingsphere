package org.apache.shardingsphere.infra.optimize.planner.rule;

import org.apache.calcite.plan.Convention;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.convert.ConverterRule;
import org.apache.calcite.rel.logical.LogicalCalc;
import org.apache.shardingsphere.infra.optimize.planner.ShardingSphereConvention;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSCalc;

public class SSCalcConverterRule extends ConverterRule {
    
    public static final Config DEFAULT_CONFIG = Config.INSTANCE
            .withConversion(LogicalCalc.class, Convention.NONE,
                    ShardingSphereConvention.INSTANCE, SSCalcConverterRule.class.getName())
            .withRuleFactory(SSCalcConverterRule::new);
    
    protected SSCalcConverterRule(final Config config) {
        super(config);
    }
    
    @Override
    public RelNode convert(final RelNode rel) {
        LogicalCalc calc = (LogicalCalc) rel;
        RelNode input = calc.getInput();
        return SSCalc.create(convert(input, input.getTraitSet().replace(ShardingSphereConvention.INSTANCE)), calc.getProgram());
    }
}
