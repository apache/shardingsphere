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

import com.google.common.base.Joiner;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.segment.AbstractTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.AutoTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.KeyGenerateSegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.ShardingStrategySegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.TableRuleSegment;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
        rules.forEach(each -> {
            result.getKeyGenerators().putAll(createKeyGeneratorConfiguration(each));
            if (each instanceof AutoTableRuleSegment) {
                result.getShardingAlgorithms().putAll(createAlgorithmConfiguration((AutoTableRuleSegment) each));
                result.getAutoTables().add(createAutoTableRuleConfiguration((AutoTableRuleSegment) each));
            }
            if (each instanceof TableRuleSegment) {
                result.getTables().add(createTableRuleConfiguration((TableRuleSegment) each));
            }
        });
        return result;
    }
    
    private static Map<String, ShardingSphereAlgorithmConfiguration> createKeyGeneratorConfiguration(final AbstractTableRuleSegment rule) {
        Map<String, ShardingSphereAlgorithmConfiguration> result = new HashMap<>();
        Optional.ofNullable(rule.getKeyGenerateSegment()).ifPresent(op ->
                result.put(getKeyGeneratorName(rule.getLogicTable(), op.getKeyGenerateAlgorithmSegment().getName()), createAlgorithmConfiguration(op.getKeyGenerateAlgorithmSegment())));
        return result;
    }
    
    private static Map<String, ShardingSphereAlgorithmConfiguration> createAlgorithmConfiguration(final AutoTableRuleSegment rule) {
        Map<String, ShardingSphereAlgorithmConfiguration> result = new HashMap<>();
        Optional.ofNullable(rule.getShardingAlgorithmSegment()).ifPresent(op ->
                result.put(getShardingAlgorithmName(rule.getLogicTable(), op.getName()), createAlgorithmConfiguration(op)));
        return result;
    }
    
    /**
     * Create algorithm configuration.
     *
     * @param segment algorithm segment
     * @return ShardingSphere algorithm configuration
     */
    public static ShardingSphereAlgorithmConfiguration createAlgorithmConfiguration(final AlgorithmSegment segment) {
        return new ShardingSphereAlgorithmConfiguration(segment.getName(), segment.getProps());
    }
    
    private static ShardingAutoTableRuleConfiguration createAutoTableRuleConfiguration(final AutoTableRuleSegment rule) {
        ShardingAutoTableRuleConfiguration result = new ShardingAutoTableRuleConfiguration(rule.getLogicTable(), Joiner.on(",").join(rule.getDataSourceNodes()));
        result.setShardingStrategy(createAutoTableStrategyConfiguration(rule));
        Optional.ofNullable(rule.getKeyGenerateSegment()).ifPresent(op ->
                result.setKeyGenerateStrategy(createKeyGenerateStrategyConfiguration(rule.getLogicTable(), rule.getKeyGenerateSegment())));
        return result;
    }
    
    private static ShardingStrategyConfiguration createAutoTableStrategyConfiguration(final AutoTableRuleSegment rule) {
        return createStrategyConfiguration(ShardingStrategyType.STANDARD.name(),
                rule.getShardingColumn(), getShardingAlgorithmName(rule.getLogicTable(), rule.getShardingAlgorithmSegment().getName()));
    }
    
    private static ShardingTableRuleConfiguration createTableRuleConfiguration(final TableRuleSegment tableRuleSegment) {
        String dataSourceNodes = String.join(",", tableRuleSegment.getDataSourceNodes());
        ShardingTableRuleConfiguration tableRuleConfiguration = new ShardingTableRuleConfiguration(tableRuleSegment.getLogicTable(), dataSourceNodes);
        Optional.ofNullable(tableRuleSegment.getTableStrategySegment()).ifPresent(op ->
                tableRuleConfiguration.setTableShardingStrategy(createShardingStrategyConfiguration(op.getType(), op)));
        Optional.ofNullable(tableRuleSegment.getDatabaseStrategySegment()).ifPresent(op ->
                tableRuleConfiguration.setDatabaseShardingStrategy(createShardingStrategyConfiguration(op.getType(), op)));
        Optional.ofNullable(tableRuleSegment.getKeyGenerateSegment()).ifPresent(op ->
                tableRuleConfiguration.setKeyGenerateStrategy(createKeyGenerateStrategyConfiguration(tableRuleSegment.getLogicTable(), op)));
        return tableRuleConfiguration;
    }
    
    private static ShardingStrategyConfiguration createShardingStrategyConfiguration(final String type, final ShardingStrategySegment segment) {
        return createStrategyConfiguration(ShardingStrategyType.getValueOf(type).name(), segment.getShardingColumn(), segment.getShardingAlgorithmName());
    }
    
    private static KeyGenerateStrategyConfiguration createKeyGenerateStrategyConfiguration(final String logicTable, final KeyGenerateSegment segment) {
        return new KeyGenerateStrategyConfiguration(segment.getKeyGenerateColumn(), getKeyGeneratorName(logicTable, segment.getKeyGenerateAlgorithmSegment().getName()));
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
        return shardingStrategyType.getConfiguration(shardingAlgorithmName, shardingColumn);
    }
    
    private static String getShardingAlgorithmName(final String tableName, final String algorithmType) {
        return String.format("%s_%s", tableName, algorithmType);
    }
    
    private static String getKeyGeneratorName(final String tableName, final String columnName) {
        return String.format("%s_%s", tableName, columnName);
    }
}
