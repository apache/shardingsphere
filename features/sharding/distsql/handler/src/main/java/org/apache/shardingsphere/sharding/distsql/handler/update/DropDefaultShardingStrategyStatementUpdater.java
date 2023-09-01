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

import org.apache.shardingsphere.distsql.handler.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.distsql.handler.update.RuleDefinitionDropUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.enums.ShardingStrategyLevelType;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropDefaultShardingStrategyStatement;

import java.util.Optional;

/**
 * Drop default sharding strategy statement updater.
 */
public final class DropDefaultShardingStrategyStatementUpdater implements RuleDefinitionDropUpdater<DropDefaultShardingStrategyStatement, ShardingRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database, final DropDefaultShardingStrategyStatement sqlStatement,
                                  final ShardingRuleConfiguration currentRuleConfig) {
        if (!isExistRuleConfig(currentRuleConfig) && sqlStatement.isIfExists()) {
            return;
        }
        String databaseName = database.getName();
        checkCurrentRuleConfiguration(databaseName, currentRuleConfig);
        checkExist(databaseName, sqlStatement, currentRuleConfig);
    }
    
    private void checkExist(final String databaseName, final DropDefaultShardingStrategyStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        if (sqlStatement.isIfExists()) {
            return;
        }
        Optional<ShardingStrategyConfiguration> strategyConfig = getStrategyConfiguration(currentRuleConfig, sqlStatement.getDefaultType());
        ShardingSpherePreconditions.checkState(strategyConfig.isPresent(), () -> new MissingRequiredRuleException(
                String.format("Default sharding %s strategy", sqlStatement.getDefaultType().toLowerCase()), databaseName));
    }
    
    private Optional<ShardingStrategyConfiguration> getStrategyConfiguration(final ShardingRuleConfiguration currentRuleConfig, final String type) {
        ShardingStrategyConfiguration result = type.equalsIgnoreCase(ShardingStrategyLevelType.TABLE.name())
                ? currentRuleConfig.getDefaultTableShardingStrategy()
                : currentRuleConfig.getDefaultDatabaseShardingStrategy();
        return Optional.ofNullable(result);
    }
    
    private void checkCurrentRuleConfiguration(final String databaseName, final ShardingRuleConfiguration currentRuleConfig) {
        ShardingSpherePreconditions.checkNotNull(currentRuleConfig, () -> new MissingRequiredRuleException("Sharding", databaseName));
    }
    
    @Override
    public boolean hasAnyOneToBeDropped(final DropDefaultShardingStrategyStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        if (sqlStatement.getDefaultType().equalsIgnoreCase(ShardingStrategyLevelType.TABLE.name())) {
            return null != currentRuleConfig && null != currentRuleConfig.getDefaultTableShardingStrategy();
        }
        return null != currentRuleConfig && null != currentRuleConfig.getDefaultDatabaseShardingStrategy();
    }
    
    @Override
    public ShardingRuleConfiguration buildToBeDroppedRuleConfiguration(final ShardingRuleConfiguration currentRuleConfig, final DropDefaultShardingStrategyStatement sqlStatement) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        if (sqlStatement.getDefaultType().equalsIgnoreCase(ShardingStrategyLevelType.TABLE.name())) {
            result.setDefaultTableShardingStrategy(currentRuleConfig.getDefaultTableShardingStrategy());
            currentRuleConfig.setDefaultTableShardingStrategy(null);
        } else {
            result.setDefaultDatabaseShardingStrategy(currentRuleConfig.getDefaultDatabaseShardingStrategy());
            currentRuleConfig.setDefaultDatabaseShardingStrategy(null);
        }
        UnusedAlgorithmFinder.find(currentRuleConfig).forEach(each -> result.getShardingAlgorithms().put(each, currentRuleConfig.getShardingAlgorithms().get(each)));
        return result;
    }
    
    @Override
    public boolean updateCurrentRuleConfiguration(final DropDefaultShardingStrategyStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        if (sqlStatement.getDefaultType().equalsIgnoreCase(ShardingStrategyLevelType.TABLE.name())) {
            currentRuleConfig.setDefaultTableShardingStrategy(null);
        } else {
            currentRuleConfig.setDefaultDatabaseShardingStrategy(null);
        }
        UnusedAlgorithmFinder.find(currentRuleConfig).forEach(each -> currentRuleConfig.getShardingAlgorithms().remove(each));
        return currentRuleConfig.isEmpty();
    }
    
    @Override
    public Class<ShardingRuleConfiguration> getRuleConfigurationClass() {
        return ShardingRuleConfiguration.class;
    }
    
    @Override
    public Class<DropDefaultShardingStrategyStatement> getType() {
        return DropDefaultShardingStrategyStatement.class;
    }
}
