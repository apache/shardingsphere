package org.apache.shardingsphere.infra.optimize.planner.rule;

import org.apache.calcite.plan.Convention;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.convert.ConverterRule;
import org.apache.calcite.rel.logical.LogicalAggregate;
import org.apache.shardingsphere.infra.optimize.planner.ShardingSphereConvention;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSHashAggregate;

public class SSHashAggregateConverterRule extends ConverterRule {
    
    public static final Config DEFAULT_CONFIG = Config.INSTANCE
            .withConversion(LogicalAggregate.class, Convention.NONE,
                    ShardingSphereConvention.INSTANCE, SSHashAggregateConverterRule.class.getName())
            .withRuleFactory(SSHashAggregateConverterRule::new);
    
    protected SSHashAggregateConverterRule(final Config config) {
        super(config);
    }
    
    @Override
    public RelNode convert(final RelNode rel) {
        final LogicalAggregate agg = (LogicalAggregate) rel;
        final RelTraitSet traitSet = rel.getCluster()
                .traitSet().replace(ShardingSphereConvention.INSTANCE);
            return SSHashAggregate.create(rel.getCluster(), traitSet, convert(agg.getInput(), traitSet), 
                    agg.getGroupSet(), agg.getGroupSets(), agg.getAggCallList());
        
    }
}
