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

package org.apache.shardingsphere.proxy.backend.text.distsql.rdl.impl;

import com.google.common.base.Splitter;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
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
 * Drop sharding table rule backend handler.
 */
public final class DropShardingTableRuleBackendHandler extends RDLBackendHandler<DropShardingTableRuleStatement, ShardingRuleConfiguration> {
    
    public DropShardingTableRuleBackendHandler(final DropShardingTableRuleStatement sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
    }
    
    @Override
    public void check(final String schemaName, final DropShardingTableRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        Collection<String> tableNames = sqlStatement.getTableNames().stream().map(each -> each.getIdentifier().getValue()).collect(Collectors.toList());
        if (null == currentRuleConfig) {
            throw new ShardingTableRuleNotExistedException(schemaName, tableNames);
        }
        Collection<String> shardingTableNames = getShardingTables(currentRuleConfig);
        Collection<String> notExistedTableNames = tableNames.stream().filter(each -> !shardingTableNames.contains(each)).collect(Collectors.toList());
        if (!notExistedTableNames.isEmpty()) {
            throw new ShardingTableRuleNotExistedException(schemaName, notExistedTableNames);
        }
        Collection<String> bindingTables = getBindingTables(currentRuleConfig);
        Collection<String> usedTableNames = tableNames.stream().filter(bindingTables::contains).collect(Collectors.toList());
        if (!usedTableNames.isEmpty()) {
            throw new ShardingTableRulesInUsedException(usedTableNames);
        }
    }
    
    @Override
    public void doExecute(final String schemaName, final DropShardingTableRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        for (String each : getDroppedTables(sqlStatement)) {
            dropShardingTable(currentRuleConfig, each);
        }
    }
    
    private Collection<String> getDroppedTables(final DropShardingTableRuleStatement sqlStatement) {
        return sqlStatement.getTableNames().stream().map(each -> each.getIdentifier().getValue()).collect(Collectors.toList());
    }
    
    private Collection<String> getShardingTables(final ShardingRuleConfiguration shardingRuleConfig) {
        Collection<String> result = new LinkedList<>();
        result.addAll(shardingRuleConfig.getTables().stream().map(ShardingTableRuleConfiguration::getLogicTable).collect(Collectors.toList()));
        result.addAll(shardingRuleConfig.getAutoTables().stream().map(ShardingAutoTableRuleConfiguration::getLogicTable).collect(Collectors.toList()));
        return result;
    }
    
    private void dropShardingTable(final ShardingRuleConfiguration currentRuleConfig, final String tableName) {
        currentRuleConfig.getTables().removeAll(currentRuleConfig.getTables().stream().filter(each -> tableName.equalsIgnoreCase(each.getLogicTable())).collect(Collectors.toList()));
        currentRuleConfig.getAutoTables().removeAll(currentRuleConfig.getAutoTables().stream().filter(each -> tableName.equalsIgnoreCase(each.getLogicTable())).collect(Collectors.toList()));
    }
    
    private Collection<String> getBindingTables(final ShardingRuleConfiguration shardingRuleConfig) {
        Collection<String> result = new LinkedHashSet<>();
        shardingRuleConfig.getBindingTableGroups().forEach(each -> result.addAll(Splitter.on(",").splitToList(each)));
        return result;
    }
}
