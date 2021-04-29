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

import org.apache.shardingsphere.distsql.parser.segment.DataSourceSegment;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.AddResourceStatement;
import org.apache.shardingsphere.governance.core.registry.listener.event.datasource.DataSourceAddedEvent;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceValidator;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.DuplicateResourceException;
import org.apache.shardingsphere.proxy.backend.exception.InvalidResourceException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.SchemaRequiredBackendHandler;
import org.apache.shardingsphere.proxy.config.util.DataSourceParameterConverter;
import org.apache.shardingsphere.proxy.converter.AddResourcesStatementConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Add resource backend handler.
 */
public final class AddResourceBackendHandler extends SchemaRequiredBackendHandler<AddResourceStatement> {
    
    private final DatabaseType databaseType;
    
    private final DataSourceValidator dataSourceValidator;
    
    public AddResourceBackendHandler(final DatabaseType databaseType, final AddResourceStatement sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
        this.databaseType = databaseType;
        dataSourceValidator = new DataSourceValidator();
    }
    
    @Override
    public ResponseHeader execute(final String schemaName, final AddResourceStatement sqlStatement) {
        post(schemaName, check(schemaName, sqlStatement));
        return new UpdateResponseHeader(sqlStatement);
    }
    
    private Map<String, DataSourceConfiguration> check(final String schemaName, final AddResourceStatement sqlStatement) {
        List<String> dataSourceNames = new ArrayList<>(sqlStatement.getDataSources().size());
        Set<String> duplicateDataSourceNames = new HashSet<>(sqlStatement.getDataSources().size(), 1);
        for (DataSourceSegment each : sqlStatement.getDataSources()) {
            if (dataSourceNames.contains(each.getName()) || ProxyContext.getInstance().getMetaData(schemaName).getResource().getDataSources().containsKey(each.getName())) {
                duplicateDataSourceNames.add(each.getName());
            }
            dataSourceNames.add(each.getName());
        }
        if (!duplicateDataSourceNames.isEmpty()) {
            throw new DuplicateResourceException(duplicateDataSourceNames);
        }
        Map<String, DataSourceConfiguration> result = DataSourceParameterConverter.getDataSourceConfigurationMap(
                DataSourceParameterConverter.getDataSourceParameterMapFromYamlConfiguration(AddResourcesStatementConverter.convert(databaseType, sqlStatement)));
        Collection<String> invalidDataSourceNames = new LinkedList<>();
        for (Entry<String, DataSourceConfiguration> entry : result.entrySet()) {
            if (!dataSourceValidator.validate(entry.getValue())) {
                invalidDataSourceNames.add(entry.getKey());
            }
        }
        if (!invalidDataSourceNames.isEmpty()) {
            throw new InvalidResourceException(invalidDataSourceNames);
        }
        return result;
    }
    
    private void post(final String schemaName, final Map<String, DataSourceConfiguration> dataSources) {
        // TODO Need to get the executed feedback from registry center for returning.
        ShardingSphereEventBus.getInstance().post(new DataSourceAddedEvent(schemaName, dataSources));
    }
}
