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

package org.apache.shardingsphere.infra.binder.segment.insert.values;

import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.SimpleExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class OnDuplicateUpdateContextTest {
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertInstanceConstructedOk() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Collection<AssignmentSegment> assignments = Collections.emptyList();
        List<Object> parameters = Collections.emptyList();
        int parametersOffset = 0;
        OnDuplicateUpdateContext onDuplicateUpdateContext = new OnDuplicateUpdateContext(assignments, parameters, parametersOffset);
        Method getValueExpressionsMethod = OnDuplicateUpdateContext.class.getDeclaredMethod("getValueExpressions", Collection.class);
        getValueExpressionsMethod.setAccessible(true);
        List<ExpressionSegment> getValueExpressionsResult = (List<ExpressionSegment>) getValueExpressionsMethod.invoke(onDuplicateUpdateContext, new Object[]{assignments});
        assertThat(onDuplicateUpdateContext.getValueExpressions(), is(getValueExpressionsResult));
        Method getParametersMethod = OnDuplicateUpdateContext.class.getDeclaredMethod("getParameters", List.class, int.class);
        getParametersMethod.setAccessible(true);
        List<Object> getParametersResult = (List<Object>) getParametersMethod.invoke(onDuplicateUpdateContext, new Object[]{parameters, parametersOffset});
        assertThat(onDuplicateUpdateContext.getParameters(), is(getParametersResult));
    }
    
    @Test
    public void assertGetValueWhenParameterMarker() {
        Collection<AssignmentSegment> assignments = createParameterMarkerExpressionAssignmentSegment();
        String parameterValue1 = "test1";
        String parameterValue2 = "test2";
        List<Object> parameters = Arrays.asList(parameterValue1, parameterValue2);
        int parametersOffset = 0;
        OnDuplicateUpdateContext onDuplicateUpdateContext = new OnDuplicateUpdateContext(assignments, parameters, parametersOffset);
        Object valueFromInsertValueContext1 = onDuplicateUpdateContext.getValue(0);
        assertThat(valueFromInsertValueContext1, is(parameterValue1));
        Object valueFromInsertValueContext2 = onDuplicateUpdateContext.getValue(1);
        assertThat(valueFromInsertValueContext2, is(parameterValue2));
    }
    
    private Collection<AssignmentSegment> createParameterMarkerExpressionAssignmentSegment() {
        ParameterMarkerExpressionSegment parameterMarkerExpressionSegment0 = new ParameterMarkerExpressionSegment(0, 10, 5);
        AssignmentSegment assignmentSegment1 = createAssignmentSegment(parameterMarkerExpressionSegment0);
        ParameterMarkerExpressionSegment parameterMarkerExpressionSegment1 = new ParameterMarkerExpressionSegment(0, 10, 6);
        AssignmentSegment assignmentSegment2 = createAssignmentSegment(parameterMarkerExpressionSegment1);
        return Arrays.asList(assignmentSegment1, assignmentSegment2);
    }
    
    @Test
    public void assertGetValueWhenLiteralExpressionSegment() {
        Object literalObject = new Object();
        Collection<AssignmentSegment> assignments = createLiteralExpressionSegment(literalObject);
        List<Object> parameters = Collections.emptyList();
        OnDuplicateUpdateContext onDuplicateUpdateContext = new OnDuplicateUpdateContext(assignments, parameters, 0);
        Object valueFromInsertValueContext = onDuplicateUpdateContext.getValue(0);
        assertThat(valueFromInsertValueContext, is(literalObject));
    }
    
    private Collection<AssignmentSegment> createLiteralExpressionSegment(final Object literalObject) {
        LiteralExpressionSegment parameterLiteralExpression = new LiteralExpressionSegment(0, 10, literalObject);
        AssignmentSegment assignmentSegment = createAssignmentSegment(parameterLiteralExpression);
        return Collections.singleton(assignmentSegment);
    }
    
    private BinaryOperationExpression createBinaryOperationExpression() {
        ExpressionSegment left = new ColumnSegment(0, 0, new IdentifierValue("columnNameStr"));
        ExpressionSegment right = new ParameterMarkerExpressionSegment(0, 0, 0);
        return new BinaryOperationExpression(0, 0, left, right, "=", "columnNameStr=?");
    }
    
    private AssignmentSegment createAssignmentSegment(final SimpleExpressionSegment expressionSegment) {
        List<ColumnSegment> columnSegments = Collections.singletonList(new ColumnSegment(0, 0, new IdentifierValue("columnNameStr")));
        return new ColumnAssignmentSegment(0, 0, columnSegments, expressionSegment);
    }
    
    private AssignmentSegment createAssignmentSegment(final BinaryOperationExpression binaryOperationExpression) {
        List<ColumnSegment> columnSegments = Collections.singletonList(new ColumnSegment(0, 0, new IdentifierValue("columnNameStr")));
        return new ColumnAssignmentSegment(0, 0, columnSegments, binaryOperationExpression);
    }
    
    @Test
    public void assertGetColumn() {
        Object literalObject = new Object();
        Collection<AssignmentSegment> assignments = createLiteralExpressionSegment(literalObject);
        List<Object> parameters = Collections.emptyList();
        OnDuplicateUpdateContext onDuplicateUpdateContext = new OnDuplicateUpdateContext(assignments, parameters, 0);
        ColumnSegment column = onDuplicateUpdateContext.getColumn(0);
        assertThat(column, is(assignments.iterator().next().getColumns().get(0)));
    }
    
    @Test
    public void assertParameterCount() {
        List<AssignmentSegment> assignments = Arrays.asList(
                createAssignmentSegment(createBinaryOperationExpression()),
                createAssignmentSegment(new ParameterMarkerExpressionSegment(0, 10, 5)),
                createAssignmentSegment(new LiteralExpressionSegment(0, 10, new Object())));
        OnDuplicateUpdateContext onDuplicateUpdateContext = new OnDuplicateUpdateContext(assignments, Arrays.asList("1", "2"), 0);
        assertThat(onDuplicateUpdateContext.getParameterCount(), is(2));
    }
}
