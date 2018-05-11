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

package io.shardingsphere.core.merger.dql.orderby;

import io.shardingsphere.core.constant.OrderDirection;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class CompareUtilTest {
    
    @Test
    public void assertCompareToWhenBothNull() {
        assertThat(CompareUtil.compareTo(null, null, OrderDirection.DESC, OrderDirection.ASC), is(0));
    }
    
    @Test
    public void assertCompareToWhenFirstValueIsNullForOrderByAscAndNullOrderByAsc() {
        assertThat(CompareUtil.compareTo(null, 1, OrderDirection.ASC, OrderDirection.ASC), is(-1));
    }
    
    @Test
    public void assertCompareToWhenFirstValueIsNullForOrderByAscAndNullOrderByDesc() {
        assertThat(CompareUtil.compareTo(null, 1, OrderDirection.ASC, OrderDirection.DESC), is(1));
    }
    
    @Test
    public void assertCompareToWhenFirstValueIsNullForOrderByDescAndNullOrderByAsc() {
        assertThat(CompareUtil.compareTo(null, 1, OrderDirection.DESC, OrderDirection.ASC), is(1));
    }
    
    @Test
    public void assertCompareToWhenFirstValueIsNullForOrderByDescAndNullOrderByDesc() {
        assertThat(CompareUtil.compareTo(null, 1, OrderDirection.DESC, OrderDirection.DESC), is(-1));
    }
    
    @Test
    public void assertCompareToWhenSecondValueIsNullForOrderByAscAndNullOrderByAsc() {
        assertThat(CompareUtil.compareTo(1, null, OrderDirection.ASC, OrderDirection.ASC), is(1));
    }
    
    @Test
    public void assertCompareToWhenSecondValueIsNullForOrderByAscAndNullOrderByDesc() {
        assertThat(CompareUtil.compareTo(1, null, OrderDirection.ASC, OrderDirection.DESC), is(-1));
    }
    
    @Test
    public void assertCompareToWhenSecondValueIsNullForOrderByDescAndNullOrderByAsc() {
        assertThat(CompareUtil.compareTo(1, null, OrderDirection.DESC, OrderDirection.ASC), is(-1));
    }
    
    @Test
    public void assertCompareToWhenSecondValueIsNullForOrderByDescAndNullOrderByDesc() {
        assertThat(CompareUtil.compareTo(1, null, OrderDirection.DESC, OrderDirection.DESC), is(1));
    }
    
    @Test
    public void assertCompareToWhenAsc() {
        assertThat(CompareUtil.compareTo(1, 2, OrderDirection.ASC, OrderDirection.ASC), is(-1));
    }
    
    @Test
    public void assertCompareToWhenDesc() {
        assertThat(CompareUtil.compareTo(1, 2, OrderDirection.DESC, OrderDirection.ASC), is(1));
    }
}
