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

import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.impl.DropShardingTableRuleStatement;
import org.apache.shardingsphere.governance.core.registry.listener.event.rule.RuleConfigurationsAlteredEvent;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.ShardingRuleNotExistedException;
import org.apache.shardingsphere.proxy.backend.exception.ShardingTableRuleNotExistedException;
import org.apache.shardingsphere.proxy.backend.exception.TablesInUsedException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.SchemaRequiredBackendHandler;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Drop sharding table rule backend handler.
 */
public final class DropShardingTableRuleBackendHandler extends SchemaRequiredBackendHandler<DropShardingTableRuleStatement> {
    
    public DropShardingTableRuleBackendHandler(final DropShardingTableRuleStatement sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
    }
    
    @Override
    public ResponseHeader execute(final String schemaName, final DropShardingTableRuleStatement sqlStatement) {
        Collection<String> tableNames = sqlStatement.getTableNames().stream().map(each -> each.getIdentifier().getValue()).collect(Collectors.toList());
        ShardingRuleConfiguration shardingRuleConfiguration = check(schemaName, tableNames);
        drop(shardingRuleConfiguration, tableNames);
        post(schemaName);
        return new UpdateResponseHeader(sqlStatement);
    }
    
    private ShardingRuleConfiguration check(final String schemaName, final Collection<String> tableNames) {
        Optional<ShardingRuleConfiguration> shardingRuleConfiguration = getShardingRuleConfiguration(schemaName);
        if (!shardingRuleConfiguration.isPresent()) {
            throw new ShardingRuleNotExistedException();
        }
        Collection<String> shardingTableNames = getShardingTables(shardingRuleConfiguration.get());
        Collection<String> notExistedTableNames = tableNames.stream().filter(each -> !shardingTableNames.contains(each)).collect(Collectors.toList());
        if (!notExistedTableNames.isEmpty()) {
            throw new ShardingTableRuleNotExistedException(notExistedTableNames);
        }
        Collection<String> inUsedTableNames = tableNames.stream().filter(each -> ProxyContext.getInstance().getMetaData(schemaName).getSchema().containsTable(each)).collect(Collectors.toList());
        if (!inUsedTableNames.isEmpty()) {
            throw new TablesInUsedException(inUsedTableNames);
        }
        return shardingRuleConfiguration.get();
    }

    private Collection<String> getShardingTables(final ShardingRuleConfiguration shardingRuleConfiguration) {
        Collection<String> result = new LinkedList<>();
        result.addAll(shardingRuleConfiguration.getTables().stream().map(ShardingTableRuleConfiguration::getLogicTable).collect(Collectors.toList()));
        result.addAll(shardingRuleConfiguration.getAutoTables().stream().map(ShardingAutoTableRuleConfiguration::getLogicTable).collect(Collectors.toList()));
        return result;
    }
    
    private void drop(final ShardingRuleConfiguration shardingRuleConfiguration, final Collection<String> tableNames) {
        // TODO add global lock
        for (String each : tableNames) {
            dropShardingTable(each, shardingRuleConfiguration);
        }
    }
    
    private void dropShardingTable(final String tableName, final ShardingRuleConfiguration shardingRuleConfig) {
        shardingRuleConfig.getTables().removeAll(shardingRuleConfig.getTables().stream().filter(each -> tableName.equalsIgnoreCase(each.getLogicTable())).collect(Collectors.toList()));
        shardingRuleConfig.getAutoTables().removeAll(shardingRuleConfig.getAutoTables().stream().filter(each -> tableName.equalsIgnoreCase(each.getLogicTable())).collect(Collectors.toList()));
    }

    private Optional<ShardingRuleConfiguration> getShardingRuleConfiguration(final String schemaName) {
        return ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations().stream()
                .filter(each -> each instanceof ShardingRuleConfiguration).map(each -> (ShardingRuleConfiguration) each).findFirst();
    }

    private void post(final String schemaName) {
        // TODO should use RuleConfigurationsChangeEvent
        ShardingSphereEventBus.getInstance().post(new RuleConfigurationsAlteredEvent(schemaName, ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations()));
        // TODO Need to get the executed feedback from registry center for returning.
    }
}
