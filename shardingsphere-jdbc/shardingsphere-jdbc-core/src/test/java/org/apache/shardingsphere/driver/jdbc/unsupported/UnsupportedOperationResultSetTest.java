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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class UnsupportedOperationResultSetTest extends AbstractShardingSphereDataSourceForShardingTest {
    
    private static final String SQL = "SELECT user_id AS usr_id FROM t_order WHERE status = 'init'";
    
    private final List<ShardingSphereConnection> shardingSphereConnections = new ArrayList<>();
    
    private final List<Statement> statements = new ArrayList<>();
    
    private final List<ResultSet> resultSets = new ArrayList<>();
    
    @Before
    public void init() throws SQLException {
        ShardingSphereConnection connection = getShardingSphereDataSource().getConnection();
        shardingSphereConnections.add(connection);
        Statement statement = connection.createStatement();
        statements.add(statement);
        resultSets.add(statement.executeQuery(SQL));
    }
    
    @After
    public void close() throws SQLException {
        for (ShardingSphereConnection each : shardingSphereConnections) {
            each.close();
        }
        for (Statement each : statements) {
            each.close();
        }
        for (ResultSet each : resultSets) {
            each.close();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertPrevious() throws SQLException {
        for (ResultSet each : resultSets) {
            each.previous();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertIsBeforeFirst() throws SQLException {
        for (ResultSet each : resultSets) {
            each.isBeforeFirst();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertIsAfterLast() throws SQLException {
        for (ResultSet each : resultSets) {
            each.isAfterLast();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertIsFirst() throws SQLException {
        for (ResultSet each : resultSets) {
            each.isFirst();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertIsLast() throws SQLException {
        for (ResultSet each : resultSets) {
            each.isLast();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertBeforeFirst() throws SQLException {
        for (ResultSet each : resultSets) {
            each.beforeFirst();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertAfterLast() throws SQLException {
        for (ResultSet each : resultSets) {
            each.afterLast();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertFirst() throws SQLException {
        for (ResultSet each : resultSets) {
            each.first();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertLast() throws SQLException {
        for (ResultSet each : resultSets) {
            each.last();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertAbsolute() throws SQLException {
        for (ResultSet each : resultSets) {
            each.absolute(1);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertRelative() throws SQLException {
        for (ResultSet each : resultSets) {
            each.relative(1);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetRow() throws SQLException {
        for (ResultSet each : resultSets) {
            each.getRow();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertInsertRow() throws SQLException {
        for (ResultSet each : resultSets) {
            each.insertRow();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateRow() throws SQLException {
        for (ResultSet each : resultSets) {
            each.updateRow();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertDeleteRow() throws SQLException {
        for (ResultSet each : resultSets) {
            each.deleteRow();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertRefreshRow() throws SQLException {
        for (ResultSet each : resultSets) {
            each.refreshRow();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertCancelRowUpdates() throws SQLException {
        for (ResultSet each : resultSets) {
            each.cancelRowUpdates();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertMoveToInsertRow() throws SQLException {
        for (ResultSet each : resultSets) {
            each.moveToInsertRow();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertMoveToCurrentRow() throws SQLException {
        for (ResultSet each : resultSets) {
            each.moveToCurrentRow();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertRowInserted() throws SQLException {
        for (ResultSet each : resultSets) {
            each.rowInserted();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertRowUpdated() throws SQLException {
        for (ResultSet each : resultSets) {
            each.rowUpdated();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertRowDeleted() throws SQLException {
        for (ResultSet each : resultSets) {
            each.rowDeleted();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetCursorName() throws SQLException {
        for (ResultSet each : resultSets) {
            each.getCursorName();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetHoldability() throws SQLException {
        for (ResultSet each : resultSets) {
            each.getHoldability();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetNClobForColumnIndex() throws SQLException {
        for (ResultSet each : resultSets) {
            each.getNClob(1);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetNClobForColumnLabel() throws SQLException {
        for (ResultSet each : resultSets) {
            each.getNClob("label");
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getNCharacterStreamForColumnIndex() throws SQLException {
        for (ResultSet each : resultSets) {
            each.getNCharacterStream(1);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getNCharacterStreamForColumnLabel() throws SQLException {
        for (ResultSet each : resultSets) {
            each.getNCharacterStream("label");
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetRefForColumnIndex() throws SQLException {
        for (ResultSet each : resultSets) {
            each.getRef(1);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetRefForColumnLabel() throws SQLException {
        for (ResultSet each : resultSets) {
            each.getRef("label");
        }
    }

    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetRowIdForColumnIndex() throws SQLException {
        for (ResultSet each : resultSets) {
            each.getRowId(1);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetRowIdForColumnLabel() throws SQLException {
        for (ResultSet each : resultSets) {
            each.getRowId("label");
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertObjectForColumnIndexWithType() throws SQLException {
        for (ResultSet each : resultSets) {
            each.getObject(1, String.class);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertObjectForColumnLabelWithType() throws SQLException {
        for (ResultSet each : resultSets) {
            each.getObject("label", String.class);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertObjectForColumnIndexWithMap() throws SQLException {
        for (ResultSet each : resultSets) {
            each.getObject(1, Collections.emptyMap());
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertObjectForColumnLabelWithMap() throws SQLException {
        for (ResultSet each : resultSets) {
            each.getObject("label", Collections.emptyMap());
        }
    }
}
