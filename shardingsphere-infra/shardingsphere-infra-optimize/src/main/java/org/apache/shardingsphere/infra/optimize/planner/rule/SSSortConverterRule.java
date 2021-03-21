package org.apache.shardingsphere.infra.optimize.planner.rule;

import org.apache.calcite.plan.Convention;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.convert.ConverterRule;
import org.apache.calcite.rel.logical.LogicalSort;
import org.apache.shardingsphere.infra.optimize.planner.ShardingSphereConvention;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSSort;

public class SSSortConverterRule extends ConverterRule {
    
    public static final Config DEFAULT_CONFIG = Config.INSTANCE
            .withConversion(LogicalSort.class, Convention.NONE,
                    ShardingSphereConvention.INSTANCE, SSSortConverterRule.class.getName())
            .withRuleFactory(SSSortConverterRule::new);
    
    protected SSSortConverterRule(final Config config) {
        super(config);
    }
    
    @Override
    public final RelNode convert(final RelNode rel) {
        LogicalSort sort = (LogicalSort) rel;
        if (sort.fetch != null || sort.offset != null) {
            return null;
        }
        return SSSort.create(convert(sort.getInput(), out), sort.getCollation());
    }
}
