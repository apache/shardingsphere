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
import org.apache.shardingsphere.proxy.backend.exception.ShardingTableRuleNotExistedException;
import org.apache.shardingsphere.proxy.backend.exception.ShardingTableRulesInUsedException;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingTableRuleStatement;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Drop sharding table rule statement updater.
 */
public final class DropShardingTableRuleStatementUpdater implements RDLUpdater<DropShardingTableRuleStatement, ShardingRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final String schemaName, final DropShardingTableRuleStatement sqlStatement, 
                                  final ShardingRuleConfiguration currentRuleConfig, final ShardingSphereResource resource) {
        checkCurrentRuleConfiguration(schemaName, sqlStatement, currentRuleConfig);
        checkToBeDroppedShardingTableNames(schemaName, sqlStatement, currentRuleConfig);
        checkBindingTables(sqlStatement, currentRuleConfig);
    }
    
    private void checkCurrentRuleConfiguration(final String schemaName, final DropShardingTableRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        if (null == currentRuleConfig) {
            throw new ShardingTableRuleNotExistedException(schemaName, getToBeDroppedShardingTableNames(sqlStatement));
        }
    }
    
    private Collection<String> getToBeDroppedShardingTableNames(final DropShardingTableRuleStatement sqlStatement) {
        return sqlStatement.getTableNames().stream().map(each -> each.getIdentifier().getValue()).collect(Collectors.toList());
    }
    
    private void checkToBeDroppedShardingTableNames(final String schemaName, final DropShardingTableRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        Collection<String> currentShardingTableNames = getCurrentShardingTableNames(currentRuleConfig);
        Collection<String> notExistedTableNames = getToBeDroppedShardingTableNames(sqlStatement).stream().filter(each -> !currentShardingTableNames.contains(each)).collect(Collectors.toList());
        if (!notExistedTableNames.isEmpty()) {
            throw new ShardingTableRuleNotExistedException(schemaName, notExistedTableNames);
        }
    }
    
    private Collection<String> getCurrentShardingTableNames(final ShardingRuleConfiguration shardingRuleConfig) {
        Collection<String> result = new LinkedList<>();
        result.addAll(shardingRuleConfig.getTables().stream().map(ShardingTableRuleConfiguration::getLogicTable).collect(Collectors.toList()));
        result.addAll(shardingRuleConfig.getAutoTables().stream().map(ShardingAutoTableRuleConfiguration::getLogicTable).collect(Collectors.toList()));
        return result;
    }
    
    private void checkBindingTables(final DropShardingTableRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        Collection<String> bindingTables = getBindingTables(currentRuleConfig);
        Collection<String> usedTableNames = getToBeDroppedShardingTableNames(sqlStatement).stream().filter(bindingTables::contains).collect(Collectors.toList());
        if (!usedTableNames.isEmpty()) {
            throw new ShardingTableRulesInUsedException(usedTableNames);
        }
    }
    
    private Collection<String> getBindingTables(final ShardingRuleConfiguration shardingRuleConfig) {
        Collection<String> result = new LinkedHashSet<>();
        shardingRuleConfig.getBindingTableGroups().forEach(each -> result.addAll(Splitter.on(",").splitToList(each)));
        return result;
    }
    
    @Override
    public boolean updateCurrentRuleConfiguration(final String schemaName, final DropShardingTableRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        for (String each : getToBeDroppedShardingTableNames(sqlStatement)) {
            dropShardingTable(currentRuleConfig, each);
        }
        return false;
    }
    
    private void dropShardingTable(final ShardingRuleConfiguration currentRuleConfig, final String tableName) {
        currentRuleConfig.getTables().removeAll(currentRuleConfig.getTables().stream().filter(each -> tableName.equalsIgnoreCase(each.getLogicTable())).collect(Collectors.toList()));
        currentRuleConfig.getAutoTables().removeAll(currentRuleConfig.getAutoTables().stream().filter(each -> tableName.equalsIgnoreCase(each.getLogicTable())).collect(Collectors.toList()));
    }
    
    @Override
    public String getType() {
        return DropShardingTableRuleStatement.class.getCanonicalName();
    }
}
