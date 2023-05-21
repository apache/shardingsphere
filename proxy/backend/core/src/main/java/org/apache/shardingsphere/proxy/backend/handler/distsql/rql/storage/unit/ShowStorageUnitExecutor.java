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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rql.storage.unit;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import org.apache.shardingsphere.distsql.handler.query.RQLExecutor;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowStorageUnitsStatement;
import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Show storage unit executor.
 */
public final class ShowStorageUnitExecutor implements RQLExecutor<ShowStorageUnitsStatement> {
    
    private static final String CONNECTION_TIMEOUT_MILLISECONDS = "connectionTimeoutMilliseconds";
    
    private static final String IDLE_TIMEOUT_MILLISECONDS = "idleTimeoutMilliseconds";
    
    private static final String MAX_LIFETIME_MILLISECONDS = "maxLifetimeMilliseconds";
    
    private static final String MAX_POOL_SIZE = "maxPoolSize";
    
    private static final String MIN_POOL_SIZE = "minPoolSize";
    
    private static final String READ_ONLY = "readOnly";
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("name", "type", "host", "port", "db", "connection_timeout_milliseconds", "idle_timeout_milliseconds",
                "max_lifetime_milliseconds", "max_pool_size", "min_pool_size", "read_only", "other_attributes");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShardingSphereDatabase database, final ShowStorageUnitsStatement sqlStatement) {
        ShardingSphereResourceMetaData resourceMetaData = database.getResourceMetaData();
        Map<String, DataSourceProperties> dataSourcePropsMap = getDataSourcePropsMap(database, sqlStatement);
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        for (Entry<String, DataSourceProperties> entry : dataSourcePropsMap.entrySet()) {
            String key = entry.getKey();
            DataSourceProperties dataSourceProps = entry.getValue();
            DataSourceMetaData metaData = resourceMetaData.getDataSourceMetaData(key);
            Map<String, Object> standardProps = dataSourceProps.getPoolPropertySynonyms().getStandardProperties();
            Map<String, Object> otherProps = dataSourceProps.getCustomDataSourceProperties().getProperties();
            result.add(new LocalDataQueryResultRow(key,
                    resourceMetaData.getStorageType(key).getType(),
                    metaData.getHostname(),
                    metaData.getPort(),
                    metaData.getCatalog(),
                    getStandardProperty(standardProps, CONNECTION_TIMEOUT_MILLISECONDS),
                    getStandardProperty(standardProps, IDLE_TIMEOUT_MILLISECONDS),
                    getStandardProperty(standardProps, MAX_LIFETIME_MILLISECONDS),
                    getStandardProperty(standardProps, MAX_POOL_SIZE),
                    getStandardProperty(standardProps, MIN_POOL_SIZE),
                    getStandardProperty(standardProps, READ_ONLY),
                    otherProps.isEmpty() ? "" : new Gson().toJson(otherProps)));
        }
        return result;
    }
    
    private Map<String, DataSourceProperties> getDataSourcePropsMap(final ShardingSphereDatabase database, final ShowStorageUnitsStatement sqlStatement) {
        Map<String, DataSourceProperties> result = new LinkedHashMap<>(database.getResourceMetaData().getDataSources().size(), 1F);
        Optional<Integer> usageCountOptional = sqlStatement.getUsageCount();
        if (usageCountOptional.isPresent()) {
            Multimap<String, String> inUsedMultiMap = getInUsedResources(database.getRuleMetaData());
            for (Entry<String, DataSource> entry : database.getResourceMetaData().getDataSources().entrySet()) {
                Integer currentUsageCount = inUsedMultiMap.containsKey(entry.getKey()) ? inUsedMultiMap.get(entry.getKey()).size() : 0;
                if (usageCountOptional.get().equals(currentUsageCount)) {
                    result.put(entry.getKey(), DataSourcePropertiesCreator.create(entry.getValue()));
                }
            }
        } else {
            for (Entry<String, DataSource> entry : database.getResourceMetaData().getDataSources().entrySet()) {
                result.put(entry.getKey(), DataSourcePropertiesCreator.create(entry.getValue()));
            }
        }
        return result;
    }
    
    private Multimap<String, String> getInUsedResources(final ShardingSphereRuleMetaData ruleMetaData) {
        Multimap<String, String> result = LinkedListMultimap.create();
        for (DataSourceContainedRule each : ruleMetaData.findRules(DataSourceContainedRule.class)) {
            getInUsedResourceNames(each).forEach(eachResource -> result.put(eachResource, each.getType()));
        }
        for (DataNodeContainedRule each : ruleMetaData.findRules(DataNodeContainedRule.class)) {
            getInUsedResourceNames(each).forEach(eachResource -> result.put(eachResource, each.getType()));
        }
        return result;
    }
    
    private Collection<String> getInUsedResourceNames(final DataSourceContainedRule rule) {
        Set<String> result = new HashSet<>();
        for (Collection<String> each : rule.getDataSourceMapper().values()) {
            result.addAll(each);
        }
        return result;
    }
    
    private Collection<String> getInUsedResourceNames(final DataNodeContainedRule rule) {
        Set<String> result = new HashSet<>();
        for (Collection<DataNode> each : rule.getAllDataNodes().values()) {
            result.addAll(each.stream().map(DataNode::getDataSourceName).collect(Collectors.toSet()));
        }
        return result;
    }
    
    private String getStandardProperty(final Map<String, Object> standardProps, final String key) {
        if (standardProps.containsKey(key) && null != standardProps.get(key)) {
            return standardProps.get(key).toString();
        }
        return "";
    }
    
    @Override
    public String getType() {
        return ShowStorageUnitsStatement.class.getName();
    }
}
