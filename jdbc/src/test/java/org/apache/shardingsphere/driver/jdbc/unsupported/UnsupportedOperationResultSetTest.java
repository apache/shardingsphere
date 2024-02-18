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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

class UnsupportedOperationResultSetTest {
    
    private ShardingSphereResultSet shardingSphereResultSet;
    
    @BeforeEach
    void init() throws SQLException {
        shardingSphereResultSet = new ShardingSphereResultSet(
                Collections.singletonList(mock(ResultSet.class, RETURNS_DEEP_STUBS)), mock(MergedResult.class), mock(Statement.class), true, mock(ExecutionContext.class));
    }
    
    @Test
    void assertPrevious() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.previous());
    }
    
    @Test
    void assertIsBeforeFirst() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.isBeforeFirst());
    }
    
    @Test
    void assertIsAfterLast() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.isAfterLast());
    }
    
    @Test
    void assertIsFirst() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.isFirst());
    }
    
    @Test
    void assertIsLast() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.isLast());
    }
    
    @Test
    void assertBeforeFirst() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.beforeFirst());
    }
    
    @Test
    void assertAfterLast() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.afterLast());
    }
    
    @Test
    void assertFirst() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.first());
    }
    
    @Test
    void assertLast() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.last());
    }
    
    @Test
    void assertAbsolute() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.absolute(1));
    }
    
    @Test
    void assertRelative() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.relative(1));
    }
    
    @Test
    void assertGetRow() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.getRow());
    }
    
    @Test
    void assertInsertRow() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.insertRow());
    }
    
    @Test
    void assertUpdateRow() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.updateRow());
    }
    
    @Test
    void assertDeleteRow() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.deleteRow());
    }
    
    @Test
    void assertRefreshRow() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.refreshRow());
    }
    
    @Test
    void assertCancelRowUpdates() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.cancelRowUpdates());
    }
    
    @Test
    void assertMoveToInsertRow() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.moveToInsertRow());
    }
    
    @Test
    void assertMoveToCurrentRow() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.moveToCurrentRow());
    }
    
    @Test
    void assertRowInserted() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.rowInserted());
    }
    
    @Test
    void assertRowUpdated() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.rowUpdated());
    }
    
    @Test
    void assertRowDeleted() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.rowDeleted());
    }
    
    @Test
    void assertGetCursorName() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.getCursorName());
    }
    
    @Test
    void assertGetHoldability() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.getHoldability());
    }
    
    @Test
    void assertGetNClobForColumnIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.getNClob(1));
    }
    
    @Test
    void assertGetNClobForColumnLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.getNClob("label"));
    }
    
    @Test
    void assertGetNCharacterStreamForColumnIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.getNCharacterStream(1));
    }
    
    @Test
    void assertGetNCharacterStreamForColumnLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.getNCharacterStream("label"));
    }
    
    @Test
    void assertGetRefForColumnIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.getRef(1));
    }
    
    @Test
    void assertGetRefForColumnLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.getRef("label"));
    }
    
    @Test
    void assertGetRowIdForColumnIndex() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.getRowId(1));
    }
    
    @Test
    void assertGetRowIdForColumnLabel() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.getRowId("label"));
    }
    
    @Test
    void assertObjectForColumnIndexWithMap() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.getObject(1, Collections.emptyMap()));
    }
    
    @Test
    void assertObjectForColumnLabelWithMap() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereResultSet.getObject("label", Collections.emptyMap()));
    }
}
