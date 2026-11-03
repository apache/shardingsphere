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

package org.apache.shardingsphere.infra.binder.engine.segment.dml.projection;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.RowExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProjectionsSegmentBinderTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("shorthandScopeCases")
    void assertBindShorthandScope(final String name, final String localAlias, final boolean qualified, final String expectedLabel) {
        ShorthandProjectionSegment shorthand = new ShorthandProjectionSegment(7, 18);
        if (qualified) {
            shorthand.setOwner(new OwnerSegment(7, 16, new IdentifierValue("foo_source")));
        }
        ProjectionsSegment projections = new ProjectionsSegment(7, 18);
        projections.getProjections().add(shorthand);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> local = LinkedHashMultimap.create();
        if (!localAlias.isEmpty()) {
            local.put(CaseInsensitiveString.of(localAlias), createSourceContext("foo_local"));
        }
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> outer = LinkedHashMultimap.create();
        outer.put(CaseInsensitiveString.of("foo_source"), createSourceContext("foo_outer"));
        SQLStatementBinderContext context = mock(SQLStatementBinderContext.class);
        when(context.getSqlStatement()).thenReturn(SelectStatement.builder().databaseType(TypedSPILoader.getService(DatabaseType.class, "FIXTURE")).projections(projections).build());
        SimpleTableSegment boundTable = localAlias.isEmpty() ? null : mock(SimpleTableSegment.class);
        if (null != boundTable) {
            TableNameSegment tableName = mock(TableNameSegment.class);
            when(tableName.getIdentifier()).thenReturn(new IdentifierValue(localAlias));
            when(boundTable.getTableName()).thenReturn(tableName);
        }
        ProjectionsSegment actual = ProjectionsSegmentBinder.bind(projections, context, boundTable, local, outer);
        ShorthandProjectionSegment actualShorthand = (ShorthandProjectionSegment) actual.getProjections().iterator().next();
        assertThat(actualShorthand.getActualProjectionSegments().size(), is(1));
        assertThat(actualShorthand.getActualProjectionSegments().iterator().next().getColumnLabel(), is(expectedLabel));
    }
    
    private static Stream<Arguments> shorthandScopeCases() {
        return Stream.of(
                Arguments.of("correlated star without local FROM", "", true, "foo_outer"),
                Arguments.of("unrelated local alias", "bar_source", true, "foo_outer"),
                Arguments.of("local alias shadows outer", "foo_source", true, "foo_local"),
                Arguments.of("unqualified star stays local", "foo_source", false, "foo_local"));
    }
    
    private TableSegmentBinderContext createSourceContext(final String column) {
        TableSegmentBinderContext result = mock(TableSegmentBinderContext.class);
        when(result.getProjectionSegments()).thenReturn(Collections.singleton(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue(column)))));
        return result;
    }
    
    @Test
    void assertBindValuesRowsPreservesProjectionShape() {
        ProjectionsSegment projections = new ProjectionsSegment(0, 15);
        LiteralExpressionSegment firstValue = new LiteralExpressionSegment(8, 8, 1);
        projections.getProjections().add(new ExpressionProjectionSegment(8, 8, "1", firstValue));
        RowExpression first = new RowExpression(7, 9, "(1)");
        first.getItems().add(firstValue);
        projections.getValuesRows().add(first);
        RowExpression second = new RowExpression(12, 14, "(2)");
        LiteralExpressionSegment secondValue = new LiteralExpressionSegment(13, 13, 2);
        second.getItems().add(secondValue);
        projections.getValuesRows().add(second);
        SQLStatementBinderContext context = mock(SQLStatementBinderContext.class);
        when(context.getSqlStatement()).thenReturn(SelectStatement.builder().databaseType(TypedSPILoader.getService(DatabaseType.class, "FIXTURE")).projections(projections).build());
        ProjectionsSegment actual = ProjectionsSegmentBinder.bind(projections, context, null, LinkedHashMultimap.create(), LinkedHashMultimap.create());
        assertThat(actual.getProjections().size(), is(1));
        assertThat(actual.getValuesRows().size(), is(2));
        assertThat(actual.getValuesRows().get(1), not(sameInstance(second)));
        assertThat(actual.getValuesRows().get(1).getText(), is("(2)"));
        assertThat(actual.getValuesRows().get(1).getStartIndex(), is(12));
        assertThat(actual.getValuesRows().get(1).getStopIndex(), is(14));
        assertThat(actual.getValuesRows().get(1).getItems().iterator().next(), sameInstance(secondValue));
    }
}
