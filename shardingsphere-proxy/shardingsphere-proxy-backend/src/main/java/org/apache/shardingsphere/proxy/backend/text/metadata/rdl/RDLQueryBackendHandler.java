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

package org.apache.shardingsphere.proxy.backend.text.metadata.rdl;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.rdl.show.impl.ShowShardingRuleStatement;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.rdl.ShowShardingRuleStatementContext;
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
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

/**
 * Backend handler for RDL query.
 */
@RequiredArgsConstructor
public final class RDLQueryBackendHandler implements TextProtocolBackendHandler {
    
    private final BackendConnection backendConnection;
    
    private final SQLStatement sqlStatement;
    
    private Iterator<RuleConfiguration> ruleConfigData;
    
    @Override
    public ResponseHeader execute() {
        SQLStatementContext<?> context = getSQLStatementContext();
        return getResponseHeader(context);
    }
    
    private ResponseHeader execute(final ShowShardingRuleStatementContext context) {
        String schemaName = null == context.getSqlStatement().getSchemaName() ? backendConnection.getSchemaName() : context.getSqlStatement().getSchemaName().getIdentifier().getValue();
        QueryHeader queryHeader = new QueryHeader(schemaName, "", "ShardingRule", "ShardingRule", Types.CHAR, "CHAR", 255, 0, false, false, false, false);
        Optional<RuleConfiguration> ruleConfig = 
                ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations().stream().filter(each -> each instanceof ShardingRuleConfiguration).findAny();
        ruleConfig.ifPresent(shardingSphereRule -> ruleConfigData = Collections.singleton(shardingSphereRule).iterator());
        return new QueryResponseHeader(Collections.singletonList(queryHeader));
    }
    
    private SQLStatementContext<?> getSQLStatementContext() {
        if (sqlStatement instanceof ShowShardingRuleStatement) {
            return new ShowShardingRuleStatementContext((ShowShardingRuleStatement) sqlStatement);
        }
        throw new UnsupportedOperationException(sqlStatement.getClass().getName());
    }
    
    private ResponseHeader getResponseHeader(final SQLStatementContext<?> context) {
        if (context instanceof ShowShardingRuleStatementContext) {
            return execute((ShowShardingRuleStatementContext) context);
        }
        throw new UnsupportedOperationException(context.getClass().getName());
    }
    
    @Override
    public boolean next() {
        return null != ruleConfigData && ruleConfigData.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        RuleConfiguration ruleConfig = ruleConfigData.next();
        YamlRuleConfiguration yamlRuleConfig = new YamlRuleConfigurationSwapperEngine().swapToYamlConfigurations(Collections.singleton(ruleConfig)).iterator().next();
        return Collections.singleton(YamlEngine.marshal(yamlRuleConfig));
    }
}
