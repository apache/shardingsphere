/*
 * Copyright 2016-2018 shardingsphere.io.
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

package io.shardingsphere.core.parsing.integrate.asserts.condition;

import io.shardingsphere.core.parsing.integrate.asserts.SQLStatementAssertMessage;
import io.shardingsphere.core.parsing.integrate.jaxb.condition.ExpectedAndCondition;
import io.shardingsphere.core.parsing.integrate.jaxb.condition.ExpectedCondition;
import io.shardingsphere.core.parsing.integrate.jaxb.condition.ExpectedOrCondition;
import io.shardingsphere.core.parsing.integrate.jaxb.condition.ExpectedValue;
import io.shardingsphere.core.parsing.parser.context.condition.AndCondition;
import io.shardingsphere.core.parsing.parser.context.condition.Condition;
import io.shardingsphere.core.parsing.parser.context.condition.OrCondition;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Condition assert.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class ConditionAssert {
    
    private final SQLStatementAssertMessage assertMessage;
    
    /**
     * Assert or condition.
     * 
     * @param actual actual or condition
     * @param expected expected or condition
     */
    public void assertOrCondition(final OrCondition actual, final ExpectedOrCondition expected) {
        assertThat(assertMessage.getFullAssertMessage("Or condition size assertion error: "), actual.getAndConditions().size(), is(expected.getAndConditions().size()));
        int count = 0;
        for (ExpectedAndCondition each : expected.getAndConditions()) {
            assertAndCondition(actual.getAndConditions().get(count), each);
            count++;
        }
    }
    
    private void assertAndCondition(final AndCondition actual, final ExpectedAndCondition expected) {
        assertThat(assertMessage.getFullAssertMessage("And condition size assertion error: "), actual.getConditions().size(), is(expected.getConditions().size()));
        int count = 0;
        for (ExpectedCondition each : expected.getConditions()) {
            assertCondition(actual.getConditions().get(count), each);
            count++;
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
    
    @SneakyThrows
    private Object getField(final Object actual, final String fieldName) {
        Field field = actual.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(actual);
    }
}
