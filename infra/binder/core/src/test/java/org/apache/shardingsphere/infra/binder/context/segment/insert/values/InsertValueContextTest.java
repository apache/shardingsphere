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

package org.apache.shardingsphere.infra.binder.context.segment.insert.values;

import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InsertValueContextTest {
    
    @Test
    void assertInstanceConstructedOk() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        InsertValueContext insertValueContext = new InsertValueContext(Collections.emptyList(), Collections.emptyList(), 0);
        assertThat(insertValueContext.getValueExpressions(), is(
                Plugins.getMemberAccessor().invoke(InsertValueContext.class.getDeclaredMethod("getValueExpressions", Collection.class), insertValueContext, Collections.emptyList())));
        assertThat(insertValueContext.getParameters(), is(
                Plugins.getMemberAccessor().invoke(InsertValueContext.class.getDeclaredMethod("getParameters", List.class, int.class), insertValueContext, Collections.emptyList(), 0)));
    }
    
    @Test
    void assertGetLiteralValueWithParameterMarker() {
        Collection<ExpressionSegment> assignments = makeParameterMarkerExpressionSegment();
        String parameterValue = "test";
        InsertValueContext insertValueContext = new InsertValueContext(assignments, Collections.singletonList(parameterValue), 0);
        Optional<Object> valueFromInsertValueContext = insertValueContext.getLiteralValue(0);
        assertTrue(valueFromInsertValueContext.isPresent());
        assertThat(valueFromInsertValueContext.get(), is(parameterValue));
    }
    
    @Test
    void assertGetLiteralValueWhenParameterIsNull() {
        Collection<ExpressionSegment> assignments = makeParameterMarkerExpressionSegment();
        int parametersOffset = 0;
        InsertValueContext insertValueContext = new InsertValueContext(assignments, Collections.singletonList(null), parametersOffset);
        Optional<Object> literalValue = insertValueContext.getLiteralValue(0);
        assertThat(false, is(literalValue.isPresent()));
    }
    
    private Collection<ExpressionSegment> makeParameterMarkerExpressionSegment() {
        return Collections.singleton(new ParameterMarkerExpressionSegment(0, 10, 5));
    }
    
    @Test
    void assertGetLiteralValueWhenLiteralExpressionSegment() {
        Object literalObject = new Object();
        Collection<ExpressionSegment> assignments = makeLiteralExpressionSegment(literalObject);
        InsertValueContext insertValueContext = new InsertValueContext(assignments, Collections.emptyList(), 0);
        Optional<Object> valueFromInsertValueContext = insertValueContext.getLiteralValue(0);
        assertTrue(valueFromInsertValueContext.isPresent());
        assertThat(valueFromInsertValueContext.get(), is(literalObject));
    }
    
    private Collection<ExpressionSegment> makeLiteralExpressionSegment(final Object literalObject) {
        return Collections.singleton(new LiteralExpressionSegment(0, 10, literalObject));
    }
    
    @Test
    void assertGetParameterCount() {
        Collection<ExpressionSegment> expressions = Arrays.asList(
                new LiteralExpressionSegment(0, 10, null),
                new ExpressionProjectionSegment(0, 10, ""),
                new ParameterMarkerExpressionSegment(0, 10, 5),
                new BinaryOperationExpression(0, 0, new ColumnSegment(0, 0, new IdentifierValue("")), new ParameterMarkerExpressionSegment(0, 10, 5), "=", ""));
        InsertValueContext insertValueContext = new InsertValueContext(expressions, Arrays.asList("", ""), 0);
        assertThat(insertValueContext.getParameterCount(), is(2));
    }
}
