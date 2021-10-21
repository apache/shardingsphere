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

import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionCreateUpdater;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.converter.ShardingRuleStatementConverter;
import org.apache.shardingsphere.sharding.distsql.parser.segment.ShardingAlgorithmSegment;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingAlgorithmStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Create sharding algorithm statement updater.
 */
public final class CreateShardingAlgorithmStatementUpdater implements RuleDefinitionCreateUpdater<CreateShardingAlgorithmStatement, ShardingRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereMetaData shardingSphereMetaData, final CreateShardingAlgorithmStatement sqlStatement,
                                  final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        String schemaName = shardingSphereMetaData.getName();
        checkDuplicate(schemaName, sqlStatement, currentRuleConfig);
    }
    
    private void checkDuplicate(final String schemaName, final CreateShardingAlgorithmStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        LinkedList<String> shardingAlgorithmNames = sqlStatement.getAlgorithmSegments().stream()
                .map(ShardingAlgorithmSegment::getShardingAlgorithmName).collect(Collectors.toCollection(LinkedList::new));
        checkDuplicate(shardingAlgorithmNames, duplicated -> new DuplicateRuleException("SHARDING", schemaName, duplicated));
        checkExist(shardingAlgorithmNames, currentRuleConfig.getShardingAlgorithms().keySet(), duplicated -> new DuplicateRuleException("SHARDING", schemaName, duplicated));
    }
    
    private static void checkDuplicate(final Collection<String> rules, final Function<Set<String>, DistSQLException> thrower) throws DistSQLException {
        Set<String> duplicateRequire = rules.stream().collect(Collectors.groupingBy(each -> each, Collectors.counting())).entrySet().stream()
                .filter(each -> each.getValue() > 1).map(Map.Entry::getKey).collect(Collectors.toSet());
        DistSQLException.predictionThrow(duplicateRequire.isEmpty(), thrower.apply(duplicateRequire));
    }
    
    private static void checkExist(final Collection<String> requireRules, final Collection<String> currentRules, final Function<Set<String>, DistSQLException> thrower) throws DistSQLException {
        Set<String> identical = requireRules.stream().filter(currentRules::contains).collect(Collectors.toSet());
        DistSQLException.predictionThrow(identical.isEmpty(), thrower.apply(identical));
    }
    
    @Override
    public ShardingRuleConfiguration buildToBeCreatedRuleConfiguration(final CreateShardingAlgorithmStatement sqlStatement) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        Map<String, ShardingSphereAlgorithmConfiguration> algorithmConfigurationMap = sqlStatement.getAlgorithmSegments().stream()
                .collect(Collectors.toMap(ShardingAlgorithmSegment::getShardingAlgorithmName, each -> ShardingRuleStatementConverter.createAlgorithmConfiguration(each.getAlgorithmSegment())));
        result.setShardingAlgorithms(algorithmConfigurationMap);
        return result;
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final ShardingRuleConfiguration currentRuleConfig, final ShardingRuleConfiguration toBeCreatedRuleConfig) {
        if (null != currentRuleConfig) {
            currentRuleConfig.getShardingAlgorithms().putAll(toBeCreatedRuleConfig.getShardingAlgorithms());
        }
    }
    
    @Override
    public Class<ShardingRuleConfiguration> getRuleConfigurationClass() {
        return ShardingRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return CreateShardingAlgorithmStatement.class.getCanonicalName();
    }
}
