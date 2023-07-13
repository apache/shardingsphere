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

package org.apache.shardingsphere.driver.jdbc.core.connection;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.instance.metadata.proxy.ProxyInstanceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.apache.shardingsphere.traffic.rule.TrafficRule;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(DataSourcePoolCreator.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DriverDatabaseConnectionManagerTest {
    
    private DriverDatabaseConnectionManager databaseConnectionManager;
    
    @BeforeEach
    void setUp() throws SQLException {
        databaseConnectionManager = new DriverDatabaseConnectionManager(DefaultDatabase.LOGIC_NAME, mockContextManager());
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private ContextManager mockContextManager() throws SQLException {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        Map<String, DataSource> dataSourceMap = mockDataSourceMap();
        when(result.getDataSourceMap(DefaultDatabase.LOGIC_NAME)).thenReturn(dataSourceMap);
        MetaDataPersistService persistService = mockMetaDataPersistService();
        when(result.getMetaDataContexts().getPersistService()).thenReturn(persistService);
        when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(
                new ShardingSphereRuleMetaData(Arrays.asList(mock(TransactionRule.class, RETURNS_DEEP_STUBS), mock(TrafficRule.class, RETURNS_DEEP_STUBS))));
        when(result.getInstanceContext().getAllClusterInstances(InstanceType.PROXY, Arrays.asList("OLTP", "OLAP"))).thenReturn(
                Collections.singletonList(new ProxyInstanceMetaData("foo_id", "127.0.0.1@3307", "foo_version")));
        Map<String, DataSource> trafficDataSourceMap = mockTrafficDataSourceMap();
        when(DataSourcePoolCreator.create((Map) any())).thenReturn(trafficDataSourceMap);
        return result;
    }
    
    private Map<String, DataSource> mockDataSourceMap() throws SQLException {
        Map<String, DataSource> result = new HashMap<>(2, 1F);
        result.put("ds", new MockedDataSource());
        DataSource invalidDataSource = mock(DataSource.class);
        when(invalidDataSource.getConnection()).thenThrow(new SQLException());
        result.put("invalid_ds", invalidDataSource);
        return result;
    }
    
    private MetaDataPersistService mockMetaDataPersistService() {
        MetaDataPersistService result = mock(MetaDataPersistService.class, RETURNS_DEEP_STUBS);
        when(result.getDataSourceUnitService().load(DefaultDatabase.LOGIC_NAME))
                .thenReturn(Collections.singletonMap(DefaultDatabase.LOGIC_NAME, new DataSourceProperties(HikariDataSource.class.getName(), createProperties())));
        when(result.getGlobalRuleService().loadUsers()).thenReturn(Collections.singletonList(new ShardingSphereUser("root", "root", "localhost")));
        return result;
    }
    
    private Map<String, Object> createProperties() {
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        result.put("jdbcUrl", "jdbc:mysql://127.0.0.1:3306/demo_ds_0?serverTimezone=UTC&useSSL=false");
        result.put("username", "root");
        result.put("password", "123456");
        return result;
    }
    
    private Map<String, DataSource> mockTrafficDataSourceMap() throws SQLException {
        MockedDataSource result = new MockedDataSource(mock(Connection.class, RETURNS_DEEP_STUBS));
        result.setUrl("jdbc:mysql://127.0.0.1:3307/logic_db?serverTimezone=UTC&useSSL=false");
        result.setUsername("root");
        result.setPassword("123456");
        when(result.getConnection().getMetaData().getURL()).thenReturn(result.getUrl());
        when(result.getConnection().getMetaData().getUserName()).thenReturn(result.getUsername());
        return Collections.singletonMap("127.0.0.1@3307", result);
    }
    
    @Test
    void assertGetRandomPhysicalDataSourceNameFromContextManager() {
        String actual = databaseConnectionManager.getRandomPhysicalDataSourceName();
        assertTrue(Arrays.asList("ds", "invalid_ds").contains(actual));
    }
    
    @Test
    void assertGetRandomPhysicalDataSourceNameFromCache() throws SQLException {
        databaseConnectionManager.getConnections("ds", 0, 1, ConnectionMode.MEMORY_STRICTLY);
        String actual = databaseConnectionManager.getRandomPhysicalDataSourceName();
        assertThat(actual, is("ds"));
    }
    
    @Test
    void assertGetConnection() throws SQLException {
        assertThat(databaseConnectionManager.getConnections("ds", 0, 1, ConnectionMode.MEMORY_STRICTLY),
                is(databaseConnectionManager.getConnections("ds", 0, 1, ConnectionMode.MEMORY_STRICTLY)));
    }
    
    @Test
    void assertGetConnectionWithConnectionOffset() throws SQLException {
        assertThat(databaseConnectionManager.getConnections("ds", 0, 1, ConnectionMode.MEMORY_STRICTLY),
                is(databaseConnectionManager.getConnections("ds", 0, 1, ConnectionMode.MEMORY_STRICTLY)));
        assertThat(databaseConnectionManager.getConnections("ds", 1, 1, ConnectionMode.MEMORY_STRICTLY),
                is(databaseConnectionManager.getConnections("ds", 1, 1, ConnectionMode.MEMORY_STRICTLY)));
        assertThat(databaseConnectionManager.getConnections("ds", 0, 1, ConnectionMode.MEMORY_STRICTLY),
                not(databaseConnectionManager.getConnections("ds", 1, 1, ConnectionMode.MEMORY_STRICTLY)));
    }
    
    @Test
    void assertGetConnectionWhenConfigTrafficRule() throws SQLException {
        List<Connection> actual = databaseConnectionManager.getConnections("127.0.0.1@3307", 0, 1, ConnectionMode.MEMORY_STRICTLY);
        assertThat(actual, is(databaseConnectionManager.getConnections("127.0.0.1@3307", 0, 1, ConnectionMode.MEMORY_STRICTLY)));
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getMetaData().getUserName(), is("root"));
        assertThat(actual.get(0).getMetaData().getURL(), is("jdbc:mysql://127.0.0.1:3307/logic_db?serverTimezone=UTC&useSSL=false"));
    }
    
    @Test
    void assertGetConnectionsWhenAllInCache() throws SQLException {
        Connection expected = databaseConnectionManager.getConnections("ds", 0, 1, ConnectionMode.MEMORY_STRICTLY).get(0);
        List<Connection> actual = databaseConnectionManager.getConnections("ds", 0, 1, ConnectionMode.CONNECTION_STRICTLY);
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0), is(expected));
    }
    
    @Test
    void assertGetConnectionsWhenConfigTrafficRuleAndAllInCache() throws SQLException {
        Connection expected = databaseConnectionManager.getConnections("127.0.0.1@3307", 0, 1, ConnectionMode.MEMORY_STRICTLY).get(0);
        List<Connection> actual = databaseConnectionManager.getConnections("127.0.0.1@3307", 0, 1, ConnectionMode.CONNECTION_STRICTLY);
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0), is(expected));
        assertThat(actual.get(0).getMetaData().getUserName(), is("root"));
        assertThat(actual.get(0).getMetaData().getURL(), is("jdbc:mysql://127.0.0.1:3307/logic_db?serverTimezone=UTC&useSSL=false"));
    }
    
    @Test
    void assertGetConnectionsWhenEmptyCache() throws SQLException {
        List<Connection> actual = databaseConnectionManager.getConnections("ds", 0, 1, ConnectionMode.MEMORY_STRICTLY);
        assertThat(actual.size(), is(1));
    }
    
    @Test
    void assertGetConnectionsWhenConfigTrafficRuleAndEmptyCache() throws SQLException {
        List<Connection> actual = databaseConnectionManager.getConnections("127.0.0.1@3307", 0, 1, ConnectionMode.MEMORY_STRICTLY);
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getMetaData().getUserName(), is("root"));
        assertThat(actual.get(0).getMetaData().getURL(), is("jdbc:mysql://127.0.0.1:3307/logic_db?serverTimezone=UTC&useSSL=false"));
    }
    
    @Test
    void assertGetConnectionsWhenPartInCacheWithMemoryStrictlyMode() throws SQLException {
        databaseConnectionManager.getConnections("ds", 0, 1, ConnectionMode.MEMORY_STRICTLY);
        List<Connection> actual = databaseConnectionManager.getConnections("ds", 0, 3, ConnectionMode.MEMORY_STRICTLY);
        assertThat(actual.size(), is(3));
    }
    
    @Test
    void assertGetConnectionsWhenPartInCacheWithConnectionStrictlyMode() throws SQLException {
        databaseConnectionManager.getConnections("ds", 0, 1, ConnectionMode.MEMORY_STRICTLY);
        List<Connection> actual = databaseConnectionManager.getConnections("ds", 0, 3, ConnectionMode.CONNECTION_STRICTLY);
        assertThat(actual.size(), is(3));
    }
    
    @Test
    void assertGetConnectionsWhenConnectionCreateFailed() {
        SQLException ex = assertThrows(SQLException.class, () -> databaseConnectionManager.getConnections("invalid_ds", 0, 3, ConnectionMode.CONNECTION_STRICTLY));
        assertThat(ex.getMessage(), is("Can not get 3 connections one time, partition succeed connection(0) have released. "
                + "Please consider increasing the `maxPoolSize` of the data sources or decreasing the `max-connections-size-per-query` in properties."));
    }
}
