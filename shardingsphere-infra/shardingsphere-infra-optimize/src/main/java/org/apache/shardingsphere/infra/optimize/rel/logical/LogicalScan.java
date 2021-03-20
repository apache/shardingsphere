package org.apache.shardingsphere.infra.optimize.rel.logical;

import com.google.common.collect.Sets;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.rel.AbstractRelNode;
import org.apache.calcite.rel.RelFieldCollation;
import org.apache.calcite.rel.RelFieldCollation.Direction;
import org.apache.calcite.rel.RelFieldCollation.NullDirection;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelShuttleImpl;
import org.apache.calcite.rel.core.TableScan;
import org.apache.calcite.rel.logical.LogicalAggregate;
import org.apache.calcite.rel.logical.LogicalFilter;
import org.apache.calcite.rel.logical.LogicalJoin;
import org.apache.calcite.rel.logical.LogicalProject;
import org.apache.calcite.rel.logical.LogicalSort;
import org.apache.calcite.rex.RexInputRef;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.tools.RelBuilder.GroupKey;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.optimize.planner.ShardingSphereConvention;
import org.apache.shardingsphere.infra.optimize.rel.TableAndRexShuttle;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSRel;
import org.apache.shardingsphere.infra.optimize.tools.OptimizerContext;
import org.apache.shardingsphere.infra.optimize.tools.PushdownRelBuilder;
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
import java.util.Set;
import java.util.stream.Collectors;

/**
 * LogicalScan represent a logical relation expression that can be pushed down to storage. For sharding database, we can
 * pushdown table scan with filter, project, sort and so on. We can also pushdown join that can be executed in the 
 * sharding database.  
 */
public class LogicalScan extends AbstractRelNode implements SSRel {
    
    private PushdownRelBuilder relBuilder;
    
    private RouteContext routeContext;
    
    protected LogicalScan(TableScan tableScan) {
        super(tableScan.getCluster(), tableScan.getTraitSet());
        this.traitSet = this.getTraitSet().replace(ShardingSphereConvention.INSTANCE);
        relBuilder = PushdownRelBuilder.create(tableScan);
        relBuilder.scan(tableScan.getTable().getQualifiedName());
        refreshRowType(tableScan);
        resetRouteContext();
    }
    
    public LogicalScan pushdown(LogicalFilter logicalFilter) {
        relBuilder.filter(logicalFilter.getVariablesSet(), logicalFilter.getCondition());
        refreshRowType(logicalFilter);
        resetRouteContext();
        return this;
    }
    
    public LogicalScan pushdown(LogicalProject logicalProject) {
        relBuilder.project(logicalProject.getProjects(), logicalProject.getRowType().getFieldNames());
        refreshRowType(logicalProject);
        return this;
    }
    
    /**
     * pushdown join 
     * @param join
     * @param right
     * @return
     */
    public LogicalScan pushdown(LogicalJoin join, RelNode right) {
        relBuilder.push(right);
        relBuilder.join(join.getJoinType(), join.getCondition(), join.getVariablesSet());
        refreshRowType(join);
        resetRouteContext();
        return this;
    }
    
    public LogicalScan pushdown(LogicalAggregate logicalAgg) {
        GroupKey groupKey = relBuilder.groupKey(logicalAgg.getGroupSet(), logicalAgg.getGroupSets());
        relBuilder.aggregate(groupKey, logicalAgg.getAggCallList());
        refreshRowType(logicalAgg);
        return this;
    }
    
    public LogicalScan pushdown(LogicalSort logicalSort) {
        List<RelFieldCollation> fieldCollations = logicalSort.getCollation().getFieldCollations();
        List<RexNode> sortRexNodes = fieldCollations.stream()
                .filter(collation -> collation.direction == Direction.ASCENDING || collation.direction == Direction.DESCENDING)
                .map(collation -> {
                    RexNode sortRexNode = RexInputRef.of(collation.getFieldIndex(), logicalSort.getRowType());
                    if(collation.direction == Direction.DESCENDING) {
                        sortRexNode = relBuilder.desc(sortRexNode);
                    }
                    if(collation.nullDirection == NullDirection.FIRST) {
                        sortRexNode = relBuilder.nullsFirst(sortRexNode);
                    } else if(collation.nullDirection == NullDirection.LAST) {
                        sortRexNode = relBuilder.nullsLast(sortRexNode);
                    }
                    return  sortRexNode;
                }).collect(Collectors.toList());
        relBuilder.sortLimit(logicalSort.offset, logicalSort.fetch, sortRexNodes);
        refreshRowType(logicalSort);
        return this;
    }
    
    /**
     * get all tables of this pushdown algebra expression
     * @return
     */
    public Set<RelOptTable> getTables() {
        Set<RelOptTable> tables = Sets.newHashSet();
        RelNode relNode = relBuilder.peek();
        relNode.accept(new RelShuttleImpl() {
            @Override
            public RelNode visit(final TableScan scan) {
                tables.add(scan.getTable());
                return super.visit(scan);
            }
        });
        return tables;
    }
    
    protected void refreshRowType(RelNode relNode) {
        this.rowType = relNode.getRowType();
    }
    
    /**
     * 获取下推完成的逻辑表达式，用于后续的下推 SQL 生成或者
     * @return
     */
    public RelNode build() {
        return relBuilder.build();    
    }
    
    private void resetRouteContext() {
        routeContext = null;
    }
    
    public boolean isSingleRouting() {
        if(routeContext == null) {
            this.route();
        }
        return routeContext.isSingleRouting();
    }
    
    public RouteContext route() {
        if(routeContext != null) {
            return this.routeContext;
        }
        ShardingRule shardingRule = OptimizerContext.getCurrentOptimizerContext().get().getShardingRule();
        Map<String, List<ShardingConditionValue>> map = TableAndRexShuttle.getTableAndShardingCondition(relBuilder.peek(), shardingRule);
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
    
    private ShardingRouteEngine getShardingRouteEngine(ShardingRule shardingRule, ShardingConditions shardingConditions,
                                                              Collection<String> tableNames, ConfigurationProperties props) {
        Collection<String> shardingTableNames = shardingRule.getShardingLogicTableNames(tableNames);
        if(shardingTableNames.size() == 1 || shardingRule.isAllBindingTables(shardingTableNames)) {
            return new ShardingStandardRoutingEngine(shardingTableNames.iterator().next(), shardingConditions, props);
        }
        return new ShardingComplexRoutingEngine(tableNames, shardingConditions, props);
    }
    
    public static LogicalScan create(TableScan tableScan) {
        return new LogicalScan(tableScan);
    }
}
