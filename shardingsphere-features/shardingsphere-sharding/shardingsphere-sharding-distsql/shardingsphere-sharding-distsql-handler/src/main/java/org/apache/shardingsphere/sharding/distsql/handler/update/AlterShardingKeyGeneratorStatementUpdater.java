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
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionCreateUpdater;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.converter.ShardingTableRuleStatementConverter;
import org.apache.shardingsphere.sharding.distsql.parser.segment.ShardingKeyGeneratorSegment;
import org.apache.shardingsphere.sharding.distsql.parser.statement.AlterShardingKeyGeneratorStatement;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;
import org.apache.shardingsphere.spi.typed.TypedSPIRegistry;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Alter sharding key generator statement updater.
 */
public final class AlterShardingKeyGeneratorStatementUpdater implements RuleDefinitionCreateUpdater<AlterShardingKeyGeneratorStatement, ShardingRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereMetaData shardingSphereMetaData, final AlterShardingKeyGeneratorStatement sqlStatement,
                                  final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        String schemaName = shardingSphereMetaData.getName();
        LinkedList<String> requireNames = sqlStatement.getKeyGeneratorSegments().stream().map(ShardingKeyGeneratorSegment::getKeyGeneratorName).collect(Collectors.toCollection(LinkedList::new));
        checkDuplicate(schemaName, requireNames);
        checkExist(requireNames, currentRuleConfig);
        checkAlgorithmType(sqlStatement);
    }

    private void checkDuplicate(final String schemaName, final Collection<String> requireNames) throws DistSQLException {
        Set<String> duplicateRequire = requireNames.stream().collect(Collectors.groupingBy(each -> each, Collectors.counting())).entrySet().stream()
                .filter(each -> each.getValue() > 1).map(Map.Entry::getKey).collect(Collectors.toSet());
        DistSQLException.predictionThrow(duplicateRequire.isEmpty(), new DuplicateRuleException("sharding", schemaName, duplicateRequire));
    }
    
    private void checkExist(final Collection<String> requireNames, final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        LinkedList<String> notExistAlgorithms = requireNames.stream().filter(each -> !currentRuleConfig.getKeyGenerators().containsKey(each)).collect(Collectors.toCollection(LinkedList::new));
        DistSQLException.predictionThrow(notExistAlgorithms.isEmpty(), new RequiredAlgorithmMissedException("sharding", notExistAlgorithms));
    }
    
    private void checkAlgorithmType(final AlterShardingKeyGeneratorStatement sqlStatement) throws DistSQLException {
        LinkedList<String> requireNames = sqlStatement.getKeyGeneratorSegments().stream()
                .map(ShardingKeyGeneratorSegment::getAlgorithmSegment).map(AlgorithmSegment::getName).collect(Collectors.toCollection(LinkedList::new));
        Collection<String> invalidAlgorithmNames = requireNames.stream()
                .filter(each -> !TypedSPIRegistry.findRegisteredService(KeyGenerateAlgorithm.class, each, new Properties()).isPresent()).collect(Collectors.toList());
        DistSQLException.predictionThrow(invalidAlgorithmNames.isEmpty(), new InvalidAlgorithmConfigurationException("sharding", invalidAlgorithmNames));
    }
    
    @Override
    public ShardingRuleConfiguration buildToBeCreatedRuleConfiguration(final AlterShardingKeyGeneratorStatement sqlStatement) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        Map<String, ShardingSphereAlgorithmConfiguration> algorithmConfigurationMap = sqlStatement.getKeyGeneratorSegments().stream()
                .collect(Collectors.toMap(ShardingKeyGeneratorSegment::getKeyGeneratorName, each -> ShardingTableRuleStatementConverter.createAlgorithmConfiguration(each.getAlgorithmSegment())));
        result.setKeyGenerators(algorithmConfigurationMap);
        return result;
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final ShardingRuleConfiguration currentRuleConfig, final ShardingRuleConfiguration toBeCreatedRuleConfig) {
        currentRuleConfig.getKeyGenerators().putAll(toBeCreatedRuleConfig.getShardingAlgorithms());
    }
    
    @Override
    public Class<ShardingRuleConfiguration> getRuleConfigurationClass() {
        return ShardingRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return AlterShardingKeyGeneratorStatement.class.getCanonicalName();
    }
}
