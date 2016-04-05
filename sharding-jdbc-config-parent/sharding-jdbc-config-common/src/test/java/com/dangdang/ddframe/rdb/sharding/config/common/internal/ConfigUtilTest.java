/**
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

package com.dangdang.ddframe.rdb.sharding.config.common.internal;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ConfigUtilTest {
    
    @Test
    public void testTransformCommaStringToList() {
        assertThat(ConfigUtil.transformCommaStringToList(" 1 , 2 "), hasItems("1", "2"));
        assertThat(ConfigUtil.transformCommaStringToList(" 1 , t_order_${1..2} "), hasItems("1", "t_order_1", "t_order_2"));
        assertThat(ConfigUtil.transformCommaStringToList(" 1 , t_order_${null} "), hasItems("1", "t_order_"));
        assertThat(ConfigUtil.transformCommaStringToList(" 1 , t_order_${'xx'} "), hasItems("1", "t_order_xx"));
        assertThat(ConfigUtil.transformCommaStringToList(" t_${['new','old']}_order_${1..2} "), hasItems("t_new_order_1", "t_new_order_2", "t_old_order_1", "t_old_order_2"));
        assertThat(ConfigUtil.transformCommaStringToList(" t_${[\"new${1+2}\",'old']}_order_${1..2} "), hasItems("t_new3_order_1", "t_new3_order_2", "t_old_order_1", "t_old_order_2"));
    }
    
    @Test
    public void testSplitWithCommaForOneRegularValue() {
        assertThat(ConfigUtil.splitWithComma("order"), is(Collections.singletonList("order")));
    }
    
    @Test
    public void testSplitWithCommaForOneInlineValue() {
        assertThat(ConfigUtil.splitWithComma("order_${0..7}"), is(Collections.singletonList("order_${0..7}")));
    }
    
    @Test
    public void testSplitWithCommaForRegularValuesWithSpace() {
        assertThat(ConfigUtil.splitWithComma(" order , order_item "), is(Arrays.asList("order", "order_item")));
    }
    
    @Test
    public void testSplitWithCommaForMixedValuesWithSpace() {
        assertThat(ConfigUtil.splitWithComma(" order , order_item_${0..7} "), is(Arrays.asList("order", "order_item_${0..7}")));
    }
    
    @Test
    public void testSplitWithCommaForMixedValuesAndWithCommaAtInlineExpression() {
        assertThat(ConfigUtil.splitWithComma(" order , order_item_${0, 2, 4} "), is(Arrays.asList("order", "order_item_${0, 2, 4}")));
    }
    
    @Test
    public void testSplitWithCommaForMixedValuesAndNestedInlineExpression() {
        assertThat(ConfigUtil.splitWithComma(" order , order_item_${0, ${1..7}} "), is(Arrays.asList("order", "order_item_${0, ${1..7}}")));
    }
    
    @Test
    public void testSplitWithCommaForValuesNotInlineExpression() {
        assertThat(ConfigUtil.splitWithComma(" order , order_item_$ {0, 1} "), is(Arrays.asList("order", "order_item_$ {0", "1}")));
    }
}
