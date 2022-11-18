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

package org.apache.shardingsphere.sharding.merge.dql.orderby;

import org.apache.shardingsphere.sql.parser.sql.common.constant.NullsOrderDirection;
import org.apache.shardingsphere.sql.parser.sql.common.constant.OrderDirection;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class CompareUtilTest {
    
    private final boolean caseSensitive = true;
    
    @Test
    public void assertCompareToWhenBothNull() {
        assertThat(CompareUtil.compareTo(null, null, OrderDirection.DESC, NullsOrderDirection.FIRST, caseSensitive), is(0));
    }
    
    @Test
    public void assertCompareToWhenFirstValueIsNullForOrderByAscAndNullsFirst() {
        assertThat(CompareUtil.compareTo(null, 1, OrderDirection.ASC, NullsOrderDirection.FIRST, caseSensitive), is(-1));
    }
    
    @Test
    public void assertCompareToWhenFirstValueIsNullForOrderByAscAndNullsLast() {
        assertThat(CompareUtil.compareTo(null, 1, OrderDirection.ASC, NullsOrderDirection.LAST, caseSensitive), is(1));
    }
    
    @Test
    public void assertCompareToWhenFirstValueIsNullForOrderByDescAndNullsFirst() {
        assertThat(CompareUtil.compareTo(null, 1, OrderDirection.DESC, NullsOrderDirection.FIRST, caseSensitive), is(-1));
    }
    
    @Test
    public void assertCompareToWhenFirstValueIsNullForOrderByDescAndNullsLast() {
        assertThat(CompareUtil.compareTo(null, 1, OrderDirection.DESC, NullsOrderDirection.LAST, caseSensitive), is(1));
    }
    
    @Test
    public void assertCompareToWhenSecondValueIsNullForOrderByAscAndNullsFirst() {
        assertThat(CompareUtil.compareTo(1, null, OrderDirection.ASC, NullsOrderDirection.FIRST, caseSensitive), is(1));
    }
    
    @Test
    public void assertCompareToWhenSecondValueIsNullForOrderByAscAndNullsLast() {
        assertThat(CompareUtil.compareTo(1, null, OrderDirection.ASC, NullsOrderDirection.LAST, caseSensitive), is(-1));
    }
    
    @Test
    public void assertCompareToWhenSecondValueIsNullForOrderByDescAndNullsFirst() {
        assertThat(CompareUtil.compareTo(1, null, OrderDirection.DESC, NullsOrderDirection.FIRST, caseSensitive), is(1));
    }
    
    @Test
    public void assertCompareToWhenSecondValueIsNullForOrderByDescAndNullsLast() {
        assertThat(CompareUtil.compareTo(1, null, OrderDirection.DESC, NullsOrderDirection.LAST, caseSensitive), is(-1));
    }
    
    @Test
    public void assertCompareToWhenAsc() {
        assertThat(CompareUtil.compareTo(1, 2, OrderDirection.ASC, NullsOrderDirection.FIRST, caseSensitive), is(-1));
    }
    
    @Test
    public void assertCompareToWhenDesc() {
        assertThat(CompareUtil.compareTo(1, 2, OrderDirection.DESC, NullsOrderDirection.FIRST, caseSensitive), is(1));
    }
    
    @Test
    public void assetCompareToStringWithCaseSensitive() {
        assertThat(CompareUtil.compareTo("A", "a", OrderDirection.DESC, NullsOrderDirection.FIRST, caseSensitive), is(32));
    }
    
    @Test
    public void assetCompareToStringWithCaseInsensitive() {
        assertThat(CompareUtil.compareTo("A", "a", OrderDirection.DESC, NullsOrderDirection.FIRST, !caseSensitive), is(0));
    }
}
