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

import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.impl.DropReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.governance.core.registry.listener.event.rule.RuleConfigurationsAlteredEvent;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.ReadwriteSplittingRulesNotExistedException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.SchemaRequiredBackendHandler;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.common.yaml.config.YamlReadwriteSplittingRuleConfiguration;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Drop readwrite splitting rule backend handler.
 */
public final class DropReadwriteSplittingRuleBackendHandler extends SchemaRequiredBackendHandler<DropReadwriteSplittingRuleStatement> {

    public DropReadwriteSplittingRuleBackendHandler(final DropReadwriteSplittingRuleStatement sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
    }
    
    @Override
    public ResponseHeader execute(final String schemaName, final DropReadwriteSplittingRuleStatement sqlStatement) {
        Collection<String> droppedRuleNames = sqlStatement.getRuleNames();
        ReadwriteSplittingRuleConfiguration readwriteSplittingRuleConfiguration = getReadwriteSplittingRuleConfiguration(schemaName, droppedRuleNames);
        check(schemaName, readwriteSplittingRuleConfiguration, droppedRuleNames);
        YamlReadwriteSplittingRuleConfiguration yamlReadwriteSplittingRuleConfiguration = getYamlReadwriteSplittingRuleConfiguration(readwriteSplittingRuleConfiguration);
        drop(yamlReadwriteSplittingRuleConfiguration, droppedRuleNames);
        post(schemaName, new YamlRuleConfigurationSwapperEngine()
                .swapToRuleConfigurations(yamlReadwriteSplittingRuleConfiguration.getDataSources()
                        .isEmpty() ? Collections.emptyList() : Collections.singletonList(yamlReadwriteSplittingRuleConfiguration)));
        return new UpdateResponseHeader(sqlStatement);
    }

    private ReadwriteSplittingRuleConfiguration getReadwriteSplittingRuleConfiguration(final String schemaName, final Collection<String> droppedRuleNames) {
        Optional<ReadwriteSplittingRuleConfiguration> ruleConfig = ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations().stream()
                .filter(each -> each instanceof ReadwriteSplittingRuleConfiguration).map(each -> (ReadwriteSplittingRuleConfiguration) each).findFirst();
        if (!ruleConfig.isPresent()) {
            throw new ReadwriteSplittingRulesNotExistedException(schemaName, droppedRuleNames);
        }
        return ruleConfig.get();
    }

    private YamlReadwriteSplittingRuleConfiguration getYamlReadwriteSplittingRuleConfiguration(final ReadwriteSplittingRuleConfiguration readwriteSplittingRuleConfiguration) {
        return new YamlRuleConfigurationSwapperEngine()
                .swapToYamlRuleConfigurations(Collections.singletonList(readwriteSplittingRuleConfiguration)).stream()
                .map(each -> (YamlReadwriteSplittingRuleConfiguration) each).findFirst().get();
    }
    
    private void check(final String schemaName, final ReadwriteSplittingRuleConfiguration ruleConfig, final Collection<String> droppedRuleNames) {
        Collection<String> existRuleNames = ruleConfig.getDataSources().stream().map(ReadwriteSplittingDataSourceRuleConfiguration::getName).collect(Collectors.toList());
        Collection<String> notExistedRuleNames = droppedRuleNames.stream().filter(each -> !existRuleNames.contains(each)).collect(Collectors.toList());
        if (!notExistedRuleNames.isEmpty()) {
            throw new ReadwriteSplittingRulesNotExistedException(schemaName, droppedRuleNames);
        }
    }

    private void drop(final YamlReadwriteSplittingRuleConfiguration yamlConfig, final Collection<String> ruleNames) {
        for (String each : ruleNames) {
            yamlConfig.getLoadBalancers().remove(yamlConfig.getDataSources().get(each).getLoadBalancerName());
            yamlConfig.getDataSources().remove(each);
        }
    }
    
    private void post(final String schemaName, final Collection<RuleConfiguration> rules) {
        ShardingSphereEventBus.getInstance().post(new RuleConfigurationsAlteredEvent(schemaName, rules));
    }
}
