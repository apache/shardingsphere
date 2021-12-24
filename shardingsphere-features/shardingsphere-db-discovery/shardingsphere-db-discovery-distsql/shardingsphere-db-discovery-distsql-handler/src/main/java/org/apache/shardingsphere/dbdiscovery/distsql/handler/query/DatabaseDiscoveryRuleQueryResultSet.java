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
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.constant.ExportableConstants;
import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.ExportableRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Result set for show database discovery rule.
 */
public final class DatabaseDiscoveryRuleQueryResultSet implements DistSQLResultSet {
    
    private static final String NAME = "name";
    
    private static final String DATA_SOURCE_NAMES = "data_source_names";
    
    private static final String PRIMARY_DATA_SOURCE_NAME = "primary_data_source_name";
    
    private static final String DISCOVER_TYPE = "discovery_type";
    
    private static final String HEARTBEAT = "discovery_heartbeat";
    
    private Iterator<DatabaseDiscoveryDataSourceRuleConfiguration> data;
    
    private Map<String, ShardingSphereAlgorithmConfiguration> discoveryTypes;
    
    private Map<String, DatabaseDiscoveryHeartBeatConfiguration> discoveryHeartbeats;
    
    private Map<String, String> primaryDataSources;
    
    @Override
    public void init(final ShardingSphereMetaData metaData, final SQLStatement sqlStatement) {
        Optional<DatabaseDiscoveryRuleConfiguration> ruleConfig = metaData.getRuleMetaData().getConfigurations()
                .stream().filter(each -> each instanceof DatabaseDiscoveryRuleConfiguration).map(each -> (DatabaseDiscoveryRuleConfiguration) each).findAny();
        data = ruleConfig.map(optional -> optional.getDataSources().iterator()).orElse(Collections.emptyIterator());
        discoveryTypes = ruleConfig.map(DatabaseDiscoveryRuleConfiguration::getDiscoveryTypes).orElse(Collections.emptyMap());
        discoveryHeartbeats = ruleConfig.map(DatabaseDiscoveryRuleConfiguration::getDiscoveryHeartbeats).orElse(Collections.emptyMap());
        Optional<ExportableRule> exportableRule = metaData.getRuleMetaData().getRules()
                .stream().filter(each -> each instanceof ExportableRule).map(each -> (ExportableRule) each).findAny();
        primaryDataSources = (Map<String, String>) exportableRule.map(optional -> optional.export().get(ExportableConstants.PRIMARY_DATA_SOURCE_KEY)).orElse(Collections.emptyMap());
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList(NAME, DATA_SOURCE_NAMES, PRIMARY_DATA_SOURCE_NAME, DISCOVER_TYPE, HEARTBEAT);
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        DatabaseDiscoveryDataSourceRuleConfiguration dataSourceRuleConfig = data.next();
        Map<String, String> typeMap = new LinkedHashMap<>();
        typeMap.put(NAME, dataSourceRuleConfig.getDiscoveryTypeName());
        typeMap.putAll(convertToMap(discoveryTypes.get(dataSourceRuleConfig.getDiscoveryTypeName())));
        Map<String, String> heartbeatMap = new LinkedHashMap<>();
        heartbeatMap.put(NAME, dataSourceRuleConfig.getDiscoveryHeartbeatName());
        heartbeatMap.putAll(convertToMap(discoveryHeartbeats.get(dataSourceRuleConfig.getDiscoveryHeartbeatName())));
        String name = dataSourceRuleConfig.getName();
        String primaryDataSourceName = null == primaryDataSources.get(name) ? "" : primaryDataSources.get(name);
        return Arrays.asList(name, String.join(",", dataSourceRuleConfig.getDataSourceNames()), primaryDataSourceName, typeMap, heartbeatMap);
    }
    
    private Map<String, String> convertToMap(final Object obj) {
        if (obj == null) {
            return Collections.emptyMap();
        }
        return new Gson().fromJson(new Gson().toJson(obj), LinkedHashMap.class);
    }
    
    @Override
    public String getType() {
        return ShowDatabaseDiscoveryRulesStatement.class.getCanonicalName();
    }
}
