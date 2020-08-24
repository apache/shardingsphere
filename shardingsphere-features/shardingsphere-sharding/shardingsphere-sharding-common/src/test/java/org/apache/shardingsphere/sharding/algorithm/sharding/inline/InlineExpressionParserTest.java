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

package org.apache.shardingsphere.sharding.algorithm.sharding.inline;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class InlineExpressionParserTest {
    
    @Test
    public void assertEvaluateForExpressionIsNull() {
        List<String> expected = new InlineExpressionParser(null).splitAndEvaluate();
        assertThat(expected, is(Collections.<String>emptyList()));
    }
    
    @Test
    public void assertEvaluateForSimpleString() {
        List<String> expected = new InlineExpressionParser(" t_order_0, t_order_1 ").splitAndEvaluate();
        assertThat(expected.size(), is(2));
        assertThat(expected, hasItems("t_order_0", "t_order_1"));
    }
    
    @Test
    public void assertEvaluateForNull() {
        List<String> expected = new InlineExpressionParser("t_order_${null}").splitAndEvaluate();
        assertThat(expected.size(), is(1));
        assertThat(expected, hasItems("t_order_"));
    }
    
    @Test
    public void assertEvaluateForLiteral() {
        List<String> expected = new InlineExpressionParser("t_order_${'xx'}").splitAndEvaluate();
        assertThat(expected.size(), is(1));
        assertThat(expected, hasItems("t_order_xx"));
    }
    
    @Test
    public void assertEvaluateForArray() {
        List<String> expected = new InlineExpressionParser("t_order_${[0, 1, 2]},t_order_item_${[0, 2]}").splitAndEvaluate();
        assertThat(expected.size(), is(5));
        assertThat(expected, hasItems("t_order_0", "t_order_1", "t_order_2", "t_order_item_0", "t_order_item_2"));
    }
    
    @Test
    public void assertEvaluateForRange() {
        List<String> expected = new InlineExpressionParser("t_order_${0..2},t_order_item_${0..1}").splitAndEvaluate();
        assertThat(expected.size(), is(5));
        assertThat(expected, hasItems("t_order_0", "t_order_1", "t_order_2", "t_order_item_0", "t_order_item_1"));
    }
    
    @Test
    public void assertEvaluateForComplex() {
        List<String> expected = new InlineExpressionParser("t_${['new','old']}_order_${1..2}, t_config").splitAndEvaluate();
        assertThat(expected.size(), is(5));
        assertThat(expected, hasItems("t_new_order_1", "t_new_order_2", "t_old_order_1", "t_old_order_2", "t_config"));
    }
    
    @Test
    public void assertEvaluateForCalculate() {
        List<String> expected = new InlineExpressionParser("t_${[\"new${1+2}\",'old']}_order_${1..2}").splitAndEvaluate();
        assertThat(expected.size(), is(4));
        assertThat(expected, hasItems("t_new3_order_1", "t_new3_order_2", "t_old_order_1", "t_old_order_2"));
    }
    
    @Test
    public void assertEvaluateForExpressionPlaceHolder() {
        List<String> expected = new InlineExpressionParser("t_$->{[\"new$->{1+2}\",'old']}_order_$->{1..2}").splitAndEvaluate();
        assertThat(expected.size(), is(4));
        assertThat(expected, hasItems("t_new3_order_1", "t_new3_order_2", "t_old_order_1", "t_old_order_2"));
    }
    
    @Test
    public void assertEvaluateForLong() {
        StringBuilder expression = new StringBuilder();
        for (int i = 0; i < 1024; i++) {
            expression.append("ds_");
            expression.append(i / 64);
            expression.append(".t_user_");
            expression.append(i);
            if (i != 1023) {
                expression.append(",");
            }
        }
        List<String> expected = new InlineExpressionParser(expression.toString()).splitAndEvaluate();
        assertThat(expected.size(), is(1024));
        assertThat(expected, hasItems("ds_0.t_user_0", "ds_15.t_user_1023"));
    }
}
