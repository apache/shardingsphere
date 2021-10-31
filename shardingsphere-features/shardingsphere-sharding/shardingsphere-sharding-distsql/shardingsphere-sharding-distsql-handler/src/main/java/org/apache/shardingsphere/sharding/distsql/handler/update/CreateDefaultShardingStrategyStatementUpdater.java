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
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredAlgorithmMissedException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionCreateUpdater;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.converter.ShardingTableRuleStatementConverter;
import org.apache.shardingsphere.sharding.distsql.handler.converter.ShardingStrategyType;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateDefaultShardingStrategyStatement;

import java.util.Collections;
import java.util.Optional;

/**
 * Create default sharding strategy statement updater.
 */
public final class CreateDefaultShardingStrategyStatementUpdater implements RuleDefinitionCreateUpdater<CreateDefaultShardingStrategyStatement, ShardingRuleConfiguration> {
    
    private static final String STRATEGY_TYPE_TABLE = "TABLE";
    
    @Override
    public void checkSQLStatement(final ShardingSphereMetaData shardingSphereMetaData, final CreateDefaultShardingStrategyStatement sqlStatement,
                                  final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        String schemaName = shardingSphereMetaData.getName();
        checkAlgorithm(schemaName, currentRuleConfig, sqlStatement);
        checkExist(schemaName, sqlStatement, currentRuleConfig);
    }
    
    private void checkAlgorithm(final String schemaName, final ShardingRuleConfiguration currentRuleConfig, final CreateDefaultShardingStrategyStatement sqlStatement) throws DistSQLException {
        DistSQLException.predictionThrow(ShardingStrategyType.contain(sqlStatement.getStrategyType()), new InvalidAlgorithmConfigurationException(sqlStatement.getStrategyType()));
        boolean isAlgorithmExist = currentRuleConfig.getShardingAlgorithms().containsKey(sqlStatement.getShardingAlgorithmName());
        DistSQLException.predictionThrow(isAlgorithmExist, new RequiredAlgorithmMissedException(schemaName, Collections.singleton(sqlStatement.getShardingAlgorithmName())));
    }
    
    private void checkExist(final String schemaName, final CreateDefaultShardingStrategyStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        Optional<ShardingStrategyConfiguration> strategyConfiguration = getStrategyConfiguration(currentRuleConfig, sqlStatement.getDefaultType());
        DistSQLException.predictionThrow(!strategyConfiguration.isPresent(),
                new DuplicateRuleException(String.format("default sharding %s strategy", sqlStatement.getDefaultType().toLowerCase()), schemaName));
    }
    
    private Optional<ShardingStrategyConfiguration> getStrategyConfiguration(final ShardingRuleConfiguration currentRuleConfig, final String type) {
        ShardingStrategyConfiguration result = type.equalsIgnoreCase(STRATEGY_TYPE_TABLE)
                ? currentRuleConfig.getDefaultTableShardingStrategy() : currentRuleConfig.getDefaultDatabaseShardingStrategy();
        return Optional.ofNullable(result);
    }
    
    @Override
    public ShardingRuleConfiguration buildToBeCreatedRuleConfiguration(final CreateDefaultShardingStrategyStatement sqlStatement) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        ShardingStrategyConfiguration strategyConfiguration = ShardingTableRuleStatementConverter.createStrategyConfiguration(sqlStatement.getStrategyType(),
                sqlStatement.getShardingColumn(), sqlStatement.getShardingAlgorithmName());
        setStrategyConfiguration(result, sqlStatement.getDefaultType(), strategyConfiguration);
        return result;
    }
    
    private void setStrategyConfiguration(final ShardingRuleConfiguration configuration, final String type, final ShardingStrategyConfiguration shardingStrategyConfiguration) {
        if (type.equalsIgnoreCase(STRATEGY_TYPE_TABLE)) {
            configuration.setDefaultTableShardingStrategy(shardingStrategyConfiguration);
        } else {
            configuration.setDefaultDatabaseShardingStrategy(shardingStrategyConfiguration);
        }
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final ShardingRuleConfiguration currentRuleConfig, final ShardingRuleConfiguration toBeCreatedRuleConfig) {
        if (toBeCreatedRuleConfig.getDefaultTableShardingStrategy() != null && currentRuleConfig.getDefaultTableShardingStrategy() == null) {
            currentRuleConfig.setDefaultTableShardingStrategy(toBeCreatedRuleConfig.getDefaultTableShardingStrategy());
        }
        if (toBeCreatedRuleConfig.getDefaultDatabaseShardingStrategy() != null && currentRuleConfig.getDefaultDatabaseShardingStrategy() == null) {
            currentRuleConfig.setDefaultDatabaseShardingStrategy(toBeCreatedRuleConfig.getDefaultDatabaseShardingStrategy());
        }
    }
    
    @Override
    public Class<ShardingRuleConfiguration> getRuleConfigurationClass() {
        return ShardingRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return CreateDefaultShardingStrategyStatement.class.getCanonicalName();
    }
}
