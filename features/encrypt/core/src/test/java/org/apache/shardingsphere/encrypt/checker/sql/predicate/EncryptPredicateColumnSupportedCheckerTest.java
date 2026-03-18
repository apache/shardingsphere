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

package org.apache.shardingsphere.encrypt.checker.sql.predicate;

import org.apache.shardingsphere.encrypt.exception.metadata.MissingMatchedEncryptQueryAlgorithmException;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.fixture.EncryptGeneratorFixtureBuilder;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EncryptPredicateColumnSupportedCheckerTest {
    
    @Test
    void assertIsCheckWithNotWhereContextAvailable() {
        assertFalse(new EncryptPredicateColumnSupportedChecker().isCheck(mock(SQLStatementContext.class)));
    }
    
    @Test
    void assertIsCheckWithEmptyWhereSegment() {
        assertFalse(new EncryptPredicateColumnSupportedChecker().isCheck(mock(SelectStatementContext.class)));
    }
    
    @Test
    void assertIsCheckWithNotEmptyWhereSegments() {
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class);
        when(sqlStatementContext.getWhereSegments()).thenReturn(Collections.singleton(mock(WhereSegment.class)));
        assertTrue(new EncryptPredicateColumnSupportedChecker().isCheck(sqlStatementContext));
    }
    
    @Test
    void assertCheckWithDifferentEncryptorsInJoinCondition() {
        assertThrows(UnsupportedSQLOperationException.class, () -> new EncryptPredicateColumnSupportedChecker()
                .check(EncryptGeneratorFixtureBuilder.createEncryptRule(), null, null, mockSelectStatementContextWithDifferentEncryptorsInJoinCondition()));
    }
    
    private SQLStatementContext mockSelectStatementContextWithDifferentEncryptorsInJoinCondition() {
        ColumnSegment leftColumn = new ColumnSegment(0, 0, new IdentifierValue("user_name"));
        leftColumn.setColumnBoundInfo(new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")), new IdentifierValue("t_user"),
                new IdentifierValue("user_name"), TableSourceType.PHYSICAL_TABLE));
        ColumnSegment rightColumn = new ColumnSegment(0, 0, new IdentifierValue("user_id"));
        rightColumn.setColumnBoundInfo(
                new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")), new IdentifierValue("t_user"), new IdentifierValue("user_id"),
                        TableSourceType.PHYSICAL_TABLE));
        SelectStatementContext result = mock(SelectStatementContext.class);
        when(result.getJoinConditions()).thenReturn(Collections.singleton(new BinaryOperationExpression(0, 0, leftColumn, rightColumn, "=", "")));
        return result;
    }
    
    @Test
    void assertCheckWithNotMatchedLikeQueryEncryptor() {
        assertThrows(MissingMatchedEncryptQueryAlgorithmException.class, () -> new EncryptPredicateColumnSupportedChecker()
                .check(EncryptGeneratorFixtureBuilder.createEncryptRule(), null, null, mockSelectStatementContextWithLike()));
    }
    
    private SQLStatementContext mockSelectStatementContextWithLike() {
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("user_name"));
        columnSegment.setColumnBoundInfo(new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")), new IdentifierValue("t_user"),
                new IdentifierValue("user_name"), TableSourceType.PHYSICAL_TABLE));
        SelectStatementContext result = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getColumnSegments()).thenReturn(Collections.singleton(columnSegment));
        when(result.getWhereSegments()).thenReturn(Collections.singleton(new WhereSegment(0, 0, new BinaryOperationExpression(0, 0, columnSegment, columnSegment, "LIKE", ""))));
        when(result.getSubqueryContexts()).thenReturn(Collections.emptyMap());
        when(result.getJoinConditions()).thenReturn(Collections.emptyList());
        return result;
    }
    
    @Test
    void assertCheckSuccess() {
        assertDoesNotThrow(() -> new EncryptPredicateColumnSupportedChecker().check(EncryptGeneratorFixtureBuilder.createEncryptRule(), null, null, mockSelectStatementContextWithEqual()));
    }
    
    private SQLStatementContext mockSelectStatementContextWithEqual() {
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("user_name"));
        columnSegment.setColumnBoundInfo(new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")), new IdentifierValue("t_user"),
                new IdentifierValue("user_name"), TableSourceType.PHYSICAL_TABLE));
        SelectStatementContext result = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getColumnSegments()).thenReturn(Collections.singleton(columnSegment));
        when(result.getWhereSegments()).thenReturn(Collections.singleton(new WhereSegment(0, 0, new BinaryOperationExpression(0, 0, columnSegment, columnSegment, "=", ""))));
        when(result.getSubqueryContexts()).thenReturn(Collections.emptyMap());
        when(result.getJoinConditions()).thenReturn(Collections.emptyList());
        return result;
    }
}
