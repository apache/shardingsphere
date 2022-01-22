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

import com.google.gson.Gson;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowResourcesStatement;
import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourceReflection;
import org.apache.shardingsphere.infra.datasource.pool.metadata.DataSourcePoolMetaData;
import org.apache.shardingsphere.infra.datasource.pool.metadata.DataSourcePoolMetaDataFactory;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;
import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Result set for show data source.
 */
public final class DataSourceQueryResultSet implements DistSQLResultSet {
    
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
        Optional<MetaDataPersistService> persistService = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaDataPersistService();
        dataSourcePropsMap = persistService.isPresent()
                ? persistService.get().getDataSourceService().load(metaData.getName())
                : DataSourcePropertiesCreator.create(metaData.getResource().getDataSources());
        dataSourceNames = dataSourcePropsMap.keySet().iterator();
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
        DataSourceProperties dataSourceProperties = dataSourcePropsMap.get(dataSourceName);
        DataSourcePoolMetaData poolMetaData = DataSourcePoolMetaDataFactory.newInstance(dataSourceProperties.getDataSourceClassName());
        Map<String, Object> allProperties = getAllProperties(resource.getDataSources().get(dataSourceName));
        Map<String, Object> standardProps = getStandardProperties(dataSourceProperties, poolMetaData, allProperties);
        return getRowData(dataSourceName, poolMetaData, allProperties, standardProps);
    }
    
    private Collection<Object> getRowData(final String dataSourceName, final DataSourcePoolMetaData poolMetaData, final Map<String, Object> allProperties, final Map<String, Object> standardProps) {
        DataSourceMetaData metaData = resource.getDataSourcesMetaData().getDataSourceMetaData(dataSourceName);
        Collection<Object> result = new LinkedList<>();
        result.add(dataSourceName);
        result.add(resource.getDatabaseType().getName());
        result.add(metaData.getHostname());
        result.add(metaData.getPort());
        result.add(metaData.getCatalog());
        result.add(getStandardProperty(standardProps, CONNECTION_TIMEOUT_MILLISECONDS));
        result.add(getStandardProperty(standardProps, IDLE_TIMEOUT_MILLISECONDS));
        result.add(getStandardProperty(standardProps, MAX_LIFETIME_MILLISECONDS));
        result.add(getStandardProperty(standardProps, MAX_POOL_SIZE));
        result.add(getStandardProperty(standardProps, MIN_POOL_SIZE));
        result.add(getStandardProperty(standardProps, READ_ONLY));
        result.add(new Gson().toJson(getFilteredUndisplayedProperties(poolMetaData, allProperties, standardProps)));
        return result;
    }
    
    private Map<String, Object> getAllProperties(final DataSource dataSource) {
        DataSourceReflection dataSourceReflection = new DataSourceReflection(dataSource);
        Map<String, Object> result = dataSourceReflection.convertToProperties();
        return result;
    }
    
    private Map<String, Object> getStandardProperties(final DataSourceProperties dataSourceProperties, final DataSourcePoolMetaData poolMetaData, final Map<String, Object> allProperties) {
        Collection<String> standardPropertyKeys = dataSourceProperties.getPoolPropertySynonyms().getStandardPropertyKeys();
        Map<String, Object> result = new HashMap<>(standardPropertyKeys.size(), 1);
        Map<String, String> propertySynonyms = poolMetaData.getPropertySynonyms();
        for (String each : standardPropertyKeys) {
            if (propertySynonyms.containsKey(each)) {
                result.put(each, allProperties.get(propertySynonyms.get(each)));
            } else {
                result.put(each, allProperties.get(each));
            }
        }
        return result;
    }
    
    private String getStandardProperty(final Map<String, Object> standardProps, final String key) {
        if (standardProps.containsKey(key) && null != standardProps.get(key)) {
            return standardProps.get(key).toString();
        }
        return "";
    }
    
    private Map<String, Object> getFilteredUndisplayedProperties(final DataSourcePoolMetaData poolMetaData, final Map<String, Object> allProperties, final Map<String, Object> standardProps) {
        Map<String, String> propertySynonyms = poolMetaData.getPropertySynonyms();
        Map<String, Object> result = new LinkedHashMap<>(allProperties);
        standardProps.forEach(result::remove);
        propertySynonyms.keySet().forEach(result::remove);
        propertySynonyms.values().forEach(result::remove);
        poolMetaData.getTransientFieldNames().forEach(result::remove);
        result.remove(poolMetaData.getJdbcUrlPropertiesFieldName());
        return new TreeMap<>(result);
    }
    
    @Override
    public String getType() {
        return ShowResourcesStatement.class.getCanonicalName();
    }
}
