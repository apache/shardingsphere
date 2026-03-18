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

import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionProperties;
import org.apache.shardingsphere.distsql.handler.validate.DistSQLDataSourcePoolPropertiesValidator;
import org.apache.shardingsphere.distsql.segment.HostnameAndPortBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.segment.URLBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.statement.type.rdl.resource.unit.type.AlterStorageUnitStatement;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.AlterStorageUnitConnectionInfoException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.DuplicateStorageUnitException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlterStorageUnitExecutorTest {
    
    private final AlterStorageUnitExecutor executor = new AlterStorageUnitExecutor();
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        executor.setDatabase(database);
        Plugins.getMemberAccessor().set(AlterStorageUnitExecutor.class.getDeclaredField("validateHandler"), executor, mock(DistSQLDataSourcePoolPropertiesValidator.class));
    }
    
    @Test
    void assertExecuteUpdateSuccess() {
        ResourceMetaData resourceMetaData = mock(ResourceMetaData.class, RETURNS_DEEP_STUBS);
        StorageUnit storageUnit = mock(StorageUnit.class, RETURNS_DEEP_STUBS);
        ConnectionProperties connectionProps = mockConnectionProperties("ds_0");
        when(storageUnit.getConnectionProperties()).thenReturn(connectionProps);
        when(resourceMetaData.getStorageUnits()).thenReturn(Collections.singletonMap("ds_0", storageUnit));
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        assertDoesNotThrow(() -> executor.executeUpdate(createAlterStorageUnitStatement("ds_0"), mockContextManager(mock(MetaDataContexts.class, RETURNS_DEEP_STUBS))));
    }
    
    @Test
    void assertExecuteUpdateWithDuplicateStorageUnitNames() {
        assertThrows(DuplicateStorageUnitException.class, () -> executor.executeUpdate(createAlterStorageUnitStatementWithDuplicateStorageUnitNames(), mock(ContextManager.class)));
    }
    
    @Test
    void assertExecuteUpdateWithNotExistedStorageUnitNames() {
        assertThrows(MissingRequiredStorageUnitsException.class,
                () -> executor.executeUpdate(createAlterStorageUnitStatement("not_existed"), mockContextManager(mock(MetaDataContexts.class, RETURNS_DEEP_STUBS))));
    }
    
    @Test
    void assertExecuteUpdateWithAlterDatabase() {
        ResourceMetaData resourceMetaData = mock(ResourceMetaData.class, RETURNS_DEEP_STUBS);
        StorageUnit storageUnit = mock(StorageUnit.class, RETURNS_DEEP_STUBS);
        ConnectionProperties connectionProps = mockConnectionProperties("ds_1");
        when(storageUnit.getConnectionProperties()).thenReturn(connectionProps);
        when(resourceMetaData.getStorageUnits()).thenReturn(Collections.singletonMap("ds_0", storageUnit));
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        assertThrows(AlterStorageUnitConnectionInfoException.class,
                () -> executor.executeUpdate(createAlterStorageUnitStatement("ds_0"), mockContextManager(mock(MetaDataContexts.class, RETURNS_DEEP_STUBS))));
    }
    
    private ContextManager mockContextManager(final MetaDataContexts metaDataContexts) {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts()).thenReturn(metaDataContexts);
        return result;
    }
    
    private AlterStorageUnitStatement createAlterStorageUnitStatement(final String resourceName) {
        return new AlterStorageUnitStatement(Collections.singleton(new URLBasedDataSourceSegment(resourceName, "jdbc:mock://127.0.0.1:3306/ds_0", "root", "", new Properties())),
                Collections.emptySet());
    }
    
    private AlterStorageUnitStatement createAlterStorageUnitStatementWithDuplicateStorageUnitNames() {
        return new AlterStorageUnitStatement(Arrays.asList(
                new HostnameAndPortBasedDataSourceSegment("ds_0", "127.0.0.1", "3306", "ds_0", "root", "", new Properties()),
                new URLBasedDataSourceSegment("ds_0", "jdbc:mock://127.0.0.1:3306/ds_1", "root", "", new Properties())), Collections.emptySet());
    }
    
    private ConnectionProperties mockConnectionProperties(final String catalog) {
        ConnectionProperties result = mock(ConnectionProperties.class);
        when(result.getHostname()).thenReturn("127.0.0.1");
        when(result.getPort()).thenReturn(3306);
        when(result.getCatalog()).thenReturn(catalog);
        return result;
    }
}
