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
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistFacade;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DriverDatabaseConnectionManagerTest {
    
    private DriverDatabaseConnectionManager databaseConnectionManager;
    
    @BeforeEach
    void setUp() throws SQLException {
        databaseConnectionManager = new DriverDatabaseConnectionManager("foo_db", mockContextManager());
    }
    
    private ContextManager mockContextManager() throws SQLException {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        Map<String, StorageUnit> storageUnits = mockStorageUnits();
        when(result.getStorageUnits("foo_db")).thenReturn(storageUnits);
        MetaDataPersistFacade persistFacade = mockMetaDataPersistFacade();
        when(result.getPersistServiceFacade().getMetaDataFacade()).thenReturn(persistFacade);
        when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(mock(TransactionRule.class, RETURNS_DEEP_STUBS))));
        return result;
    }
    
    private Map<String, StorageUnit> mockStorageUnits() throws SQLException {
        Map<String, StorageUnit> result = new HashMap<>(2, 1F);
        result.put("ds", mockStorageUnit(new MockedDataSource()));
        DataSource invalidDataSource = mock(DataSource.class);
        when(invalidDataSource.getConnection()).thenThrow(new SQLException("Mock invalid data source"));
        result.put("invalid_ds", mockStorageUnit(invalidDataSource));
        return result;
    }
    
    private StorageUnit mockStorageUnit(final DataSource dataSource) {
        StorageUnit result = mock(StorageUnit.class, RETURNS_DEEP_STUBS);
        when(result.getDataSource()).thenReturn(dataSource);
        return result;
    }
    
    private MetaDataPersistFacade mockMetaDataPersistFacade() {
        MetaDataPersistFacade result = mock(MetaDataPersistFacade.class, RETURNS_DEEP_STUBS);
        when(result.getDataSourceUnitService().load("foo_db"))
                .thenReturn(Collections.singletonMap("foo_db", new DataSourcePoolProperties(HikariDataSource.class.getName(), createProperties())));
        return result;
    }
    
    private Map<String, Object> createProperties() {
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        result.put("jdbcUrl", "jdbc:mysql://127.0.0.1:3306/demo_ds_0?useSSL=false");
        result.put("username", "root");
        result.put("password", "123456");
        return result;
    }
    
    @Test
    void assertGetRandomPhysicalDataSourceNameFromContextManager() {
        String actual = databaseConnectionManager.getRandomPhysicalDataSourceName();
        assertTrue(Arrays.asList("foo_db", "ds", "invalid_ds").contains(actual));
    }
    
    @Test
    void assertGetRandomPhysicalDataSourceNameFromCache() throws SQLException {
        databaseConnectionManager.getConnections("foo_db", "ds", 0, 1, ConnectionMode.MEMORY_STRICTLY);
        assertThat(databaseConnectionManager.getRandomPhysicalDataSourceName(), is("ds"));
        assertThat(databaseConnectionManager.getRandomPhysicalDataSourceName(), is("ds"));
        assertThat(databaseConnectionManager.getRandomPhysicalDataSourceName(), is("ds"));
    }
    
    @Test
    void assertGetConnection() throws SQLException {
        assertThat(databaseConnectionManager.getConnections("foo_db", "ds", 0, 1, ConnectionMode.MEMORY_STRICTLY),
                is(databaseConnectionManager.getConnections("foo_db", "ds", 0, 1, ConnectionMode.MEMORY_STRICTLY)));
    }
    
    @Test
    void assertGetConnectionWithConnectionOffset() throws SQLException {
        assertThat(databaseConnectionManager.getConnections("foo_db", "ds", 0, 1, ConnectionMode.MEMORY_STRICTLY),
                is(databaseConnectionManager.getConnections("foo_db", "ds", 0, 1, ConnectionMode.MEMORY_STRICTLY)));
        assertThat(databaseConnectionManager.getConnections("foo_db", "ds", 1, 1, ConnectionMode.MEMORY_STRICTLY),
                is(databaseConnectionManager.getConnections("foo_db", "ds", 1, 1, ConnectionMode.MEMORY_STRICTLY)));
        assertThat(databaseConnectionManager.getConnections("foo_db", "ds", 0, 1, ConnectionMode.MEMORY_STRICTLY),
                not(databaseConnectionManager.getConnections("foo_db", "ds", 1, 1, ConnectionMode.MEMORY_STRICTLY)));
    }
    
    @Test
    void assertGetConnectionsWhenAllInCache() throws SQLException {
        Connection expected = databaseConnectionManager.getConnections("foo_db", "ds", 0, 1, ConnectionMode.MEMORY_STRICTLY).get(0);
        List<Connection> actual = databaseConnectionManager.getConnections("foo_db", "ds", 0, 1, ConnectionMode.CONNECTION_STRICTLY);
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0), is(expected));
    }
    
    @Test
    void assertGetConnectionsWhenEmptyCache() throws SQLException {
        List<Connection> actual = databaseConnectionManager.getConnections("foo_db", "ds", 0, 1, ConnectionMode.MEMORY_STRICTLY);
        assertThat(actual.size(), is(1));
    }
    
    @Test
    void assertGetConnectionsWhenPartInCacheWithMemoryStrictlyMode() throws SQLException {
        databaseConnectionManager.getConnections("foo_db", "ds", 0, 1, ConnectionMode.MEMORY_STRICTLY);
        List<Connection> actual = databaseConnectionManager.getConnections("foo_db", "ds", 0, 3, ConnectionMode.MEMORY_STRICTLY);
        assertThat(actual.size(), is(3));
    }
    
    @Test
    void assertGetConnectionsWhenPartInCacheWithConnectionStrictlyMode() throws SQLException {
        databaseConnectionManager.getConnections("foo_db", "ds", 0, 1, ConnectionMode.MEMORY_STRICTLY);
        List<Connection> actual = databaseConnectionManager.getConnections("foo_db", "ds", 0, 3, ConnectionMode.CONNECTION_STRICTLY);
        assertThat(actual.size(), is(3));
    }
    
    @Test
    void assertGetConnectionsWhenConnectionCreateFailed() {
        SQLException ex = assertThrows(SQLException.class, () -> databaseConnectionManager.getConnections("foo_db", "invalid_ds", 0, 3, ConnectionMode.CONNECTION_STRICTLY));
        assertThat(ex.getMessage(), is("Can not get 3 connections one time, partition succeed connection(0) have released. "
                + "Please consider increasing the 'maxPoolSize' of the data sources or decreasing the 'max-connections-size-per-query' in properties." + System.lineSeparator()
                + "More details: java.sql.SQLException: Mock invalid data source"));
    }
    
    @Test
    void assertBeginTransaction() throws SQLException {
        databaseConnectionManager.begin();
        assertTrue(databaseConnectionManager.getConnectionContext().getTransactionContext().isInTransaction());
    }
}
