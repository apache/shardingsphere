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

import org.apache.shardingsphere.distsql.parser.segment.rdl.ReplicaQueryRuleSegment;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.AlterReplicaQueryRuleStatement;
import org.apache.shardingsphere.governance.core.event.model.rule.RuleConfigurationsPersistEvent;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.yaml.config.algorithm.YamlShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.AddReplicaQueryRuleDataSourcesExistedException;
import org.apache.shardingsphere.proxy.backend.exception.ReplicaQueryRuleDataSourcesNotExistedException;
import org.apache.shardingsphere.proxy.backend.exception.ReplicaQueryRuleNotExistedException;
import org.apache.shardingsphere.proxy.backend.exception.ResourceNotExistedException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.SchemaRequiredBackendHandler;
import org.apache.shardingsphere.replicaquery.api.config.ReplicaQueryRuleConfiguration;
import org.apache.shardingsphere.replicaquery.api.config.rule.ReplicaQueryDataSourceRuleConfiguration;
import org.apache.shardingsphere.replicaquery.yaml.config.YamlReplicaQueryRuleConfiguration;
import org.apache.shardingsphere.replicaquery.yaml.config.rule.YamlReplicaQueryDataSourceRuleConfiguration;
import org.apache.shardingsphere.replicaquery.yaml.converter.AlterReplicaQueryRuleStatementConverter;

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
 * Alter replica query rule backend handler.
 */
public final class AlterReplicaQueryRuleBackendHandler extends SchemaRequiredBackendHandler<AlterReplicaQueryRuleStatement> {

    public AlterReplicaQueryRuleBackendHandler(final AlterReplicaQueryRuleStatement sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
    }
    
    @Override
    public ResponseHeader execute(final String schemaName, final AlterReplicaQueryRuleStatement sqlStatement) {
        check(sqlStatement, schemaName);
        YamlReplicaQueryRuleConfiguration alterConfig = alter(sqlStatement, schemaName);
        YamlReplicaQueryRuleConfiguration addConfig = add(alterConfig, sqlStatement, schemaName);
        Collection<RuleConfiguration> rules = new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(Collections.singleton(addConfig));
        post(schemaName, rules);
        return new UpdateResponseHeader(sqlStatement);
    }
    
    private void check(final AlterReplicaQueryRuleStatement statement, final String schemaName) {
        checkRuleExist(schemaName);
        checkAddDataSourceExist(statement, schemaName);
        checkModifyDataSourceNotExist(statement, schemaName);
        checkResourceExist(statement, schemaName);
    }
    
    private void checkRuleExist(final String schemaName) {
        Optional<ReplicaQueryRuleConfiguration> replicaQueryRuleConfig = ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations().stream()
                .filter(each -> each instanceof ReplicaQueryRuleConfiguration).map(each -> (ReplicaQueryRuleConfiguration) each).findFirst();
        if (!replicaQueryRuleConfig.isPresent()) {
            throw new ReplicaQueryRuleNotExistedException();
        }
    }
    
    private void checkAddDataSourceExist(final AlterReplicaQueryRuleStatement statement, final String schemaName) {
        Optional<ReplicaQueryRuleConfiguration> replicaQueryRuleConfig = ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations().stream()
                .filter(each -> each instanceof ReplicaQueryRuleConfiguration).map(each -> (ReplicaQueryRuleConfiguration) each).findFirst();
        Set<String> existReplicaQueryDataSourceNames = replicaQueryRuleConfig.get().getDataSources().stream().map(each -> each.getName()).collect(Collectors.toSet());
        Collection<String> addExistReplicaQueryDataSourceNames = statement.getAddReplicaQueryRules().stream().map(each -> each.getName())
                .filter(each -> existReplicaQueryDataSourceNames.contains(each)).collect(Collectors.toList());
        if (!addExistReplicaQueryDataSourceNames.isEmpty()) {
            throw new AddReplicaQueryRuleDataSourcesExistedException(addExistReplicaQueryDataSourceNames);
        }
    }
    
    private void checkResourceExist(final AlterReplicaQueryRuleStatement statement, final String schemaName) {
        List<String> resources = new LinkedList<>();
        for (ReplicaQueryRuleSegment each: statement.getModifyReplicaQueryRules()) {
            resources.add(each.getPrimaryDataSource());
            resources.addAll(each.getReplicaDataSources());
        }
        for (ReplicaQueryRuleSegment each: statement.getAddReplicaQueryRules()) {
            resources.add(each.getPrimaryDataSource());
            resources.addAll(each.getReplicaDataSources());
        }
        Map<String, DataSource> dataSourceMap = ProxyContext.getInstance().getMetaData(schemaName).getResource().getDataSources();
        Collection<String> notExistResource = resources.stream().filter(each -> !dataSourceMap.containsKey(each)).collect(Collectors.toList());
        if (!notExistResource.isEmpty()) {
            throw new ResourceNotExistedException(notExistResource);
        }
    }
    
    private void checkModifyDataSourceNotExist(final AlterReplicaQueryRuleStatement statement, final String schemaName) {
        Optional<ReplicaQueryRuleConfiguration> replicaQueryRuleConfig = ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations().stream()
                .filter(each -> each instanceof ReplicaQueryRuleConfiguration).map(each -> (ReplicaQueryRuleConfiguration) each).findFirst();
        Set<String> existReplicaQueryDataSourceNames = replicaQueryRuleConfig.get().getDataSources().stream().map(each -> each.getName()).collect(Collectors.toSet());
        Collection<String> addExistReplicaQueryDataSourceNames = statement.getModifyReplicaQueryRules().stream().map(each -> each.getName())
                .filter(each -> !existReplicaQueryDataSourceNames.contains(each)).collect(Collectors.toList());
        if (!addExistReplicaQueryDataSourceNames.isEmpty()) {
            throw new ReplicaQueryRuleDataSourcesNotExistedException(addExistReplicaQueryDataSourceNames);
        }
    }
    
    private YamlReplicaQueryRuleConfiguration alter(final AlterReplicaQueryRuleStatement statement, final String schemaName) {
        YamlReplicaQueryRuleConfiguration result = AlterReplicaQueryRuleStatementConverter.convert(statement.getModifyReplicaQueryRules());
        ReplicaQueryRuleConfiguration replicaQueryRuleConfig = ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations().stream()
                .filter(each -> each instanceof ReplicaQueryRuleConfiguration).map(each -> (ReplicaQueryRuleConfiguration) each).findFirst().get();
        for (ReplicaQueryDataSourceRuleConfiguration each : replicaQueryRuleConfig.getDataSources()) {
            YamlReplicaQueryDataSourceRuleConfiguration alterConfig = result.getDataSources().get(each.getName());
            if (null == alterConfig) {
                YamlReplicaQueryDataSourceRuleConfiguration existConfig = new YamlReplicaQueryDataSourceRuleConfiguration();
                existConfig.setName(each.getName());
                existConfig.setPrimaryDataSourceName(each.getPrimaryDataSourceName());
                existConfig.setReplicaDataSourceNames(each.getReplicaDataSourceNames());
                existConfig.setLoadBalancerName(each.getLoadBalancerName());
                existConfig.setProps(replicaQueryRuleConfig.getLoadBalancers().get(each.getLoadBalancerName()).getProps());
                result.getDataSources().put(each.getName(), existConfig);
                if (!replicaQueryRuleConfig.getLoadBalancers().containsKey(each.getLoadBalancerName())) {
                    YamlShardingSphereAlgorithmConfiguration algorithm = new YamlShardingSphereAlgorithmConfiguration();
                    algorithm.setType(each.getLoadBalancerName());
                    algorithm.setProps(replicaQueryRuleConfig.getLoadBalancers().get(each.getLoadBalancerName()).getProps());
                    result.getLoadBalancers().put(each.getLoadBalancerName(), algorithm);
                }
            } else {
                if (null == alterConfig.getLoadBalancerName()) {
                    alterConfig.setLoadBalancerName(each.getLoadBalancerName());
                    alterConfig.setProps(replicaQueryRuleConfig.getLoadBalancers().get(each.getLoadBalancerName()).getProps());
                }
            }
        }
        return result;
    }
    
    private YamlReplicaQueryRuleConfiguration add(final YamlReplicaQueryRuleConfiguration config, final AlterReplicaQueryRuleStatement statement, final String schemaName) {
        YamlReplicaQueryRuleConfiguration result = config;
        YamlReplicaQueryRuleConfiguration addConfig = AlterReplicaQueryRuleStatementConverter.convert(statement.getAddReplicaQueryRules());
        result.getDataSources().putAll(addConfig.getDataSources());
        result.getLoadBalancers().putAll(addConfig.getLoadBalancers());
        return result;
    }

    private void post(final String schemaName, final Collection<RuleConfiguration> rules) {
        ShardingSphereEventBus.getInstance().post(new RuleConfigurationsPersistEvent(schemaName, rules));
    }
}
