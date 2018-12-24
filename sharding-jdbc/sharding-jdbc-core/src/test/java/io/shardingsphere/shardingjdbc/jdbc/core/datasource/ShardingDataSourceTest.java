/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingjdbc.jdbc.core.datasource;

import com.google.common.base.Joiner;
import io.shardingsphere.api.algorithm.masterslave.MasterSlaveLoadBalanceAlgorithmType;
import io.shardingsphere.api.config.rule.MasterSlaveRuleConfiguration;
import io.shardingsphere.api.config.rule.ShardingRuleConfiguration;
import io.shardingsphere.api.config.rule.TableRuleConfiguration;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.shardingjdbc.api.MasterSlaveDataSourceFactory;
import io.shardingsphere.shardingjdbc.jdbc.core.connection.ShardingConnection;
import io.shardingsphere.shardingjdbc.jdbc.core.fixed.FixedXAShardingTransactionHandler;
import io.shardingsphere.transaction.api.TransactionType;
import io.shardingsphere.transaction.api.TransactionTypeHolder;
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
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class ShardingDataSourceTest {
    
    @After
    public void tearDown() {
        TransactionTypeHolder.set(TransactionType.LOCAL);
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertGetDatabaseProductNameWhenDataBaseProductNameDifferent() throws SQLException {
        DataSource dataSource1 = mockDataSource("MySQL");
        DataSource dataSource2 = mockDataSource("H2");
        Map<String, DataSource> dataSourceMap = new HashMap<>(2, 1);
        dataSourceMap.put("ds1", dataSource1);
        dataSourceMap.put("ds2", dataSource2);
        assertDatabaseProductName(dataSourceMap, dataSource1.getConnection(), dataSource2.getConnection());
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertGetDatabaseProductNameWhenDataBaseProductNameDifferentForMasterSlave() throws SQLException {
        DataSource dataSource1 = mockDataSource("MySQL");
        DataSource masterDataSource = mockDataSource("H2");
        DataSource slaveDataSource = mockDataSource("H2");
        Map<String, DataSource> masterSlaveDataSourceMap = new HashMap<>(2, 1);
        masterSlaveDataSourceMap.put("masterDataSource", masterDataSource);
        masterSlaveDataSourceMap.put("slaveDataSource", slaveDataSource);
        MasterSlaveDataSource dataSource2 = (MasterSlaveDataSource) MasterSlaveDataSourceFactory.createDataSource(masterSlaveDataSourceMap, 
                new MasterSlaveRuleConfiguration("ds", "masterDataSource", Collections.singletonList("slaveDataSource"), MasterSlaveLoadBalanceAlgorithmType.ROUND_ROBIN.getAlgorithm()), 
                Collections.<String, Object>emptyMap(), new Properties());
        Map<String, DataSource> dataSourceMap = new HashMap<>(2, 1);
        dataSourceMap.put("ds1", dataSource1);
        dataSourceMap.put("ds2", dataSource2);
        assertDatabaseProductName(dataSourceMap, dataSource1.getConnection(), masterDataSource.getConnection(), slaveDataSource.getConnection());
    }
    
    @Test
    public void assertGetDatabaseProductName() throws SQLException {
        DataSource dataSource1 = mockDataSource("H2");
        DataSource dataSource2 = mockDataSource("H2");
        DataSource dataSource3 = mockDataSource("H2");
        Map<String, DataSource> dataSourceMap = new HashMap<>(3, 1);
        dataSourceMap.put("ds1", dataSource1);
        dataSourceMap.put("ds2", dataSource2);
        dataSourceMap.put("ds3", dataSource3);
        assertDatabaseProductName(dataSourceMap, dataSource1.getConnection(), dataSource2.getConnection(), dataSource3.getConnection());
    }
    
    @Test
    public void assertGetDatabaseProductNameForMasterSlave() throws SQLException {
        DataSource dataSource1 = mockDataSource("H2");
        DataSource masterDataSource = mockDataSource("H2");
        DataSource slaveDataSource = mockDataSource("H2");
        DataSource dataSource3 = mockDataSource("H2");
        Map<String, DataSource> dataSourceMap = new HashMap<>(4, 1);
        dataSourceMap.put("ds1", dataSource1);
        dataSourceMap.put("masterDataSource", masterDataSource);
        dataSourceMap.put("slaveDataSource", slaveDataSource);
        dataSourceMap.put("ds3", dataSource3);
        assertDatabaseProductName(dataSourceMap, dataSource1.getConnection(), masterDataSource.getConnection(), slaveDataSource.getConnection());
    }
    
    private void assertDatabaseProductName(final Map<String, DataSource> dataSourceMap, final Connection... connections) throws SQLException {
        try {
            assertThat(createShardingDataSource(dataSourceMap).getDatabaseType(), is(DatabaseType.H2));
        } finally {
            for (Connection each : connections) {
                verify(each, atLeast(1)).close();
            }
        }
    }
    
    private DataSource mockDataSource(final String dataBaseProductName) throws SQLException {
        DataSource result = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(false);
        when(statement.getResultSet()).thenReturn(resultSet);
        when(databaseMetaData.getDatabaseProductName()).thenReturn(dataBaseProductName);
        when(result.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(statement.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(ArgumentMatchers.<String>any())).thenReturn(resultSet);
        when(statement.getConnection().getMetaData().getTables(ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(),
                ArgumentMatchers.<String>any(), ArgumentMatchers.<String[]>any())).thenReturn(resultSet);
        if ("MySQL".equals(dataBaseProductName)) {
            when(result.getConnection().getMetaData().getURL()).thenReturn("jdbc:mysql://localhost:3306/test");
        } else if ("H2".equals(dataBaseProductName)) {
            when(statement.getConnection().getMetaData().getURL()).thenReturn("jdbc:h2:mem:demo_ds;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        }
        return result;
    }
    
    @Test
    public void assertGetConnection() throws SQLException {
        DataSource dataSource = mockDataSource("H2");
        Map<String, DataSource> dataSourceMap = new HashMap<>(1, 1);
        dataSourceMap.put("ds", dataSource);
        assertThat(createShardingDataSource(dataSourceMap).getConnection().getConnection("ds"), is(dataSource.getConnection()));
    }
    
    @Test
    public void assertGetXaConnection() throws SQLException {
        DataSource dataSource = mockDataSource("MySQL");
        Map<String, DataSource> dataSourceMap = new HashMap<>(1, 1);
        dataSourceMap.put("ds", dataSource);
        TransactionTypeHolder.set(TransactionType.XA);
        ShardingDataSource shardingDataSource = createShardingDataSource(dataSourceMap);
        assertThat(shardingDataSource.getShardingTransactionalDataSources().getDataSourceMap().size(), is(1));
        ShardingConnection shardingConnection = shardingDataSource.getConnection();
        assertThat(shardingConnection.getDataSourceMap().size(), is(1));
    }
    
    @Test
    public void assertGetXaConnectionThenGetLocalConnection() throws SQLException {
        DataSource dataSource = mockDataSource("MySQL");
        Map<String, DataSource> dataSourceMap = new HashMap<>(1, 1);
        dataSourceMap.put("ds", dataSource);
        TransactionTypeHolder.set(TransactionType.XA);
        ShardingDataSource shardingDataSource = createShardingDataSource(dataSourceMap);
        ShardingConnection shardingConnection = shardingDataSource.getConnection();
        assertThat(shardingConnection.getDataSourceMap().size(), is(1));
        assertThat(shardingConnection.getTransactionType(), is(TransactionType.XA));
        assertThat(shardingConnection.getShardingTransactionHandler(), instanceOf(FixedXAShardingTransactionHandler.class));
        TransactionTypeHolder.set(TransactionType.LOCAL);
        shardingConnection = shardingDataSource.getConnection();
        assertThat(shardingConnection.getConnection("ds"), is(dataSource.getConnection()));
        assertThat(shardingConnection.getDataSourceMap(), is(dataSourceMap));
        assertThat(shardingConnection.getTransactionType(), is(TransactionType.LOCAL));
        assertThat(shardingConnection.getShardingTransactionHandler() == null, is(true));
    }
    
    private ShardingDataSource createShardingDataSource(final Map<String, DataSource> dataSourceMap) throws SQLException {
        return new ShardingDataSource(dataSourceMap, new ShardingRule(createShardingRuleConfig(dataSourceMap), dataSourceMap.keySet()));
    }
    
    private ShardingRuleConfiguration createShardingRuleConfig(final Map<String, DataSource> dataSourceMap) {
        final ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration();
        tableRuleConfig.setLogicTable("logicTable");
        List<String> orderActualDataNodes = new LinkedList<>();
        for (String each : dataSourceMap.keySet()) {
            orderActualDataNodes.add(each + ".table_${0..2}");
        }
        tableRuleConfig.setActualDataNodes(Joiner.on(",").join(orderActualDataNodes));
        result.getTableRuleConfigs().add(tableRuleConfig);
        return result;
    }
}
