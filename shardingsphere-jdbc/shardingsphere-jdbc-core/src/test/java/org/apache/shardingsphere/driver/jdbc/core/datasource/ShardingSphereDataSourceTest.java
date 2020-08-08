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

package org.apache.shardingsphere.driver.jdbc.core.datasource;

import com.google.common.base.Joiner;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.core.fixture.XAShardingTransactionManagerFixture;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypes;
import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.core.TransactionTypeHolder;
import org.junit.After;
import org.junit.Test;
import org.mockito.ArgumentMatchers;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class ShardingSphereDataSourceTest {
    
    @After
    public void tearDown() {
        TransactionTypeHolder.set(TransactionType.LOCAL);
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertGetDatabaseProductNameWhenDataBaseProductNameDifferent() throws SQLException {
        DataSource dataSource1 = mockDataSource(DatabaseTypes.getActualDatabaseType("MySQL"));
        DataSource dataSource2 = mockDataSource(DatabaseTypes.getActualDatabaseType("H2"));
        Map<String, DataSource> dataSourceMap = new HashMap<>(2, 1);
        dataSourceMap.put("ds1", dataSource1);
        dataSourceMap.put("ds2", dataSource2);
        assertDatabaseProductName(dataSourceMap, dataSource1.getConnection(), dataSource2.getConnection());
    }
    
    @Test
    public void assertGetDatabaseProductName() throws SQLException {
        DataSource dataSource1 = mockDataSource(DatabaseTypes.getActualDatabaseType("H2"));
        DataSource dataSource2 = mockDataSource(DatabaseTypes.getActualDatabaseType("H2"));
        DataSource dataSource3 = mockDataSource(DatabaseTypes.getActualDatabaseType("H2"));
        Map<String, DataSource> dataSourceMap = new HashMap<>(3, 1);
        dataSourceMap.put("ds1", dataSource1);
        dataSourceMap.put("ds2", dataSource2);
        dataSourceMap.put("ds3", dataSource3);
        assertDatabaseProductName(dataSourceMap, dataSource1.getConnection(), dataSource2.getConnection(), dataSource3.getConnection());
    }
    
    @Test
    public void assertGetDatabaseProductNameForMasterSlave() throws SQLException {
        DataSource dataSource1 = mockDataSource(DatabaseTypes.getActualDatabaseType("H2"));
        DataSource masterDataSource = mockDataSource(DatabaseTypes.getActualDatabaseType("H2"));
        DataSource slaveDataSource = mockDataSource(DatabaseTypes.getActualDatabaseType("H2"));
        DataSource dataSource3 = mockDataSource(DatabaseTypes.getActualDatabaseType("H2"));
        Map<String, DataSource> dataSourceMap = new HashMap<>(4, 1);
        dataSourceMap.put("ds1", dataSource1);
        dataSourceMap.put("masterDataSource", masterDataSource);
        dataSourceMap.put("slaveDataSource", slaveDataSource);
        dataSourceMap.put("ds3", dataSource3);
        assertDatabaseProductName(dataSourceMap, dataSource1.getConnection(), masterDataSource.getConnection(), slaveDataSource.getConnection());
    }
    
    private void assertDatabaseProductName(final Map<String, DataSource> dataSourceMap, final Connection... connections) throws SQLException {
        try {
            assertThat(createShardingSphereDataSource(dataSourceMap).getSchemaContexts().getSchemaContexts().get(DefaultSchema.LOGIC_NAME).getSchema().getDatabaseType(), 
                    instanceOf(H2DatabaseType.class));
        } finally {
            for (Connection each : connections) {
                verify(each, atLeast(1)).close();
            }
        }
    }
    
    private DataSource mockDataSource(final DatabaseType databaseType) throws SQLException {
        DataSource result = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mockDatabaseMetaData();
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(false);
        when(statement.getResultSet()).thenReturn(resultSet);
        when(result.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(statement.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(ArgumentMatchers.any())).thenReturn(resultSet);
        when(statement.getConnection().getMetaData().getTables(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(resultSet);
        if (databaseType instanceof MySQLDatabaseType) {
            when(result.getConnection().getMetaData().getURL()).thenReturn("jdbc:mysql://localhost:3306/test");
        } else if (databaseType instanceof H2DatabaseType) {
            when(statement.getConnection().getMetaData().getURL()).thenReturn("jdbc:h2:mem:demo_ds;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        }
        return result;
    }
    
    private DatabaseMetaData mockDatabaseMetaData() throws SQLException {
        DatabaseMetaData result = mock(DatabaseMetaData.class);
        when(result.getColumns(null, null, "table_0", "%")).thenReturn(mock(ResultSet.class));
        when(result.getPrimaryKeys(null, null, "table_0")).thenReturn(mock(ResultSet.class));
        when(result.getIndexInfo(null, null, "table_0", false, false)).thenReturn(mock(ResultSet.class));
        return result;
    }
    
    @Test
    public void assertGetConnection() throws SQLException {
        DataSource dataSource = mockDataSource(DatabaseTypes.getActualDatabaseType("H2"));
        Map<String, DataSource> dataSourceMap = new HashMap<>(1, 1);
        dataSourceMap.put("ds", dataSource);
        assertThat(createShardingSphereDataSource(dataSourceMap).getConnection().getConnection("ds"), is(dataSource.getConnection()));
    }
    
    @Test
    public void assertGetXaConnection() throws SQLException {
        DataSource dataSource = mockDataSource(DatabaseTypes.getActualDatabaseType("MySQL"));
        Map<String, DataSource> dataSourceMap = new HashMap<>(1, 1);
        dataSourceMap.put("ds", dataSource);
        TransactionTypeHolder.set(TransactionType.XA);
        ShardingSphereDataSource shardingSphereDataSource = createShardingSphereDataSource(dataSourceMap);
        assertThat(shardingSphereDataSource.getDataSourceMap().size(), is(1));
        ShardingSphereConnection connection = shardingSphereDataSource.getConnection();
        assertThat(connection.getDataSourceMap().size(), is(1));
    }
    
    @Test
    public void assertGetXaConnectionThenGetLocalConnection() throws SQLException {
        DataSource dataSource = mockDataSource(DatabaseTypes.getActualDatabaseType("MySQL"));
        Map<String, DataSource> dataSourceMap = new HashMap<>(1, 1);
        dataSourceMap.put("ds", dataSource);
        TransactionTypeHolder.set(TransactionType.XA);
        ShardingSphereDataSource shardingSphereDataSource = createShardingSphereDataSource(dataSourceMap);
        ShardingSphereConnection connection = shardingSphereDataSource.getConnection();
        assertThat(connection.getDataSourceMap().size(), is(1));
        assertThat(connection.getTransactionType(), is(TransactionType.XA));
        assertThat(connection.getShardingTransactionManager(), instanceOf(XAShardingTransactionManagerFixture.class));
        TransactionTypeHolder.set(TransactionType.LOCAL);
        connection = shardingSphereDataSource.getConnection();
        assertThat(connection.getConnection("ds"), is(dataSource.getConnection()));
        assertThat(connection.getDataSourceMap(), is(dataSourceMap));
        assertThat(connection.getTransactionType(), is(TransactionType.LOCAL));
        assertNull(connection.getShardingTransactionManager());
    }
    
    private ShardingSphereDataSource createShardingSphereDataSource(final Map<String, DataSource> dataSourceMap) throws SQLException {
        return new ShardingSphereDataSource(dataSourceMap, Collections.singletonList(createShardingRuleConfig(dataSourceMap)), new Properties());
    }
    
    private ShardingRuleConfiguration createShardingRuleConfig(final Map<String, DataSource> dataSourceMap) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        List<String> orderActualDataNodes = new LinkedList<>();
        for (String each : dataSourceMap.keySet()) {
            orderActualDataNodes.add(each + ".table_${0..2}");
        }
        ShardingTableRuleConfiguration tableRuleConfig = new ShardingTableRuleConfiguration("logicTable", Joiner.on(",").join(orderActualDataNodes));
        result.getTables().add(tableRuleConfig);
        return result;
    }
}
