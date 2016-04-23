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

package com.dangdang.ddframe.rdb.sharding.api;

import com.dangdang.ddframe.rdb.sharding.api.ShardingValue.ShardingValueType;
import com.google.common.collect.Range;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ShardingValueTest {
    
    @Test
    public void assertGetTypeWithSingleValue() {
        assertThat(new ShardingValue<>("columnName", "value").getType(), is(ShardingValueType.SINGLE));
    }
    
    @Test
    public void assertGetTypeWithMultipleValue() {
        assertThat(new ShardingValue<>("columnName", Collections.singletonList("value")).getType(), is(ShardingValueType.LIST));
    }
    
    @Test
    public void assertGetTypeWithRangeValue() {
        assertThat(new ShardingValue<>("columnName", Range.closed(10, 20)).getType(), is(ShardingValueType.RANGE));
    }
    
    @Test
    public void assertToStringWithSingleValue() {
        assertThat(new ShardingValue<>("columnName", "value").toString(), is("ShardingValue(columnName=columnName, value=value, values=[], valueRange=null)"));
    }
    
    @Test
    public void assertToStringWithMultipleValue() {
        assertThat(new ShardingValue<>("columnName", Collections.singletonList("value")).toString(), is("ShardingValue(columnName=columnName, value=null, values=[value], valueRange=null)"));
    }
    
    @Test
    public void assertToStringWithRangeValue() {
        assertThat(new ShardingValue<>("columnName", Range.closed(10, 20)).toString(), is("ShardingValue(columnName=columnName, value=null, values=[], valueRange=[10‥20])"));
    }
}
