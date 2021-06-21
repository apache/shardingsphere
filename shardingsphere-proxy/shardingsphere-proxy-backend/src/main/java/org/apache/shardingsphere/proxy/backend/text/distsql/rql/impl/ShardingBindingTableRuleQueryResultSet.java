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

import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.text.distsql.rql.RuleQueryResultSet;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingBindingTableRulesStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Result set for show sharding binding table rules.
 */
public final class ShardingBindingTableRuleQueryResultSet implements RuleQueryResultSet<ShowShardingBindingTableRulesStatement> {
    
    private Iterator<String> data;
    
    private final String schema;
    
    public ShardingBindingTableRuleQueryResultSet(final ShowShardingBindingTableRulesStatement sqlStatement, final BackendConnection backendConnection) {
        schema = sqlStatement.getSchema().isPresent() ? sqlStatement.getSchema().get().getIdentifier().getValue() : backendConnection.getSchemaName();
    }
    
    @Override
    public void init(final String schemaName, final ShowShardingBindingTableRulesStatement sqlStatement) {
        data = loadRuleConfiguration();
    }
    
    private Iterator<String> loadRuleConfiguration() {
        Collection<String> result = new LinkedList<>();
        Optional<ShardingRuleConfiguration> shardingRuleConfig = ProxyContext.getInstance().getMetaData(schema).getRuleMetaData().getConfigurations()
                .stream().filter(each -> each instanceof ShardingRuleConfiguration).map(each -> (ShardingRuleConfiguration) each).findFirst();
        if (shardingRuleConfig.isPresent()) {
            result = shardingRuleConfig.get().getBindingTableGroups();
        }
        return result.iterator();
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Collections.singleton("shardingBindingTables");
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        return Collections.singleton(data.next());
    }
}
