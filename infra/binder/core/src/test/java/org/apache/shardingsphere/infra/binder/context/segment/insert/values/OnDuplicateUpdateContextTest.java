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

import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.SimpleExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class OnDuplicateUpdateContextTest {
    
    @Test
    void assertInstanceConstructedOk() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        OnDuplicateUpdateContext onDuplicateUpdateContext = new OnDuplicateUpdateContext(Collections.emptyList(), Collections.emptyList(), 0);
        assertThat(onDuplicateUpdateContext.getValueExpressions(), is(Plugins.getMemberAccessor()
                .invoke(OnDuplicateUpdateContext.class.getDeclaredMethod("getValueExpressions", Collection.class), onDuplicateUpdateContext, Collections.emptyList())));
        assertThat(onDuplicateUpdateContext.getParameters(), is(Plugins.getMemberAccessor()
                .invoke(OnDuplicateUpdateContext.class.getDeclaredMethod("getParameters", List.class, int.class), onDuplicateUpdateContext, Collections.emptyList(), 0)));
    }
    
    @Test
    void assertGetValueWhenParameterMarker() {
        Collection<ColumnAssignmentSegment> assignments = createParameterMarkerExpressionAssignmentSegment();
        String parameterValue1 = "test1";
        String parameterValue2 = "test2";
        List<Object> params = Arrays.asList(parameterValue1, parameterValue2);
        OnDuplicateUpdateContext onDuplicateUpdateContext = new OnDuplicateUpdateContext(assignments, params, 0);
        Object valueFromInsertValueContext1 = onDuplicateUpdateContext.getValue(0);
        assertThat(valueFromInsertValueContext1, is(parameterValue1));
        Object valueFromInsertValueContext2 = onDuplicateUpdateContext.getValue(1);
        assertThat(valueFromInsertValueContext2, is(parameterValue2));
    }
    
    private Collection<ColumnAssignmentSegment> createParameterMarkerExpressionAssignmentSegment() {
        ParameterMarkerExpressionSegment parameterMarkerExpressionSegment0 = new ParameterMarkerExpressionSegment(0, 10, 5);
        ColumnAssignmentSegment assignmentSegment1 = createAssignmentSegment(parameterMarkerExpressionSegment0);
        ParameterMarkerExpressionSegment parameterMarkerExpressionSegment1 = new ParameterMarkerExpressionSegment(0, 10, 6);
        ColumnAssignmentSegment assignmentSegment2 = createAssignmentSegment(parameterMarkerExpressionSegment1);
        return Arrays.asList(assignmentSegment1, assignmentSegment2);
    }
    
    @Test
    void assertGetValueWhenLiteralExpressionSegment() {
        Object literalObject = new Object();
        Collection<ColumnAssignmentSegment> assignments = createLiteralExpressionSegment(literalObject);
        OnDuplicateUpdateContext onDuplicateUpdateContext = new OnDuplicateUpdateContext(assignments, Collections.emptyList(), 0);
        Object valueFromInsertValueContext = onDuplicateUpdateContext.getValue(0);
        assertThat(valueFromInsertValueContext, is(literalObject));
    }
    
    private Collection<ColumnAssignmentSegment> createLiteralExpressionSegment(final Object literalObject) {
        LiteralExpressionSegment parameterLiteralExpression = new LiteralExpressionSegment(0, 10, literalObject);
        ColumnAssignmentSegment assignmentSegment = createAssignmentSegment(parameterLiteralExpression);
        return Collections.singleton(assignmentSegment);
    }
    
    private BinaryOperationExpression createBinaryOperationExpression() {
        ExpressionSegment left = new ColumnSegment(0, 0, new IdentifierValue("columnNameStr"));
        ExpressionSegment right = new ParameterMarkerExpressionSegment(0, 0, 0);
        return new BinaryOperationExpression(0, 0, left, right, "=", "columnNameStr=?");
    }
    
    private ColumnAssignmentSegment createAssignmentSegment(final SimpleExpressionSegment expressionSegment) {
        List<ColumnSegment> columnSegments = Collections.singletonList(new ColumnSegment(0, 0, new IdentifierValue("columnNameStr")));
        return new ColumnAssignmentSegment(0, 0, columnSegments, expressionSegment);
    }
    
    private ColumnAssignmentSegment createAssignmentSegment(final BinaryOperationExpression binaryOperationExpression) {
        List<ColumnSegment> columnSegments = Collections.singletonList(new ColumnSegment(0, 0, new IdentifierValue("columnNameStr")));
        return new ColumnAssignmentSegment(0, 0, columnSegments, binaryOperationExpression);
    }
    
    @Test
    void assertGetColumn() {
        Object literalObject = new Object();
        Collection<ColumnAssignmentSegment> assignments = createLiteralExpressionSegment(literalObject);
        OnDuplicateUpdateContext onDuplicateUpdateContext = new OnDuplicateUpdateContext(assignments, Collections.emptyList(), 0);
        ColumnSegment column = onDuplicateUpdateContext.getColumn(0);
        assertThat(column, is(assignments.iterator().next().getColumns().get(0)));
    }
    
    @Test
    void assertParameterCount() {
        List<ColumnAssignmentSegment> assignments = Arrays.asList(
                createAssignmentSegment(createBinaryOperationExpression()),
                createAssignmentSegment(new ParameterMarkerExpressionSegment(0, 10, 5)),
                createAssignmentSegment(new LiteralExpressionSegment(0, 10, new Object())));
        OnDuplicateUpdateContext onDuplicateUpdateContext = new OnDuplicateUpdateContext(assignments, Arrays.asList("1", "2"), 0);
        assertThat(onDuplicateUpdateContext.getParameterCount(), is(2));
    }
}
