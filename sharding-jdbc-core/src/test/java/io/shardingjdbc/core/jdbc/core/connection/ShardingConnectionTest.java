/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.jdbc.core.connection;

import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.core.api.config.TableRuleConfiguration;
import io.shardingjdbc.core.rule.MasterSlaveRule;
import io.shardingjdbc.core.constant.SQLType;
import io.shardingjdbc.core.fixture.TestDataSource;
import io.shardingjdbc.core.jdbc.core.ShardingContext;
import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

public final class ShardingConnectionTest {
    
    private static MasterSlaveDataSource masterSlaveDataSource;
    
    private static final String DS_NAME = "default";
    
    private ShardingConnection connection;
    
    @BeforeClass
    public static void init() throws SQLException {
        DataSource masterDataSource = new TestDataSource("test_ds_master");
        DataSource slaveDataSource = new TestDataSource("test_ds_slave");
        Map<String, DataSource> slaveDataSourceMap = new HashMap<>(1, 1);
        slaveDataSourceMap.put("test_ds_slave", slaveDataSource);
        masterSlaveDataSource = new MasterSlaveDataSource(new MasterSlaveRule("test_ds", "test_ds_master", masterDataSource, slaveDataSourceMap));
        ((TestDataSource) slaveDataSource).setThrowExceptionWhenClosing(true);
    }
    
    @Before
    public void setUp() throws SQLException {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration();
        tableRuleConfig.setLogicTable("test");
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        Map<String, DataSource> dataSourceMap = new HashMap<>(1, 1);
        dataSourceMap.put(DS_NAME, masterSlaveDataSource);
        ShardingContext shardingContext = new ShardingContext(shardingRuleConfig.build(dataSourceMap), null, null, false);
        connection = new ShardingConnection(shardingContext);
    }
    
    @After
    public void clear() {
        try {
            connection.close();
        } catch (final SQLException ignored) {
        }
    }
    
    @Test
    public void assertGetConnectionSelectThenUpdate() throws Exception {
        assertNotSame(connection.getConnection(DS_NAME, SQLType.DQL), connection.getConnection(DS_NAME, SQLType.DML));
    }
    
    @Test
    public void assertGetConnectionUpdateThenSelect() throws Exception {
        assertSame(connection.getConnection(DS_NAME, SQLType.DML), connection.getConnection(DS_NAME, SQLType.DQL));
    }
    
    @Test
    public void assertGetConnectionBothSelect() throws Exception {
        assertSame(connection.getConnection(DS_NAME, SQLType.DQL), connection.getConnection(DS_NAME, SQLType.DQL));
    }
    
    @Test
    public void assertGetConnectionBothUpdate() throws Exception {
        assertSame(connection.getConnection(DS_NAME, SQLType.DML), connection.getConnection(DS_NAME, SQLType.DML));
    }
    
    @Test
    public void assertGetConnectionMixed() throws Exception {
        Connection slaveConnection = connection.getConnection(DS_NAME, SQLType.DQL);
        Connection masterConnection = connection.getConnection(DS_NAME, SQLType.DML);
        assertNotSame(slaveConnection, masterConnection);
        assertNotSame(slaveConnection, connection.getConnection(DS_NAME, SQLType.DQL));
        assertNotSame(slaveConnection, connection.getConnection(DS_NAME, SQLType.DML));
        assertSame(masterConnection, connection.getConnection(DS_NAME, SQLType.DQL));
        assertSame(masterConnection, connection.getConnection(DS_NAME, SQLType.DML));
    }
    
    @Test
    public void assertRelease() throws Exception {
        Connection conn = connection.getConnection(DS_NAME, SQLType.DML);
        connection.release(conn);
        assertNotSame(conn, connection.getConnection(DS_NAME, SQLType.DML));
    }
}
