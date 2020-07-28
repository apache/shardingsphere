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

package org.apache.shardingsphere.proxy.backend.communication.jdbc.execute.generator;

import com.google.common.base.Joiner;
import org.apache.shardingsphere.infra.yaml.config.algorithm.YamlShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.rdl.parser.binder.context.CreateShardingRuleStatementContext;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.sharding.YamlShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.sharding.YamlStandardShardingStrategyConfiguration;

/**
 * Yaml sharding rule configuration generator.
 */
public class YamlShardingRuleConfigurationGenerator implements YamlConfigurationGenerator<CreateShardingRuleStatementContext, YamlShardingRuleConfiguration> {
    
    @Override
    public YamlShardingRuleConfiguration generate(final CreateShardingRuleStatementContext sqlStatement) {
        YamlShardingRuleConfiguration result = new YamlShardingRuleConfiguration();
        addYamlShardingSphereAlgorithmConfiguration(sqlStatement, result);
        addYamlShardingAutoTableRuleConfiguration(sqlStatement, result);
        return result;
    }
    
    private void addYamlShardingSphereAlgorithmConfiguration(final CreateShardingRuleStatementContext sqlStatement, final YamlShardingRuleConfiguration ruleConfiguration) {
        YamlShardingSphereAlgorithmConfiguration algorithmConfiguration = new YamlShardingSphereAlgorithmConfiguration();
        algorithmConfiguration.setType(sqlStatement.getAlgorithmType());
        algorithmConfiguration.setProps(sqlStatement.getAlgorithmProperties());
        ruleConfiguration.getShardingAlgorithms().put(getAlgorithmName(sqlStatement.getLogicTable(), sqlStatement.getAlgorithmType()), algorithmConfiguration);
    }
    
    private void addYamlShardingAutoTableRuleConfiguration(final CreateShardingRuleStatementContext sqlStatement, final YamlShardingRuleConfiguration ruleConfiguration) {
        YamlShardingAutoTableRuleConfiguration tableRuleConfiguration = new YamlShardingAutoTableRuleConfiguration();
        tableRuleConfiguration.setLogicTable(sqlStatement.getLogicTable());
        tableRuleConfiguration.setActualDataSources(Joiner.on(",").join(sqlStatement.getDataSources()));
        tableRuleConfiguration.setShardingStrategy(createYamlShardingStrategyConfiguration(sqlStatement));
        ruleConfiguration.getAutoTables().put(sqlStatement.getLogicTable(), tableRuleConfiguration);
    }
    
    private YamlShardingStrategyConfiguration createYamlShardingStrategyConfiguration(final CreateShardingRuleStatementContext sqlStatement) {
        YamlShardingStrategyConfiguration strategy = new YamlShardingStrategyConfiguration();
        YamlStandardShardingStrategyConfiguration standard = new YamlStandardShardingStrategyConfiguration();
        standard.setShardingColumn(sqlStatement.getShardingColumn());
        standard.setShardingAlgorithmName(getAlgorithmName(sqlStatement.getLogicTable(), sqlStatement.getAlgorithmType()));
        strategy.setStandard(standard);
        return strategy;
    }
    
    private String getAlgorithmName(final String tableName, final String algorithmType) {
        return String.format("%s_%s", tableName, algorithmType);
    }
}
