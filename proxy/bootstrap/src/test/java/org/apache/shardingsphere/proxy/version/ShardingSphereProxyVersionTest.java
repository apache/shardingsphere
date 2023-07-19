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

import org.apache.shardingsphere.db.protocol.constant.DatabaseProtocolServerInfo;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
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
    
    private ContextManager mockContextManager(final String databaseType, final String databaseProductVersion) throws SQLException {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mockDatabase(databaseType, databaseProductVersion);
        when(result.getMetaDataContexts().getMetaData().getDatabases()).thenReturn(Collections.singletonMap("foo_db", database));
        return result;
    }
    
    private ShardingSphereDatabase mockDatabase(final String databaseType, final String databaseProductVersion) throws SQLException {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        when(result.getName()).thenReturn("foo_db");
        when(result.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        ShardingSphereResourceMetaData resourceMetaData = mockResourceMetaData(databaseType, databaseProductVersion);
        when(result.getResourceMetaData()).thenReturn(resourceMetaData);
        return result;
    }
    
    private ShardingSphereResourceMetaData mockResourceMetaData(final String databaseType, final String databaseProductVersion) throws SQLException {
        ShardingSphereResourceMetaData result = mock(ShardingSphereResourceMetaData.class);
        when(result.getStorageTypes()).thenReturn(Collections.singletonMap("foo_ds", TypedSPILoader.getService(DatabaseType.class, databaseType)));
        DataSource dataSource = createDataSource(databaseType, databaseProductVersion);
        when(result.getDataSources()).thenReturn(Collections.singletonMap("foo_ds", dataSource));
        return result;
    }
    
    private DataSource createDataSource(final String databaseProductName, final String databaseProductVersion) throws SQLException {
        DataSource result = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(databaseMetaData.getDatabaseProductName()).thenReturn(databaseProductName);
        when(databaseMetaData.getDatabaseProductVersion()).thenReturn(databaseProductVersion);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(result.getConnection()).thenReturn(connection);
        return result;
    }
}
