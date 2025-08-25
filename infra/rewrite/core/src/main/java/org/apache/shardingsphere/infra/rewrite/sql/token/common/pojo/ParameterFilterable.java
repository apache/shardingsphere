package org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo;

import org.apache.shardingsphere.infra.route.context.RouteUnit;

import java.util.Set;

/**
 * Parameter filtering interface
 *
 * @author yinh
 */
public interface ParameterFilterable {
    
    /**
     * Obtain the index of the parameters that need to be removed
     * 
     * @param routeUnit Routing unit
     * @return The collection of parameter indexes that need to be removed
     */
    Set<Integer> getRemovedParameterIndices(RouteUnit routeUnit);
    
    /**
     * Determine whether parameter filtering is supported
     * 
     * @return true If parameter filtering is supported
     */
    default boolean isParameterFilterable() {
        return true;
    }
}