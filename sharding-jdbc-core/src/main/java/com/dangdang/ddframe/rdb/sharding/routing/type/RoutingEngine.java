package com.dangdang.ddframe.rdb.sharding.routing.type;

/**
 * 路由引擎接口.
 *
 * @author zhangliang
 */
public interface RoutingEngine {
    
    /**
     * 路由.
     *
     * @return 路由结果
     */
    RoutingResult route();
}
