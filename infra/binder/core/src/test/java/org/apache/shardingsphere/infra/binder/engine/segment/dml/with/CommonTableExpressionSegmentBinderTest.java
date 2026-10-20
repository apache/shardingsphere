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
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.type.SubqueryTableSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.util.SubqueryTableBindUtils;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;
import java.util.Collections;
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
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({SubqueryTableSegmentBinder.class, SubqueryTableBindUtils.class})
class CommonTableExpressionSegmentBinderTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDataModificationArguments")
    void assertBindDataModification(final String name, final DMLStatement body, final boolean recursive) {
        CommonTableExpressionSegment segment = new CommonTableExpressionSegment(0, 50, new AliasSegment(5, 11, new IdentifierValue("foo_cte")), null, body);
        SQLStatementBinderContext binderContext = new SQLStatementBinderContext(mock(ShardingSphereMetaData.class), "foo_db", new HintValueContext(), body);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        UnsupportedSQLOperationException actual = assertThrows(UnsupportedSQLOperationException.class,
                () -> CommonTableExpressionSegmentBinder.bind(segment, binderContext, tableBinderContexts, recursive));
        assertThat(actual.getMessage(), is("Unsupported SQL operation: Data-modifying common table expressions."));
        assertTrue(binderContext.getCommonTableExpressionsSegmentsUniqueAliases().isEmpty());
        assertTrue(binderContext.getExternalTableBinderContexts().isEmpty());
        assertTrue(tableBinderContexts.isEmpty());
    }
    
    private static Stream<Arguments> getDataModificationArguments() {
        return Stream.of(
                Arguments.of("INSERT", InsertStatement.builder().build(), false),
                Arguments.of("UPDATE", UpdateStatement.builder().build(), true),
                Arguments.of("DELETE", DeleteStatement.builder().build(), false),
                Arguments.of("MERGE", MergeStatement.builder().build(), true));
    }
    
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
}
