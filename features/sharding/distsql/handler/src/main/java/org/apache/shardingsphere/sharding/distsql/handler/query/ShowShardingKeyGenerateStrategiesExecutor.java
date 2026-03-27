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

package org.apache.shardingsphere.sharding.distsql.handler.query;

import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorRuleAware;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.keygen.KeyGenerateStrategiesConfiguration;
import org.apache.shardingsphere.infra.config.keygen.impl.ColumnKeyGenerateStrategiesRuleConfiguration;
import org.apache.shardingsphere.infra.config.keygen.impl.SequenceKeyGenerateStrategiesRuleConfiguration;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.sharding.distsql.statement.ShowShardingKeyGenerateStrategiesStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Show sharding key generate strategies executor.
 */
@Setter
public final class ShowShardingKeyGenerateStrategiesExecutor
        implements DistSQLQueryExecutor<ShowShardingKeyGenerateStrategiesStatement>, DistSQLExecutorRuleAware<ShardingRule> {
    
    private ShardingRule rule;
    
    @Override
    public Collection<String> getColumnNames(final ShowShardingKeyGenerateStrategiesStatement sqlStatement) {
        return Arrays.asList("name", "type", "table", "column", "sequence", "generator_name", "generator_type", "generator_props");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowShardingKeyGenerateStrategiesStatement sqlStatement, final ContextManager contextManager) {
        return rule.getConfiguration().getKeyGenerateStrategies().entrySet().stream()
                .filter(entry -> !sqlStatement.getName().isPresent() || sqlStatement.getName().get().equalsIgnoreCase(entry.getKey()))
                .map(this::createRow).collect(Collectors.toList());
    }
    
    private LocalDataQueryResultRow createRow(final Entry<String, KeyGenerateStrategiesConfiguration> entry) {
        KeyGenerateStrategiesConfiguration ruleConfig = entry.getValue();
        return ruleConfig instanceof ColumnKeyGenerateStrategiesRuleConfiguration
                ? createColumnRow(entry.getKey(), (ColumnKeyGenerateStrategiesRuleConfiguration) ruleConfig)
                : createSequenceRow(entry.getKey(), (SequenceKeyGenerateStrategiesRuleConfiguration) ruleConfig);
    }
    
    private LocalDataQueryResultRow createColumnRow(final String name, final ColumnKeyGenerateStrategiesRuleConfiguration ruleConfig) {
        AlgorithmConfiguration algorithmConfig = getAlgorithmConfiguration(ruleConfig.getKeyGeneratorName());
        return new LocalDataQueryResultRow(name, ruleConfig.getKeyGenerateType(), ruleConfig.getLogicTable(), ruleConfig.getKeyGenerateColumn(), "",
                ruleConfig.getKeyGeneratorName(), getAlgorithmType(algorithmConfig), getAlgorithmProps(algorithmConfig));
    }
    
    private LocalDataQueryResultRow createSequenceRow(final String name, final SequenceKeyGenerateStrategiesRuleConfiguration ruleConfig) {
        AlgorithmConfiguration algorithmConfig = getAlgorithmConfiguration(ruleConfig.getKeyGeneratorName());
        return new LocalDataQueryResultRow(name, ruleConfig.getKeyGenerateType(), "", "", ruleConfig.getKeyGenerateSequence(),
                ruleConfig.getKeyGeneratorName(), getAlgorithmType(algorithmConfig), getAlgorithmProps(algorithmConfig));
    }
    
    private AlgorithmConfiguration getAlgorithmConfiguration(final String keyGeneratorName) {
        return rule.getConfiguration().getKeyGenerators().get(keyGeneratorName);
    }
    
    private String getAlgorithmType(final AlgorithmConfiguration algorithmConfig) {
        return null == algorithmConfig ? "" : algorithmConfig.getType();
    }
    
    private Properties getAlgorithmProps(final AlgorithmConfiguration algorithmConfig) {
        return null == algorithmConfig ? new Properties() : algorithmConfig.getProps();
    }
    
    @Override
    public Class<ShardingRule> getRuleClass() {
        return ShardingRule.class;
    }
    
    @Override
    public Class<ShowShardingKeyGenerateStrategiesStatement> getType() {
        return ShowShardingKeyGenerateStrategiesStatement.class;
    }
}
