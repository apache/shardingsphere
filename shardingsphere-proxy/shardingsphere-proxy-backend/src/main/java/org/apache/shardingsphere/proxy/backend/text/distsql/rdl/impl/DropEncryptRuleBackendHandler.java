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

import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.impl.DropEncryptRuleStatement;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.YamlEncryptRuleConfiguration;
import org.apache.shardingsphere.governance.core.registry.watcher.event.rule.RuleConfigurationsAlteredEvent;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.EncryptRulesNotExistedException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.SchemaRequiredBackendHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Drop encrypt rule backend handler.
 */
public final class DropEncryptRuleBackendHandler extends SchemaRequiredBackendHandler<DropEncryptRuleStatement> {

    public DropEncryptRuleBackendHandler(final DropEncryptRuleStatement sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
    }
    
    @Override
    public ResponseHeader execute(final String schemaName, final DropEncryptRuleStatement sqlStatement) {
        Optional<EncryptRuleConfiguration> ruleConfig = ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations().stream()
                .filter(each -> each instanceof EncryptRuleConfiguration).map(each -> (EncryptRuleConfiguration) each).findFirst();
        if (!ruleConfig.isPresent()) {
            throw new EncryptRulesNotExistedException(schemaName, sqlStatement.getTables());
        }
        check(schemaName, ruleConfig.get(), sqlStatement.getTables());
        YamlEncryptRuleConfiguration yamlEncryptRuleConfiguration = new YamlRuleConfigurationSwapperEngine()
                .swapToYamlRuleConfigurations(Collections.singletonList(ruleConfig.get())).stream()
                .map(each -> (YamlEncryptRuleConfiguration) each).findFirst().get();
        drop(yamlEncryptRuleConfiguration, sqlStatement.getTables());
        post(schemaName, new YamlRuleConfigurationSwapperEngine()
                .swapToRuleConfigurations(Collections.singletonList(yamlEncryptRuleConfiguration)));
        return new UpdateResponseHeader(sqlStatement);
    }
    
    private void check(final String schemaName, final EncryptRuleConfiguration ruleConfig, final Collection<String> droppedTables) {
        Collection<String> encryptTables = ruleConfig.getTables().stream().map(EncryptTableRuleConfiguration::getName).collect(Collectors.toList());
        Collection<String> notExistedTables = droppedTables.stream().filter(each -> !encryptTables.contains(each)).collect(Collectors.toList());
        if (!notExistedTables.isEmpty()) {
            throw new EncryptRulesNotExistedException(schemaName, notExistedTables);
        }
    }

    private void drop(final YamlEncryptRuleConfiguration yamlEncryptRuleConfiguration, final Collection<String> droppedTables) {
        for (String each : droppedTables) {
            dropEncryptors(each, yamlEncryptRuleConfiguration);
            yamlEncryptRuleConfiguration.getTables().remove(each);
        }
    }

    private void dropEncryptors(final String droppedTable, final YamlEncryptRuleConfiguration yamlEncryptRuleConfiguration) {
        yamlEncryptRuleConfiguration.getTables().get(droppedTable).getColumns()
                .values().forEach(value -> yamlEncryptRuleConfiguration.getEncryptors().remove(value.getEncryptorName()));
    }
    
    private void post(final String schemaName, final Collection<RuleConfiguration> rules) {
        ShardingSphereEventBus.getInstance().post(new RuleConfigurationsAlteredEvent(schemaName, rules));
    }
}
