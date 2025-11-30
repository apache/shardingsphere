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

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.OrderDirection;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class CompareUtilsTest {
    
    private final boolean caseSensitive = true;
    
    @Test
    void assertCompareToWhenBothNull() {
        assertThat(CompareUtils.compareTo(null, null, OrderDirection.DESC, NullsOrderType.FIRST, caseSensitive), is(0));
    }
    
    @Test
    void assertCompareToWhenFirstValueIsNullForOrderByAscAndNullsFirst() {
        assertThat(CompareUtils.compareTo(null, 1, OrderDirection.ASC, NullsOrderType.FIRST, caseSensitive), is(-1));
    }
    
    @Test
    void assertCompareToWhenFirstValueIsNullForOrderByAscAndNullsLast() {
        assertThat(CompareUtils.compareTo(null, 1, OrderDirection.ASC, NullsOrderType.LAST, caseSensitive), is(1));
    }
    
    @Test
    void assertCompareToWhenFirstValueIsNullForOrderByDescAndNullsFirst() {
        assertThat(CompareUtils.compareTo(null, 1, OrderDirection.DESC, NullsOrderType.FIRST, caseSensitive), is(-1));
    }
    
    @Test
    void assertCompareToWhenFirstValueIsNullForOrderByDescAndNullsLast() {
        assertThat(CompareUtils.compareTo(null, 1, OrderDirection.DESC, NullsOrderType.LAST, caseSensitive), is(1));
    }
    
    @Test
    void assertCompareToWhenSecondValueIsNullForOrderByAscAndNullsFirst() {
        assertThat(CompareUtils.compareTo(1, null, OrderDirection.ASC, NullsOrderType.FIRST, caseSensitive), is(1));
    }
    
    @Test
    void assertCompareToWhenSecondValueIsNullForOrderByAscAndNullsLast() {
        assertThat(CompareUtils.compareTo(1, null, OrderDirection.ASC, NullsOrderType.LAST, caseSensitive), is(-1));
    }
    
    @Test
    void assertCompareToWhenSecondValueIsNullForOrderByDescAndNullsFirst() {
        assertThat(CompareUtils.compareTo(1, null, OrderDirection.DESC, NullsOrderType.FIRST, caseSensitive), is(1));
    }
    
    @Test
    void assertCompareToWhenSecondValueIsNullForOrderByDescAndNullsLast() {
        assertThat(CompareUtils.compareTo(1, null, OrderDirection.DESC, NullsOrderType.LAST, caseSensitive), is(-1));
    }
    
    @Test
    void assertCompareToWhenAsc() {
        assertThat(CompareUtils.compareTo(1, 2, OrderDirection.ASC, NullsOrderType.FIRST, caseSensitive), is(-1));
    }
    
    @Test
    void assertCompareToWhenDesc() {
        assertThat(CompareUtils.compareTo(1, 2, OrderDirection.DESC, NullsOrderType.FIRST, caseSensitive), is(1));
    }
    
    @Test
    void assertCompareToStringWithCaseSensitive() {
        assertThat(CompareUtils.compareTo("A", "a", OrderDirection.DESC, NullsOrderType.FIRST, caseSensitive), is(32));
    }
    
    @Test
    void assertCompareToStringWithCaseInsensitive() {
        assertThat(CompareUtils.compareTo("A", "a", OrderDirection.DESC, NullsOrderType.FIRST, !caseSensitive), is(0));
    }
}
