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
import com.google.common.base.Strings;
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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Sharding rule statement converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingRuleStatementConverter {
    
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
    
    private static Collection<ShardingTableRuleConfiguration> createTableRuleConfiguration(final Collection<AbstractTableRuleSegment> rules) {
        return rules.stream().filter(each -> each instanceof TableRuleSegment).map(each -> createTableRuleConfiguration((TableRuleSegment) each)).collect(Collectors.toCollection(LinkedList::new));
    }
    
    private static Map<String, ShardingSphereAlgorithmConfiguration> createKeyGeneratorConfiguration(final Collection<AbstractTableRuleSegment> rules) {
        return rules.stream().filter(each -> Objects.nonNull(each.getKeyGenerateSegment()))
                .collect(Collectors.toMap(each -> getKeyGeneratorName(each.getLogicTable(), each.getKeyGenerateSegment().getKeyGenerateAlgorithmSegment().getName()),
                        each -> createAlgorithmConfiguration(each.getKeyGenerateSegment().getKeyGenerateAlgorithmSegment())));
    }
    
    private static Map<String, ShardingSphereAlgorithmConfiguration> createKeyGeneratorConfiguration(final AbstractTableRuleSegment rule) {
        return Collections.singletonMap(getKeyGeneratorName(rule.getLogicTable(), rule.getKeyGenerateSegment().getKeyGenerateAlgorithmSegment().getName()),
                createAlgorithmConfiguration(rule.getKeyGenerateSegment().getKeyGenerateAlgorithmSegment()));
    }
    
    private static Map<String, ShardingSphereAlgorithmConfiguration> createAlgorithmConfiguration(final Collection<AbstractTableRuleSegment> rules) {
        return rules.stream().map(each -> (AutoTableRuleSegment) each).filter(each -> Objects.nonNull(each.getShardingAlgorithmSegment()))
                .collect(Collectors.toMap(each -> getShardingAlgorithmName(each.getLogicTable(), each.getShardingAlgorithmSegment().getName()),
                        each -> createAlgorithmConfiguration(each.getShardingAlgorithmSegment())));
    }
    
    private static Map<String, ShardingSphereAlgorithmConfiguration> createAlgorithmConfiguration(final AutoTableRuleSegment rule) {
        return Collections.singletonMap(getShardingAlgorithmName(rule.getLogicTable(), rule.getShardingAlgorithmSegment().getName()),
                createAlgorithmConfiguration(rule.getShardingAlgorithmSegment()));
    }
    
    private static Collection<ShardingAutoTableRuleConfiguration> createAutoTableRuleConfiguration(final Collection<AbstractTableRuleSegment> rules) {
        return rules.stream().filter(each -> each instanceof AutoTableRuleSegment).map(each -> createAutoTableRuleConfiguration((AutoTableRuleSegment) each)).collect(Collectors.toCollection(LinkedList::new));
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
    
    private static ShardingAutoTableRuleConfiguration createAutoTableRuleConfiguration(final AutoTableRuleSegment segment) {
        ShardingAutoTableRuleConfiguration result = new ShardingAutoTableRuleConfiguration(segment.getLogicTable(), Joiner.on(",").join(segment.getDataSourceNodes()));
        result.setShardingStrategy(createAutoTableStrategyConfiguration(segment));
        if (!Strings.isNullOrEmpty(segment.getShardingColumn()) && Objects.nonNull(segment.getKeyGenerateSegment())) {
            result.setKeyGenerateStrategy(createKeyGenerateStrategyConfiguration(segment));
        }
        return result;
    }
    
    private static ShardingTableRuleConfiguration createTableRuleConfiguration(final TableRuleSegment tableRuleSegment) {
        String dataSourceNodes = String.join(",", tableRuleSegment.getDataSourceNodes());
        ShardingTableRuleConfiguration tableRuleConfiguration = new ShardingTableRuleConfiguration(tableRuleSegment.getLogicTable(), dataSourceNodes);
        Optional.ofNullable(tableRuleSegment.getTableStrategySegment()).ifPresent(op ->
                tableRuleConfiguration.setTableShardingStrategy(createShardingStrategyConfiguration(op.getType(), op)));
        Optional.ofNullable(tableRuleSegment.getDatabaseStrategySegment()).ifPresent(op ->
                tableRuleConfiguration.setDatabaseShardingStrategy(createShardingStrategyConfiguration(op.getType(), op)));
        Optional.ofNullable(tableRuleSegment.getKeyGenerateSegment()).ifPresent(op ->
                tableRuleConfiguration.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration(op.getKeyGenerateColumn(), getKeyGeneratorName(tableRuleSegment.getLogicTable(), op.getKeyGenerateAlgorithmSegment().getName()))));
        return tableRuleConfiguration;
    }
    
    private static ShardingStrategyConfiguration createAutoTableStrategyConfiguration(final AutoTableRuleSegment segment) {
        return createStrategyConfiguration(ShardingStrategyType.STANDARD.name(),
                segment.getShardingColumn(), getShardingAlgorithmName(segment.getLogicTable(), segment.getShardingAlgorithmSegment().getName()));
    }
    
    private static ShardingStrategyConfiguration createShardingStrategyConfiguration(final String type, final ShardingStrategySegment segment) {
        return createStrategyConfiguration(ShardingStrategyType.getValueOf(type).name(), segment.getShardingColumn(), segment.getShardingAlgorithmName());
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
    
    private static KeyGenerateStrategyConfiguration createKeyGenerateStrategyConfiguration(final AutoTableRuleSegment segment) {
        KeyGenerateSegment keyGenerateSegment = segment.getKeyGenerateSegment();
        return new KeyGenerateStrategyConfiguration(keyGenerateSegment.getKeyGenerateColumn(),
                getKeyGeneratorName(segment.getLogicTable(), keyGenerateSegment.getKeyGenerateAlgorithmSegment().getName()));
    }
    
    private static String getKeyGeneratorName(final String tableName, final String columnName) {
        return String.format("%s_%s", tableName, columnName);
    }
    
    /**
     * Convert the type of Class in the collection.
     *
     * @param rules rule collection
     * @param clz class
     * @param <T> target type
     * @return target type
     */
    public static <T extends AbstractTableRuleSegment> Collection<T> collectionCast(final Collection<AbstractTableRuleSegment> rules, final Class<T> clz) {
        return rules.stream().map(each -> castTo(each, clz)).collect(Collectors.toCollection(LinkedList::new));
    }
    
    private static <T extends AbstractTableRuleSegment> T castTo(final AbstractTableRuleSegment abstractTableRuleSegment, final Class<T> clz) {
        return (T) abstractTableRuleSegment;
    }
    
    private static Map<String, List<AbstractTableRuleSegment>> groupingByType(final Collection<AbstractTableRuleSegment> rules) {
        return rules.stream().collect(Collectors.groupingBy(AbstractTableRuleSegment::getType));
    }
}
