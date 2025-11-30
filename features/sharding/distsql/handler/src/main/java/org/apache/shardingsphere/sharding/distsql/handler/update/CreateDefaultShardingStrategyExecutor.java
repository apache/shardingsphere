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

import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.type.DatabaseRuleCreateExecutor;
import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.exception.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.algorithm.core.exception.MissingRequiredAlgorithmException;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.external.sql.identifier.SQLExceptionIdentifier;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.converter.ShardingTableRuleStatementConverter;
import org.apache.shardingsphere.sharding.distsql.handler.enums.ShardingStrategyLevelType;
import org.apache.shardingsphere.sharding.distsql.handler.enums.ShardingStrategyType;
import org.apache.shardingsphere.sharding.distsql.statement.CreateDefaultShardingStrategyStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Optional;

/**
 * Create default sharding strategy executor.
 */
@Setter
public final class CreateDefaultShardingStrategyExecutor implements DatabaseRuleCreateExecutor<CreateDefaultShardingStrategyStatement, ShardingRule, ShardingRuleConfiguration> {
    
    private ShardingSphereDatabase database;
    
    private ShardingRule rule;
    
    @Override
    public void checkBeforeUpdate(final CreateDefaultShardingStrategyStatement sqlStatement) {
        if (!"none".equalsIgnoreCase(sqlStatement.getStrategyType())) {
            checkAlgorithm(sqlStatement);
        }
        if (!sqlStatement.isIfNotExists()) {
            checkExist(sqlStatement);
        }
    }
    
    private void checkAlgorithm(final CreateDefaultShardingStrategyStatement sqlStatement) {
        ShardingSpherePreconditions.checkState(ShardingStrategyType.isValidType(sqlStatement.getStrategyType()), () -> new InvalidAlgorithmConfigurationException(sqlStatement.getStrategyType()));
        ShardingSpherePreconditions.checkState(ShardingStrategyType.getValueOf(sqlStatement.getStrategyType())
                .isValid(sqlStatement.getShardingColumn()), () -> new InvalidAlgorithmConfigurationException(sqlStatement.getStrategyType()));
        ShardingSpherePreconditions.checkNotNull(sqlStatement.getAlgorithmSegment(), () -> new MissingRequiredAlgorithmException("Sharding", new SQLExceptionIdentifier("")));
    }
    
    private void checkExist(final CreateDefaultShardingStrategyStatement sqlStatement) {
        if (null == rule) {
            return;
        }
        ShardingSpherePreconditions.checkState(!getStrategyConfiguration(sqlStatement.getDefaultType()).isPresent(),
                () -> new DuplicateRuleException(String.format("default sharding %s strategy", sqlStatement.getDefaultType().toLowerCase()), database.getName()));
    }
    
    private Optional<ShardingStrategyConfiguration> getStrategyConfiguration(final String type) {
        ShardingStrategyConfiguration result = type.equalsIgnoreCase(ShardingStrategyLevelType.TABLE.name())
                ? rule.getConfiguration().getDefaultTableShardingStrategy()
                : rule.getConfiguration().getDefaultDatabaseShardingStrategy();
        return Optional.ofNullable(result);
    }
    
    @Override
    public ShardingRuleConfiguration buildToBeCreatedRuleConfiguration(final CreateDefaultShardingStrategyStatement sqlStatement) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        if ("none".equalsIgnoreCase(sqlStatement.getStrategyType())) {
            setStrategyConfiguration(result, sqlStatement.getDefaultType(), new NoneShardingStrategyConfiguration());
        } else {
            String shardingAlgorithmName = getShardingAlgorithmName(sqlStatement, result);
            ShardingStrategyConfiguration strategyConfig = ShardingTableRuleStatementConverter.createStrategyConfiguration(
                    sqlStatement.getStrategyType(), sqlStatement.getShardingColumn(), shardingAlgorithmName);
            setStrategyConfiguration(result, sqlStatement.getDefaultType(), strategyConfig);
        }
        return result;
    }
    
    private String getShardingAlgorithmName(final CreateDefaultShardingStrategyStatement sqlStatement, final ShardingRuleConfiguration ruleConfig) {
        return createDefaultAlgorithm(sqlStatement, ruleConfig);
    }
    
    private String createDefaultAlgorithm(final CreateDefaultShardingStrategyStatement sqlStatement, final ShardingRuleConfiguration ruleConfig) {
        String result = getDefaultShardingAlgorithmName(sqlStatement.getDefaultType(), sqlStatement.getAlgorithmSegment().getName());
        ruleConfig.getShardingAlgorithms().put(result, createAlgorithmConfiguration(sqlStatement.getAlgorithmSegment()));
        return result;
    }
    
    private AlgorithmConfiguration createAlgorithmConfiguration(final AlgorithmSegment segment) {
        return new AlgorithmConfiguration(segment.getName(), segment.getProps());
    }
    
    private String getDefaultShardingAlgorithmName(final String defaultType, final String algorithmType) {
        return String.format("default_%s_%s", defaultType, algorithmType).toLowerCase();
    }
    
    private void setStrategyConfiguration(final ShardingRuleConfiguration ruleConfig, final String type, final ShardingStrategyConfiguration shardingStrategyConfig) {
        if (type.equalsIgnoreCase(ShardingStrategyLevelType.TABLE.name())) {
            ruleConfig.setDefaultTableShardingStrategy(shardingStrategyConfig);
        } else {
            ruleConfig.setDefaultDatabaseShardingStrategy(shardingStrategyConfig);
        }
    }
    
    @Override
    public Class<ShardingRule> getRuleClass() {
        return ShardingRule.class;
    }
    
    @Override
    public Class<CreateDefaultShardingStrategyStatement> getType() {
        return CreateDefaultShardingStrategyStatement.class;
    }
}
