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

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.impl.ShowDatabaseDiscoveryRulesStatement;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeader;
import org.apache.shardingsphere.proxy.backend.text.SchemaRequiredBackendHandler;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Backend handler for show database discovery rules.
 */
public final class DatabaseDiscoveryRulesQueryBackendHandler extends SchemaRequiredBackendHandler<ShowDatabaseDiscoveryRulesStatement> {

    private Iterator<DatabaseDiscoveryDataSourceRuleConfiguration> data;

    private Map<String, ShardingSphereAlgorithmConfiguration> discoverTypes;

    public DatabaseDiscoveryRulesQueryBackendHandler(final ShowDatabaseDiscoveryRulesStatement sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
    }
    
    @Override
    protected ResponseHeader execute(final String schemaName, final ShowDatabaseDiscoveryRulesStatement sqlStatement) {
        loadRuleConfiguration(schemaName);
        return new QueryResponseHeader(getQueryHeader(schemaName));
    }
    
    private void loadRuleConfiguration(final String schemaName) {
        Optional<DatabaseDiscoveryRuleConfiguration> ruleConfig = ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations()
                .stream().filter(each -> each instanceof DatabaseDiscoveryRuleConfiguration).map(each -> (DatabaseDiscoveryRuleConfiguration) each).findAny();
        data = ruleConfig.map(optional -> optional.getDataSources().iterator()).orElse(Collections.emptyIterator());
        discoverTypes = ruleConfig.map(DatabaseDiscoveryRuleConfiguration::getDiscoveryTypes).orElse(Maps.newHashMap());
    }
    
    private List<QueryHeader> getQueryHeader(final String schemaName) {
        List<QueryHeader> result = new LinkedList<>();
        result.add(new QueryHeader(schemaName, "", "name", "name", Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        result.add(new QueryHeader(schemaName, "", "dataSourceNames", "dataSourceNames", Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        result.add(new QueryHeader(schemaName, "", "discoverType", "discoverType", Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        result.add(new QueryHeader(schemaName, "", "discoverProps", "discoverProps", Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        return result;
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        DatabaseDiscoveryDataSourceRuleConfiguration ruleConfig = data.next();
        Properties discoverProps = Objects.nonNull(discoverTypes.get(ruleConfig.getDiscoveryTypeName())) 
                ? discoverTypes.get(ruleConfig.getDiscoveryTypeName()).getProps() : null;
        return Arrays.asList(ruleConfig.getName(), Joiner.on(",").join(ruleConfig.getDataSourceNames()),
                discoverTypes.get(ruleConfig.getDiscoveryTypeName()).getType(),
                Objects.nonNull(discoverProps) ? Joiner.on(",").join(discoverProps.entrySet().stream()
                        .map(each -> Joiner.on("=").join(each.getKey(), each.getValue())).collect(Collectors.toList())) : "");
    }
}
