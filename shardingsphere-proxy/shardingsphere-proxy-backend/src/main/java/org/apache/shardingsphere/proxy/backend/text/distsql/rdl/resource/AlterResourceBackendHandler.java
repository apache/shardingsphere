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

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.distsql.parser.segment.DataSourceSegment;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.AlterResourceStatement;
import org.apache.shardingsphere.infra.database.metadata.url.JdbcUrl;
import org.apache.shardingsphere.infra.database.metadata.url.StandardJdbcUrlParser;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesValidator;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.resource.DuplicateResourceException;
import org.apache.shardingsphere.infra.distsql.exception.resource.InvalidResourcesException;
import org.apache.shardingsphere.infra.distsql.exception.resource.RequiredResourceMissedException;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.SchemaRequiredBackendHandler;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Alter resource backend handler.
 */
@Slf4j
public final class AlterResourceBackendHandler extends SchemaRequiredBackendHandler<AlterResourceStatement> {
    
    private final DatabaseType databaseType;
    
    private final DataSourcePropertiesValidator validator;
    
    public AlterResourceBackendHandler(final DatabaseType databaseType, final AlterResourceStatement sqlStatement, final ConnectionSession connectionSession) {
        super(sqlStatement, connectionSession);
        this.databaseType = databaseType;
        validator = new DataSourcePropertiesValidator();
    }
    
    @Override
    public ResponseHeader execute(final String schemaName, final AlterResourceStatement sqlStatement) throws DistSQLException {
        checkSQLStatement(schemaName, sqlStatement);
        Map<String, DataSourceProperties> dataSourcePropsMap = ResourceSegmentsConverter.convert(databaseType, sqlStatement.getDataSources());
        validator.validate(dataSourcePropsMap);
        try {
            ProxyContext.getInstance().getContextManager().alterResource(schemaName, dataSourcePropsMap);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            log.error("Alter resource failed", ex);
            throw new InvalidResourcesException(Collections.singleton(ex.getMessage()));
        }
        return new UpdateResponseHeader(sqlStatement);
    }
    
    private void checkSQLStatement(final String schemaName, final AlterResourceStatement sqlStatement) throws DistSQLException {
        Collection<String> toBeAlteredResourceNames = getToBeAlteredResourceNames(sqlStatement);
        checkToBeAlteredDuplicateResourceNames(toBeAlteredResourceNames);
        checkResourceNameExisted(schemaName, toBeAlteredResourceNames);
        checkDatabase(schemaName, sqlStatement);
    }
    
    private void checkDatabase(final String schemaName, final AlterResourceStatement sqlStatement) throws DistSQLException {
        Map<String, DataSource> resources = ProxyContext.getInstance().getMetaData(schemaName).getResource().getDataSources();
        Set<String> invalid = sqlStatement.getDataSources().stream().collect(Collectors.toMap(DataSourceSegment::getName, each -> each)).entrySet().stream()
                .filter(each -> !isIdenticalDatabase(each.getValue(), resources.get(each.getKey()))).map(Entry::getKey).collect(Collectors.toSet());
        DistSQLException.predictionThrow(invalid.isEmpty(), () -> new InvalidResourcesException(Collections.singleton(String.format("Cannot alter the database of %s", invalid))));
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
    
    private boolean isIdenticalDatabase(final DataSourceSegment segment, final DataSource dataSource) {
        String hostName = segment.getHostname();
        String port = segment.getPort();
        String database = segment.getDatabase();
        if (null != segment.getUrl() && (null == hostName || null == port || null == database)) {
            JdbcUrl segmentJdbcUrl = new StandardJdbcUrlParser().parse(segment.getUrl());
            hostName = segmentJdbcUrl.getHostname();
            port = String.valueOf(segmentJdbcUrl.getPort());
            database = segmentJdbcUrl.getDatabase();
        }
        DataSourceProperties dataSourceProperties = DataSourcePropertiesCreator.create(dataSource);
        String url = String.valueOf(dataSourceProperties.getConnectionPropertySynonyms().getStandardProperties().get("url"));
        JdbcUrl dataSourceJdbcUrl = new StandardJdbcUrlParser().parse(url);
        return hostName.equals(dataSourceJdbcUrl.getHostname()) && port.equals(String.valueOf(dataSourceJdbcUrl.getPort())) && database.equals(dataSourceJdbcUrl.getDatabase());
    }
}
