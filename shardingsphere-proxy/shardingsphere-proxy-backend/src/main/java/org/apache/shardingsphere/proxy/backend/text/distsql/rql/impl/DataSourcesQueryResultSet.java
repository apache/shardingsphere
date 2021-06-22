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
import org.apache.shardingsphere.infra.config.datasource.DataSourceConverter;
import org.apache.shardingsphere.infra.config.datasource.DataSourceParameter;
import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.text.distsql.rql.RuleQueryResultSet;
import org.apache.shardingsphere.proxy.config.util.DataSourceParameterConverter;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Result set for show resources.
 */
public final class DataSourcesQueryResultSet implements RuleQueryResultSet {
    
    private Map<String, DataSourceParameter> dataSourceParameterMap;
    
    private Iterator<String> dataSourceNames;
    
    private String schemaName;
    
    @Override
    public void init(final String schemaName, final SQLStatement sqlStatement) {
        this.schemaName = schemaName;
        dataSourceParameterMap = DataSourceParameterConverter.getDataSourceParameterMap(
                DataSourceConverter.getDataSourceConfigurationMap(ProxyContext.getInstance().getMetaData(schemaName).getResource().getDataSources()));
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
        DataSourceMetaData dataSourceMetaData = ProxyContext.getInstance().getMetaData(schemaName).getResource().getDataSourcesMetaData().getDataSourceMetaData(dataSourceName);
        Map<Object, Object> attributeMap = new HashMap<>();
        attributeMap.put("connectionTimeoutMilliseconds", dataSourceParameterMap.get(dataSourceName).getConnectionTimeoutMilliseconds());
        attributeMap.put("idleTimeoutMilliseconds", dataSourceParameterMap.get(dataSourceName).getIdleTimeoutMilliseconds());
        attributeMap.put("maxLifetimeMilliseconds", dataSourceParameterMap.get(dataSourceName).getMaxLifetimeMilliseconds());
        attributeMap.put("maxPoolSize", dataSourceParameterMap.get(dataSourceName).getMaxPoolSize());
        attributeMap.put("minPoolSize", dataSourceParameterMap.get(dataSourceName).getMinPoolSize());
        attributeMap.put("maintenanceIntervalMilliseconds", dataSourceParameterMap.get(dataSourceName).getMaintenanceIntervalMilliseconds());
        attributeMap.put("readOnly", dataSourceParameterMap.get(dataSourceName).isReadOnly());
        String type = ProxyContext.getInstance().getMetaData(schemaName).getResource().getDatabaseType().getName();
        String host = dataSourceMetaData.getHostName();
        int port = dataSourceMetaData.getPort();
        String db = dataSourceMetaData.getCatalog();
        return Arrays.asList(dataSourceName, type, host, port, db, (new Gson()).toJson(attributeMap));
    }
}
