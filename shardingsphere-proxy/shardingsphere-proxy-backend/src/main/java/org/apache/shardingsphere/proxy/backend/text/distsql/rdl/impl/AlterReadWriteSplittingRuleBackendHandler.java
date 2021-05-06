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

import org.apache.shardingsphere.distsql.parser.segment.rdl.ReadWriteSplittingRuleSegment;
import org.apache.shardingsphere.distsql.parser.statement.rdl.AlterReadWriteSplittingRuleStatement;
import org.apache.shardingsphere.governance.core.registry.listener.event.rule.RuleConfigurationsAlteredEvent;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.yaml.config.algorithm.YamlShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.AddReadWriteSplittingRuleDataSourcesExistedException;
import org.apache.shardingsphere.proxy.backend.exception.ReadWriteSplittingRuleDataSourcesNotExistedException;
import org.apache.shardingsphere.proxy.backend.exception.ReadWriteSplittingRuleNotExistedException;
import org.apache.shardingsphere.proxy.backend.exception.ResourceNotExistedException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.SchemaRequiredBackendHandler;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadWriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.common.yaml.config.YamlReadWriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.common.yaml.config.rule.YamlReadWriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.common.yaml.converter.AlterReadWriteSplittingRuleStatementConverter;

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
public final class AlterReadWriteSplittingRuleBackendHandler extends SchemaRequiredBackendHandler<AlterReadWriteSplittingRuleStatement> {

    public AlterReadWriteSplittingRuleBackendHandler(final AlterReadWriteSplittingRuleStatement sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
    }
    
    @Override
    public ResponseHeader execute(final String schemaName, final AlterReadWriteSplittingRuleStatement sqlStatement) {
        Optional<ReadwriteSplittingRuleConfiguration> ruleConfig = ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations().stream()
                .filter(each -> each instanceof ReadwriteSplittingRuleConfiguration).map(each -> (ReadwriteSplittingRuleConfiguration) each).findFirst();
        if (!ruleConfig.isPresent()) {
            throw new ReadWriteSplittingRuleNotExistedException();
        }
        check(ruleConfig.get(), sqlStatement, schemaName);
        YamlReadWriteSplittingRuleConfiguration alterConfig = alter(ruleConfig.get(), sqlStatement);
        YamlReadWriteSplittingRuleConfiguration addConfig = add(alterConfig, sqlStatement);
        Collection<RuleConfiguration> rules = new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(Collections.singleton(addConfig));
        post(schemaName, rules);
        return new UpdateResponseHeader(sqlStatement);
    }
    
    private void check(final ReadwriteSplittingRuleConfiguration ruleConfig, final AlterReadWriteSplittingRuleStatement statement, final String schemaName) {
        checkAddDataSourceExist(ruleConfig, statement);
        checkModifyDataSourceNotExist(ruleConfig, statement);
        checkResourceExist(statement, schemaName);
    }
    
    private void checkAddDataSourceExist(final ReadwriteSplittingRuleConfiguration ruleConfig, final AlterReadWriteSplittingRuleStatement statement) {
        Set<String> existReadWriteSplittingDataSourceNames = ruleConfig.getDataSources().stream().map(ReadWriteSplittingDataSourceRuleConfiguration::getName).collect(Collectors.toSet());
        Collection<String> addExistReplicaQueryDataSourceNames = statement.getAddReplicaQueryRules().stream().map(ReadWriteSplittingRuleSegment::getName)
                .filter(existReadWriteSplittingDataSourceNames::contains).collect(Collectors.toList());
        if (!addExistReplicaQueryDataSourceNames.isEmpty()) {
            throw new AddReadWriteSplittingRuleDataSourcesExistedException(addExistReplicaQueryDataSourceNames);
        }
    }
    
    private void checkResourceExist(final AlterReadWriteSplittingRuleStatement statement, final String schemaName) {
        List<String> resources = new LinkedList<>();
        for (ReadWriteSplittingRuleSegment each: statement.getModifyReplicaQueryRules()) {
            resources.add(each.getWriteDataSource());
            resources.addAll(each.getReadDataSources());
        }
        for (ReadWriteSplittingRuleSegment each: statement.getAddReplicaQueryRules()) {
            resources.add(each.getWriteDataSource());
            resources.addAll(each.getReadDataSources());
        }
        Map<String, DataSource> dataSourceMap = ProxyContext.getInstance().getMetaData(schemaName).getResource().getDataSources();
        Collection<String> notExistResource = resources.stream().filter(each -> !dataSourceMap.containsKey(each)).collect(Collectors.toList());
        if (!notExistResource.isEmpty()) {
            throw new ResourceNotExistedException(notExistResource);
        }
    }
    
    private void checkModifyDataSourceNotExist(final ReadwriteSplittingRuleConfiguration ruleConfig, final AlterReadWriteSplittingRuleStatement statement) {
        Set<String> existReplicaQueryDataSourceNames = ruleConfig.getDataSources().stream().map(ReadWriteSplittingDataSourceRuleConfiguration::getName).collect(Collectors.toSet());
        Collection<String> addExistReplicaQueryDataSourceNames = statement.getModifyReplicaQueryRules().stream().map(ReadWriteSplittingRuleSegment::getName)
                .filter(each -> !existReplicaQueryDataSourceNames.contains(each)).collect(Collectors.toList());
        if (!addExistReplicaQueryDataSourceNames.isEmpty()) {
            throw new ReadWriteSplittingRuleDataSourcesNotExistedException(addExistReplicaQueryDataSourceNames);
        }
    }
    
    private YamlReadWriteSplittingRuleConfiguration alter(final ReadwriteSplittingRuleConfiguration ruleConfig, final AlterReadWriteSplittingRuleStatement statement) {
        YamlReadWriteSplittingRuleConfiguration result = AlterReadWriteSplittingRuleStatementConverter.convert(statement.getModifyReplicaQueryRules());
        for (ReadWriteSplittingDataSourceRuleConfiguration each : ruleConfig.getDataSources()) {
            YamlReadWriteSplittingDataSourceRuleConfiguration alterConfig = result.getDataSources().get(each.getName());
            if (null == alterConfig) {
                YamlReadWriteSplittingDataSourceRuleConfiguration existConfig = new YamlReadWriteSplittingDataSourceRuleConfiguration();
                existConfig.setName(each.getName());
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
    
    private YamlReadWriteSplittingRuleConfiguration add(final YamlReadWriteSplittingRuleConfiguration config, final AlterReadWriteSplittingRuleStatement statement) {
        YamlReadWriteSplittingRuleConfiguration addConfig = AlterReadWriteSplittingRuleStatementConverter.convert(statement.getAddReplicaQueryRules());
        config.getDataSources().putAll(addConfig.getDataSources());
        config.getLoadBalancers().putAll(addConfig.getLoadBalancers());
        return config;
    }

    private void post(final String schemaName, final Collection<RuleConfiguration> rules) {
        ShardingSphereEventBus.getInstance().post(new RuleConfigurationsAlteredEvent(schemaName, rules));
    }
}
