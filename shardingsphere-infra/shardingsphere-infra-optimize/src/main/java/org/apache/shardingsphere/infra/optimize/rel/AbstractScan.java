package org.apache.shardingsphere.infra.optimize.rel;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.AbstractRelNode;
import org.apache.calcite.rel.RelNode;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingCondition;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.type.ShardingRouteEngine;
import org.apache.shardingsphere.sharding.route.engine.type.complex.ShardingComplexRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.type.standard.ShardingStandardRoutingEngine;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public abstract class AbstractScan extends AbstractRelNode {
    
    @Getter(AccessLevel.PROTECTED)
    private RouteContext routeContext;

    public AbstractScan(final RelOptCluster cluster, final RelTraitSet traitSet) {
        super(cluster, traitSet);
    }
    
    /**
     * reset RouteContext.
     */
    protected void resetRouteContext() {
        routeContext = null;
    }
    
    /**
     * Route current <code>RelNode</code>.
     * @param shardingRule table sharding rule.
     * @return <code>RouteContext</code>
     */
    public abstract RouteContext route(ShardingRule shardingRule);
    
    /**
     * Route this <code>LogicalScan</code>. 
     * @return route context
     */
    protected RouteContext route(final RelNode relNode, final ShardingRule shardingRule) {
        if (routeContext != null) {
            return this.routeContext;
        }
        Map<String, List<ShardingConditionValue>> map = TableAndRexShuttle.getTableAndShardingCondition(relNode, shardingRule);
        List<ShardingCondition> shardingConditions = map.values().stream().map(item -> {
            ShardingCondition result = new ShardingCondition();
            result.getValues().addAll(item);
            return result;
        }).collect(Collectors.toList());
        ShardingRouteEngine shardingRouteEngine = getShardingRouteEngine(shardingRule,
                new ShardingConditions(shardingConditions), map.keySet(),
                new ConfigurationProperties(new Properties()));
        RouteContext routeContext = new RouteContext();
        shardingRouteEngine.route(routeContext, shardingRule);
        this.routeContext = routeContext;
        return this.routeContext;
    }
    
    private ShardingRouteEngine getShardingRouteEngine(final ShardingRule shardingRule, final ShardingConditions shardingConditions,
                                                       final Collection<String> tableNames, final ConfigurationProperties props) {
        Collection<String> shardingTableNames = shardingRule.getShardingLogicTableNames(tableNames);
        if (shardingTableNames.size() == 1 || shardingRule.isAllBindingTables(shardingTableNames)) {
            return new ShardingStandardRoutingEngine(shardingTableNames.iterator().next(), shardingConditions, props);
        }
        return new ShardingComplexRoutingEngine(tableNames, shardingConditions, props);
    }
}
