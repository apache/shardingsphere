package org.apache.shardingsphere.infra.optimize.planner.rule;

public interface Rules {
    
    PushJoinToScanRule JOIN_TO_SCAN_RULE = PushJoinToScanRule.Config.DEFAULT.toRule();
    
    PushFilterToScanRule PUSH_FILTER_TO_SCAN_RULE = PushFilterToScanRule.Config.DEFAULT.toRule();
    
    PushProjectToScanRule PUSH_PROJECT_TO_SCAN_RULE = PushProjectToScanRule.Config.DEFAULT.toRule();
    
    PushAggToScanRule PUSH_AGG_TO_SCAN_RULE = PushAggToScanRule.Config.DEFAULT.toRule();
    
    PushSortToScanRule PUSH_SORT_TO_SINGLE_ROUTING_RULE = PushSortToSingleRoutingRule.Config.DEFAULT.toRule();
    
    PushSortToScanRule PUSH_SORT_TO_MULTI_ROUTING_RULE = PushSortToMultiRoutingRule.Config.DEFAULT.toRule();
    
}
