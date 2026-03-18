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

package org.apache.shardingsphere.encrypt.checker.sql.projection;

import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.combine.CombineSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EncryptSelectProjectionSupportedCheckerTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertIsCheckWithNotSelectStatementContext() {
        assertFalse(new EncryptSelectProjectionSupportedChecker().isCheck(mock(SQLStatementContext.class)));
    }
    
    @Test
    void assertIsCheckWithEmptyTable() {
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getSimpleTables().isEmpty()).thenReturn(true);
        assertFalse(new EncryptSelectProjectionSupportedChecker().isCheck(sqlStatementContext));
    }
    
    @Test
    void assertIsCheckWithTables() {
        assertTrue(new EncryptSelectProjectionSupportedChecker().isCheck(mock(SelectStatementContext.class, RETURNS_DEEP_STUBS)));
    }
    
    @Test
    void assertCheckWhenShorthandExpandContainsSubqueryTable() {
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.containsTableSubquery()).thenReturn(true);
        when(sqlStatementContext.getSqlStatement().getProjections().getProjections()).thenReturn(Collections.singletonList(new ShorthandProjectionSegment(0, 0)));
        assertThrows(UnsupportedSQLOperationException.class, () -> new EncryptSelectProjectionSupportedChecker().check(mock(EncryptRule.class, RETURNS_DEEP_STUBS), null, null, sqlStatementContext));
    }
    
    @Test
    void assertCheckWhenCombineStatementContainsEncryptColumn() {
        SelectStatementContext sqlStatementContext = mockSelectStatementContext();
        assertThrows(UnsupportedSQLOperationException.class, () -> new EncryptSelectProjectionSupportedChecker().check(mock(EncryptRule.class, RETURNS_DEEP_STUBS), null, null, sqlStatementContext));
    }
    
    @Test
    void assertCheckSuccess() {
        SelectStatementContext sqlStatementContext = mockSelectStatementContext();
        assertDoesNotThrow(() -> new EncryptSelectProjectionSupportedChecker().check(mockEncryptRule(), null, null, sqlStatementContext));
    }
    
    private SelectStatementContext mockSelectStatementContext() {
        SelectStatementContext result = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        CombineSegment combineSegment = mock(CombineSegment.class, RETURNS_DEEP_STUBS);
        when(combineSegment.getLeft().getStartIndex()).thenReturn(0);
        when(combineSegment.getRight().getStartIndex()).thenReturn(1);
        when(result.getSqlStatement().getCombine()).thenReturn(Optional.of(combineSegment));
        ColumnProjection leftColumn1 = new ColumnProjection(new IdentifierValue("f"), new IdentifierValue("foo_col_1"), null, databaseType, null, null,
                new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(new IdentifierValue(""), new IdentifierValue("")), new IdentifierValue("foo_tbl"), new IdentifierValue("foo_col_1"),
                        TableSourceType.PHYSICAL_TABLE));
        ColumnProjection leftColumn2 = new ColumnProjection(new IdentifierValue("f"), new IdentifierValue("foo_col_2"), null, databaseType, null, null,
                new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(new IdentifierValue(""), new IdentifierValue("")), new IdentifierValue("foo_tbl"), new IdentifierValue("foo_col_2"),
                        TableSourceType.PHYSICAL_TABLE));
        SelectStatementContext leftSelectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(leftSelectStatementContext.getProjectionsContext().getExpandProjections()).thenReturn(Arrays.asList(leftColumn1, leftColumn2));
        ColumnProjection rightColumn1 = new ColumnProjection(new IdentifierValue("b"), new IdentifierValue("bar_col_1"), null, databaseType, null, null,
                new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(new IdentifierValue(""), new IdentifierValue("")), new IdentifierValue("bar_tbl"), new IdentifierValue("bar_col_1"),
                        TableSourceType.PHYSICAL_TABLE));
        ColumnProjection rightColumn2 = new ColumnProjection(new IdentifierValue("b"), new IdentifierValue("bar_col_2"), null, databaseType, null, null,
                new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(new IdentifierValue(""), new IdentifierValue("")), new IdentifierValue("bar_tbl"), new IdentifierValue("bar_col_2"),
                        TableSourceType.PHYSICAL_TABLE));
        SelectStatementContext rightSelectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(rightSelectStatementContext.getProjectionsContext().getExpandProjections()).thenReturn(Arrays.asList(rightColumn1, rightColumn2));
        Map<Integer, SelectStatementContext> subqueryContexts = new LinkedHashMap<>(2, 1F);
        subqueryContexts.put(0, leftSelectStatementContext);
        subqueryContexts.put(1, rightSelectStatementContext);
        when(result.getSubqueryContexts()).thenReturn(subqueryContexts);
        return result;
    }
    
    private EncryptRule mockEncryptRule() {
        EncryptRule result = mock(EncryptRule.class, RETURNS_DEEP_STUBS);
        when(result.findQueryEncryptor("foo_tbl", "foo_col_1")).thenReturn(Optional.empty());
        when(result.findQueryEncryptor("foo_tbl", "foo_col_2")).thenReturn(Optional.empty());
        when(result.findQueryEncryptor("bar_tbl", "bar_col_1")).thenReturn(Optional.empty());
        when(result.findQueryEncryptor("bar_tbl", "bar_col_2")).thenReturn(Optional.empty());
        return result;
    }
}
