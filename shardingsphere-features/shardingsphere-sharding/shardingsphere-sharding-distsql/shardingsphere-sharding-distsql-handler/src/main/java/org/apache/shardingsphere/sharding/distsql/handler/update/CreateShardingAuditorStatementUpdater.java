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
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateAuditorException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionCreateUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.converter.ShardingTableRuleStatementConverter;
import org.apache.shardingsphere.sharding.distsql.parser.segment.ShardingAuditorSegment;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingAuditorStatement;
import org.apache.shardingsphere.sharding.factory.ShardingAuditAlgorithmFactory;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Create sharding auditor statement updater.
 */
public final class CreateShardingAuditorStatementUpdater implements RuleDefinitionCreateUpdater<CreateShardingAuditorStatement, ShardingRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database, final CreateShardingAuditorStatement sqlStatement,
                                  final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        checkDuplicate(database.getName(), sqlStatement, currentRuleConfig);
        checkAuditorAlgorithm(sqlStatement);
    }
    
    private void checkDuplicate(final String databaseName, final CreateShardingAuditorStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        Collection<String> auditorNames = sqlStatement.getAuditorSegments().stream().map(ShardingAuditorSegment::getAuditorName).collect(Collectors.toList());
        checkDuplicateInput(auditorNames, duplicated -> new DuplicateAuditorException("sharding", databaseName, duplicated));
        if (null != currentRuleConfig) {
            checkExist(auditorNames, currentRuleConfig.getAuditors().keySet(), duplicated -> new DuplicateAuditorException("sharding", databaseName, duplicated));
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
    
    private void checkAuditorAlgorithm(final CreateShardingAuditorStatement sqlStatement) throws DistSQLException {
        Collection<String> notExistedAuditorAlgorithms = sqlStatement.getAuditorSegments().stream().map(ShardingAuditorSegment::getAlgorithmSegment).map(AlgorithmSegment::getName)
                .filter(each -> !ShardingAuditAlgorithmFactory.contains(each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(notExistedAuditorAlgorithms.isEmpty(), () -> new InvalidAlgorithmConfigurationException("sharding", notExistedAuditorAlgorithms));
    }
    
    @Override
    public ShardingRuleConfiguration buildToBeCreatedRuleConfiguration(final CreateShardingAuditorStatement sqlStatement) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        Map<String, AlgorithmConfiguration> auditorConfigurationMap = sqlStatement.getAuditorSegments().stream()
                .collect(Collectors.toMap(ShardingAuditorSegment::getAuditorName, each -> ShardingTableRuleStatementConverter.createAlgorithmConfiguration(each.getAlgorithmSegment())));
        result.setAuditors(auditorConfigurationMap);
        return result;
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final ShardingRuleConfiguration currentRuleConfig, final ShardingRuleConfiguration toBeCreatedRuleConfig) {
        if (null != currentRuleConfig) {
            currentRuleConfig.getAuditors().putAll(toBeCreatedRuleConfig.getAuditors());
        }
    }
    
    @Override
    public Class<ShardingRuleConfiguration> getRuleConfigurationClass() {
        return ShardingRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return CreateShardingAuditorStatement.class.getName();
    }
}
