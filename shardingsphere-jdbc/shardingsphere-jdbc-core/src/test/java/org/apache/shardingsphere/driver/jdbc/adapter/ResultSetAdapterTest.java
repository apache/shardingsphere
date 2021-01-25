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

package org.apache.shardingsphere.driver.jdbc.adapter;

import org.apache.shardingsphere.driver.common.base.AbstractShardingSphereDataSourceForShardingTest;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.util.JDBCTestSQL;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ResultSetAdapterTest extends AbstractShardingSphereDataSourceForShardingTest {
    
    private final List<ShardingSphereConnection> shardingSphereConnections = new ArrayList<>();
    
    private final List<Statement> statements = new ArrayList<>();
    
    private final Map<DatabaseType, ResultSet> resultSets = new HashMap<>();
    
    @Before
    public void init() throws SQLException {
        ShardingSphereConnection connection = getShardingSphereDataSource().getConnection();
        shardingSphereConnections.add(connection);
        Statement statement = connection.createStatement();
        statements.add(statement);
        resultSets.put(DatabaseTypeRegistry.getActualDatabaseType("H2"), statement.executeQuery(JDBCTestSQL.SELECT_GROUP_BY_USER_ID_SQL));
    }
    
    @After
    public void close() throws SQLException {
        for (ResultSet each : resultSets.values()) {
            each.close();
        }
        for (Statement each : statements) {
            each.close();
        }
        for (ShardingSphereConnection each : shardingSphereConnections) {
            each.close();
        }
    }
    
    @Test
    public void assertClose() throws SQLException {
        for (Entry<DatabaseType, ResultSet> each : resultSets.entrySet()) {
            each.getValue().close();
            assertClose((AbstractResultSetAdapter) each.getValue(), each.getKey());
        }
    }
    
    private void assertClose(final AbstractResultSetAdapter actual, final DatabaseType type) throws SQLException {
        assertTrue(actual.isClosed());
        assertThat(actual.getResultSets().size(), is(4));
        if (DatabaseTypeRegistry.getActualDatabaseType("Oracle") != type) {
            for (ResultSet each : actual.getResultSets()) {
                assertTrue(each.isClosed());
            }
        }
    }
    
    @Test
    public void assertWasNull() throws SQLException {
        for (ResultSet each : resultSets.values()) {
            assertFalse(each.wasNull());
        }
    }
    
    @Test
    public void assertSetFetchDirection() throws SQLException {
        for (Entry<DatabaseType, ResultSet> each : resultSets.entrySet()) {
            assertThat(each.getValue().getFetchDirection(), is(ResultSet.FETCH_FORWARD));
            try {
                each.getValue().setFetchDirection(ResultSet.FETCH_REVERSE);
            } catch (final SQLException ignored) {
            }
            if (each.getKey() == DatabaseTypeRegistry.getActualDatabaseType("MySQL") || each.getKey() == DatabaseTypeRegistry.getActualDatabaseType("PostgreSQL")) {
                assertFetchDirection((AbstractResultSetAdapter) each.getValue(), ResultSet.FETCH_REVERSE, each.getKey());
            }
        }
    }
    
    private void assertFetchDirection(final AbstractResultSetAdapter actual, final int fetchDirection, final DatabaseType type) throws SQLException {
        // H2 do not implement getFetchDirection
        assertThat(actual.getFetchDirection(), is(
                DatabaseTypeRegistry.getActualDatabaseType("H2") == type || DatabaseTypeRegistry.getActualDatabaseType("PostgreSQL") == type ? ResultSet.FETCH_FORWARD : fetchDirection));
        assertThat(actual.getResultSets().size(), is(4));
        for (ResultSet each : actual.getResultSets()) {
            assertThat(each.getFetchDirection(), is(
                    DatabaseTypeRegistry.getActualDatabaseType("H2") == type || DatabaseTypeRegistry.getActualDatabaseType("PostgreSQL") == type ? ResultSet.FETCH_FORWARD : fetchDirection));
        }
    }
    
    @Test
    public void assertSetFetchSize() throws SQLException {
        for (Entry<DatabaseType, ResultSet> each : resultSets.entrySet()) {
            if (DatabaseTypeRegistry.getActualDatabaseType("MySQL") == each.getKey() || DatabaseTypeRegistry.getActualDatabaseType("PostgreSQL") == each.getKey()) {
                assertThat(each.getValue().getFetchSize(), is(0));
            }
            each.getValue().setFetchSize(100);
            assertFetchSize((AbstractResultSetAdapter) each.getValue(), each.getKey());
        }
    }
    
    private void assertFetchSize(final AbstractResultSetAdapter actual, final DatabaseType type) throws SQLException {
        // H2 do not implement getFetchSize
        assertThat(actual.getFetchSize(), is(DatabaseTypeRegistry.getActualDatabaseType("H2") == type ? 0 : 100));
        assertThat(actual.getResultSets().size(), is(4));
        for (ResultSet each : actual.getResultSets()) {
            assertThat(each.getFetchSize(), is(DatabaseTypeRegistry.getActualDatabaseType("H2") == type ? 0 : 100));
        }
    }
    
    @Test
    public void assertGetType() throws SQLException {
        for (ResultSet each : resultSets.values()) {
            assertThat(each.getType(), is(ResultSet.TYPE_FORWARD_ONLY));
        }
    }
    
    @Test
    public void assertGetConcurrency() throws SQLException {
        for (ResultSet each : resultSets.values()) {
            assertThat(each.getConcurrency(), is(ResultSet.CONCUR_READ_ONLY));
        }
    }
    
    @Test
    public void assertGetStatement() throws SQLException {
        for (ResultSet each : resultSets.values()) {
            assertNotNull(each.getStatement());
        }
    }
    
    @Test
    public void assertClearWarnings() throws SQLException {
        for (ResultSet each : resultSets.values()) {
            assertNull(each.getWarnings());
            each.clearWarnings();
            assertNull(each.getWarnings());
        }
    }
    
    @Test
    public void assertGetMetaData() throws SQLException {
        for (ResultSet each : resultSets.values()) {
            assertNotNull(each.getMetaData());
        }
    }
    
    @Test
    public void assertFindColumn() throws SQLException {
        for (Entry<DatabaseType, ResultSet> each : resultSets.entrySet()) {
            assertThat(each.getValue().findColumn("user_id"), is(1));
        }
    }
}
