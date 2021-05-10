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

import org.apache.shardingsphere.distsql.parser.segment.rdl.ReadwriteSplittingRuleSegment;
import org.apache.shardingsphere.distsql.parser.statement.rdl.AlterReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.governance.core.registry.listener.event.rule.RuleConfigurationsAlteredEvent;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.yaml.config.algorithm.YamlShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.AddReadwriteSplittingRuleDataSourcesExistedException;
import org.apache.shardingsphere.proxy.backend.exception.ReadwriteSplittingRuleDataSourcesNotExistedException;
import org.apache.shardingsphere.proxy.backend.exception.ReadwriteSplittingRuleNotExistedException;
import org.apache.shardingsphere.proxy.backend.exception.ResourceNotExistedException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.SchemaRequiredBackendHandler;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.common.yaml.config.YamlReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.common.yaml.config.rule.YamlReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.common.yaml.converter.AlterReadwriteSplittingRuleStatementConverter;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Alter readwrite-splitting rule backend handler.
 */
public final class AlterReadwriteSplittingRuleBackendHandler extends SchemaRequiredBackendHandler<AlterReadwriteSplittingRuleStatement> {

    public AlterReadwriteSplittingRuleBackendHandler(final AlterReadwriteSplittingRuleStatement sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
    }
    
    @Override
    public ResponseHeader execute(final String schemaName, final AlterReadwriteSplittingRuleStatement sqlStatement) {
        Optional<ReadwriteSplittingRuleConfiguration> ruleConfig = ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations().stream()
                .filter(each -> each instanceof ReadwriteSplittingRuleConfiguration).map(each -> (ReadwriteSplittingRuleConfiguration) each).findFirst();
        if (!ruleConfig.isPresent()) {
            throw new ReadwriteSplittingRuleNotExistedException();
        }
        check(ruleConfig.get(), sqlStatement, schemaName);
        YamlReadwriteSplittingRuleConfiguration alterConfig = alter(ruleConfig.get(), sqlStatement);
        YamlReadwriteSplittingRuleConfiguration addConfig = add(alterConfig, sqlStatement);
        Collection<RuleConfiguration> rules = new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(Collections.singleton(addConfig));
        post(schemaName, rules);
        return new UpdateResponseHeader(sqlStatement);
    }
    
    private void check(final ReadwriteSplittingRuleConfiguration ruleConfig, final AlterReadwriteSplittingRuleStatement statement, final String schemaName) {
        checkAddDataSourceExist(ruleConfig, statement);
        checkModifyDataSourceNotExist(ruleConfig, statement);
        checkResourceExist(statement, schemaName);
    }
    
    private void checkAddDataSourceExist(final ReadwriteSplittingRuleConfiguration ruleConfig, final AlterReadwriteSplittingRuleStatement statement) {
        Set<String> existReadwriteSplittingDataSourceNames = ruleConfig.getDataSources().stream().map(ReadwriteSplittingDataSourceRuleConfiguration::getName).collect(Collectors.toSet());
        Collection<String> addExistReplicaQueryDataSourceNames = statement.getAddReadwriteSplittingRules().stream().map(ReadwriteSplittingRuleSegment::getName)
                .filter(existReadwriteSplittingDataSourceNames::contains).collect(Collectors.toList());
        if (!addExistReplicaQueryDataSourceNames.isEmpty()) {
            throw new AddReadwriteSplittingRuleDataSourcesExistedException(addExistReplicaQueryDataSourceNames);
        }
    }
    
    private void checkResourceExist(final AlterReadwriteSplittingRuleStatement statement, final String schemaName) {
        List<String> resources = new LinkedList<>();
        for (ReadwriteSplittingRuleSegment each: statement.getModifyReadwriteSplittingRules()) {
            resources.add(each.getWriteDataSource());
            resources.addAll(each.getReadDataSources());
        }
        for (ReadwriteSplittingRuleSegment each: statement.getAddReadwriteSplittingRules()) {
            resources.add(each.getWriteDataSource());
            resources.addAll(each.getReadDataSources());
        }
        Map<String, DataSource> dataSourceMap = ProxyContext.getInstance().getMetaData(schemaName).getResource().getDataSources();
        Collection<String> notExistResource = resources.stream().filter(each -> !dataSourceMap.containsKey(each)).collect(Collectors.toList());
        if (!notExistResource.isEmpty()) {
            throw new ResourceNotExistedException(notExistResource);
        }
    }
    
    private void checkModifyDataSourceNotExist(final ReadwriteSplittingRuleConfiguration ruleConfig, final AlterReadwriteSplittingRuleStatement statement) {
        Set<String> existReplicaQueryDataSourceNames = ruleConfig.getDataSources().stream().map(ReadwriteSplittingDataSourceRuleConfiguration::getName).collect(Collectors.toSet());
        Collection<String> addExistReplicaQueryDataSourceNames = statement.getModifyReadwriteSplittingRules().stream().map(ReadwriteSplittingRuleSegment::getName)
                .filter(each -> !existReplicaQueryDataSourceNames.contains(each)).collect(Collectors.toList());
        if (!addExistReplicaQueryDataSourceNames.isEmpty()) {
            throw new ReadwriteSplittingRuleDataSourcesNotExistedException(addExistReplicaQueryDataSourceNames);
        }
    }
    
    private YamlReadwriteSplittingRuleConfiguration alter(final ReadwriteSplittingRuleConfiguration ruleConfig, final AlterReadwriteSplittingRuleStatement statement) {
        YamlReadwriteSplittingRuleConfiguration result = AlterReadwriteSplittingRuleStatementConverter.convert(statement.getModifyReadwriteSplittingRules());
        for (ReadwriteSplittingDataSourceRuleConfiguration each : ruleConfig.getDataSources()) {
            YamlReadwriteSplittingDataSourceRuleConfiguration alterConfig = result.getDataSources().get(each.getName());
            if (null == alterConfig) {
                YamlReadwriteSplittingDataSourceRuleConfiguration existConfig = new YamlReadwriteSplittingDataSourceRuleConfiguration();
                existConfig.setWriteDataSourceName(each.getWriteDataSourceName());
                existConfig.setReadDataSourceNames(each.getReadDataSourceNames());
                existConfig.setLoadBalancerName(each.getLoadBalancerName());
                existConfig.setProps(ruleConfig.getLoadBalancers().get(each.getLoadBalancerName()).getProps());
                result.getDataSources().put(each.getName(), existConfig);
                if (!ruleConfig.getLoadBalancers().containsKey(each.getLoadBalancerName())) {
                    YamlShardingSphereAlgorithmConfiguration algorithm = new YamlShardingSphereAlgorithmConfiguration();
                    algorithm.setType(each.getLoadBalancerName());
                    algorithm.setProps(ruleConfig.getLoadBalancers().get(each.getLoadBalancerName()).getProps());
                    result.getLoadBalancers().put(each.getLoadBalancerName(), algorithm);
                }
            } else {
                if (null == alterConfig.getLoadBalancerName()) {
                    alterConfig.setLoadBalancerName(each.getLoadBalancerName());
                    alterConfig.setProps(ruleConfig.getLoadBalancers().get(each.getLoadBalancerName()).getProps());
                }
            }
        }
        return result;
    }
    
    private YamlReadwriteSplittingRuleConfiguration add(final YamlReadwriteSplittingRuleConfiguration config, final AlterReadwriteSplittingRuleStatement statement) {
        YamlReadwriteSplittingRuleConfiguration addConfig = AlterReadwriteSplittingRuleStatementConverter.convert(statement.getAddReadwriteSplittingRules());
        config.getDataSources().putAll(addConfig.getDataSources());
        config.getLoadBalancers().putAll(addConfig.getLoadBalancers());
        return config;
    }

    private void post(final String schemaName, final Collection<RuleConfiguration> rules) {
        ShardingSphereEventBus.getInstance().post(new RuleConfigurationsAlteredEvent(schemaName, rules));
    }
}
