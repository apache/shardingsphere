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

import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;

public class ConfigUtilTest {
    
    @Test
    public void testTransformCommaStringToList() throws Exception {
        assertThat(ConfigUtil.transformCommaStringToList(" 1 , 2 "), hasItems("1", "2"));
        assertThat(ConfigUtil.transformCommaStringToList(" 1 , t_order_${1..2} "), hasItems("1", "t_order_1", "t_order_2"));
        assertThat(ConfigUtil.transformCommaStringToList(" 1 , t_order_${null} "), hasItems("1", "t_order_"));
        assertThat(ConfigUtil.transformCommaStringToList(" 1 , t_order_${'xx'} "), hasItems("1", "t_order_xx"));
        assertThat(ConfigUtil.transformCommaStringToList(" t_${['new','old']}_order_${1..2} "), hasItems("t_new_order_1", "t_new_order_2", "t_old_order_1", "t_old_order_2"));
        assertThat(ConfigUtil.transformCommaStringToList(" t_${[\"new${1+2}\",'old']}_order_${1..2} "), hasItems("t_new3_order_1", "t_new3_order_2", "t_old_order_1", "t_old_order_2"));
    }
}
