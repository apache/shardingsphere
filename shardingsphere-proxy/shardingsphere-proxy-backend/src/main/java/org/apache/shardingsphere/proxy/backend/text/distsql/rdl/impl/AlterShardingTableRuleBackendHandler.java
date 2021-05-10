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
import org.apache.shardingsphere.governance.core.registry.listener.event.rule.RuleConfigurationsAlteredEvent;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.DuplicateTablesException;
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

import java.util.Set;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
        if (!getShardingRuleConfiguration(schemaName).isPresent()) {
            throw new ShardingRuleNotExistedException();
        }
        check(schemaName, sqlStatement);
        Optional<YamlShardingRuleConfiguration> yamlShardingRuleConfig = new YamlRuleConfigurationSwapperEngine()
                .swapToYamlRuleConfigurations(Collections.singleton(getShardingRuleConfiguration(schemaName).get())).stream()
                .filter(each -> each instanceof YamlShardingRuleConfiguration).map(each -> (YamlShardingRuleConfiguration) each).findFirst();
        if (!yamlShardingRuleConfig.isPresent()) {
            throw new ShardingRuleNotExistedException();
        }
        YamlShardingRuleConfiguration existYamlShardingRuleConfiguration = yamlShardingRuleConfig.get();
        YamlShardingRuleConfiguration yamlShardingRuleConfiguration = ShardingRuleStatementConverter.convert(sqlStatement);
        existYamlShardingRuleConfiguration.getShardingAlgorithms().putAll(yamlShardingRuleConfiguration.getShardingAlgorithms());
        yamlShardingRuleConfiguration.getTables().keySet().forEach(each -> {
            if (existYamlShardingRuleConfiguration.getTables().keySet().contains(each)) {
                existYamlShardingRuleConfiguration.getTables().remove(each);
            }
        });
        existYamlShardingRuleConfiguration.getAutoTables().putAll(yamlShardingRuleConfiguration.getAutoTables());
        post(schemaName, new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(Collections.singleton(existYamlShardingRuleConfiguration)));
        return new UpdateResponseHeader(sqlStatement);
    }

    private void check(final String schemaName, final AlterShardingTableRuleStatement sqlStatement) {
        Collection<String> logicTableNames = new ArrayList<>(sqlStatement.getTables().size());
        Set<String> duplicateTableNames = new HashSet<>(sqlStatement.getTables().size(), 1);
        for (TableRuleSegment tableRuleSegment : sqlStatement.getTables()) {
            if (logicTableNames.contains(tableRuleSegment.getLogicTable())) {
                duplicateTableNames.add(tableRuleSegment.getLogicTable());
            }
            logicTableNames.add(tableRuleSegment.getLogicTable());
        }
        if (!duplicateTableNames.isEmpty()) {
            throw new DuplicateTablesException(duplicateTableNames);
        }
        Collection<String> existTables = getLogicTables(schemaName);
        Collection<String> notExistTables = logicTableNames.stream().filter(each -> !existTables.contains(each)).collect(Collectors.toList());
        if (!notExistTables.isEmpty()) {
            throw new ShardingTableRuleNotExistedException(notExistTables);
        }
    }

    private Collection<String> getLogicTables(final String schemaName) {
        Optional<ShardingRuleConfiguration> shardingRuleConfiguration = getShardingRuleConfiguration(schemaName);
        Collection<String> result = new LinkedList<>();
        if (!shardingRuleConfiguration.isPresent()) {
            return result;
        }
        result.addAll(shardingRuleConfiguration.get().getTables().stream().map(ShardingTableRuleConfiguration::getLogicTable).collect(Collectors.toList()));
        result.addAll(shardingRuleConfiguration.get().getAutoTables().stream().map(ShardingAutoTableRuleConfiguration::getLogicTable).collect(Collectors.toList()));
        return result;
    }

    private Optional<ShardingRuleConfiguration> getShardingRuleConfiguration(final String schemaName) {
        return ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations().stream()
                .filter(each -> each instanceof ShardingRuleConfiguration).map(each -> (ShardingRuleConfiguration) each).findFirst();
    }
    
    private void post(final String schemaName, final Collection<RuleConfiguration> rules) {
        ShardingSphereEventBus.getInstance().post(new RuleConfigurationsAlteredEvent(schemaName, rules));
    }
}
