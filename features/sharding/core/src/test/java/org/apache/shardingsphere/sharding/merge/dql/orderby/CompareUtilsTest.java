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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalTime;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class CompareUtilsTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("comparisonArguments")
    void assertCompareTo(final String name, final Comparable<?> firstValue, final Comparable<?> secondValue, final OrderDirection direction, final NullsOrderType nullsOrderType,
                         final boolean caseSensitive, final int expected) {
        assertThat(CompareUtils.compareTo(firstValue, secondValue, direction, nullsOrderType, caseSensitive), is(expected));
    }
    
    private static Stream<Arguments> comparisonArguments() {
        LocalTime firstValue = LocalTime.of(12, 34, 56, 123456000);
        LocalTime secondValue = LocalTime.of(12, 34, 56, 123457000);
        return Stream.of(
                Arguments.of("both-null", null, null, OrderDirection.DESC, NullsOrderType.FIRST, true, 0),
                Arguments.of("first-null-ascending-first", null, 1, OrderDirection.ASC, NullsOrderType.FIRST, true, -1),
                Arguments.of("first-null-ascending-last", null, 1, OrderDirection.ASC, NullsOrderType.LAST, true, 1),
                Arguments.of("first-null-descending-first", null, 1, OrderDirection.DESC, NullsOrderType.FIRST, true, -1),
                Arguments.of("first-null-descending-last", null, 1, OrderDirection.DESC, NullsOrderType.LAST, true, 1),
                Arguments.of("second-null-ascending-first", 1, null, OrderDirection.ASC, NullsOrderType.FIRST, true, 1),
                Arguments.of("second-null-ascending-last", 1, null, OrderDirection.ASC, NullsOrderType.LAST, true, -1),
                Arguments.of("second-null-descending-first", 1, null, OrderDirection.DESC, NullsOrderType.FIRST, true, 1),
                Arguments.of("second-null-descending-last", 1, null, OrderDirection.DESC, NullsOrderType.LAST, true, -1),
                Arguments.of("ascending-numbers", 1, 2, OrderDirection.ASC, NullsOrderType.FIRST, true, -1),
                Arguments.of("descending-numbers", 1, 2, OrderDirection.DESC, NullsOrderType.FIRST, true, 1),
                Arguments.of("case-sensitive-strings", "A", "a", OrderDirection.DESC, NullsOrderType.FIRST, true, 32),
                Arguments.of("case-insensitive-strings", "A", "a", OrderDirection.DESC, NullsOrderType.FIRST, false, 0),
                Arguments.of("ascending-microseconds", firstValue, secondValue, OrderDirection.ASC, NullsOrderType.FIRST, true, -1),
                Arguments.of("descending-microseconds", firstValue, secondValue, OrderDirection.DESC, NullsOrderType.FIRST, true, 1),
                Arguments.of("equal-microseconds", firstValue, firstValue, OrderDirection.ASC, NullsOrderType.FIRST, true, 0));
    }
}
