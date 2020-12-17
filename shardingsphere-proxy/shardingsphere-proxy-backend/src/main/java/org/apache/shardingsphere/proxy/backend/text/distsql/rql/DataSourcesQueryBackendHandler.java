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

package org.apache.shardingsphere.proxy.backend.text.distsql.rql;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowResourcesStatement;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConverter;
import org.apache.shardingsphere.infra.config.datasource.DataSourceParameter;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.NoDatabaseSelectedException;
import org.apache.shardingsphere.proxy.backend.exception.UnknownDatabaseException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeader;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.config.util.DataSourceParameterConverter;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Backend handler for data sources query.
 */
@RequiredArgsConstructor
public final class DataSourcesQueryBackendHandler implements TextProtocolBackendHandler {
    
    private final ShowResourcesStatement sqlStatement;
    
    private final BackendConnection backendConnection;
    
    private Map<String, DataSourceParameter> dataSourceParameterMap;
    
    private Iterator<String> dataSourceNames;
    
    @Override
    public ResponseHeader execute() {
        String schemaName = getSchemaName(sqlStatement);
        QueryHeader nameQueryHeader = new QueryHeader(schemaName, "", "name", "name", Types.CHAR, "CHAR", 255, 0, false, false, false, false);
        QueryHeader contentQueryHeader = new QueryHeader(schemaName, "", "data source", "data source", Types.CHAR, "CHAR", 255, 0, false, false, false, false);
        dataSourceParameterMap = DataSourceParameterConverter.getDataSourceParameterMap(
                DataSourceConverter.getDataSourceConfigurationMap(ProxyContext.getInstance().getMetaData(schemaName).getResource().getDataSources()));
        dataSourceNames = dataSourceParameterMap.keySet().iterator();
        return new QueryResponseHeader(Arrays.asList(nameQueryHeader, contentQueryHeader));
    }
    
    private String getSchemaName(final ShowResourcesStatement sqlStatement) {
        String result = sqlStatement.getSchema().isPresent() ? sqlStatement.getSchema().get().getIdentifier().getValue() : backendConnection.getSchemaName();
        if (null == result) {
            throw new NoDatabaseSelectedException();
        }
        if (!ProxyContext.getInstance().schemaExists(result)) {
            throw new UnknownDatabaseException(result);
        }
        return result;
    }
    
    @Override
    public boolean next() {
        return dataSourceNames.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        String dataSourceName = dataSourceNames.next();
        return Arrays.asList(dataSourceName, YamlEngine.marshal(dataSourceParameterMap.get(dataSourceName)));
    }
}
