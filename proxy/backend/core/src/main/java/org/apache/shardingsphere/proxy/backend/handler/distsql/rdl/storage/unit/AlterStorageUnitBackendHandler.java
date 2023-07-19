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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.storage.unit;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.DuplicateStorageUnitException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.InvalidStorageUnitsException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.distsql.handler.validate.DataSourcePropertiesValidateHandler;
import org.apache.shardingsphere.distsql.parser.segment.DataSourceSegment;
import org.apache.shardingsphere.distsql.parser.segment.HostnameAndPortBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.parser.segment.URLBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.parser.segment.converter.DataSourceSegmentsConverter;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.AlterStorageUnitStatement;
import org.apache.shardingsphere.infra.database.core.url.JdbcUrl;
import org.apache.shardingsphere.infra.database.core.url.StandardJdbcUrlParser;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.exception.external.ShardingSphereExternalException;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Alter storage unit backend handler.
 */
@Slf4j
public final class AlterStorageUnitBackendHandler extends StorageUnitDefinitionBackendHandler<AlterStorageUnitStatement> {
    
    private final DatabaseType databaseType;
    
    private final DataSourcePropertiesValidateHandler validateHandler;
    
    public AlterStorageUnitBackendHandler(final AlterStorageUnitStatement sqlStatement, final ConnectionSession connectionSession) {
        super(sqlStatement, connectionSession);
        databaseType = connectionSession.getProtocolType();
        validateHandler = new DataSourcePropertiesValidateHandler();
    }
    
    @Override
    public ResponseHeader execute(final String databaseName, final AlterStorageUnitStatement sqlStatement) {
        checkSQLStatement(databaseName, sqlStatement);
        Map<String, DataSourceProperties> dataSourcePropsMap = DataSourceSegmentsConverter.convert(databaseType, sqlStatement.getStorageUnits());
        validateHandler.validate(dataSourcePropsMap);
        try {
            ProxyContext.getInstance().getContextManager().getInstanceContext().getModeContextManager().alterStorageUnits(databaseName, dataSourcePropsMap);
        } catch (final SQLException | ShardingSphereExternalException ex) {
            log.error("Alter storage unit failed", ex);
            throw new InvalidStorageUnitsException(Collections.singleton(ex.getMessage()));
        }
        return new UpdateResponseHeader(sqlStatement);
    }
    
    @Override
    public void checkSQLStatement(final String databaseName, final AlterStorageUnitStatement sqlStatement) {
        Collection<String> toBeAlteredStorageUnitNames = getToBeAlteredStorageUnitNames(sqlStatement);
        checkDuplicatedStorageUnitNames(toBeAlteredStorageUnitNames);
        checkStorageUnitNameExisted(databaseName, toBeAlteredStorageUnitNames);
        checkDatabase(databaseName, sqlStatement);
    }
    
    private Collection<String> getToBeAlteredStorageUnitNames(final AlterStorageUnitStatement sqlStatement) {
        return sqlStatement.getStorageUnits().stream().map(DataSourceSegment::getName).collect(Collectors.toList());
    }
    
    private void checkDuplicatedStorageUnitNames(final Collection<String> storageUnitNames) {
        Collection<String> duplicatedStorageUnitNames = getDuplicatedStorageUnitNames(storageUnitNames);
        ShardingSpherePreconditions.checkState(duplicatedStorageUnitNames.isEmpty(), () -> new DuplicateStorageUnitException(duplicatedStorageUnitNames));
    }
    
    private Collection<String> getDuplicatedStorageUnitNames(final Collection<String> storageUnitNames) {
        return storageUnitNames.stream().filter(each -> storageUnitNames.stream().filter(each::equals).count() > 1).collect(Collectors.toList());
    }
    
    private void checkStorageUnitNameExisted(final String databaseName, final Collection<String> storageUnitNames) {
        Map<String, DataSource> storageUnits = ProxyContext.getInstance().getDatabase(databaseName).getResourceMetaData().getDataSources();
        Collection<String> notExistedStorageUnitNames = storageUnitNames.stream().filter(each -> !storageUnits.containsKey(each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(notExistedStorageUnitNames.isEmpty(), () -> new MissingRequiredStorageUnitsException(databaseName, notExistedStorageUnitNames));
    }
    
    private void checkDatabase(final String databaseName, final AlterStorageUnitStatement sqlStatement) {
        Map<String, DataSource> storageUnits = ProxyContext.getInstance().getDatabase(databaseName).getResourceMetaData().getDataSources();
        Collection<String> invalidStorageUnitNames = sqlStatement.getStorageUnits().stream().collect(Collectors.toMap(DataSourceSegment::getName, each -> each)).entrySet().stream()
                .filter(each -> !isIdenticalDatabase(each.getValue(), storageUnits.get(each.getKey()))).map(Entry::getKey).collect(Collectors.toSet());
        ShardingSpherePreconditions.checkState(invalidStorageUnitNames.isEmpty(),
                () -> new InvalidStorageUnitsException(Collections.singleton(String.format("Cannot alter the database of %s", invalidStorageUnitNames))));
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
        return Objects.equals(hostName, dataSourceJdbcUrl.getHostname()) && Objects.equals(port, String.valueOf(dataSourceJdbcUrl.getPort()))
                && Objects.equals(database, dataSourceJdbcUrl.getDatabase());
    }
}
