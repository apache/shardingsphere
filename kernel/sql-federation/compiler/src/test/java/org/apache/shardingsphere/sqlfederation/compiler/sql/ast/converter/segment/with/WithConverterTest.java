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

package org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.with;

import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlWith;
import org.apache.calcite.sql.SqlWithItem;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.MergeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.ColumnConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.statement.type.SelectStatementConverter;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedConstruction;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ColumnConverter.class)
class WithConverterTest {
    
    @Test
    void assertConvertWithoutCommonTableExpressions() {
        SqlNode expectedBody = mock(SqlNode.class);
        SqlWith actual = (SqlWith) WithConverter.convert(new WithSegment(0, 30, Collections.emptyList()), expectedBody).orElseThrow(AssertionError::new);
        assertTrue(actual.withList.isEmpty());
        assertThat(actual.body, sameInstance(expectedBody));
    }
    
    @Test
    void assertConvertWithSelect() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        CommonTableExpressionSegment commonTableExpression = createSelectCommonTableExpression(selectStatement);
        SqlNode expectedQuery = mock(SqlNode.class);
        SqlNode expectedBody = mock(SqlNode.class);
        try (
                MockedConstruction<SelectStatementConverter> ignored = mockConstruction(SelectStatementConverter.class,
                        (mock, context) -> when(mock.convert(selectStatement)).thenReturn(expectedQuery))) {
            SqlWith actual = (SqlWith) WithConverter.convert(new WithSegment(0, 30, Collections.singletonList(commonTableExpression)), expectedBody).orElseThrow(AssertionError::new);
            assertThat(actual.withList.size(), is(1));
            SqlWithItem actualItem = (SqlWithItem) actual.withList.get(0);
            assertThat(actualItem.name.getSimple(), is("foo_cte"));
            assertNull(actualItem.columnList);
            assertThat(actualItem.query, sameInstance(expectedQuery));
            assertFalse(actualItem.recursive.booleanValue());
            assertThat(actual.body, sameInstance(expectedBody));
        }
    }
    
    private CommonTableExpressionSegment createSelectCommonTableExpression(final SelectStatement selectStatement) {
        return new CommonTableExpressionSegment(0, 20, new AliasSegment(0, 6, new IdentifierValue("foo_cte")), new SubquerySegment(8, 20, selectStatement, ""), null);
    }
    
    @Test
    void assertConvertWithColumns() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        CommonTableExpressionSegment commonTableExpression = createSelectCommonTableExpression(selectStatement);
        ColumnSegment column = new ColumnSegment(0, 0, new IdentifierValue("foo_column"));
        commonTableExpression.getColumns().add(column);
        SqlIdentifier expectedColumn = new SqlIdentifier("foo_column", SqlParserPos.ZERO);
        when(ColumnConverter.convert(column)).thenReturn(expectedColumn);
        SqlNode expectedQuery = mock(SqlNode.class);
        try (
                MockedConstruction<SelectStatementConverter> ignored = mockConstruction(SelectStatementConverter.class,
                        (mock, context) -> when(mock.convert(selectStatement)).thenReturn(expectedQuery))) {
            SqlWith actual = (SqlWith) WithConverter.convert(new WithSegment(0, 30, Collections.singletonList(commonTableExpression)), mock(SqlNode.class)).orElseThrow(AssertionError::new);
            SqlWithItem actualItem = (SqlWithItem) actual.withList.get(0);
            assertThat(actualItem.columnList.size(), is(1));
            assertThat(actualItem.columnList.get(0), sameInstance(expectedColumn));
            assertThat(actualItem.query, sameInstance(expectedQuery));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getSearchOrCycleArguments")
    void assertConvertWithSearchOrCycle(final String name, final boolean search, final boolean cycle) {
        CommonTableExpressionSegment commonTableExpression = new CommonTableExpressionSegment(0, 40, new AliasSegment(0, 6, new IdentifierValue("foo_cte")),
                new SubquerySegment(8, 20, mock(SelectStatement.class), ""), null);
        ColumnSegment column = new ColumnSegment(12, 21, new IdentifierValue("foo_column"));
        if (search) {
            commonTableExpression.getSearchColumns().add(column);
        }
        if (cycle) {
            commonTableExpression.getCycleColumns().add(column);
        }
        commonTableExpression.getColumns().add(new ColumnSegment(30, 39, new IdentifierValue("foo_generated")));
        WithSegment withSegment = new WithSegment(0, 40, Collections.singletonList(commonTableExpression));
        UnsupportedSQLOperationException actualException = assertThrows(UnsupportedSQLOperationException.class, () -> WithConverter.convert(withSegment, mock(SqlNode.class)));
        assertThat(actualException.getMessage(), is("Unsupported SQL operation: SEARCH or CYCLE clauses in SQL Federation."));
    }
    
    private static Stream<Arguments> getSearchOrCycleArguments() {
        return Stream.of(Arguments.of("SEARCH", true, false), Arguments.of("CYCLE", false, true), Arguments.of("SEARCH and CYCLE", true, true));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDataModificationArguments")
    void assertConvertWithDataModification(final String name, final DMLStatement dataModification) {
        CommonTableExpressionSegment commonTableExpression = new CommonTableExpressionSegment(0, 20, new AliasSegment(0, 6, new IdentifierValue("foo_cte")), null, dataModification);
        WithSegment withSegment = new WithSegment(0, 30, Collections.singletonList(commonTableExpression));
        UnsupportedSQLOperationException actualException = assertThrows(UnsupportedSQLOperationException.class, () -> WithConverter.convert(withSegment, mock(SqlNode.class)));
        assertThat(actualException.getMessage(), is("Unsupported SQL operation: Data-modifying common table expressions in SQL Federation."));
    }
    
    private static Stream<Arguments> getDataModificationArguments() {
        return Stream.of(
                Arguments.of("INSERT", InsertStatement.builder().build()),
                Arguments.of("UPDATE", UpdateStatement.builder().build()),
                Arguments.of("DELETE", DeleteStatement.builder().build()),
                Arguments.of("MERGE", MergeStatement.builder().build()));
    }
    
    @Test
    void assertConvertWithDataModificationAfterSelect() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        CommonTableExpressionSegment select = createSelectCommonTableExpression(selectStatement);
        CommonTableExpressionSegment dataModification = new CommonTableExpressionSegment(21, 40, new AliasSegment(21, 27, new IdentifierValue("bar_cte")), null, UpdateStatement.builder().build());
        WithSegment withSegment = new WithSegment(0, 50, Arrays.asList(select, dataModification));
        try (
                MockedConstruction<SelectStatementConverter> ignored = mockConstruction(SelectStatementConverter.class,
                        (mock, context) -> when(mock.convert(selectStatement)).thenReturn(mock(SqlNode.class)))) {
            UnsupportedSQLOperationException actualException = assertThrows(UnsupportedSQLOperationException.class, () -> WithConverter.convert(withSegment, mock(SqlNode.class)));
            assertThat(actualException.getMessage(), is("Unsupported SQL operation: Data-modifying common table expressions in SQL Federation."));
        }
    }
}
