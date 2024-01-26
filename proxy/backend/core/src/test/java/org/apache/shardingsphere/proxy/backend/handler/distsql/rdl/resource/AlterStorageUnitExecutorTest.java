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

import org.apache.shardingsphere.distsql.handler.exception.storageunit.DuplicateStorageUnitException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.InvalidStorageUnitsException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.distsql.handler.validate.DataSourcePoolPropertiesValidator;
import org.apache.shardingsphere.distsql.segment.HostnameAndPortBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.segment.URLBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.statement.rdl.resource.unit.type.AlterStorageUnitStatement;
import org.apache.shardingsphere.infra.database.core.connector.ConnectionProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.resource.type.AlterStorageUnitExecutor;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class AlterStorageUnitExecutorTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    private AlterStorageUnitExecutor executor;
    
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        executor = new AlterStorageUnitExecutor();
        Plugins.getMemberAccessor().set(executor.getClass().getDeclaredField("validateHandler"), executor, mock(DataSourcePoolPropertiesValidator.class));
    }
    
    @Test
    void assertExecute() {
        ContextManager contextManager = mockContextManager(mock(MetaDataContexts.class, RETURNS_DEEP_STUBS));
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        ResourceMetaData resourceMetaData = mock(ResourceMetaData.class, RETURNS_DEEP_STUBS);
        StorageUnit storageUnit = mock(StorageUnit.class, RETURNS_DEEP_STUBS);
        ConnectionProperties connectionProperties = mockConnectionProperties("ds_0");
        when(storageUnit.getConnectionProperties()).thenReturn(connectionProperties);
        when(resourceMetaData.getStorageUnits()).thenReturn(Collections.singletonMap("ds_0", storageUnit));
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        executor.setDatabase(database);
        assertDoesNotThrow(() -> executor.execute(createAlterStorageUnitStatement("ds_0"), mock(ContextManager.class)));
    }
    
    @Test
    void assertExecuteWithDuplicateStorageUnitNames() {
        executor.setDatabase(database);
        assertThrows(DuplicateStorageUnitException.class, () -> executor.execute(createAlterStorageUnitStatementWithDuplicateStorageUnitNames(), mock(ContextManager.class)));
    }
    
    @Test
    void assertExecuteWithNotExistedStorageUnitNames() {
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        ContextManager contextManager = mockContextManager(metaDataContexts);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        executor.setDatabase(database);
        assertThrows(MissingRequiredStorageUnitsException.class, () -> executor.execute(createAlterStorageUnitStatement("not_existed"), mock(ContextManager.class)));
    }
    
    @Test
    void assertExecuteWithAlterDatabase() {
        ContextManager contextManager = mockContextManager(mock(MetaDataContexts.class, RETURNS_DEEP_STUBS));
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        ResourceMetaData resourceMetaData = mock(ResourceMetaData.class, RETURNS_DEEP_STUBS);
        StorageUnit storageUnit = mock(StorageUnit.class, RETURNS_DEEP_STUBS);
        ConnectionProperties connectionProperties = mockConnectionProperties("ds_1");
        when(storageUnit.getConnectionProperties()).thenReturn(connectionProperties);
        when(resourceMetaData.getStorageUnits()).thenReturn(Collections.singletonMap("ds_0", storageUnit));
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        executor.setDatabase(database);
        assertThrows(InvalidStorageUnitsException.class, () -> executor.execute(createAlterStorageUnitStatement("ds_0"), mock(ContextManager.class)));
    }
    
    private ContextManager mockContextManager(final MetaDataContexts metaDataContexts) {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts()).thenReturn(metaDataContexts);
        return result;
    }
    
    private AlterStorageUnitStatement createAlterStorageUnitStatement(final String resourceName) {
        return new AlterStorageUnitStatement(Collections.singleton(new URLBasedDataSourceSegment(resourceName, "jdbc:mysql://127.0.0.1:3306/ds_0", "root", "", new Properties())));
    }
    
    private AlterStorageUnitStatement createAlterStorageUnitStatementWithDuplicateStorageUnitNames() {
        return new AlterStorageUnitStatement(Arrays.asList(
                new HostnameAndPortBasedDataSourceSegment("ds_0", "127.0.0.1", "3306", "ds_0", "root", "", new Properties()),
                new URLBasedDataSourceSegment("ds_0", "jdbc:mysql://127.0.0.1:3306/ds_1", "root", "", new Properties())));
    }
    
    private ConnectionProperties mockConnectionProperties(final String catalog) {
        ConnectionProperties result = mock(ConnectionProperties.class);
        when(result.getHostname()).thenReturn("127.0.0.1");
        when(result.getPort()).thenReturn(3306);
        when(result.getCatalog()).thenReturn(catalog);
        return result;
    }
}
