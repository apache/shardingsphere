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

import com.google.common.base.Strings;
import org.apache.shardingsphere.distsql.parser.segment.rdl.ReadwriteSplittingRuleSegment;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.AlterReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.governance.core.registry.listener.event.rule.RuleConfigurationsAlteredEvent;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.typed.TypedSPIRegistry;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.InvalidLoadBalancersException;
import org.apache.shardingsphere.proxy.backend.exception.ReadwriteSplittingRulesNotExistedException;
import org.apache.shardingsphere.proxy.backend.exception.ResourceNotExistedException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.SchemaRequiredBackendHandler;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.common.yaml.config.YamlReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.common.yaml.converter.ReadwriteSplittingRuleStatementConverter;
import org.apache.shardingsphere.readwritesplitting.spi.ReplicaLoadBalanceAlgorithm;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Alter readwrite-splitting rule backend handler.
 */
public final class AlterReadwriteSplittingRuleBackendHandler extends SchemaRequiredBackendHandler<AlterReadwriteSplittingRuleStatement> {

    static {
        ShardingSphereServiceLoader.register(ReplicaLoadBalanceAlgorithm.class);
    }

    public AlterReadwriteSplittingRuleBackendHandler(final AlterReadwriteSplittingRuleStatement sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
    }
    
    @Override
    public ResponseHeader execute(final String schemaName, final AlterReadwriteSplittingRuleStatement sqlStatement) {
        Collection<String> alteredRuleNames = getAlteredRuleNames(sqlStatement);
        Optional<ReadwriteSplittingRuleConfiguration> ruleConfig = ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations().stream()
                .filter(each -> each instanceof ReadwriteSplittingRuleConfiguration).map(each -> (ReadwriteSplittingRuleConfiguration) each).findFirst();
        if (!ruleConfig.isPresent()) {
            throw new ReadwriteSplittingRulesNotExistedException(schemaName, alteredRuleNames);
        }
        check(schemaName, sqlStatement, ruleConfig.get(), alteredRuleNames);
        YamlReadwriteSplittingRuleConfiguration alterConfig = alter(ruleConfig.get(), sqlStatement);
        Collection<RuleConfiguration> rules = new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(Collections.singleton(alterConfig));
        post(schemaName, rules);
        return new UpdateResponseHeader(sqlStatement);
    }

    private Collection<String> getAlteredRuleNames(final AlterReadwriteSplittingRuleStatement sqlStatement) {
        return sqlStatement.getReadwriteSplittingRules().stream().map(ReadwriteSplittingRuleSegment::getName).collect(Collectors.toSet());
    }
    
    private void check(final String schemaName, final AlterReadwriteSplittingRuleStatement sqlStatement,
                       final ReadwriteSplittingRuleConfiguration ruleConfig, final Collection<String> alteredRuleNames) {
        checkAlteredRules(schemaName, ruleConfig, alteredRuleNames);
        checkResources(sqlStatement, schemaName);
        checkLoadBalancer(sqlStatement);
    }

    private void checkAlteredRules(final String schemaName, final ReadwriteSplittingRuleConfiguration ruleConfig, final Collection<String> alteredRuleNames) {
        Set<String> existRuleNames = ruleConfig.getDataSources().stream().map(ReadwriteSplittingDataSourceRuleConfiguration::getName).collect(Collectors.toSet());
        Collection<String> notExistRuleNames = alteredRuleNames.stream()
                .filter(each -> !existRuleNames.contains(each)).collect(Collectors.toList());
        if (!notExistRuleNames.isEmpty()) {
            throw new ReadwriteSplittingRulesNotExistedException(schemaName, notExistRuleNames);
        }
    }

    private void checkResources(final AlterReadwriteSplittingRuleStatement sqlStatement, final String schemaName) {
        Collection<String> resources = new LinkedHashSet<>();
        sqlStatement.getReadwriteSplittingRules().stream().filter(each -> Strings.isNullOrEmpty(each.getAutoAwareResource())).forEach(each -> {
            resources.add(each.getWriteDataSource());
            resources.addAll(each.getReadDataSources());
        });
        Collection<String> notExistResources = resources.stream().filter(each -> !this.isValidResource(schemaName, each)).collect(Collectors.toList());
        if (!notExistResources.isEmpty()) {
            throw new ResourceNotExistedException(notExistResources);
        }
    }

    private void checkLoadBalancer(final AlterReadwriteSplittingRuleStatement sqlStatement) {
        Collection<String> invalidLoadBalances = sqlStatement.getReadwriteSplittingRules().stream().map(each -> each.getLoadBalancer()).distinct()
                .filter(each -> !TypedSPIRegistry.findRegisteredService(ReplicaLoadBalanceAlgorithm.class, each, new Properties()).isPresent())
                .collect(Collectors.toList());
        if (!invalidLoadBalances.isEmpty()) {
            throw new InvalidLoadBalancersException(invalidLoadBalances);
        }
    }

    private boolean isValidResource(final String schemaName, final String resourceName) {
        return Objects.nonNull(ProxyContext.getInstance().getMetaData(schemaName).getResource())
                && ProxyContext.getInstance().getMetaData(schemaName).getResource().getDataSources().containsKey(resourceName);
    }
    
    private YamlReadwriteSplittingRuleConfiguration alter(final ReadwriteSplittingRuleConfiguration ruleConfig, final AlterReadwriteSplittingRuleStatement sqlStatement) {
        YamlReadwriteSplittingRuleConfiguration alterYamlReadwriteSplittingRuleConfiguration
                = ReadwriteSplittingRuleStatementConverter.convert(sqlStatement);
        YamlReadwriteSplittingRuleConfiguration result = new YamlRuleConfigurationSwapperEngine()
                .swapToYamlRuleConfigurations(Collections.singletonList(ruleConfig)).stream()
                .map(each -> (YamlReadwriteSplittingRuleConfiguration) each).findFirst().get();
        alterYamlReadwriteSplittingRuleConfiguration.getDataSources().keySet()
                .forEach(each -> result.getLoadBalancers().remove(result.getDataSources().get(each).getLoadBalancerName()));
        result.getDataSources().putAll(alterYamlReadwriteSplittingRuleConfiguration.getDataSources());
        result.getLoadBalancers().putAll(alterYamlReadwriteSplittingRuleConfiguration.getLoadBalancers());
        return result;
    }

    private void post(final String schemaName, final Collection<RuleConfiguration> rules) {
        ShardingSphereEventBus.getInstance().post(new RuleConfigurationsAlteredEvent(schemaName, rules));
    }
}
