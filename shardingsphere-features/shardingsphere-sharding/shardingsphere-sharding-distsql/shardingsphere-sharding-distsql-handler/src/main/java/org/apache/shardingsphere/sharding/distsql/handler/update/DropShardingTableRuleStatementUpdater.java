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

import com.google.common.base.Splitter;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RuleDefinitionViolationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RuleInUsedException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionDropUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingTableRuleStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Drop sharding table rule statement updater.
 */
public final class DropShardingTableRuleStatementUpdater implements RuleDefinitionDropUpdater<DropShardingTableRuleStatement, ShardingRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database,
                                  final DropShardingTableRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) throws RuleDefinitionViolationException {
        if (!isExistRuleConfig(currentRuleConfig) && sqlStatement.isIfExists()) {
            return;
        }
        String databaseName = database.getName();
        checkCurrentRuleConfiguration(databaseName, currentRuleConfig);
        checkToBeDroppedShardingTableNames(databaseName, sqlStatement, currentRuleConfig);
        checkBindingTables(databaseName, sqlStatement, currentRuleConfig);
    }
    
    private void checkCurrentRuleConfiguration(final String databaseName, final ShardingRuleConfiguration currentRuleConfig) throws RequiredRuleMissedException {
        if (null == currentRuleConfig) {
            throw new RequiredRuleMissedException("Sharding", databaseName);
        }
    }
    
    private Collection<String> getToBeDroppedShardingTableNames(final DropShardingTableRuleStatement sqlStatement) {
        return sqlStatement.getTableNames().stream().map(each -> each.getIdentifier().getValue()).collect(Collectors.toList());
    }
    
    private void checkToBeDroppedShardingTableNames(final String databaseName, final DropShardingTableRuleStatement sqlStatement,
                                                    final ShardingRuleConfiguration currentRuleConfig) throws RequiredRuleMissedException {
        if (sqlStatement.isIfExists()) {
            return;
        }
        Collection<String> currentShardingTableNames = getCurrentShardingTableNames(currentRuleConfig);
        Collection<String> notExistedTableNames =
                getToBeDroppedShardingTableNames(sqlStatement).stream().filter(each -> !containsIgnoreCase(currentShardingTableNames, each)).collect(Collectors.toList());
        if (!notExistedTableNames.isEmpty()) {
            throw new RequiredRuleMissedException("sharding", databaseName, notExistedTableNames);
        }
    }
    
    private boolean containsIgnoreCase(final Collection<String> collection, final String str) {
        return collection.stream().anyMatch(each -> each.equalsIgnoreCase(str));
    }
    
    private Collection<String> getCurrentShardingTableNames(final ShardingRuleConfiguration shardingRuleConfig) {
        Collection<String> result = new LinkedList<>();
        result.addAll(shardingRuleConfig.getTables().stream().map(ShardingTableRuleConfiguration::getLogicTable).collect(Collectors.toList()));
        result.addAll(shardingRuleConfig.getAutoTables().stream().map(ShardingAutoTableRuleConfiguration::getLogicTable).collect(Collectors.toList()));
        return result;
    }
    
    private void checkBindingTables(final String databaseName, final DropShardingTableRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) throws RuleInUsedException {
        Collection<String> bindingTables = getBindingTables(currentRuleConfig);
        Collection<String> usedTableNames = getToBeDroppedShardingTableNames(sqlStatement).stream().filter(each -> containsIgnoreCase(bindingTables, each)).collect(Collectors.toList());
        if (!usedTableNames.isEmpty()) {
            throw new RuleInUsedException("Sharding", databaseName, usedTableNames);
        }
    }
    
    private Collection<String> getBindingTables(final ShardingRuleConfiguration shardingRuleConfig) {
        Collection<String> result = new LinkedHashSet<>();
        shardingRuleConfig.getBindingTableGroups().forEach(each -> result.addAll(Splitter.on(",").splitToList(each)));
        return result;
    }
    
    @Override
    public boolean hasAnyOneToBeDropped(final DropShardingTableRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        if (null == currentRuleConfig) {
            return false;
        }
        Collection<String> currentTableNames = new LinkedList<>();
        currentTableNames.addAll(currentRuleConfig.getTables().stream().map(ShardingTableRuleConfiguration::getLogicTable).collect(Collectors.toSet()));
        currentTableNames.addAll(currentRuleConfig.getAutoTables().stream().map(ShardingAutoTableRuleConfiguration::getLogicTable).collect(Collectors.toSet()));
        return !getIdenticalData(currentTableNames, sqlStatement.getTableNames().stream().map(each -> each.getIdentifier().getValue()).collect(Collectors.toSet())).isEmpty();
    }
    
    @Override
    public boolean updateCurrentRuleConfiguration(final DropShardingTableRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        boolean dropUnusedAlgorithms = sqlStatement.isDropUnusedAlgorithms();
        Collection<String> toBeDroppedShardingTableNames = getToBeDroppedShardingTableNames(sqlStatement);
        toBeDroppedShardingTableNames.forEach(each -> dropShardingTable(currentRuleConfig, each));
        if (dropUnusedAlgorithms) {
            toBeDroppedShardingTableNames.forEach(each -> dropUnusedAlgorithms(currentRuleConfig));
        }
        return false;
    }
    
    private void dropUnusedAlgorithms(final ShardingRuleConfiguration currentRuleConfig) {
        Collection<String> inUsedAlgorithms = currentRuleConfig.getTables().stream()
                .map(each -> Arrays.asList(each.getTableShardingStrategy(), each.getDatabaseShardingStrategy()))
                .flatMap(Collection::stream).filter(Objects::nonNull).map(ShardingStrategyConfiguration::getShardingAlgorithmName).collect(Collectors.toSet());
        inUsedAlgorithms.addAll(currentRuleConfig.getAutoTables().stream().map(each -> each.getShardingStrategy().getShardingAlgorithmName()).collect(Collectors.toSet()));
        if (null != currentRuleConfig.getDefaultTableShardingStrategy()) {
            inUsedAlgorithms.add(currentRuleConfig.getDefaultTableShardingStrategy().getShardingAlgorithmName());
        }
        if (null != currentRuleConfig.getDefaultDatabaseShardingStrategy()) {
            inUsedAlgorithms.add(currentRuleConfig.getDefaultDatabaseShardingStrategy().getShardingAlgorithmName());
        }
        Collection<String> unusedAlgorithms = currentRuleConfig.getShardingAlgorithms().keySet().stream().filter(each -> !inUsedAlgorithms.contains(each)).collect(Collectors.toSet());
        unusedAlgorithms.forEach(each -> currentRuleConfig.getShardingAlgorithms().remove(each));
    }
    
    private void dropShardingTable(final ShardingRuleConfiguration currentRuleConfig, final String tableName) {
        currentRuleConfig.getTables().removeAll(currentRuleConfig.getTables().stream().filter(each -> tableName.equalsIgnoreCase(each.getLogicTable())).collect(Collectors.toList()));
        currentRuleConfig.getAutoTables().removeAll(currentRuleConfig.getAutoTables().stream().filter(each -> tableName.equalsIgnoreCase(each.getLogicTable())).collect(Collectors.toList()));
    }
    
    @Override
    public Class<ShardingRuleConfiguration> getRuleConfigurationClass() {
        return ShardingRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return DropShardingTableRuleStatement.class.getName();
    }
}
