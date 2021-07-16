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

import org.apache.shardingsphere.infra.distsql.exception.rule.RuleDefinitionViolationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredAlgorithmMissedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.AlgorithmInUsedException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionDropUpdater;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingAlgorithmStatement;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Drop sharding algorithm statement updater.
 */
public final class DropShardingAlgorithmStatementUpdater implements RuleDefinitionDropUpdater<DropShardingAlgorithmStatement, ShardingRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final String schemaName, final DropShardingAlgorithmStatement sqlStatement,
                                  final ShardingRuleConfiguration currentRuleConfig, final ShardingSphereResource resource) throws RuleDefinitionViolationException {
        checkCurrentRuleConfiguration(schemaName, currentRuleConfig);
        checkToBeDroppedShardingAlgorithms(schemaName, sqlStatement, currentRuleConfig);
        checkShardingAlgorithmsInUsed(schemaName, sqlStatement, currentRuleConfig);
    }
    
    private void checkCurrentRuleConfiguration(final String schemaName, final ShardingRuleConfiguration currentRuleConfig) throws RequiredAlgorithmMissedException {
        if (null == currentRuleConfig) {
            throw new RequiredAlgorithmMissedException(schemaName);
        }
    }
    
    private void checkToBeDroppedShardingAlgorithms(final String schemaName, final DropShardingAlgorithmStatement sqlStatement,
                                                    final ShardingRuleConfiguration currentRuleConfig) throws RequiredAlgorithmMissedException {
        Collection<String> currentShardingAlgorithms = getCurrentShardingAlgorithms(currentRuleConfig);
        Collection<String> notExistedAlgorithms = sqlStatement.getAlgorithmNames().stream().filter(each -> !currentShardingAlgorithms.contains(each)).collect(Collectors.toList());
        if (!notExistedAlgorithms.isEmpty()) {
            throw new RequiredAlgorithmMissedException(schemaName, notExistedAlgorithms);
        }
    }
    
    private void checkShardingAlgorithmsInUsed(final String schemaName, final DropShardingAlgorithmStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig)
            throws AlgorithmInUsedException {
        Collection<String> allInUsed = getAllOfAlgorithmsInUsed(currentRuleConfig);
        Collection<String> usedAlgorithms = sqlStatement.getAlgorithmNames().stream().filter(allInUsed::contains).collect(Collectors.toList());
        if (!usedAlgorithms.isEmpty()) {
            throw new AlgorithmInUsedException(schemaName, usedAlgorithms);
        }
    }
    
    private Collection<String> getAllOfAlgorithmsInUsed(final ShardingRuleConfiguration shardingRuleConfig) {
        Collection<String> result = new LinkedHashSet<>();
        shardingRuleConfig.getTables().stream().forEach(each -> {
            if (Objects.nonNull(each.getDatabaseShardingStrategy())) {
                result.add(each.getDatabaseShardingStrategy().getShardingAlgorithmName());
            }
            if (Objects.nonNull(each.getTableShardingStrategy())) {
                result.add(each.getTableShardingStrategy().getShardingAlgorithmName());
            }
        });
        shardingRuleConfig.getAutoTables().stream().filter(each -> Objects.nonNull(each.getShardingStrategy()))
                .forEach(each -> result.add(each.getShardingStrategy().getShardingAlgorithmName()));
        return result;
    }
    
    private Collection<String> getCurrentShardingAlgorithms(final ShardingRuleConfiguration shardingRuleConfig) {
        return shardingRuleConfig.getShardingAlgorithms().keySet();
    }
    
    @Override
    public boolean updateCurrentRuleConfiguration(final DropShardingAlgorithmStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        for (String each : sqlStatement.getAlgorithmNames()) {
            dropShardingAlgorithm(currentRuleConfig, each);
        }
        return false;
    }
    
    private void dropShardingAlgorithm(final ShardingRuleConfiguration currentRuleConfig, final String algorithmName) {
        getCurrentShardingAlgorithms(currentRuleConfig).removeIf(key -> algorithmName.equalsIgnoreCase(key));
    }
    
    @Override
    public Class<ShardingRuleConfiguration> getRuleConfigurationClass() {
        return ShardingRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return DropShardingAlgorithmStatement.class.getCanonicalName();
    }
}
