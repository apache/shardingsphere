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
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.segment.TableRuleSegment;

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
    public static ShardingRuleConfiguration convert(final Collection<TableRuleSegment> ruleSegments) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        for (TableRuleSegment each : ruleSegments) {
            if (null != each.getTableStrategy()) {
                result.getShardingAlgorithms().put(getShardingAlgorithmName(each.getLogicTable(), each.getTableStrategy().getName()), createAlgorithmConfiguration(each.getTableStrategy()));
                result.getAutoTables().add(createAutoTableRuleConfiguration(each));
            }
            if (null != each.getKeyGenerateStrategy()) {
                result.getKeyGenerators().put(getKeyGeneratorName(each.getLogicTable(), each.getKeyGenerateStrategy().getName()), createAlgorithmConfiguration(each.getKeyGenerateStrategy()));
            }
        }
        return result;
    }
    
    /**
     * Create algorithm configuration.
     * @param segment algorithm segment
     * @return ShardingSphere algorithm configuration
     */
    public static ShardingSphereAlgorithmConfiguration createAlgorithmConfiguration(final AlgorithmSegment segment) {
        return new ShardingSphereAlgorithmConfiguration(segment.getName(), segment.getProps());
    }
    
    private static ShardingAutoTableRuleConfiguration createAutoTableRuleConfiguration(final TableRuleSegment segment) {
        ShardingAutoTableRuleConfiguration result = new ShardingAutoTableRuleConfiguration(segment.getLogicTable(), Joiner.on(",").join(segment.getDataSources()));
        result.setShardingStrategy(createTableStrategyConfiguration(segment));
        if (!Strings.isNullOrEmpty(segment.getTableStrategyColumn()) && Objects.nonNull(segment.getKeyGenerateStrategy())) {
            result.setKeyGenerateStrategy(createKeyGenerateStrategyConfiguration(segment));
        }
        return result;
    }
    
    // TODO consider other sharding strategy type, for example: complex, hint
    private static ShardingStrategyConfiguration createTableStrategyConfiguration(final TableRuleSegment segment) {
        return new StandardShardingStrategyConfiguration(segment.getTableStrategyColumn(), getShardingAlgorithmName(segment.getLogicTable(), segment.getTableStrategy().getName()));
    }
    
    private static String getShardingAlgorithmName(final String tableName, final String algorithmType) {
        return String.format("%s_%s", tableName, algorithmType);
    }
    
    private static KeyGenerateStrategyConfiguration createKeyGenerateStrategyConfiguration(final TableRuleSegment segment) {
        return new KeyGenerateStrategyConfiguration(segment.getKeyGenerateStrategyColumn(), getKeyGeneratorName(segment.getLogicTable(), segment.getKeyGenerateStrategy().getName()));
    }
    
    private static String getKeyGeneratorName(final String tableName, final String columnName) {
        return String.format("%s_%s", tableName, columnName);
    }
}
