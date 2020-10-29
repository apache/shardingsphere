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

package org.apache.shardingsphere.sharding.convert;

import com.google.common.base.Joiner;
import org.apache.shardingsphere.infra.yaml.config.algorithm.YamlShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.distsql.parser.binder.context.CreateShardingRuleStatementContext;
import org.apache.shardingsphere.distsql.parser.binder.generator.SQLStatementContextConverter;
import org.apache.shardingsphere.distsql.parser.statement.rdl.TableRuleSegment;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.sharding.YamlShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.sharding.YamlStandardShardingStrategyConfiguration;

import java.util.Properties;

/**
 * Create sharding rule statement context converter.
 */
public final class CreateShardingRuleStatementContextConverter implements SQLStatementContextConverter<CreateShardingRuleStatementContext, YamlShardingRuleConfiguration> {
    
    @Override
    public YamlShardingRuleConfiguration convert(final CreateShardingRuleStatementContext context) {
        YamlShardingRuleConfiguration result = new YamlShardingRuleConfiguration();
        for (TableRuleSegment each : context.getSqlStatement().getTables()) {
            result.getShardingAlgorithms().put(getAlgorithmName(each.getLogicTable(), each.getAlgorithmType()),
                    createAlgorithmConfiguration(each, context.getAlgorithmProperties(each)));
            result.getAutoTables().put(each.getLogicTable(), createAutoTableRuleConfiguration(each));
        }
        return result;
    }
    
    private YamlShardingSphereAlgorithmConfiguration createAlgorithmConfiguration(final TableRuleSegment segment, final Properties properties) {
        YamlShardingSphereAlgorithmConfiguration result = new YamlShardingSphereAlgorithmConfiguration();
        result.setType(segment.getAlgorithmType());
        result.setProps(properties);
        return result;
    }
    
    private YamlShardingAutoTableRuleConfiguration createAutoTableRuleConfiguration(final TableRuleSegment segment) {
        YamlShardingAutoTableRuleConfiguration result = new YamlShardingAutoTableRuleConfiguration();
        result.setLogicTable(segment.getLogicTable());
        result.setActualDataSources(Joiner.on(",").join(segment.getDataSources()));
        result.setShardingStrategy(createStrategyConfiguration(segment));
        return result;
    }
    
    private YamlShardingStrategyConfiguration createStrategyConfiguration(final TableRuleSegment segment) {
        YamlShardingStrategyConfiguration result = new YamlShardingStrategyConfiguration();
        YamlStandardShardingStrategyConfiguration standard = new YamlStandardShardingStrategyConfiguration();
        standard.setShardingColumn(segment.getShardingColumn());
        standard.setShardingAlgorithmName(getAlgorithmName(segment.getLogicTable(), segment.getAlgorithmType()));
        result.setStandard(standard);
        return result;
    }
    
    private String getAlgorithmName(final String tableName, final String algorithmType) {
        return String.format("%s_%s", tableName, algorithmType);
    }
}
