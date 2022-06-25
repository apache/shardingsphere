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

package org.apache.shardingsphere.sharding.distsql.handler.update;

import org.apache.shardingsphere.infra.distsql.exception.rule.AlgorithmInUsedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredAlgorithmMissedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RuleDefinitionViolationException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionDropUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingAlgorithmStatement;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Drop sharding algorithm statement updater.
 */
public final class DropShardingAlgorithmStatementUpdater implements RuleDefinitionDropUpdater<DropShardingAlgorithmStatement, ShardingRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database,
                                  final DropShardingAlgorithmStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) throws RuleDefinitionViolationException {
        if (null == currentRuleConfig && sqlStatement.isIfExists()) {
            return;
        }
        String databaseName = database.getName();
        checkCurrentRuleConfiguration(databaseName, currentRuleConfig);
        checkToBeDroppedShardingAlgorithms(databaseName, sqlStatement, currentRuleConfig);
        checkShardingAlgorithmsInUsed(databaseName, sqlStatement, currentRuleConfig);
    }
    
    private void checkCurrentRuleConfiguration(final String databaseName, final ShardingRuleConfiguration currentRuleConfig) throws RequiredAlgorithmMissedException {
        if (null == currentRuleConfig) {
            throw new RequiredAlgorithmMissedException(databaseName);
        }
    }
    
    private void checkToBeDroppedShardingAlgorithms(final String databaseName, final DropShardingAlgorithmStatement sqlStatement,
                                                    final ShardingRuleConfiguration currentRuleConfig) throws RequiredAlgorithmMissedException {
        if (sqlStatement.isIfExists()) {
            return;
        }
        Collection<String> currentShardingAlgorithms = getCurrentShardingAlgorithms(currentRuleConfig);
        Collection<String> notExistedAlgorithms = sqlStatement.getAlgorithmNames().stream().filter(each -> !currentShardingAlgorithms.contains(each)).collect(Collectors.toList());
        if (!notExistedAlgorithms.isEmpty()) {
            throw new RequiredAlgorithmMissedException(databaseName, notExistedAlgorithms);
        }
    }
    
    private void checkShardingAlgorithmsInUsed(final String databaseName, final DropShardingAlgorithmStatement sqlStatement,
                                               final ShardingRuleConfiguration currentRuleConfig) throws AlgorithmInUsedException {
        Collection<String> allInUsed = getAllOfAlgorithmsInUsed(currentRuleConfig);
        Collection<String> usedAlgorithms = sqlStatement.getAlgorithmNames().stream().filter(allInUsed::contains).collect(Collectors.toList());
        if (!usedAlgorithms.isEmpty()) {
            throw new AlgorithmInUsedException(databaseName, usedAlgorithms);
        }
    }
    
    private Collection<String> getAllOfAlgorithmsInUsed(final ShardingRuleConfiguration shardingRuleConfig) {
        Collection<String> result = new LinkedHashSet<>();
        shardingRuleConfig.getTables().forEach(each -> {
            if (Objects.nonNull(each.getDatabaseShardingStrategy())) {
                result.add(each.getDatabaseShardingStrategy().getShardingAlgorithmName());
            }
            if (Objects.nonNull(each.getTableShardingStrategy())) {
                result.add(each.getTableShardingStrategy().getShardingAlgorithmName());
            }
        });
        shardingRuleConfig.getAutoTables().stream().filter(each -> Objects.nonNull(each.getShardingStrategy())).forEach(each -> result.add(each.getShardingStrategy().getShardingAlgorithmName()));
        ShardingStrategyConfiguration tableShardingStrategy = shardingRuleConfig.getDefaultTableShardingStrategy();
        if (null != tableShardingStrategy && !tableShardingStrategy.getShardingAlgorithmName().isEmpty()) {
            result.add(tableShardingStrategy.getShardingAlgorithmName());
        }
        ShardingStrategyConfiguration databaseShardingStrategy = shardingRuleConfig.getDefaultDatabaseShardingStrategy();
        if (null != databaseShardingStrategy && !databaseShardingStrategy.getShardingAlgorithmName().isEmpty()) {
            result.add(databaseShardingStrategy.getShardingAlgorithmName());
        }
        return result;
    }
    
    private Collection<String> getCurrentShardingAlgorithms(final ShardingRuleConfiguration shardingRuleConfig) {
        return shardingRuleConfig.getShardingAlgorithms().keySet();
    }
    
    @Override
    public boolean updateCurrentRuleConfiguration(final DropShardingAlgorithmStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        for (String each : sqlStatement.getAlgorithmNames()) {
            dropShardingAlgorithm(currentRuleConfig, each);
        }
        return false;
    }
    
    @Override
    public boolean hasAnyOneToBeDropped(final DropShardingAlgorithmStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        return null != currentRuleConfig && !getIdenticalData(getCurrentShardingAlgorithms(currentRuleConfig), sqlStatement.getAlgorithmNames()).isEmpty();
    }
    
    private void dropShardingAlgorithm(final ShardingRuleConfiguration currentRuleConfig, final String algorithmName) {
        getCurrentShardingAlgorithms(currentRuleConfig).removeIf(algorithmName::equalsIgnoreCase);
    }
    
    @Override
    public Class<ShardingRuleConfiguration> getRuleConfigurationClass() {
        return ShardingRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return DropShardingAlgorithmStatement.class.getName();
    }
}
