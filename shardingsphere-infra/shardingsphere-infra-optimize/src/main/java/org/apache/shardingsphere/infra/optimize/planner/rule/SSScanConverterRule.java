package org.apache.shardingsphere.infra.optimize.planner.rule;

import org.apache.calcite.plan.Convention;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.convert.ConverterRule;
import org.apache.shardingsphere.infra.optimize.planner.ShardingSphereConvention;
import org.apache.shardingsphere.infra.optimize.rel.logical.LogicalScan;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSScan;

public final class SSScanConverterRule extends ConverterRule {
    
    public static final Config DEFAULT_CONFIG = Config.INSTANCE
            .withConversion(LogicalScan.class, Convention.NONE,
                    ShardingSphereConvention.INSTANCE, SSScanConverterRule.class.getName())
            .withRuleFactory(SSScanConverterRule::new);
    
    protected SSScanConverterRule(final Config config) {
        super(config);
    }
    
    
    @Override
    public RelNode convert(final RelNode rel) {
        LogicalScan logicalScan = (LogicalScan) rel;
        RelNode input = logicalScan.peek();
        SSScan scan = SSScan.create(logicalScan.getCluster(), logicalScan.getTraitSet(), input);
        return convert(scan, logicalScan.getTraitSet().replace(ShardingSphereConvention.INSTANCE));
    }
}
