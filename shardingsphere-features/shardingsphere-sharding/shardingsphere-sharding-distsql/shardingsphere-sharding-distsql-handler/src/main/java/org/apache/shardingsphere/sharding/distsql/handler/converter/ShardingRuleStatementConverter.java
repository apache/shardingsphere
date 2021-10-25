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
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.segment.AutoTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.KeyGenerateSegment;

import java.util.Collection;
import java.util.Objects;

/**
 * Sharding rule statement converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingRuleStatementConverter {
    
    /**
     * Convert sharding table rule segments to sharding rule configuration.
     *
     * @param ruleSegments sharding table rule statements
     * @return sharding rule configuration
     */
    public static ShardingRuleConfiguration convert(final Collection<AutoTableRuleSegment> ruleSegments) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        for (AutoTableRuleSegment each : ruleSegments) {
            if (null != each.getShardingAlgorithmSegment()) {
                String shardingAlgorithmName = getShardingAlgorithmName(each.getLogicTable(), each.getShardingAlgorithmSegment().getName());
                result.getShardingAlgorithms().put(shardingAlgorithmName, createAlgorithmConfiguration(each.getShardingAlgorithmSegment()));
                result.getAutoTables().add(createAutoTableRuleConfiguration(each));
            }
            KeyGenerateSegment keyGenerateSegment = each.getKeyGenerateSegment();
            if (null != keyGenerateSegment && null != keyGenerateSegment.getKeyGenerateAlgorithmSegment()) {
                String keyGeneratorName = getKeyGeneratorName(each.getLogicTable(), keyGenerateSegment.getKeyGenerateAlgorithmSegment().getName());
                result.getKeyGenerators().put(keyGeneratorName, createAlgorithmConfiguration(keyGenerateSegment.getKeyGenerateAlgorithmSegment()));
            }
        }
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
    
    private static ShardingAutoTableRuleConfiguration createAutoTableRuleConfiguration(final AutoTableRuleSegment segment) {
        ShardingAutoTableRuleConfiguration result = new ShardingAutoTableRuleConfiguration(segment.getLogicTable(), Joiner.on(",").join(segment.getDataSources()));
        result.setShardingStrategy(createTableStrategyConfiguration(segment));
        if (!Strings.isNullOrEmpty(segment.getShardingColumn()) && Objects.nonNull(segment.getKeyGenerateSegment()) 
                && Objects.nonNull(segment.getKeyGenerateSegment().getKeyGenerateAlgorithmSegment())) {
            result.setKeyGenerateStrategy(createKeyGenerateStrategyConfiguration(segment));
        }
        return result;
    }
    
    private static ShardingStrategyConfiguration createTableStrategyConfiguration(final AutoTableRuleSegment segment) {
        return createStrategyConfiguration(ShardingStrategyType.STANDARD.name(),
                segment.getShardingColumn(), getShardingAlgorithmName(segment.getLogicTable(), segment.getShardingAlgorithmSegment().getName()));
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
        return new KeyGenerateStrategyConfiguration(keyGenerateSegment.getKeyGenerateColumn(), getKeyGeneratorName(segment.getLogicTable(),
                keyGenerateSegment.getKeyGenerateAlgorithmSegment().getName()));
    }
    
    private static String getKeyGeneratorName(final String tableName, final String columnName) {
        return String.format("%s_%s", tableName, columnName);
    }
}
