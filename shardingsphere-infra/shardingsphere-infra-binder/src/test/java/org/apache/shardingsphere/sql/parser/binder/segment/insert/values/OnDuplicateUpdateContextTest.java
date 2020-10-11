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

package org.apache.shardingsphere.sql.parser.binder.segment.insert.values;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.SimpleExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class OnDuplicateUpdateContextTest {
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertInstanceConstructedOk() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Collection<AssignmentSegment> assignments = Lists.newArrayList();
        List<Object> parameters = Collections.emptyList();
        int parametersOffset = 0;
        OnDuplicateUpdateContext onDuplicateUpdateContext = new OnDuplicateUpdateContext(assignments, parameters, parametersOffset);
        Method calculateParameterCountMethod = OnDuplicateUpdateContext.class.getDeclaredMethod("calculateParameterCount", Collection.class);
        calculateParameterCountMethod.setAccessible(true);
        int calculateParameterCountResult = (int) calculateParameterCountMethod.invoke(onDuplicateUpdateContext, new Object[]{assignments});
        assertThat(onDuplicateUpdateContext.getParameterCount(), is(calculateParameterCountResult));
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
        Collection<AssignmentSegment> assignments = makeParameterMarkerExpressionAssignmentSegment();
        String parameterValue1 = "test1";
        String parameterValue2 = "test2";
        List<Object> parameters = Lists.newArrayList(parameterValue1, parameterValue2);
        int parametersOffset = 0;
        OnDuplicateUpdateContext onDuplicateUpdateContext = new OnDuplicateUpdateContext(assignments, parameters, parametersOffset);
        Object valueFromInsertValueContext1 = onDuplicateUpdateContext.getValue(0);
        assertThat(valueFromInsertValueContext1, is(parameterValue1));
        Object valueFromInsertValueContext2 = onDuplicateUpdateContext.getValue(1);
        assertThat(valueFromInsertValueContext2, is(parameterValue2));
    }
    
    private Collection<AssignmentSegment> makeParameterMarkerExpressionAssignmentSegment() {
        ParameterMarkerExpressionSegment parameterMarkerExpressionSegment0 = new ParameterMarkerExpressionSegment(0, 10, 5);
        AssignmentSegment assignmentSegment1 = makeAssignmentSegment(parameterMarkerExpressionSegment0);
        ParameterMarkerExpressionSegment parameterMarkerExpressionSegment1 = new ParameterMarkerExpressionSegment(0, 10, 6);
        AssignmentSegment assignmentSegment2 = makeAssignmentSegment(parameterMarkerExpressionSegment1);
        return Lists.newArrayList(assignmentSegment1, assignmentSegment2);
    }
    
    @Test
    public void assertGetValueWhenLiteralExpressionSegment() {
        Object literalObject = new Object();
        Collection<AssignmentSegment> assignments = makeLiteralExpressionSegment(literalObject);
        List<Object> parameters = Collections.emptyList();
        OnDuplicateUpdateContext onDuplicateUpdateContext = new OnDuplicateUpdateContext(assignments, parameters, 0);
        Object valueFromInsertValueContext = onDuplicateUpdateContext.getValue(0);
        assertThat(valueFromInsertValueContext, is(literalObject));
    }
    
    private Collection<AssignmentSegment> makeLiteralExpressionSegment(final Object literalObject) {
        LiteralExpressionSegment parameterLiteralExpression = new LiteralExpressionSegment(0, 10, literalObject);
        AssignmentSegment assignmentSegment = makeAssignmentSegment(parameterLiteralExpression);
        return Collections.singleton(assignmentSegment);
    }
    
    private AssignmentSegment makeAssignmentSegment(final SimpleExpressionSegment expressionSegment) {
        int doesNotMatterLexicalIndex = 0;
        String doesNotMatterColumnName = "columnNameStr";
        ColumnSegment column = new ColumnSegment(doesNotMatterLexicalIndex, doesNotMatterLexicalIndex, new IdentifierValue(doesNotMatterColumnName));
        return new AssignmentSegment(doesNotMatterLexicalIndex, doesNotMatterLexicalIndex, column, expressionSegment);
    }
    
    @Test
    public void assertGetParameterIndex() throws NoSuchMethodException, IllegalAccessException {
        Collection<AssignmentSegment> assignments = Lists.newArrayList();
        List<Object> parameters = Collections.emptyList();
        int parametersOffset = 0;
        OnDuplicateUpdateContext onDuplicateUpdateContext = new OnDuplicateUpdateContext(assignments, parameters, parametersOffset);
        Method getParameterIndexMethod = OnDuplicateUpdateContext.class.getDeclaredMethod("getParameterIndex", ExpressionSegment.class);
        getParameterIndexMethod.setAccessible(true);
        ParameterMarkerExpressionSegment notExistsExpressionSegment = new ParameterMarkerExpressionSegment(0, 0, 0);
        Throwable targetException = null;
        try {
            getParameterIndexMethod.invoke(onDuplicateUpdateContext, notExistsExpressionSegment);
        } catch (final InvocationTargetException ex) {
            targetException = ex.getTargetException();
        }
        assertTrue("expected throw IllegalArgumentException", targetException instanceof IllegalArgumentException);
    }
    
    @Test
    public void assertGetColumn() {
        Object literalObject = new Object();
        Collection<AssignmentSegment> assignments = makeLiteralExpressionSegment(literalObject);
        List<Object> parameters = Collections.emptyList();
        OnDuplicateUpdateContext onDuplicateUpdateContext = new OnDuplicateUpdateContext(assignments, parameters, 0);
        ColumnSegment column = onDuplicateUpdateContext.getColumn(0);
        assertThat(column, is(assignments.iterator().next().getColumn()));
    }
}
