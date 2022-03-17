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
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateKeyGeneratorException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionCreateUpdater;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.converter.ShardingTableRuleStatementConverter;
import org.apache.shardingsphere.sharding.distsql.parser.segment.ShardingKeyGeneratorSegment;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingKeyGeneratorStatement;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;
import org.apache.shardingsphere.spi.typed.TypedSPIRegistry;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Create sharding key generator statement updater.
 */
public final class CreateShardingKeyGeneratorStatementUpdater implements RuleDefinitionCreateUpdater<CreateShardingKeyGeneratorStatement, ShardingRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereMetaData shardingSphereMetaData, final CreateShardingKeyGeneratorStatement sqlStatement,
                                  final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        checkDuplicate(shardingSphereMetaData.getName(), sqlStatement, currentRuleConfig);
        checkKeyGeneratorAlgorithm(sqlStatement);
    }
    
    private void checkDuplicate(final String schemaName, final CreateShardingKeyGeneratorStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        Collection<String> keyGeneratorNames = sqlStatement.getKeyGeneratorSegments().stream()
                .map(ShardingKeyGeneratorSegment::getKeyGeneratorName).collect(Collectors.toCollection(LinkedList::new));
        checkDuplicateInput(keyGeneratorNames, duplicated -> new DuplicateKeyGeneratorException("sharding", schemaName, duplicated));
        if (null != currentRuleConfig) {
            checkExist(keyGeneratorNames, currentRuleConfig.getKeyGenerators().keySet(), duplicated -> new DuplicateKeyGeneratorException("sharding", schemaName, duplicated));
        }
    }
    
    private void checkDuplicateInput(final Collection<String> rules, final Function<Collection<String>, DistSQLException> thrower) throws DistSQLException {
        Collection<String> duplicateRequire = rules.stream().collect(Collectors.groupingBy(each -> each, Collectors.counting())).entrySet().stream()
                .filter(each -> each.getValue() > 1).map(Map.Entry::getKey).collect(Collectors.toSet());
        DistSQLException.predictionThrow(duplicateRequire.isEmpty(), () -> thrower.apply(duplicateRequire));
    }
    
    private void checkExist(final Collection<String> requireRules, final Collection<String> currentRules, final Function<Collection<String>, DistSQLException> thrower) throws DistSQLException {
        Collection<String> identical = requireRules.stream().filter(currentRules::contains).collect(Collectors.toSet());
        DistSQLException.predictionThrow(identical.isEmpty(), () -> thrower.apply(identical));
    }
    
    private void checkKeyGeneratorAlgorithm(final CreateShardingKeyGeneratorStatement sqlStatement) throws DistSQLException {
        Collection<String> notExistedKeyGeneratorAlgorithms = sqlStatement.getKeyGeneratorSegments().stream().map(ShardingKeyGeneratorSegment::getAlgorithmSegment).map(AlgorithmSegment::getName)
                .filter(each -> !TypedSPIRegistry.findRegisteredService(KeyGenerateAlgorithm.class, each, new Properties()).isPresent()).collect(Collectors.toList());
        DistSQLException.predictionThrow(notExistedKeyGeneratorAlgorithms.isEmpty(), () -> new InvalidAlgorithmConfigurationException("sharding", notExistedKeyGeneratorAlgorithms));
    }
    
    @Override
    public ShardingRuleConfiguration buildToBeCreatedRuleConfiguration(final CreateShardingKeyGeneratorStatement sqlStatement) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        Map<String, ShardingSphereAlgorithmConfiguration> keyGeneratorConfigurationMap = sqlStatement.getKeyGeneratorSegments().stream()
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
