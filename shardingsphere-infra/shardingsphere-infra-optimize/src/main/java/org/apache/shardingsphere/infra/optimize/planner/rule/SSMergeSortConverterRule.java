package org.apache.shardingsphere.infra.optimize.planner.rule;

import org.apache.calcite.plan.Convention;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.convert.ConverterRule;
import org.apache.shardingsphere.infra.optimize.planner.ShardingSphereConvention;
import org.apache.shardingsphere.infra.optimize.rel.logical.LogicalMergeSort;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSMergeSort;

public final class SSMergeSortConverterRule extends ConverterRule {
    
    public static final Config DEFAULT_CONFIG = Config.INSTANCE
            .withConversion(LogicalMergeSort.class, Convention.NONE,
                    ShardingSphereConvention.INSTANCE, SSMergeSortConverterRule.class.getName())
            .withRuleFactory(SSMergeSortConverterRule::new);
    
    protected SSMergeSortConverterRule(final Config config) {
        super(config);
    }
    
    @Override
    public RelNode convert(final RelNode rel) {
        LogicalMergeSort mergeSort = (LogicalMergeSort) rel;
        return SSMergeSort.create(mergeSort.getTraitSet(), convert(mergeSort.getInput(), out), 
                mergeSort.getCollation(), mergeSort.offset, mergeSort.fetch);
    }
}
