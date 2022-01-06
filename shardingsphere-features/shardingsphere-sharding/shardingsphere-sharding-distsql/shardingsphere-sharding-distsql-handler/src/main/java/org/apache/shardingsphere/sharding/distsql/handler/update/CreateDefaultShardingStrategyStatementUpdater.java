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

import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredAlgorithmMissedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionCreateUpdater;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.converter.ShardingTableRuleStatementConverter;
import org.apache.shardingsphere.sharding.distsql.handler.enums.ShardingStrategyLevelEnum;
import org.apache.shardingsphere.sharding.distsql.handler.enums.ShardingStrategyTypeEnum;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateDefaultShardingStrategyStatement;

import java.util.Collections;
import java.util.Optional;

/**
 * Create default sharding strategy statement updater.
 */
public final class CreateDefaultShardingStrategyStatementUpdater implements RuleDefinitionCreateUpdater<CreateDefaultShardingStrategyStatement, ShardingRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereMetaData shardingSphereMetaData, final CreateDefaultShardingStrategyStatement sqlStatement,
                                  final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        String schemaName = shardingSphereMetaData.getName();
        checkCurrentRuleConfiguration(schemaName, currentRuleConfig);
        checkAlgorithm(schemaName, currentRuleConfig, sqlStatement);
        checkExist(schemaName, sqlStatement, currentRuleConfig);
    }
    
    private void checkCurrentRuleConfiguration(final String schemaName, final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        DistSQLException.predictionThrow(currentRuleConfig != null, new RequiredRuleMissedException("Sharding", schemaName));
    }
    
    private void checkAlgorithm(final String schemaName, final ShardingRuleConfiguration currentRuleConfig, final CreateDefaultShardingStrategyStatement sqlStatement) throws DistSQLException {
        DistSQLException.predictionThrow(ShardingStrategyTypeEnum.contain(sqlStatement.getStrategyType()), new InvalidAlgorithmConfigurationException(sqlStatement.getStrategyType()));
        DistSQLException.predictionThrow(ShardingStrategyTypeEnum.getValueOf(sqlStatement.getStrategyType()).isValid(sqlStatement.getShardingColumn()), 
                new InvalidAlgorithmConfigurationException(sqlStatement.getStrategyType()));
        DistSQLException.predictionThrow(isAlgorithmDefinitionExists(sqlStatement), new RequiredAlgorithmMissedException());
        if (null == sqlStatement.getShardingAlgorithmName() && null != sqlStatement.getAlgorithmSegment()) {
            return;
        }
        boolean isAlgorithmExist = currentRuleConfig.getShardingAlgorithms().containsKey(sqlStatement.getShardingAlgorithmName());
        DistSQLException.predictionThrow(isAlgorithmExist, new RequiredAlgorithmMissedException(schemaName, Collections.singleton(sqlStatement.getShardingAlgorithmName())));
    }
    
    private boolean isAlgorithmDefinitionExists(final CreateDefaultShardingStrategyStatement sqlStatement) {
        return null != sqlStatement.getShardingAlgorithmName() || null != sqlStatement.getAlgorithmSegment();
    }
    
    private void checkExist(final String schemaName, final CreateDefaultShardingStrategyStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        Optional<ShardingStrategyConfiguration> strategyConfiguration = getStrategyConfiguration(currentRuleConfig, sqlStatement.getDefaultType());
        DistSQLException.predictionThrow(!strategyConfiguration.isPresent(),
                new DuplicateRuleException(String.format("default sharding %s strategy", sqlStatement.getDefaultType().toLowerCase()), schemaName));
    }
    
    private Optional<ShardingStrategyConfiguration> getStrategyConfiguration(final ShardingRuleConfiguration currentRuleConfig, final String type) {
        ShardingStrategyConfiguration result = type.equalsIgnoreCase(ShardingStrategyLevelEnum.TABLE.name())
                ? currentRuleConfig.getDefaultTableShardingStrategy() : currentRuleConfig.getDefaultDatabaseShardingStrategy();
        return Optional.ofNullable(result);
    }
    
    @Override
    public ShardingRuleConfiguration buildToBeCreatedRuleConfiguration(final CreateDefaultShardingStrategyStatement sqlStatement) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        String shardingAlgorithmName = getShardingAlgorithmName(sqlStatement, result);
        ShardingStrategyConfiguration strategyConfiguration = ShardingTableRuleStatementConverter.createStrategyConfiguration(sqlStatement.getStrategyType(),
                sqlStatement.getShardingColumn(), shardingAlgorithmName);
        setStrategyConfiguration(result, sqlStatement.getDefaultType(), strategyConfiguration);
        return result;
    }
    
    private String getShardingAlgorithmName(final CreateDefaultShardingStrategyStatement sqlStatement, final ShardingRuleConfiguration shardingRuleConfiguration) {
        if (null != sqlStatement.getShardingAlgorithmName()) {
            return sqlStatement.getShardingAlgorithmName();
        }
        return createDefaultAlgorithm(sqlStatement, shardingRuleConfiguration);
    }
    
    private String createDefaultAlgorithm(final CreateDefaultShardingStrategyStatement sqlStatement, final ShardingRuleConfiguration shardingRuleConfiguration) {
        String algorithmName = getDefaultShardingAlgorithmName(sqlStatement.getDefaultType(), sqlStatement.getAlgorithmSegment().getName());
        shardingRuleConfiguration.getShardingAlgorithms().put(algorithmName, createAlgorithmConfiguration(sqlStatement.getAlgorithmSegment()));
        return algorithmName;
    }

    private static ShardingSphereAlgorithmConfiguration createAlgorithmConfiguration(final AlgorithmSegment segment) {
        return new ShardingSphereAlgorithmConfiguration(segment.getName(), segment.getProps());
    }

    private static String getDefaultShardingAlgorithmName(final String defaultType, final String algorithmType) {
        return String.format("default_%s_%s", defaultType.toLowerCase(), algorithmType);
    }
    
    private void setStrategyConfiguration(final ShardingRuleConfiguration configuration, final String type, final ShardingStrategyConfiguration shardingStrategyConfiguration) {
        if (type.equalsIgnoreCase(ShardingStrategyLevelEnum.TABLE.name())) {
            configuration.setDefaultTableShardingStrategy(shardingStrategyConfiguration);
        } else {
            configuration.setDefaultDatabaseShardingStrategy(shardingStrategyConfiguration);
        }
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final ShardingRuleConfiguration currentRuleConfig, final ShardingRuleConfiguration toBeCreatedRuleConfig) {
        if (!toBeCreatedRuleConfig.getShardingAlgorithms().isEmpty()) {
            currentRuleConfig.getShardingAlgorithms().putAll(toBeCreatedRuleConfig.getShardingAlgorithms());
        }
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
