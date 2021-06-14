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

import org.apache.shardingsphere.distsql.parser.statement.rql.show.impl.ShowShardingBindingTableRulesStatement;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeader;
import org.apache.shardingsphere.proxy.backend.text.SchemaRequiredBackendHandler;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Backend handler for show sharding binding table rules.
 */
public final class ShardingBindingTableRulesQueryBackendHandler extends SchemaRequiredBackendHandler<ShowShardingBindingTableRulesStatement> {

    private Iterator<String> data;

    private final String schema;

    public ShardingBindingTableRulesQueryBackendHandler(final ShowShardingBindingTableRulesStatement sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
        schema = sqlStatement.getSchema().isPresent() ? sqlStatement.getSchema().get().getIdentifier().getValue() : backendConnection.getSchemaName();
    }
    
    @Override
    protected ResponseHeader execute(final String schemaName, final ShowShardingBindingTableRulesStatement sqlStatement) {
        List<QueryHeader> queryHeader = getQueryHeader();
        data = loadRuleConfiguration();
        return new QueryResponseHeader(queryHeader);
    }

    private List<QueryHeader> getQueryHeader() {
        List<QueryHeader> result = new LinkedList<>();
        result.add(new QueryHeader(schema, "", "shardingBindingTables", "shardingBindingTables", Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        return result;
    }
    
    private Iterator<String> loadRuleConfiguration() {
        Collection<String> result = new LinkedList<>();
        Optional<ShardingRuleConfiguration> shardingRuleConfiguration = ProxyContext.getInstance().getMetaData(schema).getRuleMetaData().getConfigurations()
                .stream().filter(each -> each instanceof ShardingRuleConfiguration).map(each -> (ShardingRuleConfiguration) each).findFirst();
        if (shardingRuleConfiguration.isPresent()) {
            result = shardingRuleConfiguration.get().getBindingTableGroups();
        }
        return result.iterator();
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        return Arrays.asList(data.next());
    }
}
