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

package org.apache.shardingsphere.infra.optimizer.planner.rule;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.calcite.plan.RelOptRuleCall;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.plan.RelRule;
import org.apache.calcite.prepare.RelOptTableImpl;
import org.apache.calcite.rel.logical.LogicalJoin;
import org.apache.calcite.rel.rules.TransformationRule;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.shardingsphere.infra.optimizer.rel.logical.LogicalScan;
import org.apache.shardingsphere.infra.optimizer.tools.OptimizerContext;
import org.apache.shardingsphere.sharding.rule.BindingTableRule;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.single.SingleTableRule;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Push down join for Binding tables now. 
 * This rule can only be used for sharding database mod now. 
 * But partition-aware optimization will be applied in the future according to paper
 * [Query Optimization Techniques for Partitioned Tables].
 */
public final class PushJoinToScanRule extends RelRule<PushJoinToScanRule.Config> implements TransformationRule {

    /**
     * Creates a RelRule.
     *
     * @param config {@link Config}
     */
    public PushJoinToScanRule(final Config config) {
        super(config);
    }

    @Override
    public void onMatch(final RelOptRuleCall call) {
        LogicalJoin logicalJoin = call.rel(0);
        LogicalScan left = call.rel(1);
        LogicalScan right = call.rel(2);
        
        LogicalScan res = convert(logicalJoin, left, right);
        if (res == null) {
            return;
        }
        call.transformTo(res);
    }
    
    private LogicalScan convert(final LogicalJoin logicalJoin, final LogicalScan left, final LogicalScan right) {
        Optional<OptimizerContext> optimizerContext = OptimizerContext.getCurrentOptimizerContext();
        if (!optimizerContext.isPresent()) {
            return null;
        }
        
        Set<RelOptTable> leftTables = left.getTables();
        Set<RelOptTable> rightTables = right.getTables();
        if (CollectionUtils.isEmpty(leftTables) || CollectionUtils.isEmpty(rightTables)) {
            return null;
        }
        
        Map<String, List<String>> schemaTablesMap = parseAndMergeTableNames(leftTables, rightTables);
        // only if they are in the same logical schema
        if (schemaTablesMap.keySet().size() > 1) {
            return null;
        }
        
        if (canPushdown(schemaTablesMap)) {
            return left.pushdown(logicalJoin, right.build());
        }
        
        return null;
        
    }
    
    /**
     * if all table are  broadcast tables and binding tables or broadcast table and single table in the save datasource.
     * @param schemaTablesMap table in left and right input of LogicalJoin
     * @return true if can be pushdown, else false
     */
    private boolean canPushdown(final Map<String, List<String>> schemaTablesMap) {
        List<String> tableNames = schemaTablesMap.values().stream().flatMap(List::stream).collect(Collectors.toList());
    
        ShardingRule shardingRule = OptimizerContext.getCurrentOptimizerContext().get().getShardingRule();
        // Binding tables or broadcast tables
        Collection<BindingTableRule> bindingTableRules = shardingRule.getBindingTableRules();
        Collection<String> broadcastTableNames = shardingRule.getBroadcastTables();
    
        List<String> tablesWithoutBroadcast = tableNames.stream().filter(table -> !broadcastTableNames.contains(table)).collect(Collectors.toList());
    
        if (allBindingTables(tablesWithoutBroadcast, bindingTableRules)) {
            return true;
        }
    
        return allSingleTableInSameDataNode(tablesWithoutBroadcast, shardingRule.getSingleTableRules());
    }
    
    private boolean allBindingTables(final List<String> tablesNotBroadcase, final Collection<BindingTableRule> bindingTableRules) {
        for (BindingTableRule bindingTableRule : bindingTableRules) {
            List<String> bindingTableNames = bindingTableRule.getTableRules().stream().map(input -> input.getLogicTable().toLowerCase()).collect(Collectors.toList());
            boolean allBindings = bindingTableNames.containsAll(tablesNotBroadcase);
            if (allBindings) {
                return true;
            }
        }
        return false;
    }
    
    private boolean allSingleTableInSameDataNode(final List<String> tablesWithoutBroadcast, final Map<String, SingleTableRule> singleTableRuleMap) {
        if (!singleTableRuleMap.keySet().containsAll(tablesWithoutBroadcast)) {
            return false;
        }
        
        long dataSourceCount = tablesWithoutBroadcast.stream().map(singleTableRuleMap::get)
                                .map(SingleTableRule::getDataSourceName).distinct().count();
        return dataSourceCount == 1;
    }
    
    /**
     * Parse schema name and table name from left and right RelOptTables, then merge then by schema name.
     * @param leftTables tables to be merge
     * @param rightTables tables to be merge
     * @return merged schema name and table name mapping
     */
    private Map<String, List<String>> parseAndMergeTableNames(final Set<RelOptTable> leftTables, final Set<RelOptTable> rightTables) {
        Map<String, List<String>> schemaTablesMap = Maps.newHashMap();
        schemaTablesMap.putAll(splitSchemaAndTableNames(leftTables));
        Map<String, List<String>> rightTableNames = splitSchemaAndTableNames(rightTables);
        rightTableNames.forEach((key, value) -> {
            List<String> tableNames = schemaTablesMap.getOrDefault(key, Lists.newArrayList());
            tableNames.addAll(value);
            schemaTablesMap.put(key, tableNames);
        });
        return schemaTablesMap;
    }
    
    /**
     * split schema name and table name from qualified name of RelOptTable.
     * NOTE: <br/>
     * The qualified name of a RelOptTable is a list containing schema and table, for more detail, please refer to 
     * {@link RelOptTableImpl#create(org.apache.calcite.plan.RelOptSchema, org.apache.calcite.rel.type.RelDataType, 
     * org.apache.calcite.schema.Table, org.apache.calcite.schema.Path)} and {@link org.apache.calcite.schema.Path} for 
     * 
     * @param tables tables
     * @return schema and tables mapping
     */
    private Map<String, List<String>> splitSchemaAndTableNames(final Set<RelOptTable> tables) {
        return tables.stream().map(RelOptTable::getQualifiedName).map(list -> {
            if (list.size() == 1) {
                return new SimpleEntry<>("", list.get(0));
            } else {
                return new SimpleEntry<>(list.get(list.size() - 2), list.get(list.size() - 1));
            }
        }).collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
    }

    public interface Config extends RelRule.Config {
        Config DEFAULT = EMPTY.withOperandSupplier(b0 -> b0.operand(LogicalJoin.class).inputs(b1 -> b1.operand(LogicalScan.class).anyInputs(), 
            b2 -> b2.operand(LogicalScan.class).anyInputs())).as(Config.class);
        
        @Override
        default PushJoinToScanRule toRule() {
            return new PushJoinToScanRule(this);
        }
    }

}
