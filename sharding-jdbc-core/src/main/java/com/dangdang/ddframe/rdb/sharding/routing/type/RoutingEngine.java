package com.dangdang.ddframe.rdb.sharding.routing.type;

/**
 * Routing engine interface.
 *
 * @author zhangliang
 */
public interface RoutingEngine {
    
    /**
     * Route.
     *
     * @return routing result
     */
    RoutingResult route();
}
