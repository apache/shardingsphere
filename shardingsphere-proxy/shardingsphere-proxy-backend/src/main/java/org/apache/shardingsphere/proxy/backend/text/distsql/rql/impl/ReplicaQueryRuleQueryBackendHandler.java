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

import com.google.gson.Gson;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowRuleStatement;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeader;
import org.apache.shardingsphere.proxy.backend.text.SchemaRequiredBackendHandler;
import org.apache.shardingsphere.replicaquery.api.config.ReplicaQueryRuleConfiguration;
import org.apache.shardingsphere.replicaquery.api.config.rule.ReplicaQueryDataSourceRuleConfiguration;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Backend handler for show replica query rule.
 */
public final class ReplicaQueryRuleQueryBackendHandler extends SchemaRequiredBackendHandler<ShowRuleStatement> {
    
    private Iterator<ReplicaQueryDataSourceRuleConfiguration> data;
    
    private final String schema;
    
    public ReplicaQueryRuleQueryBackendHandler(final ShowRuleStatement sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
        if (sqlStatement.getSchema().isPresent()) {
            schema = sqlStatement.getSchema().get().getIdentifier().getValue();
        } else {
            schema = backendConnection.getSchemaName();
        }
    }
    
    @Override
    protected ResponseHeader execute(final String schemaName, final ShowRuleStatement sqlStatement) {
        loadRuleConfiguration(schema);
        return new QueryResponseHeader(getQueryHeader());
    }
    
    private void loadRuleConfiguration(final String schemaName) {
        Optional<ReplicaQueryRuleConfiguration> ruleConfig = ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations()
                .stream().filter(each -> each instanceof ReplicaQueryRuleConfiguration).map(each -> (ReplicaQueryRuleConfiguration) each).findAny();
        if (ruleConfig.isPresent()) {
            data = ruleConfig.get().getDataSources().iterator();
        } else {
            data = Collections.emptyIterator();
        }
    }
    
    private List<QueryHeader> getQueryHeader() {
        List<QueryHeader> result = new LinkedList();
        result.add(new QueryHeader(schema, "", "name", "name", Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        result.add(new QueryHeader(schema, "", "primaryDataSourceName", "primaryDataSourceName", Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        result.add(new QueryHeader(schema, "", "replicaDataSourceNames", "replicaDataSourceNames", Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        result.add(new QueryHeader(schema, "", "loadBalancerName", "loadBalancerName", Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        return result;
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        ReplicaQueryDataSourceRuleConfiguration ruleConfig = data.next();
        String name = ruleConfig.getName();
        String primaryDataSourceName = ruleConfig.getPrimaryDataSourceName();
        String replicaDataSourceNames = (new Gson()).toJson(ruleConfig.getReplicaDataSourceNames());
        String loadBalancerName = ruleConfig.getLoadBalancerName();
        return Arrays.asList(name, primaryDataSourceName, replicaDataSourceNames, loadBalancerName);
    }
}
