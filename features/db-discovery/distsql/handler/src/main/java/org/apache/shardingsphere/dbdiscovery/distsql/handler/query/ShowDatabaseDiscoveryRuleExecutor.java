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

package org.apache.shardingsphere.dbdiscovery.distsql.handler.query;

import com.google.gson.Gson;
import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryHeartBeatConfiguration;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.ShowDatabaseDiscoveryRulesStatement;
import org.apache.shardingsphere.dbdiscovery.rule.DatabaseDiscoveryRule;
import org.apache.shardingsphere.distsql.handler.query.RQLExecutor;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Show database discovery rule executor.
 */
public final class ShowDatabaseDiscoveryRuleExecutor implements RQLExecutor<ShowDatabaseDiscoveryRulesStatement> {
    
    private static final String GROUP_NAME = "group_name";
    
    private static final String DATA_SOURCE_NAMES = "data_source_names";
    
    private static final String PRIMARY_DATA_SOURCE_NAME = "primary_data_source_name";
    
    private static final String NAME = "name";
    
    private static final String DISCOVER_TYPE = "discovery_type";
    
    private static final String HEARTBEAT = "discovery_heartbeat";
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShardingSphereDatabase database, final ShowDatabaseDiscoveryRulesStatement sqlStatement) {
        Optional<DatabaseDiscoveryRule> rule = database.getRuleMetaData().findSingleRule(DatabaseDiscoveryRule.class);
        Iterator<DatabaseDiscoveryDataSourceRuleConfiguration> dataSourceRules;
        if (!rule.isPresent()) {
            return Collections.emptyList();
        }
        DatabaseDiscoveryRuleConfiguration ruleConfig = (DatabaseDiscoveryRuleConfiguration) rule.get().getConfiguration();
        dataSourceRules = ruleConfig.getDataSources().iterator();
        Map<String, AlgorithmConfiguration> discoveryTypes = ruleConfig.getDiscoveryTypes();
        Map<String, DatabaseDiscoveryHeartBeatConfiguration> discoveryHeartbeats = ruleConfig.getDiscoveryHeartbeats();
        Map<String, String> primaryDataSources = rule.get().getDataSourceRules().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getPrimaryDataSourceName(), (a, b) -> b));
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        while (dataSourceRules.hasNext()) {
            DatabaseDiscoveryDataSourceRuleConfiguration dataSourceRuleConfig = dataSourceRules.next();
            Map<String, String> typeMap = new LinkedHashMap<>();
            typeMap.put(NAME, dataSourceRuleConfig.getDiscoveryTypeName());
            typeMap.putAll(convertToMap(discoveryTypes.get(dataSourceRuleConfig.getDiscoveryTypeName())));
            Map<String, String> heartbeatMap = new LinkedHashMap<>();
            heartbeatMap.put(NAME, dataSourceRuleConfig.getDiscoveryHeartbeatName());
            heartbeatMap.putAll(convertToMap(discoveryHeartbeats.get(dataSourceRuleConfig.getDiscoveryHeartbeatName())));
            String groupName = dataSourceRuleConfig.getGroupName();
            String primaryDataSourceName = null == primaryDataSources.get(groupName) ? "" : primaryDataSources.get(groupName);
            result.add(new LocalDataQueryResultRow(groupName, String.join(",", dataSourceRuleConfig.getDataSourceNames()), primaryDataSourceName, typeMap, heartbeatMap));
        }
        return result;
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList(GROUP_NAME, DATA_SOURCE_NAMES, PRIMARY_DATA_SOURCE_NAME, DISCOVER_TYPE, HEARTBEAT);
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, String> convertToMap(final Object obj) {
        return null == obj ? Collections.emptyMap() : new Gson().fromJson(new Gson().toJson(obj), LinkedHashMap.class);
    }
    
    @Override
    public String getType() {
        return ShowDatabaseDiscoveryRulesStatement.class.getName();
    }
}
