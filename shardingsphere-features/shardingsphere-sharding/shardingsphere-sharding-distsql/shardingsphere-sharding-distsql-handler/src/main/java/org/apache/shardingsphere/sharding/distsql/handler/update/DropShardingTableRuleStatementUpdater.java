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
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingTableRuleStatement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Drop sharding table rule statement updater.
 */
public final class DropShardingTableRuleStatementUpdater implements RuleDefinitionDropUpdater<DropShardingTableRuleStatement, ShardingRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereMetaData shardingSphereMetaData, final DropShardingTableRuleStatement sqlStatement, 
                                  final ShardingRuleConfiguration currentRuleConfig) throws RuleDefinitionViolationException {
        String schemaName = shardingSphereMetaData.getName();
        if (!isExistRuleConfig(currentRuleConfig) && sqlStatement.isContainsExistClause()) {
            return;
        }
        checkCurrentRuleConfiguration(schemaName, currentRuleConfig);
        checkToBeDroppedShardingTableNames(schemaName, sqlStatement, currentRuleConfig);
        checkBindingTables(schemaName, sqlStatement, currentRuleConfig);
    }
    
    private void checkCurrentRuleConfiguration(final String schemaName, final ShardingRuleConfiguration currentRuleConfig) throws RequiredRuleMissedException {
        if (null == currentRuleConfig) {
            throw new RequiredRuleMissedException("Sharding", schemaName);
        }
    }
    
    private Collection<String> getToBeDroppedShardingTableNames(final DropShardingTableRuleStatement sqlStatement) {
        return sqlStatement.getTableNames().stream().map(each -> each.getIdentifier().getValue()).collect(Collectors.toList());
    }
    
    private void checkToBeDroppedShardingTableNames(final String schemaName, final DropShardingTableRuleStatement sqlStatement, 
                                                    final ShardingRuleConfiguration currentRuleConfig) throws RequiredRuleMissedException {
        if (sqlStatement.isContainsExistClause()) {
            return;
        }
        Collection<String> currentShardingTableNames = getCurrentShardingTableNames(currentRuleConfig);
        Collection<String> notExistedTableNames = getToBeDroppedShardingTableNames(sqlStatement).stream().filter(each -> !currentShardingTableNames.contains(each)).collect(Collectors.toList());
        if (!notExistedTableNames.isEmpty()) {
            throw new RequiredRuleMissedException("sharding", schemaName, notExistedTableNames);
        }
    }
    
    private Collection<String> getCurrentShardingTableNames(final ShardingRuleConfiguration shardingRuleConfig) {
        Collection<String> result = new LinkedList<>();
        result.addAll(shardingRuleConfig.getTables().stream().map(ShardingTableRuleConfiguration::getLogicTable).collect(Collectors.toList()));
        result.addAll(shardingRuleConfig.getAutoTables().stream().map(ShardingAutoTableRuleConfiguration::getLogicTable).collect(Collectors.toList()));
        return result;
    }
    
    private void checkBindingTables(final String schemaName, final DropShardingTableRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) throws RuleInUsedException {
        Collection<String> bindingTables = getBindingTables(currentRuleConfig);
        Collection<String> usedTableNames = getToBeDroppedShardingTableNames(sqlStatement).stream().filter(bindingTables::contains).collect(Collectors.toList());
        if (!usedTableNames.isEmpty()) {
            throw new RuleInUsedException("Sharding", schemaName, usedTableNames);
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
        Collection<String> currentTableNames = new ArrayList<>(currentRuleConfig.getTables().size() + currentRuleConfig.getAutoTables().size());
        currentTableNames.addAll(currentRuleConfig.getTables().stream().map(ShardingTableRuleConfiguration::getLogicTable).collect(Collectors.toSet()));
        currentTableNames.addAll(currentRuleConfig.getAutoTables().stream().map(ShardingAutoTableRuleConfiguration::getLogicTable).collect(Collectors.toSet()));
        return !getIdenticalData(currentTableNames,
                sqlStatement.getTableNames().stream().map(each -> each.getIdentifier().getValue()).collect(Collectors.toSet())).isEmpty();
    }
    
    @Override
    public boolean updateCurrentRuleConfiguration(final DropShardingTableRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        for (String each : getToBeDroppedShardingTableNames(sqlStatement)) {
            dropShardingTable(currentRuleConfig, each);
        }
        return false;
    }
    
    private void dropShardingTable(final ShardingRuleConfiguration currentRuleConfig, final String tableName) {
        currentRuleConfig.getTables().removeAll(currentRuleConfig.getTables().stream().filter(each -> tableName.equals(each.getLogicTable())).collect(Collectors.toList()));
        currentRuleConfig.getAutoTables().removeAll(currentRuleConfig.getAutoTables().stream().filter(each -> tableName.equals(each.getLogicTable())).collect(Collectors.toList()));
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
