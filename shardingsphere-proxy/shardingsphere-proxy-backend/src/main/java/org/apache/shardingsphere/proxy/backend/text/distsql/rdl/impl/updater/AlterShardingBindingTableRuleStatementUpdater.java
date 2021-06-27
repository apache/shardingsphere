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

package org.apache.shardingsphere.proxy.backend.text.distsql.rdl.impl.updater;

import com.google.common.base.Splitter;
import org.apache.shardingsphere.infra.distsql.RDLUpdater;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.proxy.backend.exception.DuplicateBindingTablesException;
import org.apache.shardingsphere.proxy.backend.exception.ShardingBindingTableRuleNotExistsException;
import org.apache.shardingsphere.proxy.backend.exception.ShardingTableRuleNotExistedException;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.converter.ShardingRuleStatementConverter;
import org.apache.shardingsphere.sharding.distsql.parser.segment.BindingTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.statement.AlterShardingBindingTableRulesStatement;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Alter sharding binding table rule statement updater.
 */
public final class AlterShardingBindingTableRuleStatementUpdater implements RDLUpdater<AlterShardingBindingTableRulesStatement, ShardingRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final String schemaName, final AlterShardingBindingTableRulesStatement sqlStatement, 
                                  final ShardingRuleConfiguration currentRuleConfig, final ShardingSphereResource resource) {
        checkCurrentRuleConfiguration(schemaName, currentRuleConfig);
        checkToBeAlertedBindingTables(schemaName, sqlStatement, currentRuleConfig);
    }
    
    private void checkCurrentRuleConfiguration(final String schemaName, final ShardingRuleConfiguration currentRuleConfig) {
        if (null == currentRuleConfig) {
            throw new ShardingBindingTableRuleNotExistsException(schemaName);
        }
    }
    
    private void checkToBeAlertedBindingTables(final String schemaName, final AlterShardingBindingTableRulesStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        Collection<String> toBeAlertedBindingTableGroups = sqlStatement.getRules().stream().map(BindingTableRuleSegment::getTables).collect(Collectors.toList());
        Collection<String> currentLogicTables = getCurrentLogicTables(currentRuleConfig);
        Collection<String> notExistedBindingTables = new HashSet<>();
        for (String bindingTableGroup : toBeAlertedBindingTableGroups) {
            for (String bindingTable : bindingTableGroup.split(",")) {
                if (!currentLogicTables.contains(bindingTable.trim())) {
                    notExistedBindingTables.add(bindingTable);
                }
            }
        }
        if (!notExistedBindingTables.isEmpty()) {
            throw new ShardingTableRuleNotExistedException(schemaName, notExistedBindingTables);
        }
        // TODO need to clearify what is duplicateBindingTables
        Collection<String> duplicateBindingTables = toBeAlertedBindingTableGroups.stream().filter(distinct()).collect(Collectors.toList());
        if (!duplicateBindingTables.isEmpty()) {
            throw new DuplicateBindingTablesException(duplicateBindingTables);
        }
    }
    
    private Predicate<String> distinct() {
        Collection<String> tables = new HashSet<>();
        return table -> notEquals(table, tables);
    }
    
    // TODO need to clearify the business logic
    private boolean notEquals(final String table, final Collection<String> tables) {
        for (String each : tables) {
            if (table.equals(each) || (table.length() == each.length() && Splitter.on(",").splitToList(each).containsAll(Splitter.on(",").splitToList(table)))) {
                return true;
            }
        }
        tables.add(table);
        return false;
    }
    
    private Collection<String> getCurrentLogicTables(final ShardingRuleConfiguration currentRuleConfig) {
        Collection<String> result = new HashSet<>();
        result.addAll(currentRuleConfig.getTables().stream().map(ShardingTableRuleConfiguration::getLogicTable).collect(Collectors.toSet()));
        result.addAll(currentRuleConfig.getAutoTables().stream().map(ShardingAutoTableRuleConfiguration::getLogicTable).collect(Collectors.toSet()));
        return result;
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final String schemaName, final AlterShardingBindingTableRulesStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        dropRuleConfiguration(currentRuleConfig);
        addRuleConfiguration(sqlStatement, currentRuleConfig);
    }
    
    private void dropRuleConfiguration(final ShardingRuleConfiguration currentRuleConfig) {
        currentRuleConfig.getBindingTableGroups().clear();
    }
    
    private void addRuleConfiguration(final AlterShardingBindingTableRulesStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        currentRuleConfig.getBindingTableGroups().addAll(ShardingRuleStatementConverter.convert(sqlStatement).getBindingTables());
    }
    
    @Override
    public String getType() {
        return AlterShardingBindingTableRulesStatement.class.getCanonicalName();
    }
}
