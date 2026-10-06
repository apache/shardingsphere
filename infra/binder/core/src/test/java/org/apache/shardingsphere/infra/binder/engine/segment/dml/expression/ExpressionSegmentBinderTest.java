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
import org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.type.ExistsSubqueryExpressionBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.type.SubquerySegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExistsSubqueryExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ExistsSubqueryExpressionBinder.class, SubquerySegmentBinder.class})
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
}
