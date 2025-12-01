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

package org.apache.shardingsphere.encrypt.checker.sql.orderby;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.encrypt.exception.syntax.UnsupportedEncryptSQLException;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.table.EncryptTable;
import org.apache.shardingsphere.infra.binder.context.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EncryptOrderByItemSupportedCheckerTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertIsCheckWithNotSelectStatementContext() {
        assertFalse(new EncryptOrderByItemSupportedChecker().isCheck(mock(SQLStatementContext.class)));
    }
    
    @Test
    void assertIsCheckWithoutOrderBy() {
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getOrderByContext().getItems().isEmpty()).thenReturn(true);
        assertFalse(new EncryptOrderByItemSupportedChecker().isCheck(sqlStatementContext));
    }
    
    @Test
    void assertIsCheckWithGeneratedOrderBy() {
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getOrderByContext().isGenerated()).thenReturn(true);
        assertFalse(new EncryptOrderByItemSupportedChecker().isCheck(sqlStatementContext));
    }
    
    @Test
    void assertIsCheckWithOrderBy() {
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        assertTrue(new EncryptOrderByItemSupportedChecker().isCheck(sqlStatementContext));
    }
    
    @Test
    void assertIsCheckWithSubQueryOrderBy() {
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getOrderByContext().isGenerated()).thenReturn(true);
        SelectStatementContext subQuerySelectStatementContext0 = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(subQuerySelectStatementContext0.getOrderByContext().getItems().isEmpty()).thenReturn(true);
        SelectStatementContext subQuerySelectStatementContext1 = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getSubqueryContexts().values()).thenReturn(Arrays.asList(subQuerySelectStatementContext0, subQuerySelectStatementContext1));
        assertTrue(new EncryptOrderByItemSupportedChecker().isCheck(sqlStatementContext));
    }
    
    @Test
    void assertCheckFailed() {
        assertThrows(UnsupportedEncryptSQLException.class, () -> new EncryptOrderByItemSupportedChecker().check(mockEncryptRule(), mock(), mock(), mockSelectStatementContext("foo_tbl")));
    }
    
    @Test
    void assertCheckSuccess() {
        assertDoesNotThrow(() -> new EncryptOrderByItemSupportedChecker().check(mockEncryptRule(), mock(), mock(), mockSelectStatementContext("bar_tbl")));
    }
    
    private EncryptRule mockEncryptRule() {
        EncryptRule result = mock(EncryptRule.class);
        EncryptTable encryptTable = mock(EncryptTable.class);
        when(encryptTable.isEncryptColumn("foo_col")).thenReturn(true);
        EncryptColumn encryptColumn = mock(EncryptColumn.class, RETURNS_DEEP_STUBS);
        when(encryptColumn.getAssistedQuery()).thenReturn(Optional.empty());
        when(encryptTable.getEncryptColumn("foo_col")).thenReturn(encryptColumn);
        when(result.findEncryptTable("foo_tbl")).thenReturn(Optional.of(encryptTable));
        return result;
    }
    
    private SelectStatementContext mockSelectStatementContext(final String tableName) {
        SimpleTableSegment simpleTableSegment = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue(tableName)));
        simpleTableSegment.setAlias(new AliasSegment(0, 0, new IdentifierValue("a")));
        ColumnSegment columnSegment = getColumnSegment(tableName);
        SelectStatementContext result = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatement().getDatabaseType()).thenReturn(databaseType);
        ColumnOrderByItemSegment columnOrderByItemSegment = new ColumnOrderByItemSegment(columnSegment, OrderDirection.ASC, NullsOrderType.FIRST);
        OrderByItem orderByItem = new OrderByItem(columnOrderByItemSegment);
        when(result.getOrderByContext().getItems()).thenReturn(Collections.singleton(orderByItem));
        when(result.getGroupByContext().getItems()).thenReturn(Collections.emptyList());
        when(result.getSubqueryContexts().values()).thenReturn(Collections.emptyList());
        when(result.getTablesContext()).thenReturn(new TablesContext(Collections.singleton(simpleTableSegment)));
        return result;
    }
    
    private ColumnSegment getColumnSegment(final String tableName) {
        ColumnSegment result = new ColumnSegment(0, 0, new IdentifierValue("foo_col"));
        result.setOwner(new OwnerSegment(0, 0, new IdentifierValue("a")));
        TableSegmentBoundInfo tableSegmentBoundInfo = new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_db"));
        result.setColumnBoundInfo(new ColumnSegmentBoundInfo(tableSegmentBoundInfo, new IdentifierValue(tableName), new IdentifierValue("foo_col"), TableSourceType.TEMPORARY_TABLE));
        return result;
    }
}
