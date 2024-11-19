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

package org.apache.shardingsphere.encrypt.rewrite.util;

import org.apache.shardingsphere.infra.binder.context.segment.insert.values.InsertSelectContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EncryptPredicateSegmentUtilsTest {
    
    @Test
    void assertGetAllSubqueryContextsForSelectStatement() {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        SelectStatementContext subquerySelectStatementContext = mock(SelectStatementContext.class);
        when(subquerySelectStatementContext.getSubqueryContexts()).thenReturn(Collections.emptyMap());
        when(selectStatementContext.getSubqueryContexts()).thenReturn(Collections.singletonMap(0, subquerySelectStatementContext));
        Collection<SelectStatementContext> actual = EncryptPredicateSegmentUtils.getAllSubqueryContexts(selectStatementContext);
        assertThat(actual.size(), is(1));
    }
    
    @Test
    void assertGetAllSubqueryContextsForInsertStatement() {
        InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getSubqueryContexts()).thenReturn(Collections.singletonMap(0, mock(SelectStatementContext.class)));
        InsertSelectContext insertSelectContext = mock(InsertSelectContext.class);
        when(insertSelectContext.getSelectStatementContext()).thenReturn(selectStatementContext);
        when(insertStatementContext.getInsertSelectContext()).thenReturn(insertSelectContext);
        Collection<SelectStatementContext> actual = EncryptPredicateSegmentUtils.getAllSubqueryContexts(insertStatementContext);
        assertThat(actual.size(), is(2));
    }
    
    @Test
    void assertGetWhereSegments() {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        SelectStatementContext subquerySelectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.getWhereSegments()).thenReturn(Collections.singleton(mock(WhereSegment.class)));
        when(subquerySelectStatementContext.getWhereSegments()).thenReturn(Collections.singleton(mock(WhereSegment.class)));
        Collection<WhereSegment> actual = EncryptPredicateSegmentUtils.getWhereSegments(selectStatementContext, Collections.singleton(subquerySelectStatementContext));
        assertThat(actual.size(), is(2));
    }
    
    @Test
    void assertGetColumnSegment() {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        SelectStatementContext subquerySelectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.getColumnSegments()).thenReturn(Collections.singleton(mock(ColumnSegment.class)));
        when(subquerySelectStatementContext.getColumnSegments()).thenReturn(Collections.singleton(mock(ColumnSegment.class)));
        Collection<ColumnSegment> actual = EncryptPredicateSegmentUtils.getColumnSegments(selectStatementContext, Collections.singleton(subquerySelectStatementContext));
        assertThat(actual.size(), is(2));
    }
    
    @Test
    void assertGetJoinConditions() {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        SelectStatementContext subquerySelectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.getJoinConditions()).thenReturn(Collections.singleton(mock(BinaryOperationExpression.class)));
        when(subquerySelectStatementContext.getJoinConditions()).thenReturn(Collections.singleton(mock(BinaryOperationExpression.class)));
        Collection<BinaryOperationExpression> actual = EncryptPredicateSegmentUtils.getJoinConditions(selectStatementContext, Collections.singleton(subquerySelectStatementContext));
        assertThat(actual.size(), is(2));
    }
}
