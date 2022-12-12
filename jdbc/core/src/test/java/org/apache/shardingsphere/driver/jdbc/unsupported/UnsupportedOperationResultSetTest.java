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

import org.apache.shardingsphere.driver.jdbc.core.resultset.ShardingSphereResultSet;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.junit.Before;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.Collections;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

public final class UnsupportedOperationResultSetTest {
    
    private ShardingSphereResultSet shardingSphereResultSet;
    
    @Before
    public void init() throws SQLException {
        shardingSphereResultSet = new ShardingSphereResultSet(
                Collections.singletonList(mock(ResultSet.class, RETURNS_DEEP_STUBS)), mock(MergedResult.class), mock(Statement.class), mock(ExecutionContext.class));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertPrevious() throws SQLException {
        shardingSphereResultSet.previous();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertIsBeforeFirst() throws SQLException {
        shardingSphereResultSet.isBeforeFirst();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertIsAfterLast() throws SQLException {
        shardingSphereResultSet.isAfterLast();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertIsFirst() throws SQLException {
        shardingSphereResultSet.isFirst();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertIsLast() throws SQLException {
        shardingSphereResultSet.isLast();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertBeforeFirst() throws SQLException {
        shardingSphereResultSet.beforeFirst();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertAfterLast() throws SQLException {
        shardingSphereResultSet.afterLast();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertFirst() throws SQLException {
        shardingSphereResultSet.first();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertLast() throws SQLException {
        shardingSphereResultSet.last();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertAbsolute() throws SQLException {
        shardingSphereResultSet.absolute(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertRelative() throws SQLException {
        shardingSphereResultSet.relative(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetRow() throws SQLException {
        shardingSphereResultSet.getRow();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertInsertRow() throws SQLException {
        shardingSphereResultSet.insertRow();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertUpdateRow() throws SQLException {
        shardingSphereResultSet.updateRow();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertDeleteRow() throws SQLException {
        shardingSphereResultSet.deleteRow();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertRefreshRow() throws SQLException {
        shardingSphereResultSet.refreshRow();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertCancelRowUpdates() throws SQLException {
        shardingSphereResultSet.cancelRowUpdates();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertMoveToInsertRow() throws SQLException {
        shardingSphereResultSet.moveToInsertRow();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertMoveToCurrentRow() throws SQLException {
        shardingSphereResultSet.moveToCurrentRow();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertRowInserted() throws SQLException {
        shardingSphereResultSet.rowInserted();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertRowUpdated() throws SQLException {
        shardingSphereResultSet.rowUpdated();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertRowDeleted() throws SQLException {
        shardingSphereResultSet.rowDeleted();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetCursorName() throws SQLException {
        shardingSphereResultSet.getCursorName();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetHoldability() throws SQLException {
        shardingSphereResultSet.getHoldability();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetNClobForColumnIndex() throws SQLException {
        shardingSphereResultSet.getNClob(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetNClobForColumnLabel() throws SQLException {
        shardingSphereResultSet.getNClob("label");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getNCharacterStreamForColumnIndex() throws SQLException {
        shardingSphereResultSet.getNCharacterStream(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getNCharacterStreamForColumnLabel() throws SQLException {
        shardingSphereResultSet.getNCharacterStream("label");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetRefForColumnIndex() throws SQLException {
        shardingSphereResultSet.getRef(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetRefForColumnLabel() throws SQLException {
        shardingSphereResultSet.getRef("label");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetRowIdForColumnIndex() throws SQLException {
        shardingSphereResultSet.getRowId(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetRowIdForColumnLabel() throws SQLException {
        shardingSphereResultSet.getRowId("label");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertObjectForColumnIndexWithMap() throws SQLException {
        shardingSphereResultSet.getObject(1, Collections.emptyMap());
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertObjectForColumnLabelWithMap() throws SQLException {
        shardingSphereResultSet.getObject("label", Collections.emptyMap());
    }
}
