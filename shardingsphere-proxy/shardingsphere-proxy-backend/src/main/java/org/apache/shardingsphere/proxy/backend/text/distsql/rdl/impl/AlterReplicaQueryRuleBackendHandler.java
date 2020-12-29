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

import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.AlterReplicaQueryRuleStatement;
import org.apache.shardingsphere.governance.core.event.model.rule.RuleConfigurationsPersistEvent;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.yaml.config.algorithm.YamlShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.ReplicaQueryRuleNotExistedException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.SchemaRequiredBackendHandler;
import org.apache.shardingsphere.replicaquery.api.config.ReplicaQueryRuleConfiguration;
import org.apache.shardingsphere.replicaquery.api.config.rule.ReplicaQueryDataSourceRuleConfiguration;
import org.apache.shardingsphere.replicaquery.yaml.config.YamlReplicaQueryRuleConfiguration;
import org.apache.shardingsphere.replicaquery.yaml.config.rule.YamlReplicaQueryDataSourceRuleConfiguration;
import org.apache.shardingsphere.replicaquery.yaml.converter.AlterReplicaQueryRuleStatementConverter;

import java.util.Collection;
import java.util.Collections;
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
        YamlReplicaQueryRuleConfiguration config = AlterReplicaQueryRuleStatementConverter.convert(sqlStatement);
        check(config, schemaName);
        YamlReplicaQueryRuleConfiguration alterConfig = alter(config, schemaName);
        Collection<RuleConfiguration> rules = new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(Collections.singleton(alterConfig));
        post(schemaName, rules);
        return new UpdateResponseHeader(sqlStatement);
    }

    private void check(final YamlReplicaQueryRuleConfiguration config, final String schemaName) {
//        TODO Check whether the database exists
        Optional<ReplicaQueryRuleConfiguration> replicaQueryRuleConfig = ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations().stream()
                .filter(each -> each instanceof ReplicaQueryRuleConfiguration).map(each -> (ReplicaQueryRuleConfiguration) each).findFirst();
        if (!replicaQueryRuleConfig.isPresent()) {
            throw new ReplicaQueryRuleNotExistedException(config.getDataSources().keySet());
        }
        Set<String> existReplicaQueryDatasourceNames = replicaQueryRuleConfig.get().getDataSources().stream().map(each -> each.getName()).collect(Collectors.toSet());
        Collection<String> notExistReplicaQueryDatasourceNames = config.getDataSources().keySet().stream().filter(each -> !existReplicaQueryDatasourceNames.contains(each))
                .collect(Collectors.toList());
        if (!notExistReplicaQueryDatasourceNames.isEmpty()) {
            throw new ReplicaQueryRuleNotExistedException(notExistReplicaQueryDatasourceNames);
        }
    }

    private YamlReplicaQueryRuleConfiguration alter(final YamlReplicaQueryRuleConfiguration config, final String schemaName) {
        YamlReplicaQueryRuleConfiguration result = config;
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

    private void post(final String schemaName, final Collection<RuleConfiguration> rules) {
        ShardingSphereEventBus.getInstance().post(new RuleConfigurationsPersistEvent(schemaName, rules));
    }
}
