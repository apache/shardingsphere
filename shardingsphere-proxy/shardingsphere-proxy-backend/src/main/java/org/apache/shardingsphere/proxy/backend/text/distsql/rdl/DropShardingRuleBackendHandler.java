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

package org.apache.shardingsphere.proxy.backend.text.distsql.rdl;

import com.google.common.base.Joiner;
import org.apache.shardingsphere.governance.core.event.model.rule.RuleConfigurationsPersistEvent;
import org.apache.shardingsphere.infra.binder.statement.rdl.DropShardingRuleStatementContext;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.NoDatabaseSelectedException;
import org.apache.shardingsphere.proxy.backend.exception.ShardingTableRuleNotExistedException;
import org.apache.shardingsphere.proxy.backend.exception.TablesInUsedException;
import org.apache.shardingsphere.proxy.backend.exception.UnknownDatabaseException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
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
public final class DropShardingRuleBackendHandler implements RDLBackendDetailHandler {
    
    @Override
    public ResponseHeader execute(final String schemaName, final DropShardingRuleStatementContext context) {
        if (null == schemaName) {
            throw new NoDatabaseSelectedException();
        }
        if (!ProxyContext.getInstance().schemaExists(schemaName)) {
            throw new UnknownDatabaseException(schemaName);
        }
        Collection<String> tableNames = context.getSqlStatement().getTableNames().stream().map(each -> each.getIdentifier().getValue()).collect(Collectors.toList());
        Optional<ShardingRuleConfiguration> ruleConfig = findShardingRuleConfiguration(schemaName);
        if (!ruleConfig.isPresent()) {
            throw new ShardingTableRuleNotExistedException(tableNames);
        }
        checkShardingTables(schemaName, tableNames);
        removeShardingTableRules(tableNames, ruleConfig.get());
        // TODO should use RuleConfigurationsChangeEvent
        ShardingSphereEventBus.getInstance().post(new RuleConfigurationsPersistEvent(schemaName, ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations()));
        // TODO Need to get the executed feedback from registry center for returning.
        return new UpdateResponseHeader(context.getSqlStatement());
    }
    
    private Optional<ShardingRuleConfiguration> findShardingRuleConfiguration(final String schemaName) {
        return ProxyContext.getInstance().getMetaData(schemaName)
                .getRuleMetaData().getConfigurations().stream().filter(each -> each instanceof ShardingRuleConfiguration).map(each -> (ShardingRuleConfiguration) each).findFirst();
    }
    
    private void checkShardingTables(final String schemaName, final Collection<String> tableNames) {
        ShardingSphereMetaData metaData = ProxyContext.getInstance().getMetaData(schemaName);
        Optional<ShardingRule> shardingRule = metaData.getRuleMetaData().getRules().stream().filter(each -> each instanceof ShardingRule).map(each -> (ShardingRule) each).findFirst();
        if (!shardingRule.isPresent()) {
            return;
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
    
    private void removeShardingTableRules(final Collection<String> tableNames, final ShardingRuleConfiguration ruleConfig) {
        // TODO add global lock
        for (String each : tableNames) {
            removeShardingTableRule(each, ruleConfig);
        }
    }
    
    private void removeShardingTableRule(final String tableName, final ShardingRuleConfiguration ruleConfig) {
        Collection<String> bindingTableGroups = ruleConfig.getBindingTableGroups().stream().filter(each -> Arrays.asList(each.split(",")).contains(tableName)).collect(Collectors.toList());
        ruleConfig.getBindingTableGroups().removeAll(bindingTableGroups);
        Collection<String> newBindingTableGroups = new LinkedList<>();
        for (String each : bindingTableGroups) {
            Collection<String> sss = new LinkedList<>();
            for (String str : each.split(",")) {
                if (!str.trim().equalsIgnoreCase(tableName)) {
                    sss.add(str);
                }
            }
            newBindingTableGroups.add(Joiner.on(",").join(sss));
        }
        ruleConfig.getBindingTableGroups().addAll(newBindingTableGroups);
        ruleConfig.getTables().removeAll(ruleConfig.getTables().stream().filter(each -> tableName.equalsIgnoreCase(each.getLogicTable())).collect(Collectors.toList()));
        ruleConfig.getBroadcastTables().removeAll(ruleConfig.getBroadcastTables().stream().filter(tableName::equalsIgnoreCase).collect(Collectors.toList()));
    }
}
