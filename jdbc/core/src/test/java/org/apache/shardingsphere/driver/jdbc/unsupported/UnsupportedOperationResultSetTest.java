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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

public final class UnsupportedOperationResultSetTest {
    
    private ShardingSphereResultSet shardingSphereResultSet;
    
    @Before
    public void init() throws SQLException {
        shardingSphereResultSet = new ShardingSphereResultSet(
                Collections.singletonList(mock(ResultSet.class, RETURNS_DEEP_STUBS)), mock(MergedResult.class), mock(Statement.class), mock(ExecutionContext.class));
    }
    
    @Test
    public void assertPrevious() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.previous());
    }
    
    @Test
    public void assertIsBeforeFirst() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.isBeforeFirst());
    }
    
    @Test
    public void assertIsAfterLast() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.isAfterLast());
    }
    
    @Test
    public void assertIsFirst() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.isFirst());
    }
    
    @Test
    public void assertIsLast() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.isLast());
    }
    
    @Test
    public void assertBeforeFirst() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.beforeFirst());
    }
    
    @Test
    public void assertAfterLast() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.afterLast());
    }
    
    @Test
    public void assertFirst() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.first());
    }
    
    @Test
    public void assertLast() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.last());
    }
    
    @Test
    public void assertAbsolute() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.absolute(1));
    }
    
    @Test
    public void assertRelative() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.relative(1));
    }
    
    @Test
    public void assertGetRow() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.getRow());
    }
    
    @Test
    public void assertInsertRow() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.insertRow());
    }
    
    @Test
    public void assertUpdateRow() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.updateRow());
    }
    
    @Test
    public void assertDeleteRow() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.deleteRow());
    }
    
    @Test
    public void assertRefreshRow() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.refreshRow());
    }
    
    @Test
    public void assertCancelRowUpdates() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.cancelRowUpdates());
    }
    
    @Test
    public void assertMoveToInsertRow() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.moveToInsertRow());
    }
    
    @Test
    public void assertMoveToCurrentRow() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.moveToCurrentRow());
    }
    
    @Test
    public void assertRowInserted() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.rowInserted());
    }
    
    @Test
    public void assertRowUpdated() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.rowUpdated());
    }
    
    @Test
    public void assertRowDeleted() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.rowDeleted());
    }
    
    @Test
    public void assertGetCursorName() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.getCursorName());
    }
    
    @Test
    public void assertGetHoldability() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.getHoldability());
    }
    
    @Test
    public void assertGetNClobForColumnIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.getNClob(1));
    }
    
    @Test
    public void assertGetNClobForColumnLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.getNClob("label"));
    }
    
    @Test
    public void getNCharacterStreamForColumnIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.getNCharacterStream(1));
    }
    
    @Test
    public void getNCharacterStreamForColumnLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.getNCharacterStream("label"));
    }
    
    @Test
    public void assertGetRefForColumnIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.getRef(1));
    }
    
    @Test
    public void assertGetRefForColumnLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.getRef("label"));
    }
    
    @Test
    public void assertGetRowIdForColumnIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.getRowId(1));
    }
    
    @Test
    public void assertGetRowIdForColumnLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.getRowId("label"));
    }
    
    @Test
    public void assertObjectForColumnIndexWithMap() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.getObject(1, Collections.emptyMap()));
    }
    
    @Test
    public void assertObjectForColumnLabelWithMap() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.getObject("label", Collections.emptyMap()));
    }
}
