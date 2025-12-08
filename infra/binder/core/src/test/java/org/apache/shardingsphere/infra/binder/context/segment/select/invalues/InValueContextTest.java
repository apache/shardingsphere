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

package org.apache.shardingsphere.infra.binder.context.segment.select.invalues;

import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InValueContextTest {
    
    @Test
    void assertConstructorWithParameterMarkers() {
        InExpression inExpression = createInExpressionWithParameterMarkers(3);
        List<Object> params = Arrays.asList(100, 101, 102);
        InValueContext context = new InValueContext(inExpression, params, 0);
        assertThat(context.getParameterCount(), is(3));
        assertThat(context.getParametersOffset(), is(0));
        assertThat(context.getParameters(), is(params));
        assertThat(context.getValueExpressions().size(), is(3));
    }
    
    @Test
    void assertConstructorWithParameterOffset() {
        InExpression inExpression = createInExpressionWithParameterMarkers(2);
        List<Object> params = Arrays.asList("before", 100, 101, "after");
        InValueContext context = new InValueContext(inExpression, params, 1);
        assertThat(context.getParameterCount(), is(2));
        assertThat(context.getParametersOffset(), is(1));
        assertThat(context.getParameters(), is(Arrays.asList(100, 101)));
    }
    
    @Test
    void assertConstructorWithLiteralValues() {
        InExpression inExpression = createInExpressionWithLiterals(100, 101, 102);
        InValueContext context = new InValueContext(inExpression, Collections.emptyList(), 0);
        assertThat(context.getParameterCount(), is(0));
        assertThat(context.getValueExpressions().size(), is(3));
        assertTrue(context.getParameters().isEmpty());
    }
    
    @Test
    void assertConstructorWithEmptyParams() {
        InExpression inExpression = createInExpressionWithParameterMarkers(2);
        InValueContext context = new InValueContext(inExpression, Collections.emptyList(), 0);
        assertThat(context.getParameterCount(), is(2));
        assertTrue(context.getParameters().isEmpty());
        assertTrue(context.getGroupedParameters().isEmpty());
    }
    
    @Test
    void assertConstructorWithNullParams() {
        InExpression inExpression = createInExpressionWithParameterMarkers(2);
        InValueContext context = new InValueContext(inExpression, null, 0);
        assertThat(context.getParameterCount(), is(2));
        assertTrue(context.getParameters().isEmpty());
    }
    
    @Test
    void assertGetGroupedParametersWithParameterMarkers() {
        InExpression inExpression = createInExpressionWithParameterMarkers(3);
        List<Object> params = Arrays.asList(100, 101, 102);
        InValueContext context = new InValueContext(inExpression, params, 0);
        List<List<Object>> grouped = context.getGroupedParameters();
        assertThat(grouped.size(), is(3));
        assertThat(grouped.get(0), is(Collections.singletonList(100)));
        assertThat(grouped.get(1), is(Collections.singletonList(101)));
        assertThat(grouped.get(2), is(Collections.singletonList(102)));
    }
    
    @Test
    void assertGetGroupedParametersWithLiteralValues() {
        InExpression inExpression = createInExpressionWithLiterals(100, 101, 102);
        InValueContext context = new InValueContext(inExpression, Collections.emptyList(), 0);
        List<List<Object>> grouped = context.getGroupedParameters();
        assertTrue(grouped.isEmpty());
    }
    
    @Test
    void assertGetGroupedParametersWithMixedValues() {
        ListExpression listExpression = new ListExpression(0, 50);
        listExpression.getItems().add(new ParameterMarkerExpressionSegment(0, 1, 0));
        listExpression.getItems().add(new LiteralExpressionSegment(5, 8, 200));
        listExpression.getItems().add(new ParameterMarkerExpressionSegment(10, 11, 1));
        InExpression inExpression = new InExpression(0, 50,
                new ColumnSegment(0, 10, new IdentifierValue("user_id")), listExpression, false);
        List<Object> params = Arrays.asList(100, 300);
        InValueContext context = new InValueContext(inExpression, params, 0);
        List<List<Object>> grouped = context.getGroupedParameters();
        assertThat(grouped.size(), is(2));
        assertThat(grouped.get(0), is(Collections.singletonList(100)));
        assertThat(grouped.get(1), is(Collections.singletonList(300)));
    }
    
    @Test
    void assertGetGroupedParametersWithEmptyResult() {
        InExpression inExpression = createInExpressionWithNonListRight();
        InValueContext context = new InValueContext(inExpression, Collections.emptyList(), 0);
        assertTrue(context.getGroupedParameters().isEmpty());
    }
    
    @Test
    void assertGetParametersWithZeroParameterCount() {
        InExpression inExpression = createInExpressionWithLiterals(100, 101);
        List<Object> params = Arrays.asList("some", "params");
        InValueContext context = new InValueContext(inExpression, params, 0);
        assertTrue(context.getParameters().isEmpty());
    }
    
    @Test
    void assertGetGroupedParametersWithOtherExpressionType() {
        ListExpression listExpression = new ListExpression(0, 50);
        listExpression.getItems().add(new ParameterMarkerExpressionSegment(0, 1, 0));
        listExpression.getItems().add(new ColumnSegment(5, 15, new IdentifierValue("col")));
        InExpression inExpression = new InExpression(0, 50,
                new ColumnSegment(0, 10, new IdentifierValue("user_id")), listExpression, false);
        List<Object> params = Collections.singletonList(100);
        InValueContext context = new InValueContext(inExpression, params, 0);
        List<List<Object>> grouped = context.getGroupedParameters();
        assertThat(grouped.size(), is(1));
        assertThat(grouped.get(0), is(Collections.singletonList(100)));
    }
    
    private InExpression createInExpressionWithParameterMarkers(final int count) {
        ListExpression listExpression = new ListExpression(0, 50);
        for (int i = 0; i < count; i++) {
            listExpression.getItems().add(new ParameterMarkerExpressionSegment(i * 3, i * 3 + 1, i));
        }
        return new InExpression(0, 50, new ColumnSegment(0, 10, new IdentifierValue("user_id")), listExpression, false);
    }
    
    private InExpression createInExpressionWithLiterals(final Object... values) {
        ListExpression listExpression = new ListExpression(0, 50);
        int pos = 0;
        for (Object value : values) {
            listExpression.getItems().add(new LiteralExpressionSegment(pos, pos + 3, value));
            pos += 5;
        }
        return new InExpression(0, 50, new ColumnSegment(0, 10, new IdentifierValue("user_id")), listExpression, false);
    }
    
    private InExpression createInExpressionWithNonListRight() {
        return new InExpression(0, 50,
                new ColumnSegment(0, 10, new IdentifierValue("user_id")),
                new ColumnSegment(20, 30, new IdentifierValue("other_col")), false);
    }
}
