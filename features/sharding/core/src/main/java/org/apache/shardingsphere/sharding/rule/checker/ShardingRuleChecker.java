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

package org.apache.shardingsphere.sharding.rule.checker;

import com.google.common.base.Splitter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.algorithm.core.exception.AlgorithmInitializationException;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datanode.DataNodeInfo;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.sharding.algorithm.sharding.inline.InlineShardingAlgorithm;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableReferenceRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.exception.metadata.DuplicateShardingActualDataNodeException;
import org.apache.shardingsphere.sharding.exception.metadata.InvalidBindingTablesException;
import org.apache.shardingsphere.sharding.exception.metadata.ShardingTableRuleNotFoundException;
import org.apache.shardingsphere.sharding.rule.BindingTableCheckedConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.ShardingTable;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ShardingRuleChecker {
    
    private final ShardingRule shardingRule;
    
    /**
     * Check sharding rule.
     *
     * @param ruleConfig sharding rule configuration
     */
    public void check(final ShardingRuleConfiguration ruleConfig) {
        checkUniqueActualDataNodesInTableRules();
        checkBindingTableConfiguration(ruleConfig);
        checkInlineShardingAlgorithmsInTableRules();
    }
    
    private void checkUniqueActualDataNodesInTableRules() {
        Collection<DataNode> uniqueActualDataNodes = new HashSet<>(shardingRule.getShardingTables().size(), 1F);
        shardingRule.getShardingTables().forEach((key, value) -> checkUniqueActualDataNodes(uniqueActualDataNodes, key, value.getActualDataNodes().iterator().next()));
    }
    
    private void checkUniqueActualDataNodes(final Collection<DataNode> uniqueActualDataNodes, final String logicTable, final DataNode sampleActualDataNode) {
        ShardingSpherePreconditions.checkNotContains(uniqueActualDataNodes, sampleActualDataNode,
                () -> new DuplicateShardingActualDataNodeException(logicTable, sampleActualDataNode.getDataSourceName(), sampleActualDataNode.getTableName()));
        uniqueActualDataNodes.add(sampleActualDataNode);
    }
    
    private void checkBindingTableConfiguration(final ShardingRuleConfiguration ruleConfig) {
        ShardingSpherePreconditions.checkState(
                isValidBindingTableConfiguration(shardingRule.getShardingTables(),
                        new BindingTableCheckedConfiguration(shardingRule.getDataSourceNames(), shardingRule.getShardingAlgorithms(), ruleConfig.getBindingTableGroups(),
                                shardingRule.getDefaultDatabaseShardingStrategyConfig(), shardingRule.getDefaultTableShardingStrategyConfig(), shardingRule.getDefaultShardingColumn())),
                InvalidBindingTablesException::new);
    }
    
    private boolean isValidBindingTableConfiguration(final Map<String, ShardingTable> shardingTables, final BindingTableCheckedConfiguration checkedConfig) {
        for (ShardingTableReferenceRuleConfiguration each : checkedConfig.getBindingTableGroups()) {
            Collection<String> bindingTables = Splitter.on(",").trimResults().splitToList(each.getReference());
            if (bindingTables.size() <= 1) {
                continue;
            }
            Iterator<String> iterator = bindingTables.iterator();
            ShardingTable sampleShardingTable = getShardingTable(iterator.next(), shardingTables);
            while (iterator.hasNext()) {
                ShardingTable shardingTable = getShardingTable(iterator.next(), shardingTables);
                if (!isValidActualDataSourceName(sampleShardingTable, shardingTable) || !isValidActualTableName(sampleShardingTable, shardingTable)) {
                    return false;
                }
                if (!isBindingShardingAlgorithm(sampleShardingTable, shardingTable, true, checkedConfig) || !isBindingShardingAlgorithm(sampleShardingTable, shardingTable, false, checkedConfig)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private ShardingTable getShardingTable(final String logicTableName, final Map<String, ShardingTable> shardingTables) {
        ShardingTable result = shardingTables.get(logicTableName);
        ShardingSpherePreconditions.checkNotNull(result, () -> new ShardingTableRuleNotFoundException(Collections.singleton(logicTableName)));
        return result;
    }
    
    private boolean isValidActualDataSourceName(final ShardingTable sampleShardingTable, final ShardingTable shardingTable) {
        return sampleShardingTable.getActualDataSourceNames().equals(shardingTable.getActualDataSourceNames());
    }
    
    private boolean isValidActualTableName(final ShardingTable sampleShardingTable, final ShardingTable shardingTable) {
        for (String each : sampleShardingTable.getActualDataSourceNames()) {
            Collection<String> sampleActualTableNames = sampleShardingTable.getActualTableNames(each).stream()
                    .map(actualTableName -> actualTableName.replace(sampleShardingTable.getTableDataNode().getPrefix(), "")).collect(Collectors.toSet());
            Collection<String> actualTableNames =
                    shardingTable.getActualTableNames(each).stream().map(optional -> optional.replace(shardingTable.getTableDataNode().getPrefix(), "")).collect(Collectors.toSet());
            if (!sampleActualTableNames.equals(actualTableNames)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean isBindingShardingAlgorithm(final ShardingTable sampleShardingTable, final ShardingTable shardingTable, final boolean databaseAlgorithm,
                                               final BindingTableCheckedConfiguration checkedConfig) {
        return getAlgorithmExpression(sampleShardingTable, databaseAlgorithm, checkedConfig).equals(getAlgorithmExpression(shardingTable, databaseAlgorithm, checkedConfig));
    }
    
    private Optional<String> getAlgorithmExpression(final ShardingTable shardingTable, final boolean databaseAlgorithm, final BindingTableCheckedConfiguration checkedConfig) {
        ShardingStrategyConfiguration shardingStrategyConfig = databaseAlgorithm
                ? shardingRule.getDatabaseShardingStrategyConfiguration(shardingTable)
                : shardingRule.getTableShardingStrategyConfiguration(shardingTable);
        ShardingAlgorithm shardingAlgorithm = checkedConfig.getShardingAlgorithms().get(shardingStrategyConfig.getShardingAlgorithmName());
        String dataNodePrefix = databaseAlgorithm ? shardingTable.getDataSourceDataNode().getPrefix() : shardingTable.getTableDataNode().getPrefix();
        String shardingColumn = getShardingColumn(shardingStrategyConfig, shardingRule.getDefaultShardingColumn());
        return null == shardingAlgorithm ? Optional.empty() : shardingAlgorithm.getAlgorithmStructure(dataNodePrefix, shardingColumn);
    }
    
    private String getShardingColumn(final ShardingStrategyConfiguration shardingStrategyConfig, final String defaultShardingColumn) {
        String shardingColumn = defaultShardingColumn;
        if (shardingStrategyConfig instanceof ComplexShardingStrategyConfiguration) {
            shardingColumn = ((ComplexShardingStrategyConfiguration) shardingStrategyConfig).getShardingColumns();
        }
        if (shardingStrategyConfig instanceof StandardShardingStrategyConfiguration) {
            shardingColumn = ((StandardShardingStrategyConfiguration) shardingStrategyConfig).getShardingColumn();
        }
        return null == shardingColumn ? "" : shardingColumn;
    }
    
    private void checkInlineShardingAlgorithmsInTableRules() {
        shardingRule.getShardingTables().forEach((key, value) -> {
            validateInlineShardingAlgorithm(value, shardingRule.getTableShardingStrategyConfiguration(value), value.getTableDataNode());
            validateInlineShardingAlgorithm(value, shardingRule.getDatabaseShardingStrategyConfiguration(value), value.getDataSourceDataNode());
        });
    }
    
    private void validateInlineShardingAlgorithm(final ShardingTable shardingTable, final ShardingStrategyConfiguration shardingStrategy, final DataNodeInfo dataNodeInfo) {
        if (null == shardingStrategy) {
            return;
        }
        ShardingAlgorithm shardingAlgorithm = shardingRule.getShardingAlgorithms().get(shardingStrategy.getShardingAlgorithmName());
        if (shardingAlgorithm instanceof InlineShardingAlgorithm) {
            String shardingColumn = null == ((StandardShardingStrategyConfiguration) shardingStrategy).getShardingColumn() ? shardingRule.getDefaultShardingColumn()
                    : ((StandardShardingStrategyConfiguration) shardingStrategy).getShardingColumn();
            String result = null;
            try {
                result = ((InlineShardingAlgorithm) shardingAlgorithm).doSharding(Collections.emptySet(), new PreciseShardingValue<>(shardingTable.getLogicTable(), shardingColumn, dataNodeInfo, 1));
                // CHECKSTYLE:OFF
            } catch (final Exception ignored) {
                // CHECKSTYLE:ON
            }
            ShardingSpherePreconditions.checkState(null == result || result.startsWith(dataNodeInfo.getPrefix()),
                    () -> new AlgorithmInitializationException(shardingAlgorithm, "`%s` sharding algorithm configuration of `%s` does not match the actual data nodes",
                            shardingStrategy.getShardingAlgorithmName(), shardingTable.getLogicTable()));
        }
    }
    
    /**
     * Check to be added data nodes.
     *
     * @param toBeAddedDataNodes to be added data nodes
     * @param isAlteration is alteration
     */
    public void checkToBeAddedDataNodes(final Map<String, Collection<DataNode>> toBeAddedDataNodes, final boolean isAlteration) {
        Collection<DataNode> uniqueActualDataNodes = new HashSet<>(shardingRule.getShardingTables().size() + toBeAddedDataNodes.size(), 1F);
        shardingRule.getShardingTables().forEach((key, value) -> {
            if (isAlteration && toBeAddedDataNodes.containsKey(key)) {
                return;
            }
            checkUniqueActualDataNodes(uniqueActualDataNodes, key, value.getActualDataNodes().iterator().next());
        });
        toBeAddedDataNodes.forEach((key, value) -> checkUniqueActualDataNodes(uniqueActualDataNodes, key, value.iterator().next()));
    }
}
