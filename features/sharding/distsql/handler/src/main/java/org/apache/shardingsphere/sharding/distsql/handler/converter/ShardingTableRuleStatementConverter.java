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

package org.apache.shardingsphere.sharding.distsql.handler.converter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.exception.AlgorithmInitializationException;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datanode.DataNodeUtils;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.expr.entry.InlineExpressionParserFactory;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.sharding.ShardingAutoTableAlgorithm;
import org.apache.shardingsphere.sharding.distsql.handler.enums.ShardingStrategyLevelType;
import org.apache.shardingsphere.sharding.distsql.handler.enums.ShardingStrategyType;
import org.apache.shardingsphere.sharding.distsql.segment.strategy.AuditStrategySegment;
import org.apache.shardingsphere.sharding.distsql.segment.strategy.KeyGenerateStrategySegment;
import org.apache.shardingsphere.sharding.distsql.segment.strategy.ShardingAuditorSegment;
import org.apache.shardingsphere.sharding.distsql.segment.strategy.ShardingStrategySegment;
import org.apache.shardingsphere.sharding.distsql.segment.table.AbstractTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.segment.table.AutoTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.segment.table.TableRuleSegment;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Sharding table rule converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingTableRuleStatementConverter {
    
    /**
     * Convert sharding table rule segments to sharding rule configuration.
     *
     * @param rules sharding table rule statements
     * @return sharding rule configuration
     */
    public static ShardingRuleConfiguration convert(final Collection<AbstractTableRuleSegment> rules) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        for (AbstractTableRuleSegment each : rules) {
            result.getKeyGenerators().putAll(createKeyGeneratorConfiguration(each));
            result.getAuditors().putAll(createAuditorConfiguration(each));
            if (each instanceof AutoTableRuleSegment) {
                result.getShardingAlgorithms().putAll(createAlgorithmConfiguration((AutoTableRuleSegment) each));
                result.getAutoTables().add(createAutoTableRuleConfiguration((AutoTableRuleSegment) each));
            }
            if (each instanceof TableRuleSegment) {
                result.getShardingAlgorithms().putAll(createAlgorithmConfiguration((TableRuleSegment) each));
                result.getTables().add(createTableRuleConfiguration((TableRuleSegment) each));
            }
        }
        return result;
    }
    
    private static Map<String, AlgorithmConfiguration> createKeyGeneratorConfiguration(final AbstractTableRuleSegment ruleSegment) {
        Map<String, AlgorithmConfiguration> result = new HashMap<>();
        Optional.ofNullable(ruleSegment.getKeyGenerateStrategySegment())
                .ifPresent(optional -> result.put(getKeyGeneratorName(ruleSegment.getLogicTable(), optional.getKeyGenerateAlgorithmSegment().getName()),
                        createAlgorithmConfiguration(optional.getKeyGenerateAlgorithmSegment())));
        return result;
    }
    
    private static Map<String, AlgorithmConfiguration> createAuditorConfiguration(final AbstractTableRuleSegment ruleSegment) {
        Map<String, AlgorithmConfiguration> result = new HashMap<>();
        Optional.ofNullable(ruleSegment.getAuditStrategySegment()).ifPresent(optional -> {
            for (ShardingAuditorSegment each : optional.getAuditorSegments()) {
                result.put(each.getAuditorName(), new AlgorithmConfiguration(each.getAlgorithmSegment().getName(), each.getAlgorithmSegment().getProps()));
            }
        });
        return result;
    }
    
    private static Map<String, AlgorithmConfiguration> createAlgorithmConfiguration(final AutoTableRuleSegment ruleSegment) {
        Map<String, AlgorithmConfiguration> result = new HashMap<>();
        Optional.ofNullable(ruleSegment.getShardingAlgorithmSegment())
                .ifPresent(optional -> result.put(getAutoTableShardingAlgorithmName(ruleSegment.getLogicTable(), optional.getName()), createAlgorithmConfiguration(optional)));
        return result;
    }
    
    private static Map<String, AlgorithmConfiguration> createAlgorithmConfiguration(final TableRuleSegment ruleSegment) {
        Map<String, AlgorithmConfiguration> result = new HashMap<>();
        if (null != ruleSegment.getTableStrategySegment()) {
            Optional.ofNullable(ruleSegment.getTableStrategySegment().getShardingAlgorithm())
                    .ifPresent(optional -> result.put(getTableShardingAlgorithmName(ruleSegment.getLogicTable(), ShardingStrategyLevelType.TABLE, optional.getName()),
                            createAlgorithmConfiguration(optional)));
        }
        if (null != ruleSegment.getDatabaseStrategySegment()) {
            Optional.ofNullable(ruleSegment.getDatabaseStrategySegment().getShardingAlgorithm())
                    .ifPresent(optional -> result.put(getTableShardingAlgorithmName(ruleSegment.getLogicTable(), ShardingStrategyLevelType.DATABASE, optional.getName()),
                            createAlgorithmConfiguration(optional)));
        }
        return result;
    }
    
    /**
     * Create algorithm configuration.
     *
     * @param algorithmSegment algorithm segment
     * @return ShardingSphere algorithm configuration
     */
    public static AlgorithmConfiguration createAlgorithmConfiguration(final AlgorithmSegment algorithmSegment) {
        return new AlgorithmConfiguration(algorithmSegment.getName().toLowerCase(), algorithmSegment.getProps());
    }
    
    private static ShardingAutoTableRuleConfiguration createAutoTableRuleConfiguration(final AutoTableRuleSegment ruleSegment) {
        ShardingAutoTableRuleConfiguration result = new ShardingAutoTableRuleConfiguration(ruleSegment.getLogicTable(), String.join(",", ruleSegment.getDataSourceNodes()));
        result.setShardingStrategy(createAutoTableStrategyConfiguration(ruleSegment));
        Optional.ofNullable(ruleSegment.getKeyGenerateStrategySegment())
                .ifPresent(optional -> result.setKeyGenerateStrategy(createKeyGenerateStrategyConfiguration(ruleSegment.getLogicTable(), ruleSegment.getKeyGenerateStrategySegment())));
        Optional.ofNullable(ruleSegment.getAuditStrategySegment())
                .ifPresent(optional -> result.setAuditStrategy(createShardingAuditStrategyConfiguration(ruleSegment.getAuditStrategySegment())));
        return result;
    }
    
    private static ShardingStrategyConfiguration createAutoTableStrategyConfiguration(final AutoTableRuleSegment ruleSegment) {
        return createStrategyConfiguration(ShardingStrategyType.STANDARD.name(),
                ruleSegment.getShardingColumn(), getAutoTableShardingAlgorithmName(ruleSegment.getLogicTable(), ruleSegment.getShardingAlgorithmSegment().getName()));
    }
    
    private static ShardingTableRuleConfiguration createTableRuleConfiguration(final TableRuleSegment ruleSegment) {
        String dataSourceNodes = String.join(",", ruleSegment.getDataSourceNodes());
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration(ruleSegment.getLogicTable(), dataSourceNodes);
        Optional.ofNullable(ruleSegment.getTableStrategySegment())
                .ifPresent(optional -> result.setTableShardingStrategy(createShardingStrategyConfiguration(ruleSegment.getLogicTable(),
                        ShardingStrategyLevelType.TABLE, optional.getType(), optional)));
        Optional.ofNullable(ruleSegment.getDatabaseStrategySegment())
                .ifPresent(optional -> result.setDatabaseShardingStrategy(createShardingStrategyConfiguration(ruleSegment.getLogicTable(),
                        ShardingStrategyLevelType.DATABASE, optional.getType(), optional)));
        Optional.ofNullable(ruleSegment.getKeyGenerateStrategySegment())
                .ifPresent(optional -> result.setKeyGenerateStrategy(createKeyGenerateStrategyConfiguration(ruleSegment.getLogicTable(), optional)));
        Optional.ofNullable(ruleSegment.getAuditStrategySegment())
                .ifPresent(optional -> result.setAuditStrategy(createShardingAuditStrategyConfiguration(optional)));
        return result;
    }
    
    private static ShardingStrategyConfiguration createShardingStrategyConfiguration(final String logicTable, final ShardingStrategyLevelType strategyLevel, final String type,
                                                                                     final ShardingStrategySegment strategySegment) {
        if ("none".equalsIgnoreCase(type)) {
            return new NoneShardingStrategyConfiguration();
        }
        String shardingAlgorithmName = getTableShardingAlgorithmName(logicTable, strategyLevel, strategySegment.getShardingAlgorithm().getName());
        return createStrategyConfiguration(ShardingStrategyType.getValueOf(type).name(), strategySegment.getShardingColumn(), shardingAlgorithmName);
    }
    
    private static KeyGenerateStrategyConfiguration createKeyGenerateStrategyConfiguration(final String logicTable, final KeyGenerateStrategySegment strategySegment) {
        return new KeyGenerateStrategyConfiguration(strategySegment.getKeyGenerateColumn(), getKeyGeneratorName(logicTable, strategySegment.getKeyGenerateAlgorithmSegment().getName()));
    }
    
    private static ShardingAuditStrategyConfiguration createShardingAuditStrategyConfiguration(final AuditStrategySegment strategySegment) {
        Collection<String> auditorNames = strategySegment.getAuditorSegments().stream().map(ShardingAuditorSegment::getAuditorName).collect(Collectors.toList());
        return new ShardingAuditStrategyConfiguration(auditorNames, strategySegment.isAllowHintDisable());
    }
    
    /**
     * Create strategy configuration.
     *
     * @param strategyType strategy type
     * @param shardingColumn sharding column
     * @param shardingAlgorithmName sharding algorithm name
     * @return sharding strategy configuration
     */
    public static ShardingStrategyConfiguration createStrategyConfiguration(final String strategyType, final String shardingColumn, final String shardingAlgorithmName) {
        ShardingStrategyType shardingStrategyType = ShardingStrategyType.getValueOf(strategyType);
        return shardingStrategyType.createConfiguration(shardingAlgorithmName, shardingColumn);
    }
    
    private static String getAutoTableShardingAlgorithmName(final String tableName, final String algorithmType) {
        return String.format("%s_%s", tableName, algorithmType).toLowerCase();
    }
    
    private static String getTableShardingAlgorithmName(final String tableName, final ShardingStrategyLevelType strategyLevel, final String algorithmType) {
        return String.format("%s_%s_%s", tableName, strategyLevel.name(), algorithmType).toLowerCase();
    }
    
    private static String getKeyGeneratorName(final String tableName, final String algorithmType) {
        return String.format("%s_%s", tableName, algorithmType).toLowerCase();
    }
    
    /**
     * Convert rule segments to data nodes.
     *
     * @param ruleSegments sharding table rule segments
     * @return data nodes map
     */
    public static Map<String, Collection<DataNode>> convertDataNodes(final Collection<AbstractTableRuleSegment> ruleSegments) {
        Map<String, Collection<DataNode>> result = new HashMap<>(ruleSegments.size(), 1F);
        for (AbstractTableRuleSegment each : ruleSegments) {
            if (each instanceof TableRuleSegment) {
                result.put(each.getLogicTable(), getActualDataNodes((TableRuleSegment) each));
                continue;
            }
            result.put(each.getLogicTable(), getActualDataNodes((AutoTableRuleSegment) each));
        }
        return result;
    }
    
    /**
     * Get actual data nodes for sharding table rule segment.
     *
     * @param ruleSegment sharding table rule segment
     * @return data nodes
     */
    public static Collection<DataNode> getActualDataNodes(final TableRuleSegment ruleSegment) {
        Collection<DataNode> result = new LinkedList<>();
        for (String each : ruleSegment.getDataSourceNodes()) {
            List<String> dataNodes = InlineExpressionParserFactory.newInstance(each).splitAndEvaluate();
            result.addAll(dataNodes.stream().map(DataNode::new).collect(Collectors.toList()));
        }
        return result;
    }
    
    /**
     * Get actual data nodes for auto sharding table rule segment.
     *
     * @param ruleSegment auto sharding table rule segment
     * @return data nodes
     */
    public static Collection<DataNode> getActualDataNodes(final AutoTableRuleSegment ruleSegment) {
        ShardingAlgorithm shardingAlgorithm =
                TypedSPILoader.getService(ShardingAlgorithm.class, ruleSegment.getShardingAlgorithmSegment().getName(), ruleSegment.getShardingAlgorithmSegment().getProps());
        ShardingSpherePreconditions.checkState(shardingAlgorithm instanceof ShardingAutoTableAlgorithm,
                () -> new AlgorithmInitializationException(shardingAlgorithm, "Auto sharding algorithm is required for table '%s'", ruleSegment.getLogicTable()));
        List<String> dataNodes = DataNodeUtils.getFormattedDataNodes(((ShardingAutoTableAlgorithm) shardingAlgorithm).getAutoTablesAmount(),
                ruleSegment.getLogicTable(), ruleSegment.getDataSourceNodes());
        return dataNodes.stream().map(DataNode::new).collect(Collectors.toList());
    }
}
