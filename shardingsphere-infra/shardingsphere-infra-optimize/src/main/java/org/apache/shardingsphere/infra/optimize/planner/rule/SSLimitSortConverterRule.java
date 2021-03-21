package org.apache.shardingsphere.infra.optimize.planner.rule;

import org.apache.calcite.plan.RelOptRuleCall;
import org.apache.calcite.plan.RelRule;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.Sort;
import org.apache.calcite.rel.logical.LogicalSort;
import org.apache.shardingsphere.infra.optimize.planner.ShardingSphereConvention;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSLimitSort;

public final class SSLimitSortConverterRule extends RelRule<SSLimitSortConverterRule.Config> {
    
    protected SSLimitSortConverterRule(final Config config) {
        super(config);
    }
    
    @Override
    public void onMatch(final RelOptRuleCall call) {
        final LogicalSort sort = call.rel(0);
        RelNode input = sort.getInput();
        final Sort o = SSLimitSort.create(
                convert(input, input.getTraitSet().replace(ShardingSphereConvention.INSTANCE)),
                sort.getCollation(),
                sort.offset, sort.fetch
        );
    
        call.transformTo(o);
    }
    
    public interface Config extends RelRule.Config {
        SSLimitSortConverterRule.Config DEFAULT = EMPTY.withOperandSupplier(
            b0 -> b0.operand(LogicalSort.class).predicate(sort -> sort.fetch != null).anyInputs())
                .as(SSLimitSortConverterRule.Config.class);
        
        @Override default SSLimitSortConverterRule toRule() {
            return new SSLimitSortConverterRule(this);
        }
    }
}
