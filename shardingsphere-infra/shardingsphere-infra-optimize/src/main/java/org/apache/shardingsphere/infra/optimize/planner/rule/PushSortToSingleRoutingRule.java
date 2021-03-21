package org.apache.shardingsphere.infra.optimize.planner.rule;

import org.apache.calcite.rel.logical.LogicalSort;
import org.apache.shardingsphere.infra.optimize.rel.logical.LogicalScan;

public final class PushSortToSingleRoutingRule extends PushSortToScanRule {
    /**
     * Creates a RelRule.
     *
     * @param config config
     */
    protected PushSortToSingleRoutingRule(final Config config) {
        super(config);
    }
    
    public interface Config extends PushSortToScanRule.Config {
    
        Config DEFAULT = EMPTY.as(Config.class).withOperandFor(LogicalSort.class, LogicalScan.class, LogicalScan::isSingleRouting, Config.class);
        
        @Override
        default PushSortToScanRule toRule() {
            return new PushSortToSingleRoutingRule(this);
        }
        
    }
}
