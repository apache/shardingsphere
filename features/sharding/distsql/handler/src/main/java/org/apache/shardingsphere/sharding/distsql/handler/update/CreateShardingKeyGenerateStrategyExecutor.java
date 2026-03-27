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
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.exception.UnregisteredAlgorithmException;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.external.sql.identifier.SQLExceptionIdentifier;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.converter.ShardingKeyGenerateStrategyStatementConverter;
import org.apache.shardingsphere.sharding.distsql.segment.strategy.AbstractKeyGenerateStrategyDefinitionSegment;
import org.apache.shardingsphere.sharding.distsql.statement.CreateShardingKeyGenerateStrategyStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Collections;

/**
 * Create sharding key generate strategy executor.
 */
@Setter
public final class CreateShardingKeyGenerateStrategyExecutor
        implements
            DatabaseRuleCreateExecutor<CreateShardingKeyGenerateStrategyStatement, ShardingRule, ShardingRuleConfiguration> {
    
    private ShardingSphereDatabase database;
    
    private ShardingRule rule;
    
    @Override
    public void checkBeforeUpdate(final CreateShardingKeyGenerateStrategyStatement sqlStatement) {
        if (!sqlStatement.isIfNotExists()) {
            checkDuplicateStrategy(sqlStatement);
        }
        if (isSkipped(sqlStatement)) {
            return;
        }
        String keyGeneratorName = ShardingKeyGenerateStrategyStatementConverter.getKeyGeneratorName(sqlStatement.getName(), sqlStatement.getKeyGenerateStrategySegment());
        if (sqlStatement.getKeyGenerateStrategySegment().getAlgorithmSegment().isPresent()) {
            checkDuplicateGeneratedKeyGenerator(keyGeneratorName);
        } else if (sqlStatement.getKeyGenerateStrategySegment().getKeyGeneratorName().isPresent()) {
            checkReferencedKeyGenerator(sqlStatement.getKeyGenerateStrategySegment().getKeyGeneratorName().get());
        }
    }
    
    private void checkDuplicateStrategy(final CreateShardingKeyGenerateStrategyStatement sqlStatement) {
        ShardingSpherePreconditions.checkState(!getCurrentRuleConfig().getKeyGenerateStrategies().containsKey(sqlStatement.getName()),
                () -> new DuplicateRuleException("sharding key generate strategy", database.getName(), Collections.singleton(sqlStatement.getName())));
    }
    
    private boolean isSkipped(final CreateShardingKeyGenerateStrategyStatement sqlStatement) {
        return sqlStatement.isIfNotExists() && getCurrentRuleConfig().getKeyGenerateStrategies().containsKey(sqlStatement.getName());
    }
    
    private void checkDuplicateGeneratedKeyGenerator(final String generatorName) {
        ShardingSpherePreconditions.checkState(!getCurrentRuleConfig().getKeyGenerators().containsKey(generatorName),
                () -> new DuplicateRuleException("key generator", database.getName(), Collections.singleton(generatorName)));
    }
    
    private void checkReferencedKeyGenerator(final String keyGeneratorName) {
        ShardingSpherePreconditions.checkContains(getCurrentRuleConfig().getKeyGenerators().keySet(), keyGeneratorName,
                () -> new UnregisteredAlgorithmException("Key generator", keyGeneratorName, new SQLExceptionIdentifier(database.getName())));
    }
    
    @Override
    public ShardingRuleConfiguration buildToBeCreatedRuleConfiguration(final CreateShardingKeyGenerateStrategyStatement sqlStatement) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        if (isSkipped(sqlStatement)) {
            return result;
        }
        AbstractKeyGenerateStrategyDefinitionSegment keyGenerateStrategySegment = sqlStatement.getKeyGenerateStrategySegment();
        String keyGeneratorName = ShardingKeyGenerateStrategyStatementConverter.getKeyGeneratorName(sqlStatement.getName(), keyGenerateStrategySegment);
        result.getKeyGenerateStrategies().put(sqlStatement.getName(), ShardingKeyGenerateStrategyStatementConverter.createKeyGenerateStrategiesConfig(keyGenerateStrategySegment, keyGeneratorName));
        keyGenerateStrategySegment.getAlgorithmSegment().ifPresent(each -> result.getKeyGenerators().put(keyGeneratorName, new AlgorithmConfiguration(each.getName(), each.getProps())));
        return result;
    }
    
    private ShardingRuleConfiguration getCurrentRuleConfig() {
        return null == rule ? new ShardingRuleConfiguration() : rule.getConfiguration();
    }
    
    @Override
    public Class<ShardingRule> getRuleClass() {
        return ShardingRule.class;
    }
    
    @Override
    public Class<CreateShardingKeyGenerateStrategyStatement> getType() {
        return CreateShardingKeyGenerateStrategyStatement.class;
    }
}
