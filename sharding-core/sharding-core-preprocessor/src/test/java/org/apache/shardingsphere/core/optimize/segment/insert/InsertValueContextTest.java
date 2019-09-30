package org.apache.shardingsphere.core.optimize.segment.insert;


import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
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
