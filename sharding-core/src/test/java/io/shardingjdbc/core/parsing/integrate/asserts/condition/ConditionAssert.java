/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.parsing.integrate.asserts.condition;

import com.google.common.base.Optional;
import io.shardingjdbc.core.parsing.integrate.asserts.SQLStatementAssertMessage;
import io.shardingjdbc.core.parsing.integrate.jaxb.condition.ExpectedCondition;
import io.shardingjdbc.core.parsing.integrate.jaxb.condition.ExpectedValue;
import io.shardingjdbc.core.parsing.parser.context.condition.Column;
import io.shardingjdbc.core.parsing.parser.context.condition.Condition;
import io.shardingjdbc.core.parsing.parser.context.condition.Conditions;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Condition assert.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class ConditionAssert {
    
    private final SQLStatementAssertMessage assertMessage;
    
    /**
     * Assert conditions.
     * 
     * @param actual actual conditions
     * @param expected expected conditions
     */
    public void assertConditions(final Conditions actual, final List<ExpectedCondition> expected) {
        assertThat(assertMessage.getFullAssertMessage("Conditions size assertion error: "), actual.getConditions().size(), is(expected.size()));
        for (ExpectedCondition each : expected) {
            Optional<Condition> condition = actual.find(new Column(each.getColumnName(), each.getTableName()));
            assertTrue(assertMessage.getFullAssertMessage("Table should exist: "), condition.isPresent());
            assertCondition(condition.get(), each);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void assertCondition(final Condition actual, final ExpectedCondition expected) {
        assertThat(assertMessage.getFullAssertMessage("Condition column name assertion error: "), actual.getColumn().getName().toUpperCase(), is(expected.getColumnName().toUpperCase()));
        assertThat(assertMessage.getFullAssertMessage("Condition table name assertion error: "), actual.getColumn().getTableName().toUpperCase(), is(expected.getTableName().toUpperCase()));
        assertThat(assertMessage.getFullAssertMessage("Condition operator assertion error: "), actual.getOperator().name(), is(expected.getOperator()));
        int count = 0;
        for (ExpectedValue each : expected.getValues()) {
            Map<Integer, Comparable<?>> positionValueMap = (Map<Integer, Comparable<?>>) getField(actual, "positionValueMap");
            Map<Integer, Integer> positionIndexMap = (Map<Integer, Integer>) getField(actual, "positionIndexMap");
            if (!positionValueMap.isEmpty()) {
                assertThat(assertMessage.getFullAssertMessage("Condition parameter value assertion error: "), positionValueMap.get(count), is((Comparable) each.getLiteralForAccurateType()));
            } else if (!positionIndexMap.isEmpty()) {
                assertThat(assertMessage.getFullAssertMessage("Condition parameter index assertion error: "), positionIndexMap.get(count), is(each.getIndex()));
            }
            count++;
        }
    }
    
    private Object getField(final Object actual, final String fieldName) {
        try {
            Field field = actual.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(actual);
        } catch (final ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }
}
