package io.shardingjdbc.core.routing.type;

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
