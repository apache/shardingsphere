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
import org.apache.shardingsphere.infra.config.datasource.DataSourceProperties;
import org.apache.shardingsphere.infra.config.datasource.creator.DataSourcePoolCreatorUtil;
import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Result set for show data source.
 */
public final class DataSourceQueryResultSet implements DistSQLResultSet {
    
    private ShardingSphereResource resource;
    
    private Map<String, DataSourceProperties> dataSourcePropsMap;
    
    private Iterator<String> dataSourceNames;
    
    @Override
    public void init(final ShardingSphereMetaData metaData, final SQLStatement sqlStatement) {
        resource = metaData.getResource();
        Optional<MetaDataPersistService> persistService = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaDataPersistService();
        dataSourcePropsMap = persistService.isPresent()
                ? persistService.get().getDataSourceService().load(metaData.getName())
                : DataSourcePoolCreatorUtil.getDataSourcePropertiesMap(metaData.getResource().getDataSources());
        dataSourceNames = dataSourcePropsMap.keySet().iterator();
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("name", "type", "host", "port", "db", "attribute");
    }
    
    @Override
    public boolean next() {
        return dataSourceNames.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        String dataSourceName = dataSourceNames.next();
        DataSourceMetaData metaData = resource.getDataSourcesMetaData().getDataSourceMetaData(dataSourceName);
        return Arrays.asList(dataSourceName, resource.getDatabaseType().getName(), 
                metaData.getHostname(), metaData.getPort(), metaData.getCatalog(), (new Gson()).toJson(getAttributeMap(dataSourcePropsMap.get(dataSourceName))));
    }
    
    private Map<String, Object> getAttributeMap(final DataSourceProperties dataSourceProps) {
        Map<String, Object> result = new LinkedHashMap<>(7, 1);
        result.put("connectionTimeoutMilliseconds", getProperty(dataSourceProps, "connectionTimeoutMilliseconds", "connectionTimeout"));
        result.put("idleTimeoutMilliseconds", getProperty(dataSourceProps, "idleTimeoutMilliseconds", "idleTimeout"));
        result.put("maxLifetimeMilliseconds", getProperty(dataSourceProps, "maxLifetimeMilliseconds", "maxLifetime"));
        result.put("maxPoolSize", getProperty(dataSourceProps, "maxPoolSize", "maximumPoolSize"));
        result.put("minPoolSize", getProperty(dataSourceProps, "minPoolSize", "minimumIdle"));
        result.put("readOnly", getProperty(dataSourceProps, "readOnly"));
        if (!dataSourceProps.getCustomPoolProps().isEmpty()) {
            result.put(DataSourceProperties.CUSTOM_POOL_PROPS_KEY, dataSourceProps.getCustomPoolProps());
        }
        return result;
    }
    
    private Object getProperty(final DataSourceProperties dataSourceProps, final String key, final String... synonym) {
        if (dataSourceProps.getProps().containsKey(key)) {
            return dataSourceProps.getProps().get(key);
        }
        for (String each : synonym) {
            if (dataSourceProps.getProps().containsKey(each)) {
                return dataSourceProps.getProps().get(each);
            }
        }
        return null;
    }
    
    @Override
    public String getType() {
        return ShowResourcesStatement.class.getCanonicalName();
    }
}
