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

package org.apache.shardingsphere.driver.jdbc.unsupported;

import org.apache.shardingsphere.driver.common.base.AbstractShardingSphereDataSourceForShardingTest;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class UnsupportedOperationStatementTest extends AbstractShardingSphereDataSourceForShardingTest {
    
    private final List<ShardingSphereConnection> shardingSphereConnections = new ArrayList<>();
    
    private final List<Statement> statements = new ArrayList<>();
    
    @Before
    public void init() {
        ShardingSphereConnection connection = getShardingSphereDataSource().getConnection();
        shardingSphereConnections.add(connection);
        statements.add(connection.createStatement());
    }
    
    @After
    public void close() throws SQLException {
        for (Statement each : statements) {
            each.close();
        }
        for (ShardingSphereConnection each : shardingSphereConnections) {
            each.close();
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
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetCursorName() throws SQLException {
        for (Statement each : statements) {
            each.setCursorName("cursorName");
        }
    }
}
