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

import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.impl.DropReplicaQueryRuleStatement;
import org.apache.shardingsphere.governance.core.registry.listener.event.rule.RuleConfigurationsAlteredEvent;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.ReadWriteSplittingRuleDataSourcesNotExistedException;
import org.apache.shardingsphere.proxy.backend.exception.ReadWriteSplittingRuleNotExistedException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.SchemaRequiredBackendHandler;
import org.apache.shardingsphere.readwritesplitting.api.ReadWriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadWriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.common.yaml.config.YamlReadWriteSplittingRuleConfiguration;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Drop readwrite-splitting rule backend handler.
 */
public final class DropReadWriteSplittingRuleBackendHandler extends SchemaRequiredBackendHandler<DropReplicaQueryRuleStatement> {

    public DropReadWriteSplittingRuleBackendHandler(final DropReplicaQueryRuleStatement sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
    }
    
    @Override
    public ResponseHeader execute(final String schemaName, final DropReplicaQueryRuleStatement sqlStatement) {
        Collection<String> ruleNames = sqlStatement.getRuleNames();
        Optional<ReadWriteSplittingRuleConfiguration> readWriteSplittingRuleConfig = ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations().stream()
                .filter(each -> each instanceof ReadWriteSplittingRuleConfiguration).map(each -> (ReadWriteSplittingRuleConfiguration) each).findFirst();
        if (!readWriteSplittingRuleConfig.isPresent()) {
            throw new ReadWriteSplittingRuleNotExistedException();
        }
        check(readWriteSplittingRuleConfig.get(), ruleNames);
        Optional<YamlReadWriteSplittingRuleConfiguration> yamlConfig = new YamlRuleConfigurationSwapperEngine()
                .swapToYamlRuleConfigurations(Collections.singletonList(readWriteSplittingRuleConfig.get())).stream()
                .map(each -> (YamlReadWriteSplittingRuleConfiguration) each).findFirst();
        if (!yamlConfig.isPresent()) {
            throw new ReadWriteSplittingRuleNotExistedException();
        }
        Collection<RuleConfiguration> rules = drop(yamlConfig.get(), ruleNames);
        post(schemaName, rules);
        return new UpdateResponseHeader(sqlStatement);
    }
    
    private void check(final ReadWriteSplittingRuleConfiguration readWriteSplittingRuleConfig, final Collection<String> ruleNames) {
        Collection<String> readWriteSplittingNames = readWriteSplittingRuleConfig.getDataSources().stream().map(ReadWriteSplittingDataSourceRuleConfiguration::getName).collect(Collectors.toList());
        Collection<String> notExistedRuleNames = ruleNames.stream().filter(each -> !readWriteSplittingNames.contains(each)).collect(Collectors.toList());
        if (!notExistedRuleNames.isEmpty()) {
            throw new ReadWriteSplittingRuleDataSourcesNotExistedException(notExistedRuleNames);
        }
    }

    private Collection<RuleConfiguration> drop(final YamlReadWriteSplittingRuleConfiguration yamlConfig, final Collection<String> ruleNames) {
        for (String each : ruleNames) {
            yamlConfig.getDataSources().remove(each);
        }
        if (yamlConfig.getDataSources().isEmpty()) {
            return new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(Collections.emptyList());
        } else {
            return new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(Collections.singleton(yamlConfig));
        }
    }
    
    private void post(final String schemaName, final Collection<RuleConfiguration> rules) {
        ShardingSphereEventBus.getInstance().post(new RuleConfigurationsAlteredEvent(schemaName, rules));
    }
}
