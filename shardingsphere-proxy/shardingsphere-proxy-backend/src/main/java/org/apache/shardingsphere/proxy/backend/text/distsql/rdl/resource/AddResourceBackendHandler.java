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

package org.apache.shardingsphere.proxy.backend.text.distsql.rdl.resource;

import org.apache.shardingsphere.distsql.parser.segment.DataSourceSegment;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.AddResourceStatement;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceValidator;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.resource.DuplicateResourceException;
import org.apache.shardingsphere.infra.distsql.exception.resource.InvalidResourceException;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.SchemaRequiredBackendHandler;
import org.apache.shardingsphere.proxy.config.util.DataSourceParameterConverter;
import org.apache.shardingsphere.proxy.converter.ResourceSegmentsConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
    public ResponseHeader execute(final String schemaName, final AddResourceStatement sqlStatement) throws DistSQLException {
        check(schemaName, sqlStatement);
        Map<String, DataSourceConfiguration> dataSourceConfigs = DataSourceParameterConverter.getDataSourceConfigurationMap(
                DataSourceParameterConverter.getDataSourceParameterMapFromYamlConfiguration(ResourceSegmentsConverter.convert(databaseType, sqlStatement.getDataSources())));
        Collection<String> invalidResources = dataSourceConfigs.entrySet().stream().map(entry -> validateDataSource(entry)).filter(Objects::nonNull).collect(Collectors.toList());
        DistSQLException.predictionThrow(invalidResources.isEmpty(), new InvalidResourceException(invalidResources));
        // TODO update meta data context in memory
        ProxyContext.getInstance().getContextManager()
                .getMetaDataContexts().getMetaDataPersistService().ifPresent(optional -> optional.getDataSourceService().append(schemaName, dataSourceConfigs));
        return new UpdateResponseHeader(sqlStatement);
    }
    
    private void check(final String schemaName, final AddResourceStatement sqlStatement) throws DuplicateResourceException {
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
    }
    
    private String validateDataSource(final Entry<String, DataSourceConfiguration> dataSource) {
        try {
            dataSourceValidator.validate(dataSource.getValue());
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            return String.format("`%s` %s", dataSource.getKey(), ex.getMessage());
        }
        return null;
    }
}
