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

import org.apache.shardingsphere.sql.parser.sql.common.enums.NullsOrderType;
import org.apache.shardingsphere.sql.parser.sql.common.enums.OrderDirection;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class CompareUtilTest {
    
    private final boolean caseSensitive = true;
    
    @Test
    void assertCompareToWhenBothNull() {
        assertThat(CompareUtil.compareTo(null, null, OrderDirection.DESC, NullsOrderType.FIRST, caseSensitive), is(0));
    }
    
    @Test
    void assertCompareToWhenFirstValueIsNullForOrderByAscAndNullsFirst() {
        assertThat(CompareUtil.compareTo(null, 1, OrderDirection.ASC, NullsOrderType.FIRST, caseSensitive), is(-1));
    }
    
    @Test
    void assertCompareToWhenFirstValueIsNullForOrderByAscAndNullsLast() {
        assertThat(CompareUtil.compareTo(null, 1, OrderDirection.ASC, NullsOrderType.LAST, caseSensitive), is(1));
    }
    
    @Test
    void assertCompareToWhenFirstValueIsNullForOrderByDescAndNullsFirst() {
        assertThat(CompareUtil.compareTo(null, 1, OrderDirection.DESC, NullsOrderType.FIRST, caseSensitive), is(-1));
    }
    
    @Test
    void assertCompareToWhenFirstValueIsNullForOrderByDescAndNullsLast() {
        assertThat(CompareUtil.compareTo(null, 1, OrderDirection.DESC, NullsOrderType.LAST, caseSensitive), is(1));
    }
    
    @Test
    void assertCompareToWhenSecondValueIsNullForOrderByAscAndNullsFirst() {
        assertThat(CompareUtil.compareTo(1, null, OrderDirection.ASC, NullsOrderType.FIRST, caseSensitive), is(1));
    }
    
    @Test
    void assertCompareToWhenSecondValueIsNullForOrderByAscAndNullsLast() {
        assertThat(CompareUtil.compareTo(1, null, OrderDirection.ASC, NullsOrderType.LAST, caseSensitive), is(-1));
    }
    
    @Test
    void assertCompareToWhenSecondValueIsNullForOrderByDescAndNullsFirst() {
        assertThat(CompareUtil.compareTo(1, null, OrderDirection.DESC, NullsOrderType.FIRST, caseSensitive), is(1));
    }
    
    @Test
    void assertCompareToWhenSecondValueIsNullForOrderByDescAndNullsLast() {
        assertThat(CompareUtil.compareTo(1, null, OrderDirection.DESC, NullsOrderType.LAST, caseSensitive), is(-1));
    }
    
    @Test
    void assertCompareToWhenAsc() {
        assertThat(CompareUtil.compareTo(1, 2, OrderDirection.ASC, NullsOrderType.FIRST, caseSensitive), is(-1));
    }
    
    @Test
    void assertCompareToWhenDesc() {
        assertThat(CompareUtil.compareTo(1, 2, OrderDirection.DESC, NullsOrderType.FIRST, caseSensitive), is(1));
    }
    
    @Test
    void assetCompareToStringWithCaseSensitive() {
        assertThat(CompareUtil.compareTo("A", "a", OrderDirection.DESC, NullsOrderType.FIRST, caseSensitive), is(32));
    }
    
    @Test
    void assetCompareToStringWithCaseInsensitive() {
        assertThat(CompareUtil.compareTo("A", "a", OrderDirection.DESC, NullsOrderType.FIRST, !caseSensitive), is(0));
    }
}
