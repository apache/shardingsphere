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

import org.apache.shardingsphere.distsql.parser.segment.TableRuleSegment;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.CreateShardingTableRuleStatement;
import org.apache.shardingsphere.governance.core.event.model.rule.RuleConfigurationsAlteredEvent;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.DuplicateTablesException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.SchemaRequiredBackendHandler;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.converter.ShardingRuleStatementConverter;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Create sharding table rule backend handler.
 */
public final class CreateShardingTableRuleBackendHandler extends SchemaRequiredBackendHandler<CreateShardingTableRuleStatement> {
    
    public CreateShardingTableRuleBackendHandler(final CreateShardingTableRuleStatement sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
    }
    
    @Override
    public ResponseHeader execute(final String schemaName, final CreateShardingTableRuleStatement sqlStatement) {
        check(schemaName, sqlStatement);
        YamlShardingRuleConfiguration config = ShardingRuleStatementConverter.convert(sqlStatement);
        Collection<RuleConfiguration> rules = new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(Collections.singleton(config));
        post(schemaName, rules);
        return new UpdateResponseHeader(sqlStatement);
    }
    
    private void check(final String schemaName, final CreateShardingTableRuleStatement sqlStatement) {
        Collection<String> logicTableNames = new ArrayList<>(sqlStatement.getTables().size());
        Collection<String> existLogicTables = getLogicTables(schemaName);
        Set<String> duplicateTableNames = new HashSet<>(sqlStatement.getTables().size(), 1);
        for (TableRuleSegment tableRuleSegment : sqlStatement.getTables()) {
            if (logicTableNames.contains(tableRuleSegment.getLogicTable()) 
                    || existLogicTables.contains(tableRuleSegment.getLogicTable())) {
                duplicateTableNames.add(tableRuleSegment.getLogicTable());
            }
            logicTableNames.add(tableRuleSegment.getLogicTable());
        }
        if (!duplicateTableNames.isEmpty()) {
            throw new DuplicateTablesException(duplicateTableNames);
        }
    }
    
    private Collection<String> getLogicTables(final String schemaName) {
        Optional<ShardingRuleConfiguration> shardingRuleConfig = ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations().stream()
                .filter(each -> each instanceof ShardingRuleConfiguration).map(each -> (ShardingRuleConfiguration) each).findFirst();
        if (!shardingRuleConfig.isPresent()) {
            return Collections.EMPTY_LIST;
        }
        return shardingRuleConfig.get().getTables().stream().map(each -> each.getLogicTable()).collect(Collectors.toList());
    }
    
    private void post(final String schemaName, final Collection<RuleConfiguration> rules) {
        ShardingSphereEventBus.getInstance().post(new RuleConfigurationsAlteredEvent(schemaName, rules));
        // TODO Need to get the executed feedback from registry center for returning.
    }
}
