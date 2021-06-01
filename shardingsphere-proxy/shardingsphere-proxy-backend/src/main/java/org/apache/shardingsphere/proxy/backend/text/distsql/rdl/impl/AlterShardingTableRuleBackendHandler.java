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
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.AlterShardingTableRuleStatement;
import org.apache.shardingsphere.governance.core.registry.config.event.rule.RuleConfigurationsAlteredEvent;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.DuplicateTablesException;
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
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Alter sharding table rule backend handler.
 */
public final class AlterShardingTableRuleBackendHandler extends SchemaRequiredBackendHandler<AlterShardingTableRuleStatement> {
    
    public AlterShardingTableRuleBackendHandler(final AlterShardingTableRuleStatement sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
    }
    
    @Override
    public ResponseHeader execute(final String schemaName, final AlterShardingTableRuleStatement sqlStatement) {
        check(schemaName, sqlStatement);
        YamlShardingRuleConfiguration yamlShardingRuleConfiguration = alter(schemaName, sqlStatement);
        post(schemaName, new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(Collections.singleton(yamlShardingRuleConfiguration)));
        return new UpdateResponseHeader(sqlStatement);
    }
    
    private void check(final String schemaName, final AlterShardingTableRuleStatement sqlStatement) {
        Collection<String> duplicateTables = getDuplicateTables(sqlStatement);
        if (!duplicateTables.isEmpty()) {
            throw new DuplicateTablesException(duplicateTables);
        }
        Collection<String> alteredTables = getAlteredTables(sqlStatement);
        if (!getShardingRuleConfiguration(schemaName).isPresent()) {
            throw new ShardingTableRuleNotExistedException(schemaName, alteredTables);
        }
        Collection<String> existTables = getShardingTables(getShardingRuleConfiguration(schemaName).get());
        Collection<String> notExistTables = alteredTables.stream().filter(each -> !existTables.contains(each)).collect(Collectors.toList());
        if (!notExistTables.isEmpty()) {
            throw new ShardingTableRuleNotExistedException(schemaName, notExistTables);
        }
    }
    
    private Collection<String> getDuplicateTables(final AlterShardingTableRuleStatement sqlStatement) {
        return sqlStatement.getTables().stream()
                .collect(Collectors.toMap(TableRuleSegment::getLogicTable, e -> 1, Integer::sum))
                .entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Entry::getKey)
                .collect(Collectors.toList());
    }
    
    private Collection<String> getAlteredTables(final AlterShardingTableRuleStatement sqlStatement) {
        return sqlStatement.getTables().stream().map(TableRuleSegment::getLogicTable).collect(Collectors.toList());
    }
    
    private Optional<ShardingRuleConfiguration> getShardingRuleConfiguration(final String schemaName) {
        return ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations().stream()
                .filter(each -> each instanceof ShardingRuleConfiguration).map(each -> (ShardingRuleConfiguration) each).findFirst();
    }
    
    private Collection<String> getShardingTables(final ShardingRuleConfiguration shardingRuleConfiguration) {
        Collection<String> result = new LinkedList<>();
        result.addAll(shardingRuleConfiguration.getTables().stream().map(ShardingTableRuleConfiguration::getLogicTable).collect(Collectors.toList()));
        result.addAll(shardingRuleConfiguration.getAutoTables().stream().map(ShardingAutoTableRuleConfiguration::getLogicTable).collect(Collectors.toList()));
        return result;
    }
    
    private YamlShardingRuleConfiguration alter(final String schemaName, final AlterShardingTableRuleStatement sqlStatement) {
        YamlShardingRuleConfiguration result = new YamlRuleConfigurationSwapperEngine()
                .swapToYamlRuleConfigurations(Collections.singleton(getShardingRuleConfiguration(schemaName).get())).stream()
                .filter(each -> each instanceof YamlShardingRuleConfiguration).map(each -> (YamlShardingRuleConfiguration) each).findFirst().get();
        YamlShardingRuleConfiguration yamlShardingRuleConfiguration = ShardingRuleStatementConverter.convert(sqlStatement);
        result.getShardingAlgorithms().putAll(yamlShardingRuleConfiguration.getShardingAlgorithms());
        yamlShardingRuleConfiguration.getTables().keySet().forEach(each -> result.getTables().remove(each));
        result.getAutoTables().putAll(yamlShardingRuleConfiguration.getAutoTables());
        return result;
    }
    
    private void post(final String schemaName, final Collection<RuleConfiguration> rules) {
        ShardingSphereEventBus.getInstance().post(new RuleConfigurationsAlteredEvent(schemaName, rules));
    }
}
