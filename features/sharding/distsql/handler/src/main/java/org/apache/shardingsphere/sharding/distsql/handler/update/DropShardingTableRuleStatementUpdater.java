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
import org.apache.shardingsphere.distsql.handler.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.distsql.handler.exception.rule.RuleInUsedException;
import org.apache.shardingsphere.distsql.handler.update.RuleDefinitionDropUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingTableRuleStatement;

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
    public void checkSQLStatement(final ShardingSphereDatabase database, final DropShardingTableRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        if (!isExistRuleConfig(currentRuleConfig) && sqlStatement.isIfExists()) {
            return;
        }
        String databaseName = database.getName();
        checkCurrentRuleConfiguration(databaseName, currentRuleConfig);
        checkToBeDroppedShardingTableNames(databaseName, sqlStatement, currentRuleConfig);
        checkBindingTables(databaseName, sqlStatement, currentRuleConfig);
    }
    
    private void checkCurrentRuleConfiguration(final String databaseName, final ShardingRuleConfiguration currentRuleConfig) {
        ShardingSpherePreconditions.checkNotNull(currentRuleConfig, () -> new MissingRequiredRuleException("Sharding", databaseName));
    }
    
    private Collection<String> getToBeDroppedShardingTableNames(final DropShardingTableRuleStatement sqlStatement) {
        return sqlStatement.getTableNames().stream().map(each -> each.getIdentifier().getValue()).collect(Collectors.toList());
    }
    
    private void checkToBeDroppedShardingTableNames(final String databaseName, final DropShardingTableRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        if (sqlStatement.isIfExists()) {
            return;
        }
        Collection<String> currentShardingTableNames = getCurrentShardingTableNames(currentRuleConfig);
        Collection<String> notExistedTableNames =
                getToBeDroppedShardingTableNames(sqlStatement).stream().filter(each -> !containsIgnoreCase(currentShardingTableNames, each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(notExistedTableNames.isEmpty(), () -> new MissingRequiredRuleException("sharding", databaseName, notExistedTableNames));
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
    
    private void checkBindingTables(final String databaseName, final DropShardingTableRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        Collection<String> bindingTables = getBindingTables(currentRuleConfig);
        Collection<String> usedTableNames = getToBeDroppedShardingTableNames(sqlStatement).stream().filter(each -> containsIgnoreCase(bindingTables, each)).collect(Collectors.toList());
        if (!usedTableNames.isEmpty()) {
            throw new RuleInUsedException("Sharding", databaseName, usedTableNames, "sharding table reference");
        }
    }
    
    private Collection<String> getBindingTables(final ShardingRuleConfiguration shardingRuleConfig) {
        Collection<String> result = new LinkedHashSet<>();
        shardingRuleConfig.getBindingTableGroups().forEach(each -> result.addAll(Splitter.on(",").splitToList(each.getReference())));
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
    public ShardingRuleConfiguration buildToBeDroppedRuleConfiguration(final ShardingRuleConfiguration currentRuleConfig, final DropShardingTableRuleStatement sqlStatement) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        Collection<String> toBeDroppedShardingTableNames = getToBeDroppedShardingTableNames(sqlStatement);
        for (String each : toBeDroppedShardingTableNames) {
            result.getTables().addAll(currentRuleConfig.getTables().stream().filter(table -> each.equalsIgnoreCase(table.getLogicTable())).collect(Collectors.toList()));
            result.getAutoTables().addAll(currentRuleConfig.getAutoTables().stream().filter(table -> each.equalsIgnoreCase(table.getLogicTable())).collect(Collectors.toList()));
            dropShardingTable(currentRuleConfig, each);
        }
        UnusedAlgorithmFinder.find(currentRuleConfig).forEach(each -> result.getShardingAlgorithms().put(each, currentRuleConfig.getShardingAlgorithms().get(each)));
        findUnusedKeyGenerator(currentRuleConfig).forEach(each -> result.getKeyGenerators().put(each, currentRuleConfig.getKeyGenerators().get(each)));
        findUnusedAuditors(currentRuleConfig).forEach(each -> result.getAuditors().put(each, currentRuleConfig.getAuditors().get(each)));
        return result;
    }
    
    @Override
    public boolean updateCurrentRuleConfiguration(final DropShardingTableRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        Collection<String> toBeDroppedShardingTableNames = getToBeDroppedShardingTableNames(sqlStatement);
        toBeDroppedShardingTableNames.forEach(each -> dropShardingTable(currentRuleConfig, each));
        UnusedAlgorithmFinder.find(currentRuleConfig).forEach(each -> currentRuleConfig.getShardingAlgorithms().remove(each));
        dropUnusedKeyGenerator(currentRuleConfig);
        dropUnusedAuditor(currentRuleConfig);
        return currentRuleConfig.isEmpty();
    }
    
    private void dropShardingTable(final ShardingRuleConfiguration currentRuleConfig, final String tableName) {
        currentRuleConfig.getTables().removeAll(currentRuleConfig.getTables().stream().filter(each -> tableName.equalsIgnoreCase(each.getLogicTable())).collect(Collectors.toList()));
        currentRuleConfig.getAutoTables().removeAll(currentRuleConfig.getAutoTables().stream().filter(each -> tableName.equalsIgnoreCase(each.getLogicTable())).collect(Collectors.toList()));
    }
    
    private void dropUnusedKeyGenerator(final ShardingRuleConfiguration currentRuleConfig) {
        findUnusedKeyGenerator(currentRuleConfig).forEach(each -> currentRuleConfig.getKeyGenerators().remove(each));
    }
    
    private static Collection<String> findUnusedKeyGenerator(final ShardingRuleConfiguration currentRuleConfig) {
        Collection<String> inUsedKeyGenerators = currentRuleConfig.getTables().stream().map(ShardingTableRuleConfiguration::getKeyGenerateStrategy).filter(Objects::nonNull)
                .map(KeyGenerateStrategyConfiguration::getKeyGeneratorName).collect(Collectors.toSet());
        inUsedKeyGenerators.addAll(currentRuleConfig.getAutoTables().stream().map(ShardingAutoTableRuleConfiguration::getKeyGenerateStrategy).filter(Objects::nonNull)
                .map(KeyGenerateStrategyConfiguration::getKeyGeneratorName).collect(Collectors.toSet()));
        if (null != currentRuleConfig.getDefaultKeyGenerateStrategy()) {
            inUsedKeyGenerators.add(currentRuleConfig.getDefaultKeyGenerateStrategy().getKeyGeneratorName());
        }
        return currentRuleConfig.getKeyGenerators().keySet().stream().filter(each -> !inUsedKeyGenerators.contains(each)).collect(Collectors.toSet());
    }
    
    private void dropUnusedAuditor(final ShardingRuleConfiguration currentRuleConfig) {
        findUnusedAuditors(currentRuleConfig).forEach(each -> currentRuleConfig.getAuditors().remove(each));
    }
    
    private static Collection<String> findUnusedAuditors(final ShardingRuleConfiguration currentRuleConfig) {
        Collection<String> inUsedAuditors = currentRuleConfig.getTables().stream().map(ShardingTableRuleConfiguration::getAuditStrategy).filter(Objects::nonNull)
                .flatMap(each -> each.getAuditorNames().stream()).collect(Collectors.toSet());
        inUsedAuditors.addAll(currentRuleConfig.getAutoTables().stream().map(ShardingAutoTableRuleConfiguration::getAuditStrategy).filter(Objects::nonNull)
                .flatMap(each -> each.getAuditorNames().stream()).collect(Collectors.toSet()));
        if (null != currentRuleConfig.getDefaultAuditStrategy()) {
            inUsedAuditors.addAll(currentRuleConfig.getDefaultAuditStrategy().getAuditorNames());
        }
        return currentRuleConfig.getAuditors().keySet().stream().filter(each -> !inUsedAuditors.contains(each)).collect(Collectors.toSet());
    }
    
    private boolean isEmptyShardingTables(final ShardingRuleConfiguration currentRuleConfig) {
        return currentRuleConfig.getTables().isEmpty() && currentRuleConfig.getAutoTables().isEmpty();
    }
    
    private boolean isEmptyShardingStrategy(final ShardingRuleConfiguration currentRuleConfig) {
        return null == currentRuleConfig.getDefaultDatabaseShardingStrategy() && null == currentRuleConfig.getDefaultTableShardingStrategy();
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
