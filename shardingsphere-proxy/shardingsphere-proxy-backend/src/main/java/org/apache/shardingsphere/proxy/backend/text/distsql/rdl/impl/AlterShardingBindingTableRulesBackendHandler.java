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
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.AlterShardingBindingTableRulesStatement;
import org.apache.shardingsphere.governance.core.registry.listener.event.rule.RuleConfigurationsAlteredEvent;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.ShardingRuleNotExistedException;
import org.apache.shardingsphere.proxy.backend.exception.ShardingTableRuleNotExistedException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.SchemaRequiredBackendHandler;
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
 * Alter sharding binding table rule backend handler.
 */
public final class AlterShardingBindingTableRulesBackendHandler extends SchemaRequiredBackendHandler<AlterShardingBindingTableRulesStatement> {

    public AlterShardingBindingTableRulesBackendHandler(final AlterShardingBindingTableRulesStatement sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
    }
    
    @Override
    public ResponseHeader execute(final String schemaName, final AlterShardingBindingTableRulesStatement sqlStatement) {
        YamlShardingRuleConfiguration config = ShardingRuleStatementConverter.convert(sqlStatement);
        ShardingRuleConfiguration shardingRuleConfiguration = check(schemaName, config);
        shardingRuleConfiguration.getBindingTableGroups().clear();
        shardingRuleConfiguration.getBindingTableGroups().addAll(config.getBindingTables());
        post(schemaName, ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations());
        return new UpdateResponseHeader(sqlStatement);
    }
    
    private ShardingRuleConfiguration check(final String schemaName, final YamlShardingRuleConfiguration configuration) {
        Optional<ShardingRuleConfiguration> shardingRuleConfig = ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations().stream()
                .filter(each -> each instanceof ShardingRuleConfiguration).map(each -> (ShardingRuleConfiguration) each).findFirst();
        if (!shardingRuleConfig.isPresent()) {
            throw new ShardingRuleNotExistedException();
        }
        Collection<String> invalidBindingTables = new HashSet<>();
        Collection<String> existLogicTables = new HashSet<>();
        existLogicTables.addAll(shardingRuleConfig.get().getTables().stream().map(ShardingTableRuleConfiguration::getLogicTable).collect(Collectors.toSet()));
        existLogicTables.addAll(shardingRuleConfig.get().getAutoTables().stream().map(ShardingAutoTableRuleConfiguration::getLogicTable).collect(Collectors.toSet()));
        for (String bindingTable : configuration.getBindingTables()) {
            for (String logicTable : Splitter.on(",").splitToList(bindingTable)) {
                if (!existLogicTables.contains(logicTable.trim())) {
                    invalidBindingTables.add(logicTable);
                }
            }
        }
        if (!invalidBindingTables.isEmpty()) {
            throw new ShardingTableRuleNotExistedException(invalidBindingTables);
        }
        return shardingRuleConfig.get();
    }
    
    private void post(final String schemaName, final Collection<RuleConfiguration> rules) {
        ShardingSphereEventBus.getInstance().post(new RuleConfigurationsAlteredEvent(schemaName, rules));
        // TODO Need to get the executed feedback from registry center for returning.
    }
}
