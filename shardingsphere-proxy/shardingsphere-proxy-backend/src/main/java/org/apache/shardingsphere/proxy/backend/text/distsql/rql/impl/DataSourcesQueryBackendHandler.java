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
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowResourcesStatement;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConverter;
import org.apache.shardingsphere.infra.config.datasource.DataSourceParameter;
import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeader;
import org.apache.shardingsphere.proxy.backend.text.SchemaRequiredBackendHandler;
import org.apache.shardingsphere.proxy.config.util.DataSourceParameterConverter;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Backend handler for show resources.
 */
public final class DataSourcesQueryBackendHandler extends SchemaRequiredBackendHandler<ShowResourcesStatement> {
    
    private Map<String, DataSourceParameter> dataSourceParameterMap;
    
    private Iterator<String> dataSourceNames;
    
    private String schemaName;
    
    public DataSourcesQueryBackendHandler(final ShowResourcesStatement sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
    }
    
    @Override
    public ResponseHeader execute(final String schemaName, final ShowResourcesStatement sqlStatement) {
        this.schemaName = schemaName;
        dataSourceParameterMap = DataSourceParameterConverter.getDataSourceParameterMap(
                DataSourceConverter.getDataSourceConfigurationMap(ProxyContext.getInstance().getMetaData(schemaName).getResource().getDataSources()));
        dataSourceNames = dataSourceParameterMap.keySet().iterator();
        return new QueryResponseHeader(generateResponseHeader(schemaName));
    }
    
    private List<QueryHeader> generateResponseHeader(final String schemaName) {
        List<QueryHeader> result = new LinkedList<>();
        result.add(new QueryHeader(schemaName, "", "name", "name", Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        result.add(new QueryHeader(schemaName, "", "type", "type", Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        result.add(new QueryHeader(schemaName, "", "host", "host", Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        result.add(new QueryHeader(schemaName, "", "port", "port", Types.BIGINT, "BIGINT", 255, 0, false, false, false, false));
        result.add(new QueryHeader(schemaName, "", "db", "db", Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        result.add(new QueryHeader(schemaName, "", "attribute", "attribute", Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        return result;
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
