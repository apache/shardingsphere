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
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.MissingRequiredAlgorithmException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionAlterUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.converter.ShardingTableRuleStatementConverter;
import org.apache.shardingsphere.sharding.distsql.parser.segment.ShardingAuditorSegment;
import org.apache.shardingsphere.sharding.distsql.parser.statement.AlterShardingAuditorStatement;
import org.apache.shardingsphere.sharding.factory.KeyGenerateAlgorithmFactory;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Alter sharding auditor statement updater.
 */
public final class AlterShardingAuditorStatementUpdater implements RuleDefinitionAlterUpdater<AlterShardingAuditorStatement, ShardingRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database, final AlterShardingAuditorStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        String databaseName = database.getName();
        Collection<String> requireNames = sqlStatement.getAuditorSegments().stream().map(ShardingAuditorSegment::getAuditorName).collect(Collectors.toList());
        checkDuplicate(databaseName, requireNames);
        checkExist(requireNames, currentRuleConfig);
        checkAlgorithmType(sqlStatement);
    }
    
    private void checkDuplicate(final String databaseName, final Collection<String> requireNames) {
        Collection<String> duplicateRequire = requireNames.stream().collect(Collectors.groupingBy(each -> each, Collectors.counting())).entrySet().stream()
                .filter(each -> each.getValue() > 1).map(Map.Entry::getKey).collect(Collectors.toSet());
        ShardingSpherePreconditions.checkState(duplicateRequire.isEmpty(), () -> new DuplicateRuleException("sharding", databaseName, duplicateRequire));
    }
    
    private void checkExist(final Collection<String> requireNames, final ShardingRuleConfiguration currentRuleConfig) {
        Collection<String> notExistAlgorithms = requireNames.stream().filter(each -> !currentRuleConfig.getAuditors().containsKey(each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(notExistAlgorithms.isEmpty(), () -> new MissingRequiredAlgorithmException("sharding", notExistAlgorithms));
    }
    
    private void checkAlgorithmType(final AlterShardingAuditorStatement sqlStatement) {
        Collection<String> requireNames = sqlStatement.getAuditorSegments().stream()
                .map(ShardingAuditorSegment::getAlgorithmSegment).map(AlgorithmSegment::getName).collect(Collectors.toList());
        Collection<String> invalidAlgorithmNames = requireNames.stream().filter(each -> !KeyGenerateAlgorithmFactory.contains(each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(invalidAlgorithmNames.isEmpty(), () -> new InvalidAlgorithmConfigurationException("sharding", invalidAlgorithmNames));
    }
    
    @Override
    public ShardingRuleConfiguration buildToBeAlteredRuleConfiguration(final AlterShardingAuditorStatement sqlStatement) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        Map<String, AlgorithmConfiguration> algorithmConfigMap = sqlStatement.getAuditorSegments().stream()
                .collect(Collectors.toMap(ShardingAuditorSegment::getAuditorName, each -> ShardingTableRuleStatementConverter.createAlgorithmConfiguration(each.getAlgorithmSegment())));
        result.setAuditors(algorithmConfigMap);
        return result;
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final ShardingRuleConfiguration currentRuleConfig, final ShardingRuleConfiguration toBeAlteredRuleConfig) {
        currentRuleConfig.getAuditors().putAll(toBeAlteredRuleConfig.getShardingAlgorithms());
    }
    
    @Override
    public Class<ShardingRuleConfiguration> getRuleConfigurationClass() {
        return ShardingRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return AlterShardingAuditorStatement.class.getName();
    }
}
