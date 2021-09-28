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
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfigurationValidator;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.resource.DuplicateResourceException;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.SchemaRequiredBackendHandler;
import org.apache.shardingsphere.proxy.config.util.DataSourceParameterConverter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Add resource backend handler.
 */
public final class AddResourceBackendHandler extends SchemaRequiredBackendHandler<AddResourceStatement> {
    
    private final DatabaseType databaseType;
    
    private final DataSourceConfigurationValidator dataSourceConfigValidator;
    
    public AddResourceBackendHandler(final DatabaseType databaseType, final AddResourceStatement sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
        this.databaseType = databaseType;
        dataSourceConfigValidator = new DataSourceConfigurationValidator();
    }
    
    @Override
    public ResponseHeader execute(final String schemaName, final AddResourceStatement sqlStatement) throws DistSQLException {
        checkSQLStatement(schemaName, sqlStatement);
        Map<String, DataSourceConfiguration> dataSourceConfigs
                = DataSourceParameterConverter.getDataSourceConfigurationMap(ResourceSegmentsConverter.convert(databaseType, sqlStatement.getDataSources()));
        dataSourceConfigValidator.validate(dataSourceConfigs);
        // TODO update meta data context in memory
        ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaDataPersistService().ifPresent(optional -> optional.getDataSourceService().append(schemaName, dataSourceConfigs));
        return new UpdateResponseHeader(sqlStatement);
    }
    
    private void checkSQLStatement(final String schemaName, final AddResourceStatement sqlStatement) throws DuplicateResourceException {
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
}
