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

import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLServerInfo;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.junit.Test;
import org.mockito.MockedStatic;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public final class ShardingSphereProxyVersionTest {
    
    @Test
    public void assertSetVersionWhenStorageTypeSameWithProtocolType() throws SQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getName()).thenReturn("sharding_db");
        ShardingSphereResourceMetaData resourceMetaData = mock(ShardingSphereResourceMetaData.class);
        when(resourceMetaData.getStorageTypes()).thenReturn(Collections.singletonMap("ds_0", DatabaseTypeFactory.getInstance("MySQL")));
        DataSource dataSource = createDataSource("MySQL", "5.7.32");
        when(resourceMetaData.getDataSources()).thenReturn(Collections.singletonMap("ds_0", dataSource));
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        when(database.getProtocolType()).thenReturn(DatabaseTypeFactory.getInstance("MySQL"));
        when(contextManager.getMetaDataContexts().getMetaData().getDatabases()).thenReturn(Collections.singletonMap("sharding_db", database));
        try (MockedStatic<ProxyContext> mockedStatic = mockStatic(ProxyContext.class)) {
            ProxyContext proxyContext = mock(ProxyContext.class, RETURNS_DEEP_STUBS);
            mockedStatic.when(ProxyContext::getInstance).thenReturn(proxyContext);
            when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getProps().getValue(ConfigurationPropertyKey.PROXY_MYSQL_DEFAULT_VERSION)).thenReturn("5.7.22");
            ShardingSphereProxyVersion.setVersion(contextManager);
        }
        assertThat(MySQLServerInfo.getServerVersion("sharding_db"), startsWith("5.7.32"));
    }
    
    @Test
    public void assertSetVersionWhenStorageTypeDifferentWithProtocolType() throws SQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getName()).thenReturn("sharding_db");
        ShardingSphereResourceMetaData resourceMetaData = mock(ShardingSphereResourceMetaData.class);
        when(resourceMetaData.getStorageTypes()).thenReturn(Collections.singletonMap("ds_0", DatabaseTypeEngine.getDatabaseType("Oracle")));
        DataSource dataSource = createDataSource("Oracle", "12.0.0");
        when(resourceMetaData.getDataSources()).thenReturn(Collections.singletonMap("ds_0", dataSource));
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        when(database.getProtocolType()).thenReturn(DatabaseTypeFactory.getInstance("MySQL"));
        when(contextManager.getMetaDataContexts().getMetaData().getDatabases()).thenReturn(Collections.singletonMap("sharding_db", database));
        ShardingSphereProxyVersion.setVersion(contextManager);
        assertThat(MySQLServerInfo.getServerVersion("sharding_db"), startsWith("5.7.22"));
    }
    
    private static DataSource createDataSource(final String databaseProductName, final String databaseProductVersion) throws SQLException {
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
