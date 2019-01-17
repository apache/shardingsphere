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

package io.shardingsphere.core.parsing.parser.context.condition;

import io.shardingsphere.core.parsing.parser.expression.SQLExpression;
import io.shardingsphere.core.parsing.parser.expression.SQLNumberExpression;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ConditionTest {
    
    @Test
    public void assertGetConditionValuesForEqual() {
        List<Comparable<?>> actual = new Condition(new Column("test", "test"), new SQLNumberExpression(1)).getConditionValues(Collections.emptyList());
        assertThat(actual.size(), is(1));
        assertThat((Integer) actual.get(0), is(1));
    }
    
    @Test
    public void assertGetConditionValuesForIn() {
        List<Comparable<?>> actual = new Condition(new Column("test", "test"), Arrays.<SQLExpression>asList(new SQLNumberExpression(1), new SQLNumberExpression(2)))
                .getConditionValues(Collections.emptyList());
        assertThat(actual.size(), is(2));
        assertThat((Integer) actual.get(0), is(1));
        assertThat((Integer) actual.get(1), is(2));
    }
    
    @Test
    public void assertGetConditionValuesForBetween() {
        List<Comparable<?>> actual = new Condition(new Column("test", "test"), new SQLNumberExpression(1), new SQLNumberExpression(2)).getConditionValues(Collections.emptyList());
        assertThat(actual.size(), is(2));
        assertThat((Integer) actual.get(0), is(1));
        assertThat((Integer) actual.get(1), is(2));
    }
}
