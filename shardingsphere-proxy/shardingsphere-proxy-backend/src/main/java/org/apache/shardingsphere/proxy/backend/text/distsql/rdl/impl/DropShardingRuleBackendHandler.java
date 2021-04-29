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

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.impl.DropShardingRuleStatement;
import org.apache.shardingsphere.governance.core.registry.listener.event.rule.RuleConfigurationsAlteredEvent;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.ShardingTableRuleNotExistedException;
import org.apache.shardingsphere.proxy.backend.exception.TablesInUsedException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.SchemaRequiredBackendHandler;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Drop sharding rule backend handler.
 */
public final class DropShardingRuleBackendHandler extends SchemaRequiredBackendHandler<DropShardingRuleStatement> {
    
    public DropShardingRuleBackendHandler(final DropShardingRuleStatement sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
    }
    
    @Override
    public ResponseHeader execute(final String schemaName, final DropShardingRuleStatement sqlStatement) {
        Collection<String> tableNames = sqlStatement.getTableNames().stream().map(each -> each.getIdentifier().getValue()).collect(Collectors.toList());
        check(schemaName, tableNames);
        drop(schemaName, tableNames);
        post(schemaName);
        return new UpdateResponseHeader(sqlStatement);
    }
    
    private void check(final String schemaName, final Collection<String> tableNames) {
        ShardingSphereMetaData metaData = ProxyContext.getInstance().getMetaData(schemaName);
        Optional<ShardingRule> shardingRule = metaData.getRuleMetaData().getRules().stream().filter(each -> each instanceof ShardingRule).map(each -> (ShardingRule) each).findFirst();
        if (!shardingRule.isPresent()) {
            throw new ShardingTableRuleNotExistedException(tableNames);
        }
        Collection<String> shardingTableNames = getShardingTableNames(shardingRule.get());
        Collection<String> notExistedTableNames = tableNames.stream().filter(each -> !shardingTableNames.contains(each)).collect(Collectors.toList());
        if (!notExistedTableNames.isEmpty()) {
            throw new ShardingTableRuleNotExistedException(notExistedTableNames);
        }
        Collection<String> inUsedTableNames = tableNames.stream().filter(each -> ProxyContext.getInstance().getMetaData(schemaName).getSchema().containsTable(each)).collect(Collectors.toList());
        if (!inUsedTableNames.isEmpty()) {
            throw new TablesInUsedException(inUsedTableNames);
        }
    }
    
    private Collection<String> getShardingTableNames(final ShardingRule shardingRule) {
        Collection<String> result = new LinkedList<>(shardingRule.getTables());
        result.addAll(shardingRule.getBroadcastTables());
        return result;
    }
    
    private void drop(final String schemaName, final Collection<String> tableNames) {
        Optional<ShardingRuleConfiguration> shardingRuleConfig = ProxyContext.getInstance().getMetaData(schemaName)
                .getRuleMetaData().getConfigurations().stream().filter(each -> each instanceof ShardingRuleConfiguration).map(each -> (ShardingRuleConfiguration) each).findFirst();
        Preconditions.checkState(shardingRuleConfig.isPresent());
        // TODO add global lock
        for (String each : tableNames) {
            dropShardingTable(each, shardingRuleConfig.get());
            dropBroadcastTable(each, shardingRuleConfig.get());
            dropBindingTable(each, shardingRuleConfig.get());
        }
    }
    
    private void dropShardingTable(final String tableName, final ShardingRuleConfiguration shardingRuleConfig) {
        shardingRuleConfig.getTables().removeAll(shardingRuleConfig.getTables().stream().filter(each -> tableName.equalsIgnoreCase(each.getLogicTable())).collect(Collectors.toList()));
    }
    
    private void dropBroadcastTable(final String tableName, final ShardingRuleConfiguration shardingRuleConfig) {
        shardingRuleConfig.getBroadcastTables().removeAll(shardingRuleConfig.getBroadcastTables().stream().filter(tableName::equalsIgnoreCase).collect(Collectors.toList()));
    }
    
    private void dropBindingTable(final String tableName, final ShardingRuleConfiguration shardingRuleConfig) {
        Collection<String> toBeDroppedGroups = shardingRuleConfig.getBindingTableGroups().stream().filter(each -> Arrays.asList(each.split(",")).contains(tableName)).collect(Collectors.toList());
        shardingRuleConfig.getBindingTableGroups().removeAll(toBeDroppedGroups);
        Collection<String> newGroups = toBeDroppedGroups.stream().map(each -> createBindingTableGroupWithoutTableName(tableName, each)).collect(Collectors.toList());
        shardingRuleConfig.getBindingTableGroups().addAll(newGroups);
    }
    
    private String createBindingTableGroupWithoutTableName(final String expectedTable, final String originalBindingTableGroup) {
        return Arrays.stream(originalBindingTableGroup.split(",")).filter(each -> !each.trim().equalsIgnoreCase(expectedTable)).collect(Collectors.joining(","));
    }
    
    private void post(final String schemaName) {
        // TODO should use RuleConfigurationsChangeEvent
        ShardingSphereEventBus.getInstance().post(new RuleConfigurationsAlteredEvent(schemaName, ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations()));
        // TODO Need to get the executed feedback from registry center for returning.
    }
}
