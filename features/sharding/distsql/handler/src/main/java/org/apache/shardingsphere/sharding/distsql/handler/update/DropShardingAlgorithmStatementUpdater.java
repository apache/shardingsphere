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

import org.apache.shardingsphere.distsql.handler.exception.algorithm.AlgorithmInUsedException;
import org.apache.shardingsphere.distsql.handler.exception.algorithm.MissingRequiredAlgorithmException;
import org.apache.shardingsphere.distsql.handler.update.RuleDefinitionDropUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingAlgorithmStatement;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

/**
 * Drop sharding algorithm statement updater.
 */
public final class DropShardingAlgorithmStatementUpdater implements RuleDefinitionDropUpdater<DropShardingAlgorithmStatement, ShardingRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database, final DropShardingAlgorithmStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        if (null == currentRuleConfig && sqlStatement.isIfExists()) {
            return;
        }
        String databaseName = database.getName();
        checkCurrentRuleConfiguration(databaseName, currentRuleConfig);
        checkToBeDroppedShardingAlgorithms(databaseName, sqlStatement, currentRuleConfig);
        checkShardingAlgorithmsInUsed(databaseName, sqlStatement, currentRuleConfig);
    }
    
    private void checkCurrentRuleConfiguration(final String databaseName, final ShardingRuleConfiguration currentRuleConfig) {
        ShardingSpherePreconditions.checkNotNull(currentRuleConfig, () -> new MissingRequiredAlgorithmException("Sharding", databaseName));
    }
    
    private void checkToBeDroppedShardingAlgorithms(final String databaseName, final DropShardingAlgorithmStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        if (sqlStatement.isIfExists()) {
            return;
        }
        Collection<String> currentShardingAlgorithms = getCurrentShardingAlgorithms(currentRuleConfig);
        Collection<String> notExistedAlgorithms = sqlStatement.getNames().stream().filter(each -> !currentShardingAlgorithms.contains(each)).collect(Collectors.toList());
        if (!notExistedAlgorithms.isEmpty()) {
            throw new MissingRequiredAlgorithmException(databaseName, notExistedAlgorithms);
        }
    }
    
    private void checkShardingAlgorithmsInUsed(final String databaseName, final DropShardingAlgorithmStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        Collection<String> allInUsed = getAllOfAlgorithmsInUsed(currentRuleConfig);
        Collection<String> usedAlgorithms = sqlStatement.getNames().stream().filter(allInUsed::contains).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(usedAlgorithms.isEmpty(), () -> new AlgorithmInUsedException("Sharding", databaseName, usedAlgorithms));
    }
    
    private Collection<String> getAllOfAlgorithmsInUsed(final ShardingRuleConfiguration shardingRuleConfig) {
        Collection<String> result = new LinkedHashSet<>();
        shardingRuleConfig.getTables().forEach(each -> {
            if (null != each.getDatabaseShardingStrategy()) {
                result.add(each.getDatabaseShardingStrategy().getShardingAlgorithmName());
            }
            if (null != each.getTableShardingStrategy()) {
                result.add(each.getTableShardingStrategy().getShardingAlgorithmName());
            }
        });
        shardingRuleConfig.getAutoTables().stream().filter(each -> null != each.getShardingStrategy()).forEach(each -> result.add(each.getShardingStrategy().getShardingAlgorithmName()));
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
    public ShardingRuleConfiguration buildToBeDroppedRuleConfiguration(final ShardingRuleConfiguration currentRuleConfig, final DropShardingAlgorithmStatement sqlStatement) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        for (String each : sqlStatement.getNames()) {
            result.getShardingAlgorithms().put(each, currentRuleConfig.getShardingAlgorithms().get(each));
        }
        return result;
    }
    
    @Override
    public boolean updateCurrentRuleConfiguration(final DropShardingAlgorithmStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        for (String each : sqlStatement.getNames()) {
            dropShardingAlgorithm(currentRuleConfig, each);
        }
        return false;
    }
    
    @Override
    public boolean hasAnyOneToBeDropped(final DropShardingAlgorithmStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        return null != currentRuleConfig && !getIdenticalData(getCurrentShardingAlgorithms(currentRuleConfig), sqlStatement.getNames()).isEmpty();
    }
    
    private void dropShardingAlgorithm(final ShardingRuleConfiguration currentRuleConfig, final String algorithmName) {
        getCurrentShardingAlgorithms(currentRuleConfig).removeIf(algorithmName::equalsIgnoreCase);
    }
    
    @Override
    public Class<ShardingRuleConfiguration> getRuleConfigurationClass() {
        return ShardingRuleConfiguration.class;
    }
    
    @Override
    public Class<DropShardingAlgorithmStatement> getType() {
        return DropShardingAlgorithmStatement.class;
    }
}
