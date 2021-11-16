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
import org.apache.shardingsphere.infra.config.datasource.DataSourceConverter;
import org.apache.shardingsphere.infra.config.datasource.DataSourceParameter;
import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

/**
 * Result set for show data source.
 */
public final class DataSourceQueryResultSet implements DistSQLResultSet {
    
    private Map<String, DataSourceParameter> dataSourceParameterMap;
    
    private Iterator<String> dataSourceNames;
    
    private ShardingSphereResource resource;
    
    @Override
    public void init(final ShardingSphereMetaData metaData, final SQLStatement sqlStatement) {
        resource = metaData.getResource();
        Optional<MetaDataPersistService> persistService = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaDataPersistService();
        Map<String, DataSourceConfiguration> dataSourceConfigs = persistService.isPresent()
                ? persistService.get().getDataSourceService().load(metaData.getName())
                : DataSourceConverter.getDataSourceConfigurationMap(metaData.getResource().getDataSources());
        dataSourceParameterMap = DataSourceQueryResultSetConverter.covert(dataSourceConfigs);
        dataSourceNames = dataSourceParameterMap.keySet().iterator();
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
        DataSourceMetaData dataSourceMetaData = resource.getDataSourcesMetaData().getDataSourceMetaData(dataSourceName);
        String type = resource.getDatabaseType().getName();
        String host = dataSourceMetaData.getHostName();
        int port = dataSourceMetaData.getPort();
        String db = dataSourceMetaData.getCatalog();
        return Arrays.asList(dataSourceName, type, host, port, db, (new Gson()).toJson(getAttributeMap(dataSourceParameterMap.get(dataSourceName))));
    }
    
    private Map<Object, Object> getAttributeMap(final DataSourceParameter dataSourceParameter) {
        Map<Object, Object> result = new HashMap<>(7, 1);
        result.put("connectionTimeoutMilliseconds", dataSourceParameter.getConnectionTimeoutMilliseconds());
        result.put("idleTimeoutMilliseconds", dataSourceParameter.getIdleTimeoutMilliseconds());
        result.put("maxLifetimeMilliseconds", dataSourceParameter.getMaxLifetimeMilliseconds());
        result.put("maxPoolSize", dataSourceParameter.getMaxPoolSize());
        result.put("minPoolSize", dataSourceParameter.getMinPoolSize());
        result.put("readOnly", dataSourceParameter.isReadOnly());
        if (null != dataSourceParameter.getCustomPoolProps() && !dataSourceParameter.getCustomPoolProps().isEmpty()) {
            result.put("customPoolProps", dataSourceParameter.getCustomPoolProps());
        }
        return result;
    }
    
    @Override
    public String getType() {
        return ShowResourcesStatement.class.getCanonicalName();
    }
}
