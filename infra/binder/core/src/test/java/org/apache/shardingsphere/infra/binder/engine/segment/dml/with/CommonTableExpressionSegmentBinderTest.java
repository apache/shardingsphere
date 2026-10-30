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

package org.apache.shardingsphere.infra.binder.engine.segment.dml.with;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.extractor.ProjectionIdentifierExtractEngine;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.type.SubqueryTableSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.util.SubqueryTableBindUtils;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.dml.DeleteStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.dml.InsertStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.dml.UpdateStatementBinder;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.ReturningSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.MergeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedConstruction;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({SubqueryTableSegmentBinder.class, SubqueryTableBindUtils.class})
class CommonTableExpressionSegmentBinderTest {
    
    @Test
    void assertBindSelect() {
        ProjectionSegment projection = new ColumnProjectionSegment(new ColumnSegment(27, 36, new IdentifierValue("foo_column")));
        ProjectionsSegment projections = new ProjectionsSegment(20, 36);
        projections.getProjections().add(projection);
        DatabaseType databaseType = mock(DatabaseType.class);
        SelectStatement select = SelectStatement.builder().databaseType(databaseType).projections(projections).build();
        AliasSegment alias = new AliasSegment(5, 11, new IdentifierValue("foo_cte"));
        SelectStatement boundSelect = SelectStatement.builder().databaseType(databaseType).projections(projections).build();
        SubquerySegment boundSubquery = new SubquerySegment(20, 36, boundSelect, "SELECT foo_column");
        SubqueryTableSegment boundTable = new SubqueryTableSegment(0, 50, boundSubquery);
        boundTable.setAlias(alias);
        SQLStatementBinderContext binderContext = new SQLStatementBinderContext(mock(ShardingSphereMetaData.class), "foo_db", new HintValueContext(), select);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        when(SubqueryTableSegmentBinder.bind(any(), eq(binderContext), any(), eq(tableBinderContexts), eq(true))).thenReturn(boundTable);
        when(SubqueryTableBindUtils.createSubqueryProjections(projections.getProjections(), new IdentifierValue(""), databaseType, TableSourceType.TEMPORARY_TABLE))
                .thenReturn(Collections.singleton(projection));
        CommonTableExpressionSegment segment = new CommonTableExpressionSegment(0, 50, alias, new SubquerySegment(20, 36, select, "SELECT foo_column"), null);
        CommonTableExpressionSegment actual = CommonTableExpressionSegmentBinder.bind(segment, binderContext, tableBinderContexts, false);
        assertThat(actual.getStartIndex(), is(0));
        assertThat(actual.getStopIndex(), is(50));
        assertThat(actual.getAliasSegment().orElse(null), sameInstance(alias));
        assertThat(actual.getSubquery(), sameInstance(boundSubquery));
        assertNull(actual.getDataModification());
        assertThat(binderContext.getCommonTableExpressionsSegmentsUniqueAliases(), contains("foo_cte"));
        Collection<TableSegmentBinderContext> actualContexts = binderContext.getExternalTableBinderContexts().get(CaseInsensitiveString.of("foo_cte"));
        assertThat(actualContexts.size(), is(1));
        TableSegmentBinderContext actualTableBinderContext = actualContexts.iterator().next();
        assertThat(actualTableBinderContext.getTableSourceType(), is(TableSourceType.TEMPORARY_TABLE));
        assertThat(actualTableBinderContext.getProjectionSegments(), contains(projection));
    }
    
    private static Stream<Arguments> getDataModificationArguments() {
        DatabaseType postgresql = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
        ProjectionsSegment projections = new ProjectionsSegment(31, 40);
        projections.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(31, 40, new IdentifierValue("foo_column"))));
        ReturningSegment returning = mock(ReturningSegment.class);
        when(returning.getProjections()).thenReturn(projections);
        ShorthandProjectionSegment shorthand = mock(ShorthandProjectionSegment.class);
        when(shorthand.getActualProjectionSegments()).thenReturn(projections.getProjections());
        ProjectionsSegment shorthandProjections = new ProjectionsSegment(31, 31);
        shorthandProjections.getProjections().add(shorthand);
        ReturningSegment shorthandReturning = mock(ReturningSegment.class);
        when(shorthandReturning.getProjections()).thenReturn(shorthandProjections);
        return Stream.of(
                Arguments.of("INSERT", InsertStatement.builder().databaseType(postgresql).returning(returning).build(), false, Collections.singletonList("foo_column")),
                Arguments.of("UPDATE", UpdateStatement.builder().databaseType(postgresql).returning(returning).build(), true, Collections.singletonList("foo_column")),
                Arguments.of("DELETE", DeleteStatement.builder().databaseType(postgresql).returning(returning).build(), false, Collections.singletonList("foo_column")),
                Arguments.of("DELETE without RETURNING", DeleteStatement.builder().databaseType(postgresql).build(), false, Collections.emptyList()),
                Arguments.of("INSERT with RETURNING shorthand", InsertStatement.builder().databaseType(postgresql).returning(shorthandReturning).build(), false,
                        Collections.singletonList("foo_column")));
    }
    
    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @StaticMockSettings(SubqueryTableBindUtils.class)
    class ReturningOutputLabels {
        
        @ParameterizedTest(name = "{0}")
        @MethodSource("getReturningLabelProjections")
        void assertBindDataModificationOutputLabel(final String name, final ProjectionSegment projection, final String expectedLabel) {
            ProjectionsSegment projections = new ProjectionsSegment(20, 40);
            projections.getProjections().add(projection);
            DeleteStatement body = DeleteStatement.builder().databaseType(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"))
                    .returning(new ReturningSegment(10, 40, projections)).build();
            CommonTableExpressionSegment segment = new CommonTableExpressionSegment(0, 50, new AliasSegment(5, 11, new IdentifierValue("foo_cte")), null, body);
            SQLStatementBinderContext binderContext = new SQLStatementBinderContext(mock(ShardingSphereMetaData.class), "foo_db", new HintValueContext(), body);
            when(SubqueryTableBindUtils.createSubqueryProjections(any(), eq(new IdentifierValue("")), eq(body.getDatabaseType()), eq(TableSourceType.TEMPORARY_TABLE)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            try (
                    MockedConstruction<DeleteStatementBinder> deleteBinder = mockConstruction(DeleteStatementBinder.class,
                            (binder, context) -> when(binder.bind(any(), any())).thenReturn(body));
                    MockedConstruction<ProjectionIdentifierExtractEngine> extractEngine = mockConstruction(ProjectionIdentifierExtractEngine.class, (engine, context) -> {
                        if (projection instanceof ExpressionProjectionSegment) {
                            when(engine.getColumnNameFromExpression((ExpressionProjectionSegment) projection)).thenReturn(expectedLabel);
                        } else if (projection instanceof SubqueryProjectionSegment) {
                            when(engine.getColumnNameFromSubquery((SubqueryProjectionSegment) projection)).thenReturn(expectedLabel);
                        } else if (projection instanceof ColumnProjectionSegment) {
                            when(engine.getIdentifierValue(((ColumnProjectionSegment) projection).getAlias()
                                    .orElseGet(() -> ((ColumnProjectionSegment) projection).getColumn().getIdentifier()))).thenReturn(expectedLabel);
                        }
                    })) {
                CommonTableExpressionSegment actual = CommonTableExpressionSegmentBinder.bind(segment, binderContext, LinkedHashMultimap.create(), false);
                assertThat(actual.getDataModification(), sameInstance(body));
                ColumnProjectionSegment actualProjection = (ColumnProjectionSegment) binderContext.getExternalTableBinderContexts().get(CaseInsensitiveString.of("foo_cte"))
                        .iterator().next().getProjectionSegments().iterator().next();
                assertThat(actualProjection.getColumnLabel(), is(expectedLabel));
                assertThat(actualProjection.getColumn().getIdentifier().getQuoteCharacter(), is(QuoteCharacter.QUOTE));
                assertThat(actualProjection.getColumn().getColumnBoundInfo().getTableSourceType(), is(TableSourceType.TEMPORARY_TABLE));
            }
        }
        
        private Stream<Arguments> getReturningLabelProjections() {
            ExpressionProjectionSegment expression = mock(ExpressionProjectionSegment.class);
            SubqueryProjectionSegment subquery = mock(SubqueryProjectionSegment.class);
            ColumnProjectionSegment aliasedColumn = new ColumnProjectionSegment(new ColumnSegment(20, 25, new IdentifierValue("foo_column")));
            aliasedColumn.setAlias(new AliasSegment(30, 40, new IdentifierValue("foo_alias")));
            ColumnProjectionSegment quotedAlias = new ColumnProjectionSegment(new ColumnSegment(20, 25, new IdentifierValue("foo_column")));
            quotedAlias.setAlias(new AliasSegment(30, 40, new IdentifierValue("Foo_Label", QuoteCharacter.QUOTE)));
            ColumnProjectionSegment unquotedColumn = new ColumnProjectionSegment(new ColumnSegment(20, 25, new IdentifierValue("FOO_COLUMN")));
            ProjectionSegment other = mock(ProjectionSegment.class);
            when(other.getColumnLabel()).thenReturn("foo_projection");
            return Stream.of(Arguments.of("expression label", expression, "foo_expression"), Arguments.of("scalar subquery label", subquery, "foo_subquery"),
                    Arguments.of("explicit alias label", aliasedColumn, "foo_alias"), Arguments.of("quoted mixed-case alias", quotedAlias, "Foo_Label"),
                    Arguments.of("unquoted column label", unquotedColumn, "foo_column"), Arguments.of("projection label", other, "foo_projection"));
        }
        
    }
    
    @Nested
    @StaticMockSettings(SubqueryTableBindUtils.class)
    class DataModificationStatements {
        
        @ParameterizedTest(name = "{0}")
        @MethodSource("org.apache.shardingsphere.infra.binder.engine.segment.dml.with.CommonTableExpressionSegmentBinderTest#getDataModificationArguments")
        void assertBindDataModification(final String name, final DMLStatement body, final boolean recursive, final Collection<String> expectedLabels) {
            CommonTableExpressionSegment segment = new CommonTableExpressionSegment(0, 50, new AliasSegment(5, 11, new IdentifierValue("foo_cte")), null, body);
            SQLStatementBinderContext binderContext = new SQLStatementBinderContext(mock(ShardingSphereMetaData.class), "foo_db", new HintValueContext(), body);
            Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
            when(SubqueryTableBindUtils.createSubqueryProjections(any(), eq(new IdentifierValue("")), eq(body.getDatabaseType()), eq(TableSourceType.TEMPORARY_TABLE)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            try (
                    MockedConstruction<InsertStatementBinder> insertBinder = mockConstruction(InsertStatementBinder.class,
                            (binder, context) -> when(binder.bind(any(), any())).thenReturn((InsertStatement) body));
                    MockedConstruction<UpdateStatementBinder> updateBinder = mockConstruction(UpdateStatementBinder.class,
                            (binder, context) -> when(binder.bind(any(), any())).thenReturn((UpdateStatement) body));
                    MockedConstruction<DeleteStatementBinder> deleteBinder = mockConstruction(DeleteStatementBinder.class,
                            (binder, context) -> when(binder.bind(any(), any())).thenReturn((DeleteStatement) body))) {
                CommonTableExpressionSegment actual = CommonTableExpressionSegmentBinder.bind(segment, binderContext, tableBinderContexts, recursive);
                assertThat(actual.getDataModification(), sameInstance(body));
                assertNull(actual.getSubquery());
                assertNull(segment.getSubquery());
                assertThat(binderContext.getCommonTableExpressionsSegmentsUniqueAliases(), contains("foo_cte"));
                Collection<TableSegmentBinderContext> actualContexts = binderContext.getExternalTableBinderContexts().get(CaseInsensitiveString.of("foo_cte"));
                assertThat(actualContexts.size(), is(1));
                TableSegmentBinderContext actualTableContext = actualContexts.iterator().next();
                assertThat(actualTableContext.getTableSourceType(), is(TableSourceType.TEMPORARY_TABLE));
                Collection<String> actualLabels = actualTableContext.getProjectionSegments().stream().map(ProjectionSegment::getColumnLabel).collect(Collectors.toList());
                assertThat(actualLabels, is(expectedLabels));
                for (ProjectionSegment each : actualTableContext.getProjectionSegments()) {
                    assertThat(((ColumnProjectionSegment) each).getColumn().getColumnBoundInfo().getTableSourceType(), is(TableSourceType.TEMPORARY_TABLE));
                    assertThat(((ColumnProjectionSegment) each).getColumn().getColumnBoundInfo().getOriginalDatabase().getValue(), is(""));
                    assertThat(((ColumnProjectionSegment) each).getColumn().getColumnBoundInfo().getOriginalSchema().getValue(), is(""));
                    assertThat(((ColumnProjectionSegment) each).getColumn().getColumnBoundInfo().getOriginalTable().getValue(), is(""));
                }
            }
        }
        
    }
    
    @Nested
    class UnsupportedDataModification {
        
        @Test
        void assertBindUnsupportedMerge() {
            DMLStatement body = MergeStatement.builder().build();
            CommonTableExpressionSegment segment = new CommonTableExpressionSegment(0, 50, new AliasSegment(5, 11, new IdentifierValue("foo_cte")), null, body);
            SQLStatementBinderContext binderContext = new SQLStatementBinderContext(mock(ShardingSphereMetaData.class), "foo_db", new HintValueContext(), body);
            Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
            UnsupportedSQLOperationException actual = assertThrows(UnsupportedSQLOperationException.class,
                    () -> CommonTableExpressionSegmentBinder.bind(segment, binderContext, tableBinderContexts, true));
            assertThat(actual.getMessage(), is("Unsupported SQL operation: Data-modifying common table expressions."));
            assertTrue(binderContext.getCommonTableExpressionsSegmentsUniqueAliases().isEmpty());
            assertTrue(binderContext.getExternalTableBinderContexts().isEmpty());
            assertTrue(tableBinderContexts.isEmpty());
        }
    }
    
}
