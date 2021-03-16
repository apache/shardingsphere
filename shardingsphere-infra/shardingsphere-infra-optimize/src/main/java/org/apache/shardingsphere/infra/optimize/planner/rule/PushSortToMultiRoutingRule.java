package org.apache.shardingsphere.infra.optimize.planner.rule;

import org.apache.calcite.plan.RelOptRuleCall;
import org.apache.calcite.rel.logical.LogicalSort;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.shardingsphere.infra.optimize.rel.logical.LogicalScan;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSMergeSort;

import java.util.function.Predicate;

public class PushSortToMultiRoutingRule extends PushSortToScanRule {
    
    private static final Predicate<LogicalScan> NOT_SINGLE_ROUTING_PREDICATE = logicalScan -> !logicalScan.isSingleRouting();
    /**
     * Creates a RelRule.
     *
     * @param config
     */
    protected PushSortToMultiRoutingRule(final Config config) {
        super(config);
    }
    
    @Override
    public void onMatch(final RelOptRuleCall call) {
        LogicalSort sort = call.rel(0);
        LogicalScan scan = call.rel(1);
        RexBuilder rexBuilder = sort.getCluster().getRexBuilder();
        SSMergeSort mergeSort;
        if(sort.fetch == null) {
            LogicalScan logicalScan = pushdownSort(LogicalSort.create(scan, sort.getCollation(), null, null), scan);
            mergeSort = SSMergeSort.create(sort.getTraitSet(), logicalScan, sort.collation);
        } else {
            RexNode fetch = rexBuilder.makeCall(SqlStdOperatorTable.PLUS, sort.offset, sort.fetch);
            LogicalScan logicalScan = pushdownSort(LogicalSort.create(scan, sort.getCollation(), null, fetch), scan);
            mergeSort = SSMergeSort.create(sort.getTraitSet(), logicalScan, sort.collation, sort.offset, sort.fetch);
        }
        call.transformTo(mergeSort);
    }
    
    public interface Config extends PushSortToScanRule.Config {
    
        Config DEFAULT = EMPTY.as(Config.class).withOperandFor(LogicalSort.class, LogicalScan.class, NOT_SINGLE_ROUTING_PREDICATE, Config.class);;
    
        @Override
        default PushSortToMultiRoutingRule toRule() {
            return new PushSortToMultiRoutingRule(this);
        }
        
    }
}
