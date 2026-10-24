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

package org.apache.shardingsphere.sharding.merge.dql.groupby.aggregation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalTime;
import java.util.Collections;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ComparableAggregationUnitTest {
    
    @Test
    void assertComparableAggregationForAsc() {
        ComparableAggregationUnit comparableAggregation = new ComparableAggregationUnit(true);
        comparableAggregation.merge(null);
        comparableAggregation.merge(Collections.singletonList(null));
        comparableAggregation.merge(Collections.singletonList(1));
        comparableAggregation.merge(Collections.singletonList(10));
        comparableAggregation.merge(Collections.singletonList(5));
        assertThat(comparableAggregation.getResult(), is(1));
    }
    
    @Test
    void assertComparableAggregationForDesc() {
        ComparableAggregationUnit comparableAggregation = new ComparableAggregationUnit(false);
        comparableAggregation.merge(null);
        comparableAggregation.merge(Collections.singletonList(null));
        comparableAggregation.merge(Collections.singletonList(1));
        comparableAggregation.merge(Collections.singletonList(10));
        comparableAggregation.merge(Collections.singletonList(5));
        assertThat(comparableAggregation.getResult(), is(10));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("timeArguments")
    void assertMergeTimeValues(final String name, final boolean asc, final LocalTime firstValue, final LocalTime secondValue, final LocalTime expected) {
        ComparableAggregationUnit unit = new ComparableAggregationUnit(asc);
        unit.merge(Collections.singletonList(firstValue));
        unit.merge(Collections.singletonList(secondValue));
        unit.merge(Collections.singletonList(firstValue));
        assertThat(unit.getResult(), is(expected));
    }
    
    private static Stream<Arguments> timeArguments() {
        LocalTime firstValue = LocalTime.of(12, 34, 56, 123456000);
        LocalTime secondValue = LocalTime.of(12, 34, 56, 123457000);
        return Stream.of(
                Arguments.of("minimum-microseconds", true, firstValue, secondValue, firstValue),
                Arguments.of("maximum-microseconds", false, firstValue, secondValue, secondValue),
                Arguments.of("minimum-midnight", true, firstValue, LocalTime.MIDNIGHT, LocalTime.MIDNIGHT),
                Arguments.of("maximum-midnight", false, LocalTime.MIDNIGHT, secondValue, secondValue));
    }
}
