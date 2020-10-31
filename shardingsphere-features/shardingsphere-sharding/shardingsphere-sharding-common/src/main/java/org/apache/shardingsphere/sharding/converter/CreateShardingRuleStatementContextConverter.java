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

package org.apache.shardingsphere.sharding.converter;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.segment.rdl.TableRuleSegment;
import org.apache.shardingsphere.infra.binder.statement.rdl.CreateShardingRuleStatementContext;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmPropertiesAware;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.yaml.config.algorithm.YamlShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.sharding.api.sharding.ShardingAutoTableAlgorithm;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.sharding.YamlShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.sharding.YamlStandardShardingStrategyConfiguration;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;

/**
 * Create sharding rule statement context converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreateShardingRuleStatementContextConverter {
    
    /**
     * Convert create sharding rule statement context to YAML sharding rule configuration.
     *
     * @param sqlStatementContext create sharding rule statement context
     * @return YAML sharding rule configuration
     */
    public static YamlShardingRuleConfiguration convert(final CreateShardingRuleStatementContext sqlStatementContext) {
        YamlShardingRuleConfiguration result = new YamlShardingRuleConfiguration();
        for (TableRuleSegment each : sqlStatementContext.getSqlStatement().getTables()) {
            result.getShardingAlgorithms().put(getAlgorithmName(each.getLogicTable(), each.getAlgorithmType()),
                    createAlgorithmConfiguration(each, getAlgorithmProperties(each.getAlgorithmType(), each.getProperties())));
            result.getAutoTables().put(each.getLogicTable(), createAutoTableRuleConfiguration(each));
        }
        return result;
    }
    
    private static YamlShardingSphereAlgorithmConfiguration createAlgorithmConfiguration(final TableRuleSegment segment, final Properties properties) {
        YamlShardingSphereAlgorithmConfiguration result = new YamlShardingSphereAlgorithmConfiguration();
        result.setType(segment.getAlgorithmType());
        result.setProps(properties);
        return result;
    }
    
    private static Properties getAlgorithmProperties(final String type, final Collection<String> propertyValues) {
        Collection<String> propertyKeys = findAlgorithmPropertyKeys(type);
        Preconditions.checkArgument(propertyKeys.size() == propertyValues.size(), "%s needs %d properties, but %s properties are given.", type, propertyKeys.size(), propertyValues.size());
        Properties result = new Properties();
        Iterator<String> keys = propertyKeys.iterator();
        Iterator<String> values = propertyValues.iterator();
        while (keys.hasNext()) {
            result.setProperty(keys.next(), values.next());
        }
        return result;
    }
    
    private static Collection<String> findAlgorithmPropertyKeys(final String algorithmType) {
        return ShardingSphereServiceLoader.newServiceInstances(ShardingAlgorithm.class).stream()
                .filter(each -> each instanceof ShardingAutoTableAlgorithm && each.getType().equals(algorithmType)).findFirst()
                .map(each -> ((ShardingSphereAlgorithmPropertiesAware) each).getAllPropertyKeys()).orElse(Collections.emptyList());
    }
    
    private static YamlShardingAutoTableRuleConfiguration createAutoTableRuleConfiguration(final TableRuleSegment segment) {
        YamlShardingAutoTableRuleConfiguration result = new YamlShardingAutoTableRuleConfiguration();
        result.setLogicTable(segment.getLogicTable());
        result.setActualDataSources(Joiner.on(",").join(segment.getDataSources()));
        result.setShardingStrategy(createStrategyConfiguration(segment));
        return result;
    }
    
    private static YamlShardingStrategyConfiguration createStrategyConfiguration(final TableRuleSegment segment) {
        YamlShardingStrategyConfiguration result = new YamlShardingStrategyConfiguration();
        YamlStandardShardingStrategyConfiguration standard = new YamlStandardShardingStrategyConfiguration();
        standard.setShardingColumn(segment.getShardingColumn());
        standard.setShardingAlgorithmName(getAlgorithmName(segment.getLogicTable(), segment.getAlgorithmType()));
        result.setStandard(standard);
        return result;
    }
    
    private static String getAlgorithmName(final String tableName, final String algorithmType) {
        return String.format("%s_%s", tableName, algorithmType);
    }
}
