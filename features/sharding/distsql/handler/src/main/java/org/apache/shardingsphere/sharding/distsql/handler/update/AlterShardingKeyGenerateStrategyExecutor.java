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
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.type.DatabaseRuleAlterExecutor;
import org.apache.shardingsphere.distsql.handler.required.DistSQLExecutorCurrentRuleRequired;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.exception.UnregisteredAlgorithmException;
import org.apache.shardingsphere.infra.config.keygen.KeyGenerateStrategiesConfiguration;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.external.sql.identifier.SQLExceptionIdentifier;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.converter.ShardingKeyGenerateStrategyStatementConverter;
import org.apache.shardingsphere.sharding.distsql.segment.strategy.AbstractKeyGenerateStrategyDefinitionSegment;
import org.apache.shardingsphere.sharding.distsql.statement.AlterShardingKeyGenerateStrategyStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

/**
 * Alter sharding key generate strategy executor.
 */
@DistSQLExecutorCurrentRuleRequired(ShardingRule.class)
@Setter
public final class AlterShardingKeyGenerateStrategyExecutor
        implements DatabaseRuleAlterExecutor<AlterShardingKeyGenerateStrategyStatement, ShardingRule, ShardingRuleConfiguration> {
    
    private ShardingSphereDatabase database;
    
    private ShardingRule rule;
    
    @Override
    public void checkBeforeUpdate(final AlterShardingKeyGenerateStrategyStatement sqlStatement) {
        ShardingSpherePreconditions.checkContains(rule.getConfiguration().getKeyGenerateStrategies().keySet(), sqlStatement.getName(),
                () -> new MissingRequiredRuleException("Sharding key generate strategy", database.getName(), sqlStatement.getName()));
        if (sqlStatement.getKeyGenerateStrategySegment().getAlgorithmSegment().isPresent()) {
            checkDuplicateGeneratedKeyGenerator(sqlStatement);
        } else if (sqlStatement.getKeyGenerateStrategySegment().getKeyGeneratorName().isPresent()) {
            checkReferencedKeyGenerator(sqlStatement.getKeyGenerateStrategySegment().getKeyGeneratorName().get());
        }
    }
    
    private void checkDuplicateGeneratedKeyGenerator(final AlterShardingKeyGenerateStrategyStatement sqlStatement) {
        String keyGeneratorName = ShardingKeyGenerateStrategyStatementConverter.getKeyGeneratorName(sqlStatement.getName(), sqlStatement.getKeyGenerateStrategySegment());
        String currentKeyGeneratorName = rule.getConfiguration().getKeyGenerateStrategies().get(sqlStatement.getName()).getKeyGeneratorName();
        boolean containsSameNameKeyGenerator = rule.getConfiguration().getKeyGenerators().containsKey(sqlStatement.getName());
        ShardingSpherePreconditions.checkState(!containsSameNameKeyGenerator || keyGeneratorName.equals(currentKeyGeneratorName),
                () -> new DuplicateRuleException("key generator", database.getName(), Collections.singleton(sqlStatement.getName())));
    }
    
    private void checkReferencedKeyGenerator(final String keyGeneratorName) {
        ShardingSpherePreconditions.checkContains(rule.getConfiguration().getKeyGenerators().keySet(), keyGeneratorName,
                () -> new UnregisteredAlgorithmException("Key generator", keyGeneratorName, new SQLExceptionIdentifier(database.getName())));
    }
    
    @Override
    public ShardingRuleConfiguration buildToBeAlteredRuleConfiguration(final AlterShardingKeyGenerateStrategyStatement sqlStatement) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        AbstractKeyGenerateStrategyDefinitionSegment keyGenerateStrategySegment = sqlStatement.getKeyGenerateStrategySegment();
        String keyGeneratorName = ShardingKeyGenerateStrategyStatementConverter.getKeyGeneratorName(sqlStatement.getName(), keyGenerateStrategySegment);
        result.getKeyGenerateStrategies().put(sqlStatement.getName(), ShardingKeyGenerateStrategyStatementConverter.createKeyGenerateStrategiesConfig(keyGenerateStrategySegment, keyGeneratorName));
        keyGenerateStrategySegment.getAlgorithmSegment().ifPresent(each -> result.getKeyGenerators().put(keyGeneratorName, new AlgorithmConfiguration(each.getName(), each.getProps())));
        return result;
    }
    
    @Override
    public ShardingRuleConfiguration buildToBeDroppedRuleConfiguration(final ShardingRuleConfiguration toBeAlteredRuleConfig) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        Collection<String> inUsedKeyGenerators = getInUsedKeyGeneratorsWithoutCurrentStrategies(toBeAlteredRuleConfig.getKeyGenerateStrategies().keySet());
        for (String each : toBeAlteredRuleConfig.getKeyGenerateStrategies().keySet()) {
            String currentKeyGeneratorName = rule.getConfiguration().getKeyGenerateStrategies().get(each).getKeyGeneratorName();
            String alteredKeyGeneratorName = toBeAlteredRuleConfig.getKeyGenerateStrategies().get(each).getKeyGeneratorName();
            if (!currentKeyGeneratorName.equals(alteredKeyGeneratorName) && !inUsedKeyGenerators.contains(currentKeyGeneratorName)) {
                result.getKeyGenerators().put(currentKeyGeneratorName, rule.getConfiguration().getKeyGenerators().get(currentKeyGeneratorName));
            }
        }
        return result;
    }
    
    private Collection<String> getInUsedKeyGeneratorsWithoutCurrentStrategies(final Collection<String> strategyNames) {
        ShardingRuleConfiguration currentRuleConfig = rule.getConfiguration();
        Collection<String> result = new LinkedHashSet<>(UnusedAlgorithmFinder.findInUsedKeyGenerator(currentRuleConfig));
        strategyNames.stream().map(currentRuleConfig.getKeyGenerateStrategies()::get).map(KeyGenerateStrategiesConfiguration::getKeyGeneratorName).forEach(result::remove);
        return result;
    }
    
    @Override
    public Class<ShardingRule> getRuleClass() {
        return ShardingRule.class;
    }
    
    @Override
    public Class<AlterShardingKeyGenerateStrategyStatement> getType() {
        return AlterShardingKeyGenerateStrategyStatement.class;
    }
}
