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

package org.apache.shardingsphere.proxy.backend.text.distsql.rdl.impl;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.AddResourceStatement;
import org.apache.shardingsphere.governance.core.event.model.datasource.DataSourcePersistEvent;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.config.util.DataSourceParameterConverter;
import org.apache.shardingsphere.proxy.converter.AddResourcesStatementConverter;

import java.util.Map;

/**
 * Add resource backend handler.
 */
@RequiredArgsConstructor
public final class AddResourceBackendHandler implements TextProtocolBackendHandler {
    
    private final DatabaseType databaseType;
    
    private final AddResourceStatement sqlStatement;
    
    private final BackendConnection backendConnection;
    
    @Override
    public ResponseHeader execute() {
        Map<String, DataSourceConfiguration> dataSources = DataSourceParameterConverter.getDataSourceConfigurationMap(
                DataSourceParameterConverter.getDataSourceParameterMapFromYamlConfiguration(AddResourcesStatementConverter.convert(databaseType, sqlStatement)));
        post(backendConnection, dataSources);
        return new UpdateResponseHeader(sqlStatement);
    }
    
    private void post(final BackendConnection backendConnection, final Map<String, DataSourceConfiguration> dataSources) {
        // TODO Need to get the executed feedback from registry center for returning.
        ShardingSphereEventBus.getInstance().post(new DataSourcePersistEvent(backendConnection.getSchemaName(), dataSources));
    }
}
