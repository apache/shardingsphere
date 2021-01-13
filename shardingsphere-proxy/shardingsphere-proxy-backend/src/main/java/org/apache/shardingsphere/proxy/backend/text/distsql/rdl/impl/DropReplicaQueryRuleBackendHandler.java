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
import org.apache.shardingsphere.governance.core.event.model.rule.RuleConfigurationsPersistEvent;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.ReplicaQueryRuleDataSourcesNotExistedException;
import org.apache.shardingsphere.proxy.backend.exception.ReplicaQueryRuleNotExistedException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.SchemaRequiredBackendHandler;
import org.apache.shardingsphere.replicaquery.api.config.ReplicaQueryRuleConfiguration;
import org.apache.shardingsphere.replicaquery.yaml.config.YamlReplicaQueryRuleConfiguration;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Drop replica query rule backend handler.
 */
public final class DropReplicaQueryRuleBackendHandler extends SchemaRequiredBackendHandler<DropReplicaQueryRuleStatement> {

    public DropReplicaQueryRuleBackendHandler(final DropReplicaQueryRuleStatement sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
    }
    
    @Override
    public ResponseHeader execute(final String schemaName, final DropReplicaQueryRuleStatement sqlStatement) {
        Collection<String> ruleNames = sqlStatement.getRuleNames();
        check(schemaName, ruleNames);
        Collection<RuleConfiguration> rules = drop(schemaName, ruleNames);
        post(schemaName, rules);
        return new UpdateResponseHeader(sqlStatement);
    }
    
    private void check(final String schemaName, final Collection<String> ruleNames) {
        Optional<ReplicaQueryRuleConfiguration> replicaQueryRuleConfig = ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations().stream()
                .filter(each -> each instanceof ReplicaQueryRuleConfiguration).map(each -> (ReplicaQueryRuleConfiguration) each).findFirst();
        if (!replicaQueryRuleConfig.isPresent()) {
            throw new ReplicaQueryRuleNotExistedException();
        }
        Collection<String> replicaQueryNames = replicaQueryRuleConfig.get().getDataSources().stream().map(each -> each.getName()).collect(Collectors.toList());
        Collection<String> notExistedRuleNames = ruleNames.stream().filter(each -> !replicaQueryNames.contains(each)).collect(Collectors.toList());
        if (!notExistedRuleNames.isEmpty()) {
            throw new ReplicaQueryRuleDataSourcesNotExistedException(notExistedRuleNames);
        }
    }

    private Collection<RuleConfiguration> drop(final String schemaName, final Collection<String> ruleNames) {
        Collection<RuleConfiguration> replicaQueryRuleConfig = ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations().stream()
                .filter(each -> each instanceof ReplicaQueryRuleConfiguration).map(each -> (ReplicaQueryRuleConfiguration) each).collect(Collectors.toList());
        YamlReplicaQueryRuleConfiguration yamlConfig = (YamlReplicaQueryRuleConfiguration) new YamlRuleConfigurationSwapperEngine().swapToYamlConfigurations(replicaQueryRuleConfig).stream()
                .findFirst().get();
        for (String each : ruleNames) {
            yamlConfig.getDataSources().remove(each);
        }
        if (yamlConfig.getDataSources().isEmpty()) {
            return new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(Collections.EMPTY_LIST);
        } else {
            return new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(Collections.singleton(yamlConfig));
        }
    }
    
    private void post(final String schemaName, final Collection<RuleConfiguration> rules) {
        ShardingSphereEventBus.getInstance().post(new RuleConfigurationsPersistEvent(schemaName, rules));
    }
}
