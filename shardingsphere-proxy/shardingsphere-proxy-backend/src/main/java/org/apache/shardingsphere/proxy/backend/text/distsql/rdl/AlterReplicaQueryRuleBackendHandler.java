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

package org.apache.shardingsphere.proxy.backend.text.distsql.rdl;

import org.apache.shardingsphere.distsql.parser.segment.rdl.ReplicaQueryRuleSegment;
import org.apache.shardingsphere.distsql.parser.statement.rdl.AlterReplicaQueryRuleStatement;
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
        Optional<ReplicaQueryRuleConfiguration> replicaQueryRuleConfig = ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations().stream()
                .filter(each -> each instanceof ReplicaQueryRuleConfiguration).map(each -> (ReplicaQueryRuleConfiguration) each).findFirst();
        if (!replicaQueryRuleConfig.isPresent()) {
            throw new ReplicaQueryRuleNotExistedException();
        }
        check(replicaQueryRuleConfig.get(), sqlStatement, schemaName);
        YamlReplicaQueryRuleConfiguration alterConfig = alter(replicaQueryRuleConfig.get(), sqlStatement);
        YamlReplicaQueryRuleConfiguration addConfig = add(alterConfig, sqlStatement);
        Collection<RuleConfiguration> rules = new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(Collections.singleton(addConfig));
        post(schemaName, rules);
        return new UpdateResponseHeader(sqlStatement);
    }
    
    private void check(final ReplicaQueryRuleConfiguration replicaQueryRuleConfig, final AlterReplicaQueryRuleStatement statement, final String schemaName) {
        checkAddDataSourceExist(replicaQueryRuleConfig, statement);
        checkModifyDataSourceNotExist(replicaQueryRuleConfig, statement);
        checkResourceExist(statement, schemaName);
    }
    
    private void checkAddDataSourceExist(final ReplicaQueryRuleConfiguration replicaQueryRuleConfig, final AlterReplicaQueryRuleStatement statement) {
        Set<String> existReplicaQueryDataSourceNames = replicaQueryRuleConfig.getDataSources().stream().map(ReplicaQueryDataSourceRuleConfiguration::getName).collect(Collectors.toSet());
        Collection<String> addExistReplicaQueryDataSourceNames = statement.getAddReplicaQueryRules().stream().map(ReplicaQueryRuleSegment::getName)
                .filter(existReplicaQueryDataSourceNames::contains).collect(Collectors.toList());
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
    
    private void checkModifyDataSourceNotExist(final ReplicaQueryRuleConfiguration replicaQueryRuleConfig, final AlterReplicaQueryRuleStatement statement) {
        Set<String> existReplicaQueryDataSourceNames = replicaQueryRuleConfig.getDataSources().stream().map(ReplicaQueryDataSourceRuleConfiguration::getName).collect(Collectors.toSet());
        Collection<String> addExistReplicaQueryDataSourceNames = statement.getModifyReplicaQueryRules().stream().map(ReplicaQueryRuleSegment::getName)
                .filter(each -> !existReplicaQueryDataSourceNames.contains(each)).collect(Collectors.toList());
        if (!addExistReplicaQueryDataSourceNames.isEmpty()) {
            throw new ReplicaQueryRuleDataSourcesNotExistedException(addExistReplicaQueryDataSourceNames);
        }
    }
    
    private YamlReplicaQueryRuleConfiguration alter(final ReplicaQueryRuleConfiguration replicaQueryRuleConfig, final AlterReplicaQueryRuleStatement statement) {
        YamlReplicaQueryRuleConfiguration result = AlterReplicaQueryRuleStatementConverter.convert(statement.getModifyReplicaQueryRules());
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
    
    private YamlReplicaQueryRuleConfiguration add(final YamlReplicaQueryRuleConfiguration config, final AlterReplicaQueryRuleStatement statement) {
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
