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
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredAlgorithmMissedException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionAlterUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.converter.ShardingTableRuleStatementConverter;
import org.apache.shardingsphere.sharding.distsql.parser.segment.ShardingAlgorithmSegment;
import org.apache.shardingsphere.sharding.distsql.parser.statement.AlterShardingAlgorithmStatement;
import org.apache.shardingsphere.sharding.factory.ShardingAlgorithmFactory;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Alter sharding algorithm statement updater.
 */
public final class AlterShardingAlgorithmStatementUpdater implements RuleDefinitionAlterUpdater<AlterShardingAlgorithmStatement, ShardingRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database,
                                  final AlterShardingAlgorithmStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        Collection<String> requireNames = sqlStatement.getAlgorithmSegments().stream().map(ShardingAlgorithmSegment::getShardingAlgorithmName).collect(Collectors.toList());
        checkDuplicate(database.getName(), requireNames);
        checkExist(requireNames, currentRuleConfig);
        checkAlgorithmType(sqlStatement);
    }
    
    private void checkDuplicate(final String databaseName, final Collection<String> requireNames) throws DistSQLException {
        Collection<String> duplicateRequire = requireNames.stream().collect(Collectors.groupingBy(each -> each, Collectors.counting())).entrySet().stream()
                .filter(each -> each.getValue() > 1).map(Map.Entry::getKey).collect(Collectors.toSet());
        ShardingSpherePreconditions.checkState(duplicateRequire.isEmpty(), () -> new DuplicateRuleException("sharding", databaseName, duplicateRequire));
    }
    
    private void checkExist(final Collection<String> requireNames, final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        Collection<String> notExistAlgorithms = requireNames.stream().filter(each -> !currentRuleConfig.getShardingAlgorithms().containsKey(each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(notExistAlgorithms.isEmpty(), () -> new RequiredAlgorithmMissedException("sharding", notExistAlgorithms));
    }
    
    private void checkAlgorithmType(final AlterShardingAlgorithmStatement sqlStatement) throws DistSQLException {
        Collection<String> requireNames = sqlStatement.getAlgorithmSegments().stream().map(ShardingAlgorithmSegment::getAlgorithmSegment).map(AlgorithmSegment::getName).collect(Collectors.toList());
        Collection<String> invalidAlgorithmNames = requireNames.stream().filter(each -> !ShardingAlgorithmFactory.contains(each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(invalidAlgorithmNames.isEmpty(), () -> new InvalidAlgorithmConfigurationException("sharding", invalidAlgorithmNames));
    }
    
    @Override
    public ShardingRuleConfiguration buildToBeAlteredRuleConfiguration(final AlterShardingAlgorithmStatement sqlStatement) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        Map<String, AlgorithmConfiguration> algorithmConfigMap = sqlStatement.getAlgorithmSegments().stream()
                .collect(Collectors.toMap(ShardingAlgorithmSegment::getShardingAlgorithmName, each -> ShardingTableRuleStatementConverter.createAlgorithmConfiguration(each.getAlgorithmSegment())));
        result.setShardingAlgorithms(algorithmConfigMap);
        return result;
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final ShardingRuleConfiguration currentRuleConfig, final ShardingRuleConfiguration toBeAlteredRuleConfig) {
        currentRuleConfig.getShardingAlgorithms().putAll(toBeAlteredRuleConfig.getShardingAlgorithms());
    }
    
    @Override
    public Class<ShardingRuleConfiguration> getRuleConfigurationClass() {
        return ShardingRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return AlterShardingAlgorithmStatement.class.getName();
    }
}
