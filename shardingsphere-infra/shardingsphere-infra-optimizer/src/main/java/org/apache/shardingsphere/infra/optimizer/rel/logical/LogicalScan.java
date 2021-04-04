/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.infra.optimizer.rel.logical;

import com.google.common.collect.Sets;
import org.apache.calcite.plan.RelOptTable;
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
import org.apache.shardingsphere.infra.optimizer.rel.AbstractScan;
import org.apache.shardingsphere.infra.optimizer.tools.OptimizerContext;
import org.apache.shardingsphere.infra.optimizer.tools.PushdownRelBuilder;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * LogicalScan represent a logical relation expression that can be pushed down to storage. For sharding database, we can
 * pushdown table scan with filter, project, sort and so on. We can also pushdown join that can be executed in the 
 * sharding database.  
 */
public final class LogicalScan extends AbstractScan {
    
    private PushdownRelBuilder relBuilder;
    
    protected LogicalScan(final TableScan tableScan) {
        super(tableScan.getCluster(), tableScan.getTraitSet());
        relBuilder = PushdownRelBuilder.create(tableScan);
        relBuilder.scan(tableScan.getTable().getQualifiedName());
        refreshRowType(tableScan);
        resetRouteContext();
    }
    
    /**
     * push down filter.
     * @param logicalFilter filter to pushdown
     * @return current <code>LogicalScan</code> instance
     */
    public LogicalScan pushdown(final LogicalFilter logicalFilter) {
        relBuilder.filter(logicalFilter.getVariablesSet(), logicalFilter.getCondition());
        refreshRowType(logicalFilter);
        resetRouteContext();
        return this;
    }
    
    /**
     * push down project.
     * @param logicalProject project operator to pushdown
     * @return current <code>LogicalScan</code> instance
     */
    public LogicalScan pushdown(final LogicalProject logicalProject) {
        relBuilder.project(logicalProject.getProjects(), logicalProject.getRowType().getFieldNames());
        refreshRowType(logicalProject);
        return this;
    }
    
    /**
     * pushdown join.
     * @param join join operator to pushdown
     * @param right right input of join
     * @return current <code>LogicalScan</code> instance
     */
    public LogicalScan pushdown(final LogicalJoin join, final RelNode right) {
        relBuilder.push(right);
        relBuilder.join(join.getJoinType(), join.getCondition(), join.getVariablesSet());
        refreshRowType(join);
        resetRouteContext();
        return this;
    }
    
    /**
     * pushdown aggregation.
     * @param logicalAgg aggregate operator
     * @return @return current <code>LogicalScan</code> instance
     */
    public LogicalScan pushdown(final LogicalAggregate logicalAgg) {
        GroupKey groupKey = relBuilder.groupKey(logicalAgg.getGroupSet(), logicalAgg.getGroupSets());
        relBuilder.aggregate(groupKey, logicalAgg.getAggCallList());
        refreshRowType(logicalAgg);
        return this;
    }
    
    /**
     * push down sort.
     * @param logicalSort sort operator to pushdown
     * @return current <code>LogicalScan</code> instance
     */
    public LogicalScan pushdown(final LogicalSort logicalSort) {
        List<RelFieldCollation> fieldCollations = logicalSort.getCollation().getFieldCollations();
        List<RexNode> sortRexNodes = fieldCollations.stream()
                .filter(collation -> collation.direction == Direction.ASCENDING || collation.direction == Direction.DESCENDING)
                .map(collation -> {
                    RexNode sortRexNode = RexInputRef.of(collation.getFieldIndex(), logicalSort.getRowType());
                    if (collation.direction == Direction.DESCENDING) {
                        sortRexNode = relBuilder.desc(sortRexNode);
                    }
                    if (collation.nullDirection == NullDirection.FIRST) {
                        sortRexNode = relBuilder.nullsFirst(sortRexNode);
                    } else if (collation.nullDirection == NullDirection.LAST) {
                        sortRexNode = relBuilder.nullsLast(sortRexNode);
                    }
                    return sortRexNode;
                }).collect(Collectors.toList());
        relBuilder.sortLimit(logicalSort.offset, logicalSort.fetch, sortRexNodes);
        refreshRowType(logicalSort);
        return this;
    }
    
    /**
     * Get all tables of this pushdown algebra expression.
     * @return table of this <code>LogicalScan</code> instance
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
    
    /**
     * refresh <code>RelDataType</code> of this <code>LogicalScan</code>.
     * @param relNode relNode to privide <code>RelDataType</code>
     */
    protected void refreshRowType(final RelNode relNode) {
        this.rowType = relNode.getRowType();
    }
    
    /**
     * Generate the whole rational operator that have been pushed down.
     * @return rational operator
     */
    public RelNode build() {
        return relBuilder.build();    
    }
    
    /**
     * Get the first rational operator.
     * @return rational operator
     */
    public RelNode peek() {
        return relBuilder.peek();
    }
    
    /**
     * If this <code>LogicalScan</code> will be executed in a single sharding.
     * @return true if this <code>LogicalScan</code> only route to a single sharding.
     */
    public boolean isSingleRouting() {
        if (getRouteContext() == null) {
            this.route(OptimizerContext.getCurrentOptimizerContext().get().getShardingRule());
        }
        return getRouteContext().isSingleRouting();
    }
    
    @Override
    public RouteContext route(final ShardingRule shardingRule) {
        return route(relBuilder.peek(), shardingRule);
    }
    
    /**
     * convert a <code>TableScan</code> operator to <code>LogicalScan</code>.
     * @param tableScan table scan operator
     * @return <code>LogicalScan</code> operator
     */
    public static LogicalScan create(final TableScan tableScan) {
        return new LogicalScan(tableScan);
    }
}
