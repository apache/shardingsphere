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

package org.apache.shardingsphere.proxy.backend.text.distsql.rql.impl;

import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowRuleStatement;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeader;
import org.apache.shardingsphere.proxy.backend.text.SchemaRequiredBackendHandler;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;

import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

/**
 * Backend handler for show rules.
 */
public final class RuleQueryBackendHandler extends SchemaRequiredBackendHandler<ShowRuleStatement> {
    
    private Iterator<RuleConfiguration> data;
    
    public RuleQueryBackendHandler(final ShowRuleStatement sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
    }
    
    @Override
    protected ResponseHeader execute(final String schemaName, final ShowRuleStatement sqlStatement) {
        String ruleType = sqlStatement.getRuleType();
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
                return ReadwriteSplittingRuleConfiguration.class;
            case "ENCRYPT":
                return EncryptRuleConfiguration.class;
            case "SHADOW":
                return ShadowRuleConfiguration.class;
            default:
                throw new UnsupportedOperationException(ruleType);
        }
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        RuleConfiguration ruleConfig = data.next();
        YamlRuleConfiguration yamlRuleConfig = new YamlRuleConfigurationSwapperEngine().swapToYamlRuleConfigurations(Collections.singleton(ruleConfig)).iterator().next();
        return Collections.singleton(YamlEngine.marshal(yamlRuleConfig));
    }
}
