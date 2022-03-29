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
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredAlgorithmMissedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionAlterUpdater;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.converter.ShardingTableRuleStatementConverter;
import org.apache.shardingsphere.sharding.distsql.handler.enums.ShardingStrategyLevelType;
import org.apache.shardingsphere.sharding.distsql.handler.enums.ShardingStrategyType;
import org.apache.shardingsphere.sharding.distsql.parser.statement.AlterDefaultShardingStrategyStatement;

import java.util.Collections;
import java.util.Optional;

/**
 * Alter default sharding strategy statement updater.
 */
public final class AlterDefaultShardingStrategyStatementUpdater implements RuleDefinitionAlterUpdater<AlterDefaultShardingStrategyStatement, ShardingRuleConfiguration> {
    
    private static final String TYPE = AlterDefaultShardingStrategyStatement.class.getName();
    
    @Override
    public void checkSQLStatement(final ShardingSphereMetaData shardingSphereMetaData, final AlterDefaultShardingStrategyStatement sqlStatement,
                                  final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        String schemaName = shardingSphereMetaData.getName();
        checkCurrentRuleConfiguration(schemaName, currentRuleConfig);
        checkAlgorithm(schemaName, currentRuleConfig, sqlStatement);
        checkExist(schemaName, sqlStatement, currentRuleConfig);
    }
    
    private void checkCurrentRuleConfiguration(final String schemaName, final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        DistSQLException.predictionThrow(currentRuleConfig != null, () -> new RequiredRuleMissedException("Sharding", schemaName));
    }
    
    private void checkAlgorithm(final String schemaName, final ShardingRuleConfiguration currentRuleConfig, final AlterDefaultShardingStrategyStatement sqlStatement) throws DistSQLException {
        DistSQLException.predictionThrow(ShardingStrategyType.contain(sqlStatement.getStrategyType()), () -> new InvalidAlgorithmConfigurationException(sqlStatement.getStrategyType()));
        DistSQLException.predictionThrow(ShardingStrategyType.getValueOf(sqlStatement.getStrategyType()).isValid(sqlStatement.getShardingColumn()),
            () -> new InvalidAlgorithmConfigurationException(sqlStatement.getStrategyType()));
        DistSQLException.predictionThrow(isAlgorithmDefinitionExists(sqlStatement), () -> new RequiredAlgorithmMissedException());
        if (null == sqlStatement.getShardingAlgorithmName() && null != sqlStatement.getAlgorithmSegment()) {
            return;
        }
        boolean isAlgorithmExist = currentRuleConfig.getShardingAlgorithms().containsKey(sqlStatement.getShardingAlgorithmName());
        DistSQLException.predictionThrow(isAlgorithmExist, () -> new RequiredAlgorithmMissedException(schemaName, Collections.singleton(sqlStatement.getShardingAlgorithmName())));
    }
    
    private boolean isAlgorithmDefinitionExists(final AlterDefaultShardingStrategyStatement sqlStatement) {
        return null != sqlStatement.getShardingAlgorithmName() || null != sqlStatement.getAlgorithmSegment();
    }
    
    private void checkExist(final String schemaName, final AlterDefaultShardingStrategyStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        Optional<ShardingStrategyConfiguration> strategyConfiguration = getStrategyConfiguration(currentRuleConfig, sqlStatement.getDefaultType());
        DistSQLException.predictionThrow(strategyConfiguration.isPresent(),
            () -> new RequiredRuleMissedException(String.format("Default sharding %s strategy", sqlStatement.getDefaultType().toLowerCase()), schemaName));
    }
    
    private Optional<ShardingStrategyConfiguration> getStrategyConfiguration(final ShardingRuleConfiguration currentRuleConfig, final String type) {
        ShardingStrategyConfiguration result = type.equalsIgnoreCase(ShardingStrategyLevelType.TABLE.name())
                ? currentRuleConfig.getDefaultTableShardingStrategy() : currentRuleConfig.getDefaultDatabaseShardingStrategy();
        return Optional.ofNullable(result);
    }
    
    @Override
    public ShardingRuleConfiguration buildToBeAlteredRuleConfiguration(final AlterDefaultShardingStrategyStatement sqlStatement) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        String shardingAlgorithmName = getShardingAlgorithmName(sqlStatement, result);
        ShardingStrategyConfiguration strategyConfiguration = ShardingTableRuleStatementConverter.createStrategyConfiguration(sqlStatement.getStrategyType(),
                sqlStatement.getShardingColumn(), shardingAlgorithmName);
        setStrategyConfiguration(result, sqlStatement.getDefaultType(), strategyConfiguration);
        return result;
    }
    
    private String getShardingAlgorithmName(final AlterDefaultShardingStrategyStatement sqlStatement, final ShardingRuleConfiguration shardingRuleConfiguration) {
        if (null != sqlStatement.getShardingAlgorithmName()) {
            return sqlStatement.getShardingAlgorithmName();
        }
        return createDefaultAlgorithm(sqlStatement, shardingRuleConfiguration);
    }
    
    private String createDefaultAlgorithm(final AlterDefaultShardingStrategyStatement sqlStatement, final ShardingRuleConfiguration shardingRuleConfiguration) {
        String result = getDefaultShardingAlgorithmName(sqlStatement.getDefaultType(), sqlStatement.getAlgorithmSegment().getName());
        shardingRuleConfiguration.getShardingAlgorithms().put(result, createAlgorithmConfiguration(sqlStatement.getAlgorithmSegment()));
        return result;
    }
    
    private static ShardingSphereAlgorithmConfiguration createAlgorithmConfiguration(final AlgorithmSegment segment) {
        return new ShardingSphereAlgorithmConfiguration(segment.getName(), segment.getProps());
    }
    
    private static String getDefaultShardingAlgorithmName(final String defaultType, final String algorithmType) {
        return String.format("default_%s_%s", defaultType.toLowerCase(), algorithmType);
    }
    
    private void setStrategyConfiguration(final ShardingRuleConfiguration configuration, final String type, final ShardingStrategyConfiguration shardingStrategyConfiguration) {
        if (type.equalsIgnoreCase(ShardingStrategyLevelType.TABLE.name())) {
            configuration.setDefaultTableShardingStrategy(shardingStrategyConfiguration);
        } else {
            configuration.setDefaultDatabaseShardingStrategy(shardingStrategyConfiguration);
        }
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final ShardingRuleConfiguration currentRuleConfig, final ShardingRuleConfiguration toBeAlteredRuleConfig) {
        if (!toBeAlteredRuleConfig.getShardingAlgorithms().isEmpty()) {
            currentRuleConfig.getShardingAlgorithms().putAll(toBeAlteredRuleConfig.getShardingAlgorithms());
        }
        if (null != toBeAlteredRuleConfig.getDefaultTableShardingStrategy()) {
            currentRuleConfig.setDefaultTableShardingStrategy(toBeAlteredRuleConfig.getDefaultTableShardingStrategy());
        }
        if (null != toBeAlteredRuleConfig.getDefaultDatabaseShardingStrategy()) {
            currentRuleConfig.setDefaultDatabaseShardingStrategy(toBeAlteredRuleConfig.getDefaultDatabaseShardingStrategy());
        }
    }
    
    @Override
    public Class<ShardingRuleConfiguration> getRuleConfigurationClass() {
        return ShardingRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return TYPE;
    }
}
