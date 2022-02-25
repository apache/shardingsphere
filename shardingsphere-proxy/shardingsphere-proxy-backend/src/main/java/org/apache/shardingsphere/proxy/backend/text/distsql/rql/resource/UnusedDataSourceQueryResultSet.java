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

package org.apache.shardingsphere.proxy.backend.text.distsql.rql.resource;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowUnusedResourcesStatement;
import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;
import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Result set for show unused data source.
 */
public final class UnusedDataSourceQueryResultSet implements DistSQLResultSet {
    
    private static final String CONNECTION_TIMEOUT_MILLISECONDS = "connectionTimeoutMilliseconds";
    
    private static final String IDLE_TIMEOUT_MILLISECONDS = "idleTimeoutMilliseconds";
    
    private static final String MAX_LIFETIME_MILLISECONDS = "maxLifetimeMilliseconds";
    
    private static final String MAX_POOL_SIZE = "maxPoolSize";
    
    private static final String MIN_POOL_SIZE = "minPoolSize";
    
    private static final String READ_ONLY = "readOnly";
    
    private ShardingSphereResource resource;
    
    private Map<String, DataSourceProperties> dataSourcePropsMap;
    
    private Iterator<String> dataSourceNames;
    
    @Override
    public void init(final ShardingSphereMetaData metaData, final SQLStatement sqlStatement) {
        resource = metaData.getResource();
        dataSourcePropsMap = new LinkedHashMap<>(metaData.getResource().getDataSources().size(), 1);
        Multimap<String, String> inUsedMultiMap = getInUsedResources(metaData.getRuleMetaData());
        for (Entry<String, DataSource> entry : metaData.getResource().getDataSources().entrySet()) {
            if (inUsedMultiMap.containsKey(entry.getKey())) {
                continue;
            }
            dataSourcePropsMap.put(entry.getKey(), DataSourcePropertiesCreator.create(entry.getValue()));
        }
        dataSourceNames = dataSourcePropsMap.keySet().iterator();
    }
    
    private Multimap<String, String> getInUsedResources(final ShardingSphereRuleMetaData ruleMetaData) {
        Multimap<String, String> result = LinkedListMultimap.create();
        for (ShardingSphereRule each : ruleMetaData.getRules()) {
            if (each instanceof DataSourceContainedRule) {
                Set<String> inUsedResourceNames = getInUsedResourceNames((DataSourceContainedRule) each);
                inUsedResourceNames.stream().forEach(eachResource -> result.put(eachResource, each.getType()));
            }
            if (each instanceof DataNodeContainedRule) {
                Set<String> inUsedResourceNames = getInUsedResourceNames((DataNodeContainedRule) each);
                inUsedResourceNames.stream().forEach(eachResource -> result.put(eachResource, each.getType()));
            }
        }
        return result;
    }
    
    private Set<String> getInUsedResourceNames(final DataSourceContainedRule rule) {
        Set<String> result = new HashSet<>();
        for (Collection<String> each : rule.getDataSourceMapper().values()) {
            result.addAll(each);
        }
        return result;
    }
    
    private Set<String> getInUsedResourceNames(final DataNodeContainedRule rule) {
        Set<String> result = new HashSet<>();
        for (Collection<DataNode> each : rule.getAllDataNodes().values()) {
            result.addAll(each.stream().map(DataNode::getDataSourceName).collect(Collectors.toList()));
        }
        return result;
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("name", "type", "host", "port", "db", "connection_timeout_milliseconds", "idle_timeout_milliseconds",
                "max_lifetime_milliseconds", "max_pool_size", "min_pool_size", "read_only", "other_attributes");
    }
    
    @Override
    public boolean next() {
        return dataSourceNames.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        String dataSourceName = dataSourceNames.next();
        DataSourceMetaData metaData = resource.getDataSourcesMetaData().getDataSourceMetaData(dataSourceName);
        Collection<Object> result = new LinkedList<>();
        result.add(dataSourceName);
        result.add(resource.getDatabaseType().getName());
        result.add(metaData.getHostname());
        result.add(metaData.getPort());
        result.add(metaData.getCatalog());
        DataSourceProperties dataSourceProperties = dataSourcePropsMap.get(dataSourceName);
        Map<String, Object> standardProperties = dataSourceProperties.getPoolPropertySynonyms().getStandardProperties();
        result.add(getStandardProperty(standardProperties, CONNECTION_TIMEOUT_MILLISECONDS));
        result.add(getStandardProperty(standardProperties, IDLE_TIMEOUT_MILLISECONDS));
        result.add(getStandardProperty(standardProperties, MAX_LIFETIME_MILLISECONDS));
        result.add(getStandardProperty(standardProperties, MAX_POOL_SIZE));
        result.add(getStandardProperty(standardProperties, MIN_POOL_SIZE));
        result.add(getStandardProperty(standardProperties, READ_ONLY));
        Map<String, Object> otherProperties = dataSourceProperties.getCustomDataSourceProperties().getProperties();
        result.add(otherProperties.isEmpty() ? "" : new Gson().toJson(otherProperties));
        return result;
    }
    
    private String getStandardProperty(final Map<String, Object> standardProperties, final String key) {
        if (standardProperties.containsKey(key) && null != standardProperties.get(key)) {
            return standardProperties.get(key).toString();
        }
        return "";
    }
    
    @Override
    public String getType() {
        return ShowUnusedResourcesStatement.class.getName();
    }
}
