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

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.DuplicateStorageUnitException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.InvalidStorageUnitsException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.distsql.handler.validate.DataSourcePropertiesValidateHandler;
import org.apache.shardingsphere.distsql.parser.segment.DataSourceSegment;
import org.apache.shardingsphere.distsql.parser.segment.HostnameAndPortBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.parser.segment.URLBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.AlterStorageUnitStatement;
import org.apache.shardingsphere.infra.database.mysql.MySQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class AlterStorageUnitBackendHandlerTest {
    
    @Mock
    private ShardingSphereDatabase database;
    
    private AlterStorageUnitBackendHandler handler;
    
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getProtocolType()).thenReturn(new MySQLDatabaseType());
        handler = new AlterStorageUnitBackendHandler(mock(AlterStorageUnitStatement.class), connectionSession);
        Plugins.getMemberAccessor().set(
                handler.getClass().getDeclaredField("validateHandler"), handler, mock(DataSourcePropertiesValidateHandler.class));
    }
    
    @Test
    void assertExecute() {
        ContextManager contextManager = mockContextManager(mock(MetaDataContexts.class, RETURNS_DEEP_STUBS));
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance().getDatabase("foo_db")).thenReturn(database);
        ShardingSphereResourceMetaData resourceMetaData = mock(ShardingSphereResourceMetaData.class);
        when(resourceMetaData.getDataSources()).thenReturn(Collections.singletonMap("ds_0", mockHikariDataSource("ds_0")));
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        assertThat(handler.execute("foo_db", createAlterStorageUnitStatement("ds_0")), instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    void assertExecuteWithDuplicateStorageUnitNames() {
        assertThrows(DuplicateStorageUnitException.class, () -> handler.execute("foo_db", createAlterStorageUnitStatementWithDuplicateStorageUnitNames()));
    }
    
    @Test
    void assertExecuteWithNotExistedStorageUnitNames() {
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(metaDataContexts.getMetaData().getDatabases()).thenReturn(Collections.singletonMap("foo_db", database));
        ContextManager contextManager = mockContextManager(metaDataContexts);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        assertThrows(MissingRequiredStorageUnitsException.class, () -> handler.execute("foo_db", createAlterStorageUnitStatement("not_existed")));
    }
    
    @Test
    void assertExecuteWithAlterDatabase() {
        ContextManager contextManager = mockContextManager(mock(MetaDataContexts.class, RETURNS_DEEP_STUBS));
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance().getDatabase("foo_db")).thenReturn(database);
        ShardingSphereResourceMetaData resourceMetaData = mock(ShardingSphereResourceMetaData.class);
        when(resourceMetaData.getDataSources()).thenReturn(Collections.singletonMap("ds_0", mockHikariDataSource("ds_1")));
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        assertThrows(InvalidStorageUnitsException.class, () -> handler.execute("foo_db", createAlterStorageUnitStatement("ds_0")));
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
        Collection<DataSourceSegment> result = new LinkedList<>();
        result.add(new HostnameAndPortBasedDataSourceSegment("ds_0", "127.0.0.1", "3306", "ds_0", "root", "", new Properties()));
        result.add(new URLBasedDataSourceSegment("ds_0", "jdbc:mysql://127.0.0.1:3306/ds_1", "root", "", new Properties()));
        return new AlterStorageUnitStatement(result);
    }
    
    private HikariDataSource mockHikariDataSource(final String database) {
        HikariDataSource result = new HikariDataSource();
        result.setJdbcUrl(String.format("jdbc:mysql://127.0.0.1:3306/%s?serverTimezone=UTC&useSSL=false", database));
        return result;
    }
}
