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

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.DuplicateStorageUnitException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.InvalidStorageUnitsException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.distsql.handler.type.rdl.aware.DatabaseAwareRDLExecutor;
import org.apache.shardingsphere.distsql.handler.validate.DataSourcePoolPropertiesValidateHandler;
import org.apache.shardingsphere.distsql.segment.DataSourceSegment;
import org.apache.shardingsphere.distsql.segment.HostnameAndPortBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.segment.URLBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.segment.converter.DataSourceSegmentsConverter;
import org.apache.shardingsphere.distsql.statement.rdl.alter.AlterStorageUnitStatement;
import org.apache.shardingsphere.infra.database.core.connector.ConnectionProperties;
import org.apache.shardingsphere.infra.database.core.connector.url.JdbcUrl;
import org.apache.shardingsphere.infra.database.core.connector.url.StandardJdbcUrlParser;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.core.external.ShardingSphereExternalException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;

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
@Setter
public final class AlterStorageUnitExecutor implements DatabaseAwareRDLExecutor<AlterStorageUnitStatement> {
    
    private final DataSourcePoolPropertiesValidateHandler validateHandler = new DataSourcePoolPropertiesValidateHandler();
    
    private ShardingSphereDatabase database;
    
    @Override
    public void execute(final AlterStorageUnitStatement sqlStatement) {
        checkSQLStatement(sqlStatement);
        Map<String, DataSourcePoolProperties> propsMap = DataSourceSegmentsConverter.convert(database.getProtocolType(), sqlStatement.getStorageUnits());
        validateHandler.validate(propsMap);
        try {
            ProxyContext.getInstance().getContextManager().getInstanceContext().getModeContextManager().alterStorageUnits(database.getName(), propsMap);
        } catch (final SQLException | ShardingSphereExternalException ex) {
            log.error("Alter storage unit failed", ex);
            throw new InvalidStorageUnitsException(Collections.singleton(ex.getMessage()));
        }
    }
    
    private void checkSQLStatement(final AlterStorageUnitStatement sqlStatement) {
        Collection<String> toBeAlteredStorageUnitNames = getToBeAlteredStorageUnitNames(sqlStatement);
        checkDuplicatedStorageUnitNames(toBeAlteredStorageUnitNames);
        checkStorageUnitNameExisted(toBeAlteredStorageUnitNames);
        checkDatabase(sqlStatement);
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
    
    private void checkStorageUnitNameExisted(final Collection<String> storageUnitNames) {
        Map<String, StorageUnit> storageUnits = database.getResourceMetaData().getStorageUnits();
        Collection<String> notExistedStorageUnitNames = storageUnitNames.stream().filter(each -> !storageUnits.containsKey(each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(notExistedStorageUnitNames.isEmpty(), () -> new MissingRequiredStorageUnitsException(database.getName(), notExistedStorageUnitNames));
    }
    
    private void checkDatabase(final AlterStorageUnitStatement sqlStatement) {
        Map<String, StorageUnit> storageUnits = database.getResourceMetaData().getStorageUnits();
        Collection<String> invalidStorageUnitNames = sqlStatement.getStorageUnits().stream().collect(Collectors.toMap(DataSourceSegment::getName, each -> each)).entrySet().stream()
                .filter(each -> !isSameDatabase(each.getValue(), storageUnits.get(each.getKey()))).map(Entry::getKey).collect(Collectors.toSet());
        ShardingSpherePreconditions.checkState(invalidStorageUnitNames.isEmpty(),
                () -> new InvalidStorageUnitsException(Collections.singleton(String.format("Can not alter the database of %s", invalidStorageUnitNames))));
    }
    
    private boolean isSameDatabase(final DataSourceSegment segment, final StorageUnit storageUnit) {
        String hostName = null;
        String port = null;
        String database = null;
        if (segment instanceof HostnameAndPortBasedDataSourceSegment) {
            hostName = ((HostnameAndPortBasedDataSourceSegment) segment).getHostname();
            port = ((HostnameAndPortBasedDataSourceSegment) segment).getPort();
            database = ((HostnameAndPortBasedDataSourceSegment) segment).getDatabase();
        } else if (segment instanceof URLBasedDataSourceSegment) {
            JdbcUrl segmentJdbcUrl = new StandardJdbcUrlParser().parse(((URLBasedDataSourceSegment) segment).getUrl());
            hostName = segmentJdbcUrl.getHostname();
            port = String.valueOf(segmentJdbcUrl.getPort());
            database = segmentJdbcUrl.getDatabase();
        }
        ConnectionProperties connectionProperties = storageUnit.getConnectionProperties();
        return Objects.equals(hostName, connectionProperties.getHostname()) && Objects.equals(port, String.valueOf(connectionProperties.getPort()))
                && Objects.equals(database, connectionProperties.getCatalog());
    }
    
    @Override
    public Class<AlterStorageUnitStatement> getType() {
        return AlterStorageUnitStatement.class;
    }
}
