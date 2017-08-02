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

package com.dangdang.ddframe.rdb.sharding.jdbc.unsupported;

import com.dangdang.ddframe.rdb.common.sql.base.AbstractShardingJDBCDatabaseAndTableTest;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.connection.ShardingConnection;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.datasource.ShardingDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class UnsupportedOperationStatementTest extends AbstractShardingJDBCDatabaseAndTableTest {
    
    private List<ShardingConnection> shardingConnections = new ArrayList<>();
    
    private List<Statement> statements = new ArrayList<>();
    
    @Before
    public void init() throws SQLException {
        for (ShardingDataSource each : getShardingDataSources().values()) {
            ShardingConnection connection = each.getConnection();
            shardingConnections.add(connection);
            statements.add(connection.createStatement());
        }
    }
    
    @After
    public void close() throws SQLException {
        for (Statement each : statements) {
            each.close();
        }
        for (ShardingConnection each : shardingConnections) {
            each.close();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetFetchDirection() throws SQLException {
        for (Statement each : statements) {
            each.getFetchDirection();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetFetchDirection() throws SQLException {
        for (Statement each : statements) {
            each.setFetchDirection(0);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertAddBatch() throws SQLException {
        for (Statement each : statements) {
            each.addBatch("");
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertClearBatch() throws SQLException {
        for (Statement each : statements) {
            each.clearBatch();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertExecuteBatch() throws SQLException {
        for (Statement each : statements) {
            each.executeBatch();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertCloseOnCompletion() throws SQLException {
        for (Statement each : statements) {
            each.closeOnCompletion();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertIsCloseOnCompletion() throws SQLException {
        for (Statement each : statements) {
            each.isCloseOnCompletion();
        }
    }
}
