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

package org.apache.shardingsphere.infra.binder.engine.segment.dml.expression;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.apache.shardingsphere.infra.binder.engine.segment.SegmentType;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.type.ColumnSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.type.ExistsSubqueryExpressionBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.type.SubquerySegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExistsSubqueryExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ExistsSubqueryExpressionBinder.class, SubquerySegmentBinder.class, ColumnSegmentBinder.class})
class ExpressionSegmentBinderTest {
    
    @Test
    void assertBindExistsSubqueryWithShadowedOuterTableContext() {
        SubquerySegment subquerySegment = new SubquerySegment(0, 5, mock(SelectStatement.class), "SELECT 1");
        ExistsSubqueryExpression boundSegment = new ExistsSubqueryExpression(0, 10, subquerySegment);
        TableSegmentBinderContext outerTableBinderContext = mock(TableSegmentBinderContext.class);
        TableSegmentBinderContext retainedOuterTableBinderContext = mock(TableSegmentBinderContext.class);
        TableSegmentBinderContext currentTableBinderContext = mock(TableSegmentBinderContext.class);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts = LinkedHashMultimap.create();
        outerTableBinderContexts.put(CaseInsensitiveString.of("t"), outerTableBinderContext);
        outerTableBinderContexts.put(CaseInsensitiveString.of("u"), retainedOuterTableBinderContext);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        tableBinderContexts.put(CaseInsensitiveString.of("T"), currentTableBinderContext);
        AtomicReference<Multimap<CaseInsensitiveString, TableSegmentBinderContext>> mergedContexts = new AtomicReference<>();
        when(ExistsSubqueryExpressionBinder.bind(any(), any(), any())).thenAnswer(invocation -> {
            mergedContexts.set(invocation.getArgument(2));
            return boundSegment;
        });
        ExistsSubqueryExpression segment = new ExistsSubqueryExpression(0, 10, subquerySegment);
        SQLStatementBinderContext binderContext = mock(SQLStatementBinderContext.class);
        assertThat(ExpressionSegmentBinder.bind(segment, SegmentType.PREDICATE, binderContext, tableBinderContexts, outerTableBinderContexts), is(boundSegment));
        assertThat(mergedContexts.get().get(CaseInsensitiveString.of("t")), hasItem(currentTableBinderContext));
        assertThat(mergedContexts.get().get(CaseInsensitiveString.of("u")), contains(retainedOuterTableBinderContext));
    }
    
    @Test
    void assertBindSubqueryWithShadowedOuterTableContext() {
        SubquerySegment boundSubquerySegment = new SubquerySegment(0, 5, mock(SelectStatement.class), "SELECT 1");
        TableSegmentBinderContext outerTableBinderContext = mock(TableSegmentBinderContext.class);
        TableSegmentBinderContext retainedOuterTableBinderContext = mock(TableSegmentBinderContext.class);
        TableSegmentBinderContext currentTableBinderContext = mock(TableSegmentBinderContext.class);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts = LinkedHashMultimap.create();
        outerTableBinderContexts.put(CaseInsensitiveString.of("t"), outerTableBinderContext);
        outerTableBinderContexts.put(CaseInsensitiveString.of("u"), retainedOuterTableBinderContext);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        tableBinderContexts.put(CaseInsensitiveString.of("T"), currentTableBinderContext);
        AtomicReference<Multimap<CaseInsensitiveString, TableSegmentBinderContext>> mergedContexts = new AtomicReference<>();
        when(SubquerySegmentBinder.bind(any(), any(), any())).thenAnswer(invocation -> {
            mergedContexts.set(invocation.getArgument(2));
            return boundSubquerySegment;
        });
        SubquerySegment subquerySegment = new SubquerySegment(0, 5, mock(SelectStatement.class), "SELECT 1");
        SubqueryExpressionSegment segment = new SubqueryExpressionSegment(subquerySegment);
        SQLStatementBinderContext binderContext = mock(SQLStatementBinderContext.class);
        SubqueryExpressionSegment actual = (SubqueryExpressionSegment) ExpressionSegmentBinder.bind(
                segment, SegmentType.PREDICATE, binderContext, tableBinderContexts, outerTableBinderContexts);
        assertThat(actual.getSubquery(), is(boundSubquerySegment));
        assertThat(mergedContexts.get().get(CaseInsensitiveString.of("t")), hasItem(currentTableBinderContext));
        assertThat(mergedContexts.get().get(CaseInsensitiveString.of("u")), contains(retainedOuterTableBinderContext));
    }
    
    @Test
    void assertBindCommonExpression() {
        ColumnSegment column = new ColumnSegment(0, 9, new IdentifierValue("foo_column"));
        ColumnSegment boundColumn = new ColumnSegment(0, 9, new IdentifierValue("foo_column"));
        SubquerySegment subquery = new SubquerySegment(13, 22, mock(SelectStatement.class), "(SELECT 1)");
        SubquerySegment boundSubquery = new SubquerySegment(13, 22, mock(SelectStatement.class), "(SELECT 1)");
        SubqueryExpressionSegment subqueryExpression = new SubqueryExpressionSegment(subquery);
        ParameterMarkerExpressionSegment parameterMarker = new ParameterMarkerExpressionSegment(26, 26, 0);
        CommonExpressionSegment segment = new CommonExpressionSegment(0, 26, "foo_column + (SELECT 1) + ?", Arrays.asList(column, subqueryExpression, parameterMarker));
        SQLStatementBinderContext binderContext = mock(SQLStatementBinderContext.class);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts = LinkedHashMultimap.create();
        when(ColumnSegmentBinder.bind(same(column), eq(SegmentType.PREDICATE), same(binderContext), same(tableBinderContexts), same(outerTableBinderContexts))).thenReturn(boundColumn);
        when(SubquerySegmentBinder.bind(same(subquery), same(binderContext), any())).thenReturn(boundSubquery);
        CommonExpressionSegment actual = (CommonExpressionSegment) ExpressionSegmentBinder.bind(segment, SegmentType.PREDICATE, binderContext, tableBinderContexts, outerTableBinderContexts);
        assertThat(actual, not(sameInstance(segment)));
        assertThat(actual.getStartIndex(), is(segment.getStartIndex()));
        assertThat(actual.getStopIndex(), is(segment.getStopIndex()));
        assertThat(actual.getText(), is(segment.getText()));
        Iterator<ExpressionSegment> actualChildren = actual.getChildren().iterator();
        assertThat(actualChildren.next(), sameInstance(boundColumn));
        assertThat(((SubqueryExpressionSegment) actualChildren.next()).getSubquery(), sameInstance(boundSubquery));
        assertThat(actualChildren.next(), sameInstance(parameterMarker));
        assertFalse(actualChildren.hasNext());
        assertThat(segment.getChildren(), contains(column, subqueryExpression, parameterMarker));
        assertThat(subqueryExpression.getSubquery(), sameInstance(subquery));
    }
    
    @Test
    void assertBindNestedCommonExpression() {
        ColumnSegment column = new ColumnSegment(1, 10, new IdentifierValue("foo_column"));
        ColumnSegment boundColumn = new ColumnSegment(1, 10, new IdentifierValue("foo_column"));
        CommonExpressionSegment nestedSegment = new CommonExpressionSegment(1, 10, "foo_column", Collections.singleton(column));
        CommonExpressionSegment segment = new CommonExpressionSegment(0, 11, "(foo_column)", Collections.singleton(nestedSegment));
        SQLStatementBinderContext binderContext = mock(SQLStatementBinderContext.class);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts = LinkedHashMultimap.create();
        when(ColumnSegmentBinder.bind(same(column), eq(SegmentType.PROJECTION), same(binderContext), same(tableBinderContexts), same(outerTableBinderContexts))).thenReturn(boundColumn);
        CommonExpressionSegment actual = (CommonExpressionSegment) ExpressionSegmentBinder.bind(segment, SegmentType.PROJECTION, binderContext, tableBinderContexts, outerTableBinderContexts);
        CommonExpressionSegment actualNestedSegment = (CommonExpressionSegment) actual.getChildren().iterator().next();
        assertThat(actual, not(sameInstance(segment)));
        assertThat(actualNestedSegment, not(sameInstance(nestedSegment)));
        assertThat(actualNestedSegment.getStartIndex(), is(nestedSegment.getStartIndex()));
        assertThat(actualNestedSegment.getStopIndex(), is(nestedSegment.getStopIndex()));
        assertThat(actualNestedSegment.getText(), is(nestedSegment.getText()));
        assertThat(actualNestedSegment.getChildren(), contains(boundColumn));
        assertThat(segment.getChildren(), contains(nestedSegment));
        assertThat(nestedSegment.getChildren(), contains(column));
    }
    
    @Test
    void assertBindEmptyCommonExpression() {
        CommonExpressionSegment segment = new CommonExpressionSegment(0, 9, "foo_clause", Collections.emptyList());
        assertThat(ExpressionSegmentBinder.bind(segment, SegmentType.PROJECTION, mock(SQLStatementBinderContext.class), LinkedHashMultimap.create(), LinkedHashMultimap.create()),
                sameInstance(segment));
    }
}
