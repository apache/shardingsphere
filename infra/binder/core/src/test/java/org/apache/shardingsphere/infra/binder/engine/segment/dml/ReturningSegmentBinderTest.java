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

package org.apache.shardingsphere.infra.binder.engine.segment.dml;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.projection.ProjectionsSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.ReturningSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.RowExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.DeleteMultiTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;

import java.util.Collections;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProjectionsSegmentBinder.class)
class ReturningSegmentBinderTest {
    
    @Captor
    private ArgumentCaptor<Multimap<CaseInsensitiveString, TableSegmentBinderContext>> tableContextsCaptor;
    
    @Test
    void assertBindPreservesMetadata() {
        ProjectionsSegment projections = new ProjectionsSegment(10, 20);
        ProjectionsSegment bound = new ProjectionsSegment(10, 20);
        ReturningSegment segment = new ReturningSegment(0, 30, projections);
        AliasSegment oldAlias = new AliasSegment(1, 4, new IdentifierValue("old_row"));
        AliasSegment newAlias = new AliasSegment(5, 8, new IdentifierValue("new_row"));
        segment.setOldAlias(oldAlias);
        segment.setNewAlias(newAlias);
        SQLStatementBinderContext context = mock(SQLStatementBinderContext.class);
        SimpleTableSegment table = mock(SimpleTableSegment.class);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tables = LinkedHashMultimap.create();
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> outer = LinkedHashMultimap.create();
        when(ProjectionsSegmentBinder.bind(projections, context, table, tables, outer)).thenReturn(bound);
        ReturningSegment actual = ReturningSegmentBinder.bind(segment, context, table, tables, outer);
        assertThat(actual.getProjections(), sameInstance(bound));
        assertThat(actual.getStartIndex(), is(0));
        assertThat(actual.getStopIndex(), is(30));
        assertThat(actual.getOldAlias(), sameInstance(oldAlias));
        assertThat(actual.getNewAlias(), sameInstance(newAlias));
        assertThat(segment.getProjections(), sameInstance(projections));
    }
    
    @ParameterizedTest(name = "owner={0}, parenthesized={1}")
    @CsvSource({"OLD,false", "NEW,false", "foo_old,false", "OLD,true", "NEW,true", "foo_old,true"})
    void assertBindRowAlias(final String owner, final boolean parenthesized) {
        ProjectionsSegment projections = new ProjectionsSegment(10, 20);
        if (parenthesized) {
            projections.getProjections().add(createRowExpansion(owner));
        } else {
            ShorthandProjectionSegment shorthand = new ShorthandProjectionSegment(10, 20);
            shorthand.setOwner(new OwnerSegment(10, 16, new IdentifierValue(owner)));
            projections.getProjections().add(shorthand);
        }
        ReturningSegment segment = new ReturningSegment(0, 30, projections);
        if ("foo_old".equals(owner)) {
            segment.setOldAlias(new AliasSegment(1, 7, new IdentifierValue(owner)));
        }
        SimpleTableSegment table = new SimpleTableSegment(new TableNameSegment(0, 8, new IdentifierValue("foo_table")));
        table.setAlias(new AliasSegment(10, 12, new IdentifierValue("foo_alias")));
        SQLStatementBinderContext context = mock(SQLStatementBinderContext.class);
        TableSegmentBinderContext targetContext = mock(TableSegmentBinderContext.class);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tables = LinkedHashMultimap.create();
        tables.put(CaseInsensitiveString.of("foo_alias"), targetContext);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> outer = LinkedHashMultimap.create();
        ProjectionsSegment expected = new ProjectionsSegment(10, 20);
        when(ProjectionsSegmentBinder.bind(eq(projections), eq(context), eq(table), tableContextsCaptor.capture(), eq(outer))).thenReturn(expected);
        ReturningSegment actual = ReturningSegmentBinder.bind(segment, context, table, tables, outer);
        assertThat(actual.getProjections(), sameInstance(expected));
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> actualContexts = tableContextsCaptor.getValue();
        assertThat(actualContexts, not(sameInstance(tables)));
        assertTrue(actualContexts.containsEntry(CaseInsensitiveString.of(owner), targetContext));
        assertThat(tables.size(), is(1));
    }
    
    private static ExpressionProjectionSegment createRowExpansion(final String owner) {
        CommonExpressionSegment expression = new CommonExpressionSegment(10, 20, "(" + owner + ").*",
                Collections.singletonList(new ColumnSegment(11, 11 + owner.length() - 1, new IdentifierValue(owner))));
        expression.setRowExpansion(true);
        return new ExpressionProjectionSegment(10, 20, expression.getText(), expression);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("unchangedScopeArguments")
    void assertBindWithoutAdditionalRowScope(final String caseName, final ProjectionSegment projection, final boolean existingOld) {
        ProjectionsSegment projections = new ProjectionsSegment(10, 20);
        projections.getProjections().add(projection);
        ReturningSegment segment = new ReturningSegment(0, 30, projections);
        SimpleTableSegment table = new SimpleTableSegment(new TableNameSegment(0, 8, new IdentifierValue("foo_table")));
        SQLStatementBinderContext context = mock(SQLStatementBinderContext.class);
        TableSegmentBinderContext target = mock(TableSegmentBinderContext.class);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tables = LinkedHashMultimap.create();
        tables.put(CaseInsensitiveString.of("foo_table"), target);
        if (existingOld) {
            tables.put(CaseInsensitiveString.of("OLD"), target);
        }
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> outer = LinkedHashMultimap.create();
        ProjectionsSegment expected = new ProjectionsSegment(10, 20);
        when(ProjectionsSegmentBinder.bind(eq(projections), eq(context), eq(table), tableContextsCaptor.capture(), eq(outer))).thenReturn(expected);
        ReturningSegment actual = ReturningSegmentBinder.bind(segment, context, table, tables, outer);
        assertThat(actual.getProjections(), sameInstance(expected));
        assertThat(tableContextsCaptor.getValue(), sameInstance(tables));
    }
    
    private static Stream<Arguments> unchangedScopeArguments() {
        ExpressionProjectionSegment qualified = createRowExpansion("OLD");
        ColumnSegment child = (ColumnSegment) ((CommonExpressionSegment) qualified.getExpr()).getChildren().iterator().next();
        child.setOwner(new OwnerSegment(11, 13, new IdentifierValue("foo_table")));
        ExpressionProjectionSegment ordinary = createRowExpansion("OLD");
        ((CommonExpressionSegment) ordinary.getExpr()).setRowExpansion(false);
        RowExpression row = new RowExpression(11, 18, "ROW(1,2)");
        row.getItems().add(new LiteralExpressionSegment(15, 15, 1));
        row.getItems().add(new LiteralExpressionSegment(17, 17, 2));
        CommonExpressionSegment expansion = new CommonExpressionSegment(10, 20, "(ROW(1,2)).*", Collections.singletonList(row));
        expansion.setRowExpansion(true);
        ShorthandProjectionSegment existingOld = new ShorthandProjectionSegment(10, 20);
        existingOld.setOwner(new OwnerSegment(10, 16, new IdentifierValue("OLD")));
        ShorthandProjectionSegment unrelated = new ShorthandProjectionSegment(10, 20);
        unrelated.setOwner(new OwnerSegment(10, 16, new IdentifierValue("foo_other")));
        return Stream.of(
                Arguments.of("maintained existing OLD shorthand scope", existingOld, true),
                Arguments.of("maintained unrelated shorthand owner", unrelated, false),
                Arguments.of("column projection", new ColumnProjectionSegment(new ColumnSegment(10, 13, new IdentifierValue("foo_column"))), false),
                Arguments.of("unowned star", new ShorthandProjectionSegment(10, 10), false),
                Arguments.of("row constructor expansion", new ExpressionProjectionSegment(10, 20, expansion.getText(), expansion), false),
                Arguments.of("existing OLD scope", createRowExpansion("OLD"), true),
                Arguments.of("unrelated owner", createRowExpansion("foo_other"), false),
                Arguments.of("qualified composite column", qualified, false),
                Arguments.of("ordinary parenthesized expression", ordinary, false),
                Arguments.of("non common expression", new ExpressionProjectionSegment(10, 20, "1", new LiteralExpressionSegment(10, 10, 1)), false));
    }
    
    @Test
    void assertBindDeleteRelationScope() {
        ProjectionsSegment projections = new ProjectionsSegment(10, 20);
        ProjectionsSegment bound = new ProjectionsSegment(10, 20);
        ReturningSegment segment = new ReturningSegment(0, 30, projections);
        SimpleTableSegment target = mock(SimpleTableSegment.class);
        DeleteMultiTableSegment table = new DeleteMultiTableSegment();
        table.getActualDeleteTables().add(target);
        JoinTableSegment relation = mock(JoinTableSegment.class);
        table.setRelationTable(relation);
        SQLStatementBinderContext context = mock(SQLStatementBinderContext.class);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tables = LinkedHashMultimap.create();
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> outer = LinkedHashMultimap.create();
        when(ProjectionsSegmentBinder.bind(projections, context, relation, tables, outer)).thenReturn(bound);
        ReturningSegment actual = ReturningSegmentBinder.bind(segment, context, table, tables, outer);
        assertThat(actual.getProjections(), sameInstance(bound));
    }
}
