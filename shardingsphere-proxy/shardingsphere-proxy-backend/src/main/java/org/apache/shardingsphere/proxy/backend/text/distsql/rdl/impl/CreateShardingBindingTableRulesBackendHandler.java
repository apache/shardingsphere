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

import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.CreateShardingBindingTableRulesStatement;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.exception.DuplicateBindingTablesException;
import org.apache.shardingsphere.proxy.backend.exception.ShardingBindingTableRulesNotExistsException;
import org.apache.shardingsphere.proxy.backend.exception.ShardingTableRuleNotExistedException;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.converter.ShardingRuleStatementConverter;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Create sharding binding table rule backend handler.
 */
public final class CreateShardingBindingTableRulesBackendHandler extends RDLBackendHandler<CreateShardingBindingTableRulesStatement> {
    
    public CreateShardingBindingTableRulesBackendHandler(final CreateShardingBindingTableRulesStatement sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
    }
    
    @Override
    public void before(final String schemaName, final CreateShardingBindingTableRulesStatement sqlStatement) {
        Optional<ShardingRuleConfiguration> shardingRuleConfig = getShardingRuleConfiguration(schemaName);
        if (!shardingRuleConfig.isPresent()) {
            throw new ShardingBindingTableRulesNotExistsException(schemaName);
        }
        Collection<String> invalidBindingTables = new HashSet<>();
        Collection<String> existLogicTables = getLogicTables(schemaName);
        Collection<String> bindingTables = ShardingRuleStatementConverter.convert(sqlStatement).getBindingTables();
        for (String bindingTable : bindingTables) {
            for (String logicTable : Splitter.on(",").splitToList(bindingTable)) {
                if (!existLogicTables.contains(logicTable.trim())) {
                    invalidBindingTables.add(logicTable);
                }
            }
        }
        if (!invalidBindingTables.isEmpty()) {
            throw new ShardingTableRuleNotExistedException(schemaName, invalidBindingTables);
        }
        bindingTables.addAll(shardingRuleConfig.get().getBindingTableGroups());
        Collection<String> duplicateBindingTables = bindingTables.stream().filter(distinct()).collect(Collectors.toList());
        if (!duplicateBindingTables.isEmpty()) {
            throw new DuplicateBindingTablesException(duplicateBindingTables);
        }
    }
    
    private Predicate<String> distinct() {
        Collection<String> tables = new HashSet<>();
        return table -> notEquals(table, tables);
    }
    
    private boolean notEquals(final String table, final Collection<String> tables) {
        for (String each : tables) {
            if (table.equals(each) || (table.length() == each.length() && Splitter.on(",").splitToList(each)
                    .containsAll(Splitter.on(",").splitToList(table)))) {
                return true;
            }
        }
        tables.add(table);
        return false;
    }
    
    @Override
    public void doExecute(final String schemaName, final CreateShardingBindingTableRulesStatement sqlStatement) {
        YamlShardingRuleConfiguration yamlShardingRuleConfiguration = ShardingRuleStatementConverter.convert(sqlStatement);
        getShardingRuleConfiguration(schemaName).get().getBindingTableGroups().addAll(yamlShardingRuleConfiguration.getBindingTables());
    }
    
    private Collection<String> getLogicTables(final String schemaName) {
        ShardingRuleConfiguration shardingRuleConfiguration = getShardingRuleConfiguration(schemaName).get();
        Collection<String> result = new HashSet<>();
        result.addAll(shardingRuleConfiguration.getTables().stream().map(ShardingTableRuleConfiguration::getLogicTable).collect(Collectors.toSet()));
        result.addAll(shardingRuleConfiguration.getAutoTables().stream().map(ShardingAutoTableRuleConfiguration::getLogicTable).collect(Collectors.toSet()));
        return result;
    }
}
