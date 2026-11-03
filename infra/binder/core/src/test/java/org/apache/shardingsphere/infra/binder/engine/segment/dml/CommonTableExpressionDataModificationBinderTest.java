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
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.type.SimpleTableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.binder.engine.type.DMLStatementBindEngine;
import org.apache.shardingsphere.infra.exception.kernel.syntax.DifferenceInColumnCountOfSelectListAndColumnNameListException;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.ReturningSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedConstruction;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

class CommonTableExpressionDataModificationBinderTest {
    
    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"INSERT", "UPDATE", "DELETE"})
    void assertBindReturnedRelation(final String kind) {
        DMLStatement body = createBody(kind, createReturning());
        CommonTableExpressionSegment segment = createSegment(body);
        SQLStatementBinderContext parent = createContext();
        TableSegmentBinderContext preceding = mock(TableSegmentBinderContext.class);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tables = LinkedHashMultimap.create();
        tables.put(CaseInsensitiveString.of("preceding"), preceding);
        AtomicReference<SQLStatementBinderContext> child = new AtomicReference<>();
        try (
                MockedConstruction<DMLStatementBindEngine> ignored = mockConstruction(DMLStatementBindEngine.class,
                        (engine, construction) -> when(engine.bind(any(DMLStatement.class), any(SQLStatementBinderContext.class))).thenAnswer(invocation -> {
                            child.set(invocation.getArgument(1));
                            return body;
                        }))) {
            CommonTableExpressionSegment actual = CommonTableExpressionDataModificationBinder.bind(segment, parent, tables);
            assertThat(actual.getDataModification(), sameInstance(body));
            assertNull(actual.getSubquery());
            assertThat(child.get().getSqlStatement(), sameInstance(body));
            assertThat(child.get().getCurrentDatabaseName(), is("foo_db"));
            assertThat(child.get().getMetaData(), sameInstance(parent.getMetaData()));
            assertTrue(child.get().getExternalTableBinderContexts().containsEntry(CaseInsensitiveString.of("preceding"), preceding));
            SimpleTableSegmentBinderContext actualOutput = (SimpleTableSegmentBinderContext) tables.get(CaseInsensitiveString.of("foo_cte")).iterator().next();
            assertTrue(actualOutput.isFromWithSegment());
            assertThat(parent.getExternalTableBinderContexts().get(CaseInsensitiveString.of("foo_cte")).iterator().next(), sameInstance(actualOutput));
            ColumnSegment actualColumn = ((ColumnProjectionSegment) actualOutput.findProjectionSegmentByColumnLabel("returned_value").get()).getColumn();
            assertThat(actualColumn.getColumnBoundInfo().getOriginalTable().getValue(), is(""));
            assertThat(actualColumn.getColumnBoundInfo().getTableSourceType(), is(TableSourceType.TEMPORARY_TABLE));
        }
    }
    
    @Test
    void assertBindExplicitOutputColumn() {
        DMLStatement body = createBody("UPDATE", createReturning());
        CommonTableExpressionSegment segment = createSegment(body);
        segment.getColumns().add(new ColumnSegment(12, 18, new IdentifierValue("renamed")));
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tables = LinkedHashMultimap.create();
        try (MockedConstruction<DMLStatementBindEngine> ignored = bindBody(body)) {
            CommonTableExpressionSegment actual = CommonTableExpressionDataModificationBinder.bind(segment, createContext(), tables);
            assertThat(actual.getColumns().iterator().next().getIdentifier().getValue(), is("renamed"));
            TableSegmentBinderContext actualOutput = tables.get(CaseInsensitiveString.of("foo_cte")).iterator().next();
            assertTrue(actualOutput.findProjectionSegmentByColumnLabel("renamed").isPresent());
            assertFalse(actualOutput.findProjectionSegmentByColumnLabel("returned_value").isPresent());
        }
    }
    
    @Test
    void assertRejectMismatchedOutputColumns() {
        DMLStatement body = createBody("DELETE", createReturning());
        CommonTableExpressionSegment segment = createSegment(body);
        segment.getColumns().add(new ColumnSegment(12, 13, new IdentifierValue("first")));
        segment.getColumns().add(new ColumnSegment(15, 16, new IdentifierValue("second")));
        try (MockedConstruction<DMLStatementBindEngine> ignored = bindBody(body)) {
            assertThrows(DifferenceInColumnCountOfSelectListAndColumnNameListException.class,
                    () -> CommonTableExpressionDataModificationBinder.bind(segment, createContext(), LinkedHashMultimap.create()));
        }
    }
    
    @Test
    void assertBindWriteWithoutReturning() {
        DMLStatement body = createBody("INSERT", null);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tables = LinkedHashMultimap.create();
        try (MockedConstruction<DMLStatementBindEngine> ignored = bindBody(body)) {
            CommonTableExpressionSegment actual = CommonTableExpressionDataModificationBinder.bind(createSegment(body), createContext(), tables);
            assertThat(actual.getDataModification(), sameInstance(body));
            assertTrue(tables.get(CaseInsensitiveString.of("foo_cte")).iterator().next().getProjectionSegments().isEmpty());
        }
    }
    
    private MockedConstruction<DMLStatementBindEngine> bindBody(final DMLStatement body) {
        return mockConstruction(DMLStatementBindEngine.class, (engine, construction) -> when(engine.bind(any(DMLStatement.class), any(SQLStatementBinderContext.class))).thenReturn(body));
    }
    
    private SQLStatementBinderContext createContext() {
        return new SQLStatementBinderContext(mock(ShardingSphereMetaData.class), "foo_db", new HintValueContext(), SelectStatement.builder().build(),
                Collections.singletonList(new IdentifierValue("foo_schema")));
    }
    
    private CommonTableExpressionSegment createSegment(final DMLStatement body) {
        return new CommonTableExpressionSegment(0, 80, new AliasSegment(5, 11, new IdentifierValue("foo_cte")), null, body);
    }
    
    private DMLStatement createBody(final String kind, final ReturningSegment returning) {
        if ("INSERT".equals(kind)) {
            return InsertStatement.builder().returning(returning).build();
        }
        if ("UPDATE".equals(kind)) {
            return UpdateStatement.builder().returning(returning).build();
        }
        return DeleteStatement.builder().returning(returning).build();
    }
    
    private ReturningSegment createReturning() {
        ColumnSegment source = new ColumnSegment(30, 35, new IdentifierValue("foo_column"));
        source.setColumnBoundInfo(new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")),
                new IdentifierValue("foo_table"), new IdentifierValue("foo_column"), TableSourceType.PHYSICAL_TABLE));
        ColumnProjectionSegment projection = new ColumnProjectionSegment(source);
        projection.setAlias(new AliasSegment(40, 53, new IdentifierValue("returned_value")));
        ShorthandProjectionSegment wildcard = new ShorthandProjectionSegment(29, 29);
        wildcard.getActualProjectionSegments().add(projection);
        ProjectionsSegment projections = new ProjectionsSegment(29, 53);
        projections.getProjections().add(wildcard);
        return new ReturningSegment(19, 53, projections);
    }
}
