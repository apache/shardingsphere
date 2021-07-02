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

package org.apache.shardingsphere.sharding.distsql.update;

import org.apache.shardingsphere.infra.distsql.update.RDLAlterUpdater;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.converter.ShardingRuleStatementConverter;
import org.apache.shardingsphere.sharding.distsql.exception.DuplicateBindingTablesException;
import org.apache.shardingsphere.sharding.distsql.exception.ShardingBindingTableRuleNotExistsException;
import org.apache.shardingsphere.sharding.distsql.exception.ShardingTableRuleNotExistedException;
import org.apache.shardingsphere.sharding.distsql.parser.statement.AlterShardingBindingTableRulesStatement;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * Alter sharding binding table rule statement updater.
 */
public final class AlterShardingBindingTableRuleStatementUpdater implements RDLAlterUpdater<AlterShardingBindingTableRulesStatement, ShardingRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final String schemaName, final AlterShardingBindingTableRulesStatement sqlStatement, 
                                  final ShardingRuleConfiguration currentRuleConfig, final ShardingSphereResource resource) {
        checkCurrentRuleConfiguration(schemaName, currentRuleConfig);
        checkToBeAlertedBindingTables(schemaName, sqlStatement, currentRuleConfig);
        checkToBeAlteredDuplicateBindingTables(sqlStatement);
    }
    
    private void checkCurrentRuleConfiguration(final String schemaName, final ShardingRuleConfiguration currentRuleConfig) {
        if (null == currentRuleConfig) {
            throw new ShardingBindingTableRuleNotExistsException(schemaName);
        }
    }
    
    private void checkToBeAlertedBindingTables(final String schemaName, final AlterShardingBindingTableRulesStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        Collection<String> currentLogicTables = getCurrentLogicTables(currentRuleConfig);
        Collection<String> notExistedBindingTables = sqlStatement.getBindingTables().stream().filter(each -> !currentLogicTables.contains(each)).collect(Collectors.toSet());
        if (!notExistedBindingTables.isEmpty()) {
            throw new ShardingTableRuleNotExistedException(schemaName, notExistedBindingTables);
        }
    }
    
    private Collection<String> getCurrentLogicTables(final ShardingRuleConfiguration currentRuleConfig) {
        Collection<String> result = new HashSet<>();
        result.addAll(currentRuleConfig.getTables().stream().map(ShardingTableRuleConfiguration::getLogicTable).collect(Collectors.toSet()));
        result.addAll(currentRuleConfig.getAutoTables().stream().map(ShardingAutoTableRuleConfiguration::getLogicTable).collect(Collectors.toSet()));
        return result;
    }
    
    private void checkToBeAlteredDuplicateBindingTables(final AlterShardingBindingTableRulesStatement sqlStatement) {
        Collection<String> toBeAlteredBindingTables = new HashSet<>();
        Collection<String> duplicateBindingTables = sqlStatement.getBindingTables().stream().filter(each -> !toBeAlteredBindingTables.add(each)).collect(Collectors.toSet());
        if (!duplicateBindingTables.isEmpty()) {
            throw new DuplicateBindingTablesException(duplicateBindingTables);
        }
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
