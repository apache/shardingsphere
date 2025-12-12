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

package org.apache.shardingsphere.proxy.version;

import org.apache.groovy.util.Maps;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.protocol.constant.DatabaseProtocolServerInfo;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ShardingSphereProxyVersionTest {
    
    @Test
    void assertSetVersionWhenStorageTypeSameWithProtocolType() throws SQLException {
        ShardingSphereProxyVersion.setVersion(mockContextManager("MySQL", "5.7.22"));
        assertThat(DatabaseProtocolServerInfo.getProtocolVersion("foo_db", TypedSPILoader.getService(DatabaseType.class, "MySQL")), startsWith("5.7.22"));
    }
    
    @Test
    void assertSetVersionWhenStorageTypeDifferentWithProtocolType() throws SQLException {
        ShardingSphereProxyVersion.setVersion(mockContextManager("Oracle", "12.0.0"));
        assertThat(DatabaseProtocolServerInfo.getProtocolVersion("foo_db", TypedSPILoader.getService(DatabaseType.class, "MySQL")), startsWith("5.7.22"));
    }
    
    private ContextManager mockContextManager(final String databaseProductName, final String databaseProductVersion) throws SQLException {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mockDatabase(databaseProductName, databaseProductVersion);
        when(result.getMetaDataContexts().getMetaData().getAllDatabases()).thenReturn(Collections.singleton(database));
        return result;
    }
    
    private ShardingSphereDatabase mockDatabase(final String databaseProductName, final String databaseProductVersion) throws SQLException {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        when(result.getName()).thenReturn("foo_db");
        when(result.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        ResourceMetaData resourceMetaData = mockResourceMetaData(databaseProductName, databaseProductVersion);
        when(result.getResourceMetaData()).thenReturn(resourceMetaData);
        return result;
    }
    
    private ResourceMetaData mockResourceMetaData(final String databaseProductName, final String databaseProductVersion) throws SQLException {
        ResourceMetaData result = mock(ResourceMetaData.class, RETURNS_DEEP_STUBS);
        DataSource dataSource = createDataSource(databaseProductName, databaseProductVersion);
        DataSourcePoolProperties dataSourcePoolProps = mock(DataSourcePoolProperties.class, RETURNS_DEEP_STUBS);
        when(dataSourcePoolProps.getConnectionPropertySynonyms().getStandardProperties()).thenReturn(Maps.of("url", "jdbc:mock://127.0.0.1/foo_db", "username", "test"));
        StorageUnit storageUnit = new StorageUnit(mock(StorageNode.class), dataSourcePoolProps, dataSource);
        when(result.getStorageUnits()).thenReturn(Collections.singletonMap("foo_ds", storageUnit));
        return result;
    }
    
    private DataSource createDataSource(final String databaseProductName, final String databaseProductVersion) throws SQLException {
        Connection connection = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(databaseMetaData.getDatabaseProductName()).thenReturn(databaseProductName);
        when(databaseMetaData.getDatabaseProductVersion()).thenReturn(databaseProductVersion);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(connection.getMetaData().getURL()).thenReturn("jdbc:mock://127.0.0.1/foo_ds");
        return new MockedDataSource(connection);
    }
}
