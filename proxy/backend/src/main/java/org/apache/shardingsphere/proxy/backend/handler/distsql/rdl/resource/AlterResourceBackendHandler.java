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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.resource;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.distsql.parser.segment.DataSourceSegment;
import org.apache.shardingsphere.distsql.parser.segment.HostnameAndPortBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.parser.segment.URLBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.AlterStorageUnitStatement;
import org.apache.shardingsphere.infra.database.metadata.url.JdbcUrl;
import org.apache.shardingsphere.infra.database.metadata.url.StandardJdbcUrlParser;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesValidator;
import org.apache.shardingsphere.infra.distsql.exception.resource.DuplicateResourceException;
import org.apache.shardingsphere.infra.distsql.exception.resource.InvalidResourcesException;
import org.apache.shardingsphere.infra.distsql.exception.resource.MissingRequiredResourcesException;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.exception.external.server.ShardingSphereServerException;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.DatabaseRequiredBackendHandler;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sharding.distsql.handler.converter.ResourceSegmentsConverter;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Alter resource backend handler.
 */
@Slf4j
public final class AlterResourceBackendHandler extends DatabaseRequiredBackendHandler<AlterStorageUnitStatement> {
    
    private final DatabaseType databaseType;
    
    private final DataSourcePropertiesValidator validator;
    
    public AlterResourceBackendHandler(final AlterStorageUnitStatement sqlStatement, final ConnectionSession connectionSession) {
        super(sqlStatement, connectionSession);
        databaseType = connectionSession.getProtocolType();
        validator = new DataSourcePropertiesValidator();
    }
    
    @Override
    public ResponseHeader execute(final String databaseName, final AlterStorageUnitStatement sqlStatement) {
        checkSQLStatement(databaseName, sqlStatement);
        Map<String, DataSourceProperties> dataSourcePropsMap = ResourceSegmentsConverter.convert(databaseType, sqlStatement.getDataSources());
        validator.validate(dataSourcePropsMap, databaseType);
        try {
            ProxyContext.getInstance().getContextManager().updateResources(databaseName, dataSourcePropsMap);
        } catch (final SQLException | ShardingSphereServerException ex) {
            log.error("Alter resource failed", ex);
            throw new InvalidResourcesException(Collections.singleton(ex.getMessage()));
        }
        return new UpdateResponseHeader(sqlStatement);
    }
    
    private void checkSQLStatement(final String databaseName, final AlterStorageUnitStatement sqlStatement) {
        Collection<String> toBeAlteredResourceNames = getToBeAlteredResourceNames(sqlStatement);
        checkToBeAlteredDuplicateResourceNames(toBeAlteredResourceNames);
        checkResourceNameExisted(databaseName, toBeAlteredResourceNames);
        checkDatabase(databaseName, sqlStatement);
    }
    
    private void checkDatabase(final String databaseName, final AlterStorageUnitStatement sqlStatement) {
        Map<String, DataSource> resources = ProxyContext.getInstance().getDatabase(databaseName).getResourceMetaData().getDataSources();
        Collection<String> invalid = sqlStatement.getDataSources().stream().collect(Collectors.toMap(DataSourceSegment::getName, each -> each)).entrySet().stream()
                .filter(each -> !isIdenticalDatabase(each.getValue(), resources.get(each.getKey()))).map(Entry::getKey).collect(Collectors.toSet());
        ShardingSpherePreconditions.checkState(invalid.isEmpty(), () -> new InvalidResourcesException(Collections.singleton(String.format("Cannot alter the database of %s", invalid))));
    }
    
    private Collection<String> getToBeAlteredResourceNames(final AlterStorageUnitStatement sqlStatement) {
        return sqlStatement.getDataSources().stream().map(DataSourceSegment::getName).collect(Collectors.toList());
    }
    
    private void checkToBeAlteredDuplicateResourceNames(final Collection<String> resourceNames) {
        Collection<String> duplicateResourceNames = getDuplicateResourceNames(resourceNames);
        ShardingSpherePreconditions.checkState(duplicateResourceNames.isEmpty(), () -> new DuplicateResourceException(duplicateResourceNames));
    }
    
    private Collection<String> getDuplicateResourceNames(final Collection<String> resourceNames) {
        return resourceNames.stream().filter(each -> resourceNames.stream().filter(each::equals).count() > 1).collect(Collectors.toList());
    }
    
    private void checkResourceNameExisted(final String databaseName, final Collection<String> resourceNames) {
        Map<String, DataSource> resources = ProxyContext.getInstance().getDatabase(databaseName).getResourceMetaData().getDataSources();
        Collection<String> notExistedResourceNames = resourceNames.stream().filter(each -> !resources.containsKey(each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(notExistedResourceNames.isEmpty(), () -> new MissingRequiredResourcesException(databaseName, notExistedResourceNames));
    }
    
    private boolean isIdenticalDatabase(final DataSourceSegment segment, final DataSource dataSource) {
        String hostName = null;
        String port = null;
        String database = null;
        if (segment instanceof HostnameAndPortBasedDataSourceSegment) {
            hostName = ((HostnameAndPortBasedDataSourceSegment) segment).getHostname();
            port = ((HostnameAndPortBasedDataSourceSegment) segment).getPort();
            database = ((HostnameAndPortBasedDataSourceSegment) segment).getDatabase();
        }
        if (segment instanceof URLBasedDataSourceSegment) {
            JdbcUrl segmentJdbcUrl = new StandardJdbcUrlParser().parse(((URLBasedDataSourceSegment) segment).getUrl());
            hostName = segmentJdbcUrl.getHostname();
            port = String.valueOf(segmentJdbcUrl.getPort());
            database = segmentJdbcUrl.getDatabase();
        }
        String url = String.valueOf(DataSourcePropertiesCreator.create(dataSource).getConnectionPropertySynonyms().getStandardProperties().get("url"));
        JdbcUrl dataSourceJdbcUrl = new StandardJdbcUrlParser().parse(url);
        return hostName.equals(dataSourceJdbcUrl.getHostname()) && port.equals(String.valueOf(dataSourceJdbcUrl.getPort())) && database.equals(dataSourceJdbcUrl.getDatabase());
    }
}
