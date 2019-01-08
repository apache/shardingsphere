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

package io.shardingsphere.shardingjdbc.jdbc.core.connection;

import io.shardingsphere.api.algorithm.masterslave.MasterSlaveLoadBalanceAlgorithmType;
import io.shardingsphere.api.config.rule.MasterSlaveRuleConfiguration;
import io.shardingsphere.api.config.rule.ShardingRuleConfiguration;
import io.shardingsphere.api.config.rule.TableRuleConfiguration;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.shardingjdbc.fixture.TestDataSource;
import io.shardingsphere.shardingjdbc.jdbc.core.ShardingContext;
import io.shardingsphere.shardingjdbc.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingsphere.shardingjdbc.jdbc.core.fixed.FixedBaseShardingTransactionHandler;
import io.shardingsphere.shardingjdbc.jdbc.core.fixed.FixedXAShardingTransactionHandler;
import io.shardingsphere.transaction.api.TransactionType;
import io.shardingsphere.transaction.api.TransactionTypeHolder;
import io.shardingsphere.transaction.core.context.ShardingTransactionContext;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingConnectionTest {
    
    private static MasterSlaveDataSource masterSlaveDataSource;
    
    private static final String DS_NAME = "default";
    
    private ShardingConnection connection;
    
    private ShardingContext shardingContext;
    
    private Map<String, DataSource> dataSourceMap;
    
    @BeforeClass
    public static void init() throws SQLException {
        DataSource masterDataSource = new TestDataSource("test_ds_master");
        DataSource slaveDataSource = new TestDataSource("test_ds_slave");
        Map<String, DataSource> dataSourceMap = new HashMap<>(2, 1);
        dataSourceMap.put("test_ds_master", masterDataSource);
        dataSourceMap.put("test_ds_slave", slaveDataSource);
        masterSlaveDataSource = new MasterSlaveDataSource(dataSourceMap, 
                new MasterSlaveRuleConfiguration("test_ds", "test_ds_master", Collections.singletonList("test_ds_slave"), MasterSlaveLoadBalanceAlgorithmType.ROUND_ROBIN.getAlgorithm()), 
                Collections.<String, Object>emptyMap(), new Properties());
        ((TestDataSource) slaveDataSource).setThrowExceptionWhenClosing(true);
    }
    
    @Before
    public void setUp() {
        shardingContext = mock(ShardingContext.class);
        when(shardingContext.getDatabaseType()).thenReturn(DatabaseType.H2);
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration();
        tableRuleConfig.setLogicTable("test");
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        dataSourceMap = new HashMap<>(1, 1);
        dataSourceMap.put(DS_NAME, masterSlaveDataSource);
        connection = new ShardingConnection(dataSourceMap, shardingContext, TransactionType.LOCAL);
    }
    
    @After
    public void clear() {
        try {
            connection.close();
            TransactionTypeHolder.clear();
            FixedXAShardingTransactionHandler.getInvokes().clear();
            FixedBaseShardingTransactionHandler.getInvokes().clear();
        } catch (final SQLException ignored) {
        }
    }
    
    @Test
    public void assertGetConnectionFromCache() throws SQLException {
        assertThat(connection.getConnection(DS_NAME), is(connection.getConnection(DS_NAME)));
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertGetConnectionFailure() throws SQLException {
        connection.getConnection("not_exist");
    }
    
    @Test
    public void assertXATransactionOperation() throws SQLException {
        connection = new ShardingConnection(dataSourceMap, shardingContext, TransactionType.XA);
        connection.setAutoCommit(false);
        assertThat(FixedXAShardingTransactionHandler.getInvokes().get("begin"), instanceOf(ShardingTransactionContext.class));
        connection.commit();
        assertThat(FixedXAShardingTransactionHandler.getInvokes().get("commit"), instanceOf(ShardingTransactionContext.class));
        connection.rollback();
        assertThat(FixedXAShardingTransactionHandler.getInvokes().get("rollback"), instanceOf(ShardingTransactionContext.class));
    }
    
    @Test
    public void assertBaseTransactionOperation() throws SQLException {
        connection = new ShardingConnection(dataSourceMap, shardingContext, TransactionType.BASE);
        connection.setAutoCommit(false);
        assertThat(FixedBaseShardingTransactionHandler.getInvokes().get("begin"), instanceOf(ShardingTransactionContext.class));
        connection.commit();
        assertThat(FixedBaseShardingTransactionHandler.getInvokes().get("commit"), instanceOf(ShardingTransactionContext.class));
        connection.rollback();
        assertThat(FixedBaseShardingTransactionHandler.getInvokes().get("rollback"), instanceOf(ShardingTransactionContext.class));
    }
}
