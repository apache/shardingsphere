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

package org.apache.shardingsphere.distsql.handler.executor.rdl.resource;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorDatabaseAware;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.DuplicateStorageUnitException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.InvalidStorageUnitsException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.distsql.handler.engine.update.DistSQLUpdateExecutor;
import org.apache.shardingsphere.distsql.handler.validate.DataSourcePoolPropertiesValidator;
import org.apache.shardingsphere.distsql.segment.DataSourceSegment;
import org.apache.shardingsphere.distsql.segment.HostnameAndPortBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.segment.URLBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.segment.converter.DataSourceSegmentsConverter;
import org.apache.shardingsphere.distsql.statement.rdl.resource.unit.type.AlterStorageUnitStatement;
import org.apache.shardingsphere.infra.database.core.connector.ConnectionProperties;
import org.apache.shardingsphere.infra.database.core.connector.url.JdbcUrl;
import org.apache.shardingsphere.infra.database.core.connector.url.StandardJdbcUrlParser;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.core.external.ShardingSphereExternalException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Alter storage unit executor.
 */
@Setter
@Slf4j
public final class AlterStorageUnitExecutor implements DistSQLUpdateExecutor<AlterStorageUnitStatement>, DistSQLExecutorDatabaseAware {
    
    private final DataSourcePoolPropertiesValidator validateHandler = new DataSourcePoolPropertiesValidator();
    
    private ShardingSphereDatabase database;
    
    @Override
    public void executeUpdate(final AlterStorageUnitStatement sqlStatement, final ContextManager contextManager) {
        checkBefore(sqlStatement);
        Map<String, DataSourcePoolProperties> propsMap = DataSourceSegmentsConverter.convert(database.getProtocolType(), sqlStatement.getStorageUnits());
        validateHandler.validate(propsMap);
        try {
            contextManager.getInstanceContext().getModeContextManager().alterStorageUnits(database.getName(), propsMap);
        } catch (final SQLException | ShardingSphereExternalException ex) {
            log.error("Alter storage unit failed", ex);
            throw new InvalidStorageUnitsException(Collections.singleton(ex.getMessage()));
        }
    }
    
    private void checkBefore(final AlterStorageUnitStatement sqlStatement) {
        Collection<String> toBeAlteredStorageUnitNames = sqlStatement.getStorageUnits().stream().map(DataSourceSegment::getName).collect(Collectors.toList());
        checkDuplicatedStorageUnitNames(toBeAlteredStorageUnitNames);
        checkStorageUnitNameExisted(toBeAlteredStorageUnitNames);
        checkDatabase(sqlStatement);
    }
    
    private void checkDuplicatedStorageUnitNames(final Collection<String> storageUnitNames) {
        Collection<String> duplicatedStorageUnitNames = storageUnitNames.stream().filter(each -> storageUnitNames.stream().filter(each::equals).count() > 1).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(duplicatedStorageUnitNames.isEmpty(), () -> new DuplicateStorageUnitException(duplicatedStorageUnitNames));
    }
    
    private void checkStorageUnitNameExisted(final Collection<String> storageUnitNames) {
        Collection<String> notExistedStorageUnitNames = storageUnitNames.stream().filter(each -> !database.getResourceMetaData().getStorageUnits().containsKey(each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(notExistedStorageUnitNames.isEmpty(), () -> new MissingRequiredStorageUnitsException(database.getName(), notExistedStorageUnitNames));
    }
    
    private void checkDatabase(final AlterStorageUnitStatement sqlStatement) {
        Collection<String> invalidStorageUnitNames = sqlStatement.getStorageUnits().stream().collect(Collectors.toMap(DataSourceSegment::getName, each -> each)).entrySet().stream()
                .filter(each -> !isSameDatabase(each.getValue(), database.getResourceMetaData().getStorageUnits().get(each.getKey()))).map(Entry::getKey).collect(Collectors.toSet());
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
        ConnectionProperties connectionProps = storageUnit.getConnectionProperties();
        return Objects.equals(hostName, connectionProps.getHostname()) && Objects.equals(port, String.valueOf(connectionProps.getPort())) && Objects.equals(database, connectionProps.getCatalog());
    }
    
    @Override
    public Class<AlterStorageUnitStatement> getType() {
        return AlterStorageUnitStatement.class;
    }
}
