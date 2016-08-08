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

import com.dangdang.ddframe.rdb.integrate.db.AbstractShardingDataBasesOnlyDBUnitTest;
import com.dangdang.ddframe.rdb.sharding.jdbc.ShardingConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;

public final class UnsupportedOperationResultSetTest extends AbstractShardingDataBasesOnlyDBUnitTest {
    
    private ShardingConnection shardingConnection;
    
    private Statement statement;
    
    private ResultSet actual;
    
    @Before
    public void init() throws SQLException {
        shardingConnection = getShardingDataSource().getConnection();
        statement = shardingConnection.createStatement();
        actual = statement.executeQuery("SELECT user_id AS `uid` FROM `t_order` WHERE `status` = 'init'");
    }
    
    @After
    public void close() throws SQLException {
        actual.close();
        statement.close();
        shardingConnection.close();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertPrevious() throws SQLException {
        actual.previous();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertIsBeforeFirst() throws SQLException {
        actual.isBeforeFirst();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertIsAfterLast() throws SQLException {
        actual.isAfterLast();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertIsFirst() throws SQLException {
        actual.isFirst();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertIsLast() throws SQLException {
        actual.isLast();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertBeforeFirst() throws SQLException {
        actual.beforeFirst();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertAfterLast() throws SQLException {
        actual.afterLast();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertFirst() throws SQLException {
        actual.first();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertLast() throws SQLException {
        actual.last();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertAbsolute() throws SQLException {
        actual.absolute(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertRelative() throws SQLException {
        actual.relative(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetRow() throws SQLException {
        actual.getRow();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertInsertRow() throws SQLException {
        actual.insertRow();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateRow() throws SQLException {
        actual.updateRow();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertDeleteRow() throws SQLException {
        actual.deleteRow();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertRefreshRow() throws SQLException {
        actual.refreshRow();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertCancelRowUpdates() throws SQLException {
        actual.cancelRowUpdates();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertMoveToInsertRow() throws SQLException {
        actual.moveToInsertRow();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertMoveToCurrentRow() throws SQLException {
        actual.moveToCurrentRow();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertRowInserted() throws SQLException {
        actual.rowInserted();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertRowUpdated() throws SQLException {
        actual.rowUpdated();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertRowDeleted() throws SQLException {
        actual.rowDeleted();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetCursorName() throws SQLException {
        actual.getCursorName();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetHoldability() throws SQLException {
        actual.getHoldability();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getNStringForColumnIndex() throws SQLException {
        actual.getNString(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getNStringForColumnLabel() throws SQLException {
        actual.getNString("label");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetNClobForColumnIndex() throws SQLException {
        actual.getNClob(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetNClobForColumnLabel() throws SQLException {
        actual.getNClob("label");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getNCharacterStreamForColumnIndex() throws SQLException {
        actual.getNCharacterStream(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getNCharacterStreamForColumnLabel() throws SQLException {
        actual.getNCharacterStream("label");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetRefForColumnIndex() throws SQLException {
        actual.getRef(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetRefForColumnLabel() throws SQLException {
        actual.getRef("label");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetArrayForColumnIndex() throws SQLException {
        actual.getArray(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetArrayForColumnLabel() throws SQLException {
        actual.getArray("label");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetRowIdForColumnIndex() throws SQLException {
        actual.getRowId(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetRowIdForColumnLabel() throws SQLException {
        actual.getRowId("label");
    }
    
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertObjectForColumnIndexWithType() throws SQLException {
        actual.getObject(1, String.class);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertObjectForColumnLabelWithType() throws SQLException {
        actual.getObject("label", String.class);
    }
}
