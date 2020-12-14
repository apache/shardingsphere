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

package org.apache.shardingsphere.proxy.backend.text.metadata.rdl.query;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.rdl.show.impl.ShowRuleStatement;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.rdl.ShowRuleStatementContext;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeader;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.replicaquery.api.config.ReplicaQueryRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;

import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

/**
 * Backend handler for RDL rule query.
 */
@RequiredArgsConstructor
public final class RDLRuleQueryBackendHandler implements TextProtocolBackendHandler {
    
    private final ShowRuleStatement sqlStatement;
    
    private final BackendConnection backendConnection;
    
    private Iterator<RuleConfiguration> data;
    
    @Override
    public ResponseHeader execute() {
        return getResponseHeader(new ShowRuleStatementContext(sqlStatement));
    }
    
    private ResponseHeader execute(final ShowRuleStatementContext context) {
        String schemaName = null == context.getSqlStatement().getSchemaName() ? backendConnection.getSchemaName() : context.getSqlStatement().getSchemaName().getIdentifier().getValue();
        String ruleType = context.getSqlStatement().getRuleType();
        QueryHeader queryHeader = new QueryHeader(schemaName, "", ruleType, ruleType, Types.CHAR, "CHAR", 255, 0, false, false, false, false);
        data = loadRuleConfiguration(schemaName, ruleType);
        return new QueryResponseHeader(Collections.singletonList(queryHeader));
    }
    
    private Iterator<RuleConfiguration> loadRuleConfiguration(final String schemaName, final String ruleType) {
        Class<? extends RuleConfiguration> ruleConfigurationClass = getRuleConfigurationClass(ruleType);
        Optional<RuleConfiguration> ruleConfig = ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations()
                .stream().filter(each -> ruleConfigurationClass.isAssignableFrom(each.getClass())).findAny();
        return ruleConfig.map(optional -> Collections.singleton(optional).iterator()).orElse(Collections.emptyIterator());
    }
    
    private Class<? extends RuleConfiguration> getRuleConfigurationClass(final String ruleType) {
        switch (ruleType.toUpperCase()) {
            case "SHARDING":
                return ShardingRuleConfiguration.class;
            case "REPLICA_QUERY":
                return ReplicaQueryRuleConfiguration.class;
            case "ENCRYPT":
                return EncryptRuleConfiguration.class;
            case "SHADOW":
                return ShadowRuleConfiguration.class;
            default:
                throw new UnsupportedOperationException(ruleType);
        }
    }
    
    private ResponseHeader getResponseHeader(final SQLStatementContext<?> context) {
        if (context instanceof ShowRuleStatementContext) {
            return execute((ShowRuleStatementContext) context);
        }
        throw new UnsupportedOperationException(context.getClass().getName());
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        RuleConfiguration ruleConfig = data.next();
        YamlRuleConfiguration yamlRuleConfig = new YamlRuleConfigurationSwapperEngine().swapToYamlConfigurations(Collections.singleton(ruleConfig)).iterator().next();
        return Collections.singleton(YamlEngine.marshal(yamlRuleConfig));
    }
}
