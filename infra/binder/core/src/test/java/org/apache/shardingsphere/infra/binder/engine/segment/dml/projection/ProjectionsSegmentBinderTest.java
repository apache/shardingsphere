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
import org.apache.shardingsphere.infra.binder.engine.segment.dml.projection.type.ColumnProjectionSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.projection.type.ShorthandProjectionSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.util.SubqueryTableBindUtils;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.exception.kernel.metadata.ColumnNotFoundException;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ColumnProjectionSegmentBinder.class, ShorthandProjectionSegmentBinder.class, SubqueryTableBindUtils.class})
class ProjectionsSegmentBinderTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getShorthandScopes")
    void assertBindShorthandScope(final String name, final boolean qualified, final boolean local) {
        ShorthandProjectionSegment projection = new ShorthandProjectionSegment(0, 8);
        if (qualified) {
            projection.setOwner(new OwnerSegment(0, 6, new IdentifierValue("foo_row")));
        }
        ProjectionsSegment projections = new ProjectionsSegment(0, 8);
        projections.getProjections().add(projection);
        SQLStatementBinderContext binderContext = mock(SQLStatementBinderContext.class);
        when(binderContext.getSqlStatement()).thenReturn(SelectStatement.builder().build());
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> localContexts = LinkedHashMultimap.create();
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerContexts = LinkedHashMultimap.create();
        outerContexts.put(CaseInsensitiveString.of("foo_row"), mock(TableSegmentBinderContext.class));
        if (local) {
            localContexts.put(CaseInsensitiveString.of("foo_row"), mock(TableSegmentBinderContext.class));
        }
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> expectedContexts = qualified && !local ? outerContexts : localContexts;
        ShorthandProjectionSegment expectedProjection = new ShorthandProjectionSegment(0, 8);
        when(ShorthandProjectionSegmentBinder.bind(projection, null, expectedContexts)).thenReturn(expectedProjection);
        ProjectionsSegment actual = ProjectionsSegmentBinder.bind(projections, binderContext, null, localContexts, outerContexts);
        assertThat(actual.getProjections().iterator().next(), sameInstance(expectedProjection));
    }
    
    private static Stream<Arguments> getShorthandScopes() {
        return Stream.of(Arguments.of("outer qualified row", true, false), Arguments.of("local row shadows outer", true, true), Arguments.of("unqualified local row", false, false));
    }
    
    @Test
    void assertBindOuterWholeRow() {
        ColumnProjectionSegment projection = new ColumnProjectionSegment(new ColumnSegment(0, 6, new IdentifierValue("foo_row")));
        ProjectionsSegment projections = new ProjectionsSegment(0, 6);
        projections.getProjections().add(projection);
        SQLStatementBinderContext binderContext = mock(SQLStatementBinderContext.class);
        when(binderContext.getSqlStatement()).thenReturn(SelectStatement.builder().databaseType(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL")).build());
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> localContexts = LinkedHashMultimap.create();
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerContexts = LinkedHashMultimap.create();
        outerContexts.put(CaseInsensitiveString.of("foo_row"), mock(TableSegmentBinderContext.class));
        when(ColumnProjectionSegmentBinder.bind(projection, binderContext, localContexts, outerContexts)).thenThrow(new ColumnNotFoundException("foo_row", "field list"));
        ProjectionsSegment actual = ProjectionsSegmentBinder.bind(projections, binderContext, null, localContexts, outerContexts);
        assertThat(((ExpressionProjectionSegment) actual.getProjections().iterator().next()).getText(), is("foo_row"));
    }
    
    @Test
    void assertBindNativeDefaultColumnLabel() {
        ExpressionProjectionSegment projection = new ExpressionProjectionSegment(7, 8, "42", new LiteralExpressionSegment(7, 8, 42));
        projection.setDefaultColumnLabel(new IdentifierValue("column1"));
        ProjectionsSegment projections = new ProjectionsSegment(7, 8);
        projections.getProjections().add(projection);
        SQLStatementBinderContext context = mock(SQLStatementBinderContext.class);
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
        when(context.getSqlStatement()).thenReturn(SelectStatement.builder().databaseType(databaseType).projections(projections).build());
        ProjectionsSegment actual = ProjectionsSegmentBinder.bind(projections, context, null, LinkedHashMultimap.create(), LinkedHashMultimap.create());
        ExpressionProjectionSegment actualProjection = (ExpressionProjectionSegment) actual.getProjections().iterator().next();
        assertThat(actualProjection.getDefaultColumnLabel().getValue(), is("column1"));
        assertFalse(actualProjection.getAliasSegment().isPresent());
        assertThat(actualProjection.getStartIndex(), is(7));
        assertThat(actualProjection.getStopIndex(), is(8));
    }
}
