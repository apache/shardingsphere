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
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.pool.creator.DataSourcePoolCreatorUtil;
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
    
    private Map<String, DataSourceConfiguration> dataSourceConfigs;
    
    private Iterator<String> dataSourceNames;
    
    @Override
    public void init(final ShardingSphereMetaData metaData, final SQLStatement sqlStatement) {
        resource = metaData.getResource();
        Optional<MetaDataPersistService> persistService = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaDataPersistService();
        dataSourceConfigs = persistService.isPresent()
                ? persistService.get().getDataSourceService().load(metaData.getName())
                : DataSourcePoolCreatorUtil.getDataSourceConfigurationMap(metaData.getResource().getDataSources());
        dataSourceNames = dataSourceConfigs.keySet().iterator();
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
                metaData.getHostname(), metaData.getPort(), metaData.getCatalog(), (new Gson()).toJson(getAttributeMap(dataSourceConfigs.get(dataSourceName))));
    }
    
    private Map<String, Object> getAttributeMap(final DataSourceConfiguration dataSourceConfig) {
        Map<String, Object> result = new LinkedHashMap<>(7, 1);
        result.put("connectionTimeoutMilliseconds", getProperty(dataSourceConfig, "connectionTimeoutMilliseconds", "connectionTimeout"));
        result.put("idleTimeoutMilliseconds", getProperty(dataSourceConfig, "idleTimeoutMilliseconds", "idleTimeout"));
        result.put("maxLifetimeMilliseconds", getProperty(dataSourceConfig, "maxLifetimeMilliseconds", "maxLifetime"));
        result.put("maxPoolSize", getProperty(dataSourceConfig, "maxPoolSize", "maximumPoolSize"));
        result.put("minPoolSize", getProperty(dataSourceConfig, "minPoolSize", "minimumIdle"));
        result.put("readOnly", getProperty(dataSourceConfig, "readOnly"));
        if (!dataSourceConfig.getCustomPoolProps().isEmpty()) {
            result.put(DataSourceConfiguration.CUSTOM_POOL_PROPS_KEY, dataSourceConfig.getCustomPoolProps());
        }
        return result;
    }
    
    private Object getProperty(final DataSourceConfiguration dataSourceConfig, final String key, final String... synonym) {
        if (dataSourceConfig.getProps().containsKey(key)) {
            return dataSourceConfig.getProps().get(key);
        }
        for (String each : synonym) {
            if (dataSourceConfig.getProps().containsKey(each)) {
                return dataSourceConfig.getProps().get(each);
            }
        }
        return null;
    }
    
    @Override
    public String getType() {
        return ShowResourcesStatement.class.getCanonicalName();
    }
}
