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

import com.google.common.base.Joiner;
import org.apache.shardingsphere.distsql.parser.segment.TableRuleSegment;
import org.apache.shardingsphere.distsql.parser.statement.rdl.AlterShardingRuleStatement;
import org.apache.shardingsphere.governance.core.registry.listener.event.rule.RuleConfigurationsAlteredEvent;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.ShardingRuleNotExistedException;
import org.apache.shardingsphere.proxy.backend.exception.ShardingTableRuleExistedException;
import org.apache.shardingsphere.proxy.backend.exception.ShardingTableRuleNotExistedException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.SchemaRequiredBackendHandler;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.converter.ShardingRuleStatementConverter;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlTableRuleConfiguration;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Alter sharding rule backend handler.
 */
public final class AlterShardingRuleBackendHandler extends SchemaRequiredBackendHandler<AlterShardingRuleStatement> {
    
    public AlterShardingRuleBackendHandler(final AlterShardingRuleStatement sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
    }
    
    @Override
    public ResponseHeader execute(final String schemaName, final AlterShardingRuleStatement sqlStatement) {
        Optional<ShardingRuleConfiguration> shardingRuleConfig = ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations().stream()
                .filter(each -> each instanceof ShardingRuleConfiguration).map(each -> (ShardingRuleConfiguration) each).findFirst();
        if (!shardingRuleConfig.isPresent()) {
            throw new ShardingRuleNotExistedException();
        }
        check(shardingRuleConfig.get(), sqlStatement);
        Optional<YamlShardingRuleConfiguration> yamlShardingRuleConfig = new YamlRuleConfigurationSwapperEngine().swapToYamlRuleConfigurations(Collections.singleton(shardingRuleConfig.get())).stream()
                .filter(each -> each instanceof YamlShardingRuleConfiguration).map(each -> (YamlShardingRuleConfiguration) each).findFirst();
        if (!yamlShardingRuleConfig.isPresent()) {
            throw new ShardingRuleNotExistedException();
        }
        YamlShardingRuleConfiguration rule = yamlShardingRuleConfig.get();
        alter(rule, sqlStatement);
        Collection<RuleConfiguration> rules = new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(Collections.singleton(rule));
        post(schemaName, rules);
        return new UpdateResponseHeader(sqlStatement);
    }
    
    private void check(final ShardingRuleConfiguration shardingRuleConfig, final AlterShardingRuleStatement statement) {
        checkModifyRule(shardingRuleConfig, statement);
        checkAddRule(shardingRuleConfig, statement);
        checkBindingTable(shardingRuleConfig, statement);
    }
    
    private void checkModifyRule(final ShardingRuleConfiguration shardingRuleConfig, final AlterShardingRuleStatement statement) {
        Collection<String> notExistRules = new LinkedList<>();
        for (TableRuleSegment each : statement.getModifyShardingRules()) {
            Optional<ShardingTableRuleConfiguration> existTable = shardingRuleConfig.getTables().stream().filter(t -> t.getLogicTable().equals(each.getLogicTable())).findFirst();
            if (existTable.isPresent()) {
                continue;
            }
            Optional<ShardingAutoTableRuleConfiguration> existAutoTable = shardingRuleConfig.getAutoTables().stream().filter(t -> t.getLogicTable().equals(each.getLogicTable())).findFirst();
            if (existAutoTable.isPresent()) {
                continue;
            }
            notExistRules.add(each.getLogicTable());
        }
        if (!notExistRules.isEmpty()) {
            throw new ShardingTableRuleNotExistedException(notExistRules);
        }
    }
    
    private void checkAddRule(final ShardingRuleConfiguration shardingRuleConfig, final AlterShardingRuleStatement statement) {
        Collection<String> existRules = new LinkedList<>();
        for (TableRuleSegment each : statement.getModifyShardingRules()) {
            Optional<ShardingTableRuleConfiguration> existTable = shardingRuleConfig.getTables().stream().filter(t -> !t.getLogicTable().equals(each.getLogicTable())).findFirst();
            if (existTable.isPresent()) {
                continue;
            }
            Optional<ShardingAutoTableRuleConfiguration> existAutoTable = shardingRuleConfig.getAutoTables().stream().filter(t -> !t.getLogicTable().equals(each.getLogicTable())).findFirst();
            if (existAutoTable.isPresent()) {
                continue;
            }
            existRules.add(each.getLogicTable());
        }
        if (!existRules.isEmpty()) {
            throw new ShardingTableRuleExistedException(existRules);
        }
    }
    
    private void checkBindingTable(final ShardingRuleConfiguration shardingRuleConfig, final AlterShardingRuleStatement statement) {
        Collection<String> validTables = new HashSet<>();
        validTables.addAll(shardingRuleConfig.getTables().stream().map(ShardingTableRuleConfiguration::getLogicTable).collect(Collectors.toSet()));
        validTables.addAll(shardingRuleConfig.getAutoTables().stream().map(ShardingAutoTableRuleConfiguration::getLogicTable).collect(Collectors.toSet()));
        validTables.addAll(statement.getAddShardingRules().stream().map(TableRuleSegment::getLogicTable).collect(Collectors.toList()));
        Collection<String> invalidTables = new LinkedList<>();
        for (Collection<String> each : statement.getAddBindingTables()) {
            for (String t : each) {
                if (!validTables.contains(t)) {
                    invalidTables.add(t);
                }
            }
        }
        for (Collection<String> each : statement.getDropBindingTables()) {
            for (String t : each) {
                if (!validTables.contains(t)) {
                    invalidTables.add(t);
                }
            }
        }
        if (!invalidTables.isEmpty()) {
            throw new ShardingTableRuleNotExistedException(invalidTables);
        }
    }
    
    private void alter(final YamlShardingRuleConfiguration yamlShardingRuleConfig, final AlterShardingRuleStatement statement) {
        modifyTableRule(yamlShardingRuleConfig, statement);
        addTableRule(yamlShardingRuleConfig, statement);
        Collection<String> existBindingTables = yamlShardingRuleConfig.getBindingTables();
        for (Collection<String> each : statement.getAddBindingTables()) {
            String addBindingTable = Joiner.on(",").join(each);
            if (!existBindingTables.contains(addBindingTable)) {
                existBindingTables.add(addBindingTable);
            }
        }
        for (Collection<String> each : statement.getDropBindingTables()) {
            String dropBindingTable = Joiner.on(",").join(each);
            existBindingTables.remove(dropBindingTable);
        }
        if (!statement.getBroadcastTables().isEmpty()) {
            yamlShardingRuleConfig.setBroadcastTables(statement.getBroadcastTables());
        }
        if (null != statement.getDefaultTableStrategy()) {
            yamlShardingRuleConfig.setDefaultTableStrategy(ShardingRuleStatementConverter.createDefaultTableStrategyConfiguration(statement.getDefaultTableStrategyColumn(),
                    statement.getDefaultTableStrategy()));
        }
    }
    
    private void modifyTableRule(final YamlShardingRuleConfiguration yamlShardingRuleConfig, final AlterShardingRuleStatement statement) {
        for (TableRuleSegment each : statement.getModifyShardingRules()) {
            YamlTableRuleConfiguration existTable = yamlShardingRuleConfig.getTables().get(each.getLogicTable());
            if (null != existTable) {
                yamlShardingRuleConfig.getTables().remove(each.getLogicTable());
            }
            YamlShardingAutoTableRuleConfiguration autoTable = new YamlShardingAutoTableRuleConfiguration();
            if (null != each.getDataSources() && !each.getDataSources().isEmpty()) {
                autoTable.setActualDataSources(Joiner.on(",").join(each.getDataSources()));
            }
            if (null != each.getTableStrategy()) {
                autoTable.setShardingStrategy(ShardingRuleStatementConverter.createTableStrategyConfiguration(each));
                yamlShardingRuleConfig.getShardingAlgorithms().put(ShardingRuleStatementConverter.getAlgorithmName(each.getLogicTable(), each.getTableStrategy().getAlgorithmName()),
                        ShardingRuleStatementConverter.createAlgorithmConfiguration(each.getTableStrategy()));
            }
            if (null != each.getKeyGenerateStrategy()) {
                autoTable.setKeyGenerateStrategy(ShardingRuleStatementConverter.createKeyGenerateStrategyConfiguration(each));
                yamlShardingRuleConfig.getShardingAlgorithms().put(ShardingRuleStatementConverter.getAlgorithmName(each.getLogicTable(), each.getKeyGenerateStrategy().getAlgorithmName()),
                        ShardingRuleStatementConverter.createAlgorithmConfiguration(each.getKeyGenerateStrategy()));
            }
            yamlShardingRuleConfig.getAutoTables().put(each.getLogicTable(), autoTable);
        }
    }
    
    private void addTableRule(final YamlShardingRuleConfiguration yamlShardingRuleConfig, final AlterShardingRuleStatement statement) {
        for (TableRuleSegment each : statement.getAddShardingRules()) {
            YamlShardingAutoTableRuleConfiguration table = new YamlShardingAutoTableRuleConfiguration();
            if (null != each.getDataSources() && !each.getDataSources().isEmpty()) {
                table.setActualDataSources(Joiner.on(",").join(each.getDataSources()));
            }
            if (null != each.getTableStrategy()) {
                table.setShardingStrategy(ShardingRuleStatementConverter.createTableStrategyConfiguration(each));
                yamlShardingRuleConfig.getShardingAlgorithms().put(ShardingRuleStatementConverter.getAlgorithmName(each.getLogicTable(), each.getTableStrategy().getAlgorithmName()),
                        ShardingRuleStatementConverter.createAlgorithmConfiguration(each.getTableStrategy()));
            }
            if (null != each.getKeyGenerateStrategy()) {
                table.setKeyGenerateStrategy(ShardingRuleStatementConverter.createKeyGenerateStrategyConfiguration(each));
                yamlShardingRuleConfig.getShardingAlgorithms().put(ShardingRuleStatementConverter.getAlgorithmName(each.getLogicTable(), each.getKeyGenerateStrategy().getAlgorithmName()),
                        ShardingRuleStatementConverter.createAlgorithmConfiguration(each.getKeyGenerateStrategy()));
            }
            yamlShardingRuleConfig.getAutoTables().put(each.getLogicTable(), table);
        }
    }
    
    private void post(final String schemaName, final Collection<RuleConfiguration> rules) {
        ShardingSphereEventBus.getInstance().post(new RuleConfigurationsAlteredEvent(schemaName, rules));
    }
}
