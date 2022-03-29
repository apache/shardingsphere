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

import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionDropUpdater;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.enums.ShardingStrategyLevelType;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropDefaultShardingStrategyStatement;

import java.util.Optional;

/**
 * Drop sharding table rule statement updater.
 */
public final class DropDefaultStrategyStatementUpdater implements RuleDefinitionDropUpdater<DropDefaultShardingStrategyStatement, ShardingRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereMetaData shardingSphereMetaData, final DropDefaultShardingStrategyStatement sqlStatement,
                                  final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        String schemaName = shardingSphereMetaData.getName();
        if (!isExistRuleConfig(currentRuleConfig) && sqlStatement.isContainsExistClause()) {
            return;
        }
        checkCurrentRuleConfiguration(schemaName, currentRuleConfig);
        checkExist(schemaName, sqlStatement, currentRuleConfig);
    }
    
    private void checkExist(final String schemaName, final DropDefaultShardingStrategyStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        if (sqlStatement.isContainsExistClause()) {
            return;
        }
        Optional<ShardingStrategyConfiguration> strategyConfiguration = getStrategyConfiguration(currentRuleConfig, sqlStatement.getDefaultType());
        DistSQLException.predictionThrow(strategyConfiguration.isPresent(),
            () -> new RequiredRuleMissedException(String.format("Default sharding %s strategy", sqlStatement.getDefaultType().toLowerCase()), schemaName));
    }
    
    private Optional<ShardingStrategyConfiguration> getStrategyConfiguration(final ShardingRuleConfiguration currentRuleConfig, final String type) {
        ShardingStrategyConfiguration result = type.equalsIgnoreCase(ShardingStrategyLevelType.TABLE.name())
                ? currentRuleConfig.getDefaultTableShardingStrategy() : currentRuleConfig.getDefaultDatabaseShardingStrategy();
        return Optional.ofNullable(result);
    }
    
    private void checkCurrentRuleConfiguration(final String schemaName, final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        DistSQLException.predictionThrow(currentRuleConfig != null, () -> new RequiredRuleMissedException("Sharding", schemaName));
    }
    
    @Override
    public boolean hasAnyOneToBeDropped(final DropDefaultShardingStrategyStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        if (sqlStatement.getDefaultType().equalsIgnoreCase(ShardingStrategyLevelType.TABLE.name())) {
            return null != currentRuleConfig && null != currentRuleConfig.getDefaultTableShardingStrategy();
        }
        return null != currentRuleConfig && null != currentRuleConfig.getDefaultDatabaseShardingStrategy();
    }
    
    @Override
    public boolean updateCurrentRuleConfiguration(final DropDefaultShardingStrategyStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        if (sqlStatement.getDefaultType().equalsIgnoreCase(ShardingStrategyLevelType.TABLE.name())) {
            currentRuleConfig.setDefaultTableShardingStrategy(null);
        } else {
            currentRuleConfig.setDefaultDatabaseShardingStrategy(null);
        }
        return false;
    }
    
    @Override
    public Class<ShardingRuleConfiguration> getRuleConfigurationClass() {
        return ShardingRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return DropDefaultShardingStrategyStatement.class.getName();
    }
}
