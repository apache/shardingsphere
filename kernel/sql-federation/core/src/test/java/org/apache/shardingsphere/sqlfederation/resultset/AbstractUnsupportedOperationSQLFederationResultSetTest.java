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

package org.apache.shardingsphere.sqlfederation.resultset;

import org.junit.jupiter.api.Test;

import java.sql.SQLFeatureNotSupportedException;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;

@SuppressWarnings("resource")
class AbstractUnsupportedOperationSQLFederationResultSetTest {
    
    @Test
    void assertPrevious() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).previous());
        assertThat(ex.getMessage(), is("previous"));
    }
    
    @Test
    void assertIsBeforeFirst() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).isBeforeFirst());
        assertThat(ex.getMessage(), is("isBeforeFirst"));
    }
    
    @Test
    void assertIsAfterLast() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).isAfterLast());
        assertThat(ex.getMessage(), is("isAfterLast"));
    }
    
    @Test
    void assertIsFirst() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).isFirst());
        assertThat(ex.getMessage(), is("isFirst"));
    }
    
    @Test
    void assertIsLast() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).isLast());
        assertThat(ex.getMessage(), is("isLast"));
    }
    
    @Test
    void assertBeforeFirst() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).beforeFirst());
        assertThat(ex.getMessage(), is("beforeFirst"));
    }
    
    @Test
    void assertAfterLast() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).afterLast());
        assertThat(ex.getMessage(), is("afterLast"));
    }
    
    @Test
    void assertFirst() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).first());
        assertThat(ex.getMessage(), is("first"));
    }
    
    @Test
    void assertLast() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).last());
        assertThat(ex.getMessage(), is("last"));
    }
    
    @Test
    void assertAbsolute() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).absolute(1));
        assertThat(ex.getMessage(), is("absolute"));
    }
    
    @Test
    void assertRelative() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).relative(1));
        assertThat(ex.getMessage(), is("relative"));
    }
    
    @Test
    void assertGetRow() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).getRow());
        assertThat(ex.getMessage(), is("getRow"));
    }
    
    @Test
    void assertInsertRow() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).insertRow());
        assertThat(ex.getMessage(), is("insertRow"));
    }
    
    @Test
    void assertUpdateRow() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).updateRow());
        assertThat(ex.getMessage(), is("updateRow"));
    }
    
    @Test
    void assertDeleteRow() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).deleteRow());
        assertThat(ex.getMessage(), is("deleteRow"));
    }
    
    @Test
    void assertRefreshRow() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).refreshRow());
        assertThat(ex.getMessage(), is("refreshRow"));
    }
    
    @Test
    void assertCancelRowUpdates() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).cancelRowUpdates());
        assertThat(ex.getMessage(), is("cancelRowUpdates"));
    }
    
    @Test
    void assertMoveToInsertRow() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).moveToInsertRow());
        assertThat(ex.getMessage(), is("moveToInsertRow"));
    }
    
    @Test
    void assertMoveToCurrentRow() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).moveToCurrentRow());
        assertThat(ex.getMessage(), is("moveToCurrentRow"));
    }
    
    @Test
    void assertRowInserted() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).rowInserted());
        assertThat(ex.getMessage(), is("rowInserted"));
    }
    
    @Test
    void assertRowUpdated() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).rowUpdated());
        assertThat(ex.getMessage(), is("rowUpdated"));
    }
    
    @Test
    void assertRowDeleted() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).rowDeleted());
        assertThat(ex.getMessage(), is("rowDeleted"));
    }
    
    @Test
    void assertGetCursorName() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).getCursorName());
        assertThat(ex.getMessage(), is("getCursorName"));
    }
    
    @Test
    void assertGetHoldability() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).getHoldability());
        assertThat(ex.getMessage(), is("getHoldability"));
    }
    
    @Test
    void assertGetNClobWithColumnIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).getNClob(1));
        assertThat(ex.getMessage(), is("getNClob"));
    }
    
    @Test
    void assertGetNClobWithColumnLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).getNClob("c"));
        assertThat(ex.getMessage(), is("getNClob"));
    }
    
    @Test
    void assertGetNCharacterStreamWithColumnIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).getNCharacterStream(1));
        assertThat(ex.getMessage(), is("getNCharacterStream"));
    }
    
    @Test
    void assertGetNCharacterStreamWithColumnLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).getNCharacterStream("c"));
        assertThat(ex.getMessage(), is("getNCharacterStream"));
    }
    
    @Test
    void assertGetRefWithColumnIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).getRef(1));
        assertThat(ex.getMessage(), is("getRef"));
    }
    
    @Test
    void assertGetRefWithColumnLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).getRef("c"));
        assertThat(ex.getMessage(), is("getRef"));
    }
    
    @Test
    void assertGetRowIdWithColumnIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).getRowId(1));
        assertThat(ex.getMessage(), is("getRowId"));
    }
    
    @Test
    void assertGetRowIdWithColumnLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).getRowId("c"));
        assertThat(ex.getMessage(), is("getRowId"));
    }
    
    @Test
    void assertGetObjectWithTypeByColumnIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).getObject(1, Object.class));
        assertThat(ex.getMessage(), is("getObject with type"));
    }
    
    @Test
    void assertGetObjectWithTypeByColumnLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).getObject("c", Object.class));
        assertThat(ex.getMessage(), is("getObject with type"));
    }
    
    @Test
    void assertGetObjectWithMapByColumnLabel() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).getObject("c", Collections.emptyMap()));
        assertThat(ex.getMessage(), is("getObject with map"));
    }
    
    @Test
    void assertGetObjectWithMapByColumnIndex() {
        SQLFeatureNotSupportedException ex = assertThrows(SQLFeatureNotSupportedException.class,
                () -> mock(AbstractUnsupportedOperationSQLFederationResultSet.class, CALLS_REAL_METHODS).getObject(1, Collections.emptyMap()));
        assertThat(ex.getMessage(), is("getObject with map"));
    }
}
