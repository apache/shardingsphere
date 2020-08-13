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
import org.apache.shardingsphere.rdl.parser.binder.context.CreateShardingRuleStatementContext;
import org.apache.shardingsphere.rdl.parser.binder.generator.SQLStatementContextConverter;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.sharding.YamlShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.sharding.YamlStandardShardingStrategyConfiguration;

/**
 * Create sharding rule statement context converter.
 */
public final class CreateShardingRuleStatementContextConverter implements SQLStatementContextConverter<CreateShardingRuleStatementContext, YamlShardingRuleConfiguration> {
    
    @Override
    public YamlShardingRuleConfiguration convert(final CreateShardingRuleStatementContext context) {
        YamlShardingRuleConfiguration result = new YamlShardingRuleConfiguration();
        addYamlShardingSphereAlgorithmConfiguration(context, result);
        addYamlShardingAutoTableRuleConfiguration(context, result);
        return result;
    }
    
    private void addYamlShardingSphereAlgorithmConfiguration(final CreateShardingRuleStatementContext context, final YamlShardingRuleConfiguration ruleConfiguration) {
        YamlShardingSphereAlgorithmConfiguration algorithmConfiguration = new YamlShardingSphereAlgorithmConfiguration();
        algorithmConfiguration.setType(context.getAlgorithmType());
        algorithmConfiguration.setProps(context.getAlgorithmProperties());
        ruleConfiguration.getShardingAlgorithms().put(getAlgorithmName(context.getLogicTable(), context.getAlgorithmType()), algorithmConfiguration);
    }
    
    private void addYamlShardingAutoTableRuleConfiguration(final CreateShardingRuleStatementContext context, final YamlShardingRuleConfiguration ruleConfiguration) {
        YamlShardingAutoTableRuleConfiguration tableRuleConfiguration = new YamlShardingAutoTableRuleConfiguration();
        tableRuleConfiguration.setLogicTable(context.getLogicTable());
        tableRuleConfiguration.setActualDataSources(Joiner.on(",").join(context.getDataSources()));
        tableRuleConfiguration.setShardingStrategy(createYamlShardingStrategyConfiguration(context));
        ruleConfiguration.getAutoTables().put(context.getLogicTable(), tableRuleConfiguration);
    }
    
    private YamlShardingStrategyConfiguration createYamlShardingStrategyConfiguration(final CreateShardingRuleStatementContext context) {
        YamlShardingStrategyConfiguration strategy = new YamlShardingStrategyConfiguration();
        YamlStandardShardingStrategyConfiguration standard = new YamlStandardShardingStrategyConfiguration();
        standard.setShardingColumn(context.getShardingColumn());
        standard.setShardingAlgorithmName(getAlgorithmName(context.getLogicTable(), context.getAlgorithmType()));
        strategy.setStandard(standard);
        return strategy;
    }
    
    private String getAlgorithmName(final String tableName, final String algorithmType) {
        return String.format("%s_%s", tableName, algorithmType);
    }
}
