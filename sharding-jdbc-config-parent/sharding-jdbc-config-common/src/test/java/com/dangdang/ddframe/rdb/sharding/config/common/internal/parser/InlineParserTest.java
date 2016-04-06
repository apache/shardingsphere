/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.config.common.internal.parser;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public final class InlineParserTest {
    
    @Test
    public void testSplit() {
        assertThat(new InlineParser(" 1 ").split(), is(Collections.singletonList("1")));
        assertThat(new InlineParser(" 1 , 2 ").split(), is(Arrays.asList("1", "2")));
    }
    
    @Test
    public void testEvaluate() {
        assertThat(new InlineParser(" 1 , 2 ").evaluate(), hasItems("1", "2"));
        assertThat(new InlineParser(" 1 , t_order_${1..2} ").evaluate(), hasItems("1", "t_order_1", "t_order_2"));
        assertThat(new InlineParser(" 1 , t_order_${null} ").evaluate(), hasItems("1", "t_order_"));
        assertThat(new InlineParser(" 1 , t_order_${'xx'} ").evaluate(), hasItems("1", "t_order_xx"));
        assertThat(new InlineParser(" t_${['new','old']}_order_${1..2} ").evaluate(), hasItems("t_new_order_1", "t_new_order_2", "t_old_order_1", "t_old_order_2"));
        assertThat(new InlineParser(" t_${[\"new${1+2}\",'old']}_order_${1..2} ").evaluate(), hasItems("t_new3_order_1", "t_new3_order_2", "t_old_order_1", "t_old_order_2"));
    }
    
    @Test
    public void testSplitWithInlineExpressionForOneRegularValue() {
        assertThat(new InlineParser("order").splitWithInlineExpression(), is(Collections.singletonList("order")));
    }
    
    @Test
    public void testSplitWithInlineExpressionForOneInlineValue() {
        assertThat(new InlineParser("order_${0..7}").splitWithInlineExpression(), is(Collections.singletonList("order_${0..7}")));
    }
    
    @Test
    public void testSplitWithInlineExpressionForRegularValuesWithSpace() {
        assertThat(new InlineParser(" order , order_item ").splitWithInlineExpression(), is(Arrays.asList("order", "order_item")));
    }
    
    @Test
    public void testSplitWithInlineExpressionForMixedValuesWithSpace() {
        assertThat(new InlineParser(" order , order_item_${0..7} ").splitWithInlineExpression(), is(Arrays.asList("order", "order_item_${0..7}")));
    }
    
    @Test
    public void testSplitWithInlineExpressionForMixedValuesAndWithCommaAtInlineExpression() {
        assertThat(new InlineParser(" order , order_item_${0, 2, 4} ").splitWithInlineExpression(), is(Arrays.asList("order", "order_item_${0, 2, 4}")));
    }
    
    @Test
    public void testSplitWithInlineExpressionForMixedValuesAndNestedInlineExpression() {
        assertThat(new InlineParser(" order , order_item_${0, ${1..7}} ").splitWithInlineExpression(), is(Arrays.asList("order", "order_item_${0, ${1..7}}")));
    }
    
    @Test
    public void testSplitWithInlineExpressionForValuesNotInlineExpression() {
        assertThat(new InlineParser(" order , order_item_$ {0, 1} ").splitWithInlineExpression(), is(Arrays.asList("order", "order_item_$ {0", "1}")));
    }
}
