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
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.AlterResourceStatement;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceValidator;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.resource.DuplicateResourceException;
import org.apache.shardingsphere.infra.distsql.exception.resource.InvalidResourceException;
import org.apache.shardingsphere.infra.distsql.exception.resource.RequiredResourceMissedException;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.SchemaRequiredBackendHandler;
import org.apache.shardingsphere.proxy.config.util.DataSourceParameterConverter;
import org.apache.shardingsphere.proxy.converter.ResourceSegmentsConverter;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Alter resource backend handler.
 */
public final class AlterResourceBackendHandler extends SchemaRequiredBackendHandler<AlterResourceStatement> {
    
    private final DatabaseType databaseType;
    
    private final DataSourceValidator dataSourceValidator;
    
    public AlterResourceBackendHandler(final DatabaseType databaseType, final AlterResourceStatement sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
        this.databaseType = databaseType;
        dataSourceValidator = new DataSourceValidator();
    }
    
    @Override
    public ResponseHeader execute(final String schemaName, final AlterResourceStatement sqlStatement) throws DistSQLException {
        check(schemaName, sqlStatement);
        Map<String, DataSourceConfiguration> dataSourceConfigs = DataSourceParameterConverter.getDataSourceConfigurationMap(
                DataSourceParameterConverter.getDataSourceParameterMapFromYamlConfiguration(ResourceSegmentsConverter.convert(databaseType, sqlStatement.getDataSources())));
        validate(dataSourceConfigs);
        // TODO update meta data context in memory
        ProxyContext.getInstance().getContextManager()
                .getMetaDataContexts().getMetaDataPersistService().ifPresent(optional -> optional.getDataSourceService().append(schemaName, dataSourceConfigs));
        return new UpdateResponseHeader(sqlStatement);
    }
    
    private void check(final String schemaName, final AlterResourceStatement sqlStatement) throws DuplicateResourceException, RequiredResourceMissedException {
        Collection<String> toBeAlteredResourceNames = getToBeAlteredResourceNames(sqlStatement);
        checkToBeAlteredDuplicateResourceNames(toBeAlteredResourceNames);
        checkResourceNameExisted(schemaName, toBeAlteredResourceNames);
    }
    
    private void validate(final Map<String, DataSourceConfiguration> dataSourceConfigs) throws DistSQLException {
        Collection<String> invalidResources = dataSourceConfigs.entrySet().stream().map(entry -> validateDataSource(entry)).filter(Objects::nonNull).collect(Collectors.toList());
        DistSQLException.predictionThrow(invalidResources.isEmpty(), new InvalidResourceException(invalidResources));
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
    
    private Collection<String> getToBeAlteredResourceNames(final AlterResourceStatement sqlStatement) {
        return sqlStatement.getDataSources().stream().map(DataSourceSegment::getName).collect(Collectors.toList());
    }
    
    private void checkToBeAlteredDuplicateResourceNames(final Collection<String> resourceNames) throws DuplicateResourceException {
        Collection<String> duplicateResourceNames = getDuplicateResourceNames(resourceNames);
        if (!duplicateResourceNames.isEmpty()) {
            throw new DuplicateResourceException(duplicateResourceNames);
        }
    }
    
    private Collection<String> getDuplicateResourceNames(final Collection<String> resourceNames) {
        return resourceNames.stream().filter(each -> resourceNames.stream().filter(each::equals).count() > 1).collect(Collectors.toList());
    }
    
    private void checkResourceNameExisted(final String schemaName, final Collection<String> resourceNames) throws RequiredResourceMissedException {
        Map<String, DataSource> resources = ProxyContext.getInstance().getMetaData(schemaName).getResource().getDataSources();
        Collection<String> notExistedResourceNames = resourceNames.stream().filter(each -> !resources.containsKey(each)).collect(Collectors.toList());
        if (!notExistedResourceNames.isEmpty()) {
            throw new RequiredResourceMissedException(schemaName, notExistedResourceNames);
        }
    }
}
