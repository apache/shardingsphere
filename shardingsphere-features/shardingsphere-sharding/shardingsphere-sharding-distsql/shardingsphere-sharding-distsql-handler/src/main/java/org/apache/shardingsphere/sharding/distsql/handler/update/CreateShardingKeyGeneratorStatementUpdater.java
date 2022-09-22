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
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateAlgorithmException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionCreateUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.converter.ShardingTableRuleStatementConverter;
import org.apache.shardingsphere.sharding.distsql.parser.segment.ShardingKeyGeneratorSegment;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingKeyGeneratorStatement;
import org.apache.shardingsphere.sharding.factory.KeyGenerateAlgorithmFactory;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Create sharding key generator statement updater.
 */
public final class CreateShardingKeyGeneratorStatementUpdater implements RuleDefinitionCreateUpdater<CreateShardingKeyGeneratorStatement, ShardingRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database,
                                  final CreateShardingKeyGeneratorStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        checkDuplicate(database.getName(), sqlStatement, currentRuleConfig);
        checkKeyGeneratorAlgorithm(sqlStatement);
    }
    
    private void checkDuplicate(final String databaseName, final CreateShardingKeyGeneratorStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        Collection<String> keyGeneratorNames = sqlStatement.getKeyGeneratorSegments().stream().map(ShardingKeyGeneratorSegment::getKeyGeneratorName).collect(Collectors.toList());
        checkDuplicateInput(keyGeneratorNames, duplicated -> new DuplicateAlgorithmException("Key generator", databaseName, duplicated));
        if (null != currentRuleConfig) {
            checkExist(keyGeneratorNames, currentRuleConfig.getKeyGenerators().keySet(), duplicated -> new DuplicateAlgorithmException("Key generator", databaseName, duplicated));
        }
    }
    
    private void checkDuplicateInput(final Collection<String> rules, final Function<Collection<String>, DistSQLException> thrower) throws DistSQLException {
        Collection<String> duplicateRequire = rules.stream().collect(Collectors.groupingBy(each -> each, Collectors.counting())).entrySet().stream()
                .filter(each -> each.getValue() > 1).map(Map.Entry::getKey).collect(Collectors.toSet());
        ShardingSpherePreconditions.checkState(duplicateRequire.isEmpty(), () -> thrower.apply(duplicateRequire));
    }
    
    private void checkExist(final Collection<String> requireRules, final Collection<String> currentRules, final Function<Collection<String>, DistSQLException> thrower) throws DistSQLException {
        Collection<String> identical = requireRules.stream().filter(currentRules::contains).collect(Collectors.toSet());
        ShardingSpherePreconditions.checkState(identical.isEmpty(), () -> thrower.apply(identical));
    }
    
    private void checkKeyGeneratorAlgorithm(final CreateShardingKeyGeneratorStatement sqlStatement) throws DistSQLException {
        Collection<String> notExistedKeyGeneratorAlgorithms = sqlStatement.getKeyGeneratorSegments().stream().map(ShardingKeyGeneratorSegment::getAlgorithmSegment).map(AlgorithmSegment::getName)
                .filter(each -> !KeyGenerateAlgorithmFactory.contains(each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(notExistedKeyGeneratorAlgorithms.isEmpty(), () -> new InvalidAlgorithmConfigurationException("sharding", notExistedKeyGeneratorAlgorithms));
    }
    
    @Override
    public ShardingRuleConfiguration buildToBeCreatedRuleConfiguration(final CreateShardingKeyGeneratorStatement sqlStatement) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        Map<String, AlgorithmConfiguration> keyGeneratorConfigurationMap = sqlStatement.getKeyGeneratorSegments().stream()
                .collect(Collectors.toMap(ShardingKeyGeneratorSegment::getKeyGeneratorName, each -> ShardingTableRuleStatementConverter.createAlgorithmConfiguration(each.getAlgorithmSegment())));
        result.setKeyGenerators(keyGeneratorConfigurationMap);
        return result;
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final ShardingRuleConfiguration currentRuleConfig, final ShardingRuleConfiguration toBeCreatedRuleConfig) {
        if (null != currentRuleConfig) {
            currentRuleConfig.getKeyGenerators().putAll(toBeCreatedRuleConfig.getKeyGenerators());
        }
    }
    
    @Override
    public Class<ShardingRuleConfiguration> getRuleConfigurationClass() {
        return ShardingRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return CreateShardingKeyGeneratorStatement.class.getName();
    }
}
