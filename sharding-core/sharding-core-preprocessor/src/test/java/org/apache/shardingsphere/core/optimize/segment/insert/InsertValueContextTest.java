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

package org.apache.shardingsphere.core.optimize.segment.insert;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.core.optimize.segment.insert.expression.DerivedLiteralExpressionSegment;
import org.apache.shardingsphere.core.optimize.segment.insert.expression.DerivedParameterMarkerExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class InsertValueContextTest {


    @Test
    public void assertInstanceConstructedOk() throws NoSuchMethodException {
        Collection<ExpressionSegment> assignments = Lists.newArrayList();
        List<Object> parameters = Lists.newArrayList();
        int parametersOffset = 0;

        InsertValueContext insertValueContext = new InsertValueContext(assignments,parameters, parametersOffset);

        MethodInvocation<Integer> calculateParametersCountMethod = new MethodInvocation(InsertValueContext.class.getDeclaredMethod("calculateParametersCount", Collection.class), new Object[] {assignments});
        int calculateParametersCountResult = calculateParametersCountMethod.invoke(insertValueContext);
        assertThat(insertValueContext.getParametersCount(), is(calculateParametersCountResult));

        MethodInvocation<List<ExpressionSegment>> getValueExpressionsMethod = new MethodInvocation(InsertValueContext.class.getDeclaredMethod("getValueExpressions", Collection.class), new Object[] {assignments});
        List<ExpressionSegment> getValueExpressionsResult = getValueExpressionsMethod.invoke(insertValueContext);
        assertThat(insertValueContext.getValueExpressions(), is(getValueExpressionsResult));

        MethodInvocation<List<Object>> getParametersMethod = new MethodInvocation(InsertValueContext.class.getDeclaredMethod("getParameters", new Class[]{List.class, int.class}), new Object[] {parameters, parametersOffset});
        List<Object> getParametersResult = getParametersMethod.invoke(insertValueContext);
        assertThat(insertValueContext.getParameters(), is(getParametersResult));
    }

    @Test
    public void assertGetValueWhenParameterMarker() {
        Collection<? extends ExpressionSegment> assignments = makeParameterMarkerExpresstionSegment();
        String parameterValue = "test";
        List<? extends Object> parameters = Lists.newArrayList(parameterValue);
        int parametersOffset = 0;

        InsertValueContext insertValueContext = new InsertValueContext(assignments,parameters, parametersOffset);

        final int index = 0;
        Object result = insertValueContext.getValue(index);

        assertThat((String)result, is(parameterValue));
    }

    private Collection<ParameterMarkerExpressionSegment> makeParameterMarkerExpresstionSegment() {
        ParameterMarkerExpressionSegment parameterMarkerExpressionSegment = new ParameterMarkerExpressionSegment(0, 10, 5);

        return Lists.newArrayList(parameterMarkerExpressionSegment);
    }

    @Test
    public void assertGetValueWhenLiteralExpressionSegment() {
        Object literalObject = new Object();
        Collection<? extends ExpressionSegment> assignments = makeLiteralExpressionSegment(literalObject);
        List<? extends Object> parameters = Lists.newArrayList();

        InsertValueContext insertValueContext = new InsertValueContext(assignments,parameters, 0);

        Object result = insertValueContext.getValue(0);

        assertThat(result, is(literalObject));
    }

    private Collection<LiteralExpressionSegment> makeLiteralExpressionSegment(Object literalObject) {
        LiteralExpressionSegment parameterMarkerExpressionSegment = new LiteralExpressionSegment(0, 10, literalObject);

        return Lists.newArrayList(parameterMarkerExpressionSegment);
    }

    @Test
    public void assertAppendValueWhenParametersIsEmpty() {
        Collection<? extends ExpressionSegment> assignments = Lists.newArrayList();
        List<? extends Object> parameters = Lists.newArrayList();

        InsertValueContext insertValueContext = new InsertValueContext(assignments,parameters, 0);

        Object value = "test";
        String type = "String";

        insertValueContext.appendValue(value, type);

        List<ExpressionSegment> valueExpressions = insertValueContext.getValueExpressions();
        assertThat(valueExpressions.size(), is(1));

        DerivedLiteralExpressionSegment segmentInInsertValueContext = (DerivedLiteralExpressionSegment) valueExpressions.get(0);
        assertThat(segmentInInsertValueContext, is(new DerivedLiteralExpressionSegment(value, type)));
    }

    @Test
    public void assertAppendValueWhenParametersIsNotEmpty() {
        Collection<? extends ExpressionSegment> assignments = makeParameterMarkerExpresstionSegment();
        String parameterValue = "test";
        List<? extends Object> parameters = Lists.newArrayList(parameterValue);
        int parametersOffset = 0;

        InsertValueContext insertValueContext = new InsertValueContext(assignments,parameters, parametersOffset);

        Object value = "test";
        String type = "String";

        insertValueContext.appendValue(value, type);

        List<ExpressionSegment> valueExpressions = insertValueContext.getValueExpressions();
        assertThat(valueExpressions.size(), is(2));

        DerivedParameterMarkerExpressionSegment segmentInInsertValueContext = (DerivedParameterMarkerExpressionSegment) valueExpressions.get(1);
        assertThat(segmentInInsertValueContext, is(new DerivedParameterMarkerExpressionSegment(parameters.size() - 1, type)));
    }
}


@RequiredArgsConstructor
class MethodInvocation<T> {
    @Getter
    private final Method method;

    @Getter
    private final Object[] arguments;

    /**
     * Invoke method.
     *
     * @param target target object
     */
    @SneakyThrows
    public T invoke(final Object target) {
        method.setAccessible(true);
        return (T)method.invoke(target, arguments);
    }
}
