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
import org.apache.shardingsphere.database.connector.core.checker.PrivilegeCheckType;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionProperties;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionPropertiesParser;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorDatabaseAware;
import org.apache.shardingsphere.distsql.handler.engine.update.DistSQLUpdateExecutor;
import org.apache.shardingsphere.distsql.handler.validate.DistSQLDataSourcePoolPropertiesValidator;
import org.apache.shardingsphere.distsql.segment.DataSourceSegment;
import org.apache.shardingsphere.distsql.segment.HostnameAndPortBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.segment.URLBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.segment.converter.DataSourceSegmentsConverter;
import org.apache.shardingsphere.distsql.statement.type.rdl.resource.unit.type.AlterStorageUnitStatement;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.external.ShardingSphereExternalException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.AlterStorageUnitConnectionInfoException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.DuplicateStorageUnitException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.StorageUnitsOperateException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Alter storage unit executor.
 */
@Setter
public final class AlterStorageUnitExecutor implements DistSQLUpdateExecutor<AlterStorageUnitStatement>, DistSQLExecutorDatabaseAware {
    
    private final DistSQLDataSourcePoolPropertiesValidator validateHandler = new DistSQLDataSourcePoolPropertiesValidator();
    
    private ShardingSphereDatabase database;
    
    @Override
    public void executeUpdate(final AlterStorageUnitStatement sqlStatement, final ContextManager contextManager) {
        checkBefore(sqlStatement);
        Map<String, DataSourcePoolProperties> propsMap = DataSourceSegmentsConverter.convert(database.getProtocolType(), sqlStatement.getStorageUnits());
        validateHandler.validate(propsMap, getExpectedPrivileges(sqlStatement));
        try {
            contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService().alterStorageUnits(database, propsMap);
        } catch (final ShardingSphereExternalException ex) {
            throw new StorageUnitsOperateException("alter", propsMap.keySet(), ex);
        }
    }
    
    private void checkBefore(final AlterStorageUnitStatement sqlStatement) {
        Collection<String> toBeAlteredStorageUnitNames = sqlStatement.getStorageUnits().stream().map(DataSourceSegment::getName).collect(Collectors.toList());
        checkDuplicatedStorageUnitNames(toBeAlteredStorageUnitNames);
        checkStorageUnitNameExisted(toBeAlteredStorageUnitNames);
        checkDatabase(sqlStatement);
    }
    
    private void checkDuplicatedStorageUnitNames(final Collection<String> storageUnitNames) {
        Collection<String> duplicatedStorageUnitNames = storageUnitNames.stream().filter(each -> storageUnitNames.stream().filter(each::equals).count() > 1L).collect(Collectors.toList());
        ShardingSpherePreconditions.checkMustEmpty(duplicatedStorageUnitNames, () -> new DuplicateStorageUnitException(database.getName(), duplicatedStorageUnitNames));
    }
    
    private void checkStorageUnitNameExisted(final Collection<String> storageUnitNames) {
        Collection<String> notExistedStorageUnitNames = storageUnitNames.stream().filter(each -> !database.getResourceMetaData().getStorageUnits().containsKey(each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkMustEmpty(notExistedStorageUnitNames, () -> new MissingRequiredStorageUnitsException(database.getName(), notExistedStorageUnitNames));
    }
    
    private void checkDatabase(final AlterStorageUnitStatement sqlStatement) {
        Collection<String> invalidStorageUnitNames = sqlStatement.getStorageUnits().stream().collect(Collectors.toMap(DataSourceSegment::getName, each -> each)).entrySet().stream()
                .filter(each -> !isSameDatabase(each.getValue(), database.getResourceMetaData().getStorageUnits().get(each.getKey()))).map(Entry::getKey).collect(Collectors.toSet());
        ShardingSpherePreconditions.checkMustEmpty(invalidStorageUnitNames, () -> new AlterStorageUnitConnectionInfoException(invalidStorageUnitNames));
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
            String url = ((URLBasedDataSourceSegment) segment).getUrl();
            ConnectionProperties connectionProps = DatabaseTypedSPILoader.getService(ConnectionPropertiesParser.class, DatabaseTypeFactory.get(url)).parse(url, segment.getUser(), null);
            hostName = connectionProps.getHostname();
            port = String.valueOf(connectionProps.getPort());
            database = connectionProps.getCatalog();
        }
        ConnectionProperties connectionProps = storageUnit.getConnectionProperties();
        return Objects.equals(hostName, connectionProps.getHostname()) && Objects.equals(port, String.valueOf(connectionProps.getPort())) && Objects.equals(database, connectionProps.getCatalog());
    }
    
    private Collection<PrivilegeCheckType> getExpectedPrivileges(final AlterStorageUnitStatement sqlStatement) {
        Collection<PrivilegeCheckType> result = sqlStatement.getExpectedPrivileges().stream().map(each -> PrivilegeCheckType.valueOf(each.toUpperCase())).collect(Collectors.toSet());
        if (result.isEmpty()) {
            result.add(PrivilegeCheckType.SELECT);
        }
        return result;
    }
    
    @Override
    public Class<AlterStorageUnitStatement> getType() {
        return AlterStorageUnitStatement.class;
    }
}
