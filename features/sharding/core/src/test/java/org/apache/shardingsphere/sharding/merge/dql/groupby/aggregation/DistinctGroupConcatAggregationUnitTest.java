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

import java.sql.Time;
import java.time.LocalTime;
import java.util.Collections;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class DistinctGroupConcatAggregationUnitTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("valueArguments")
    void assertMergeValue(final String name, final Comparable<?> value, final String expected) {
        DistinctGroupConcatAggregationUnit unit = new DistinctGroupConcatAggregationUnit(null);
        unit.merge(Collections.singletonList(value));
        assertThat(unit.getResult(), is(expected));
    }
    
    private static Stream<Arguments> valueArguments() {
        return Stream.of(
                Arguments.of("whole-minute", LocalTime.of(12, 34), "12:34"),
                Arguments.of("microseconds", LocalTime.of(12, 34, 56, 123456000), "12:34:56.123456"),
                Arguments.of("midnight-microsecond", LocalTime.of(0, 0, 0, 1000), "00:00:00.000001"),
                Arguments.of("legacy-time", Time.valueOf("12:34:00"), "12:34:00"),
                Arguments.of("string", "foo", "foo"),
                Arguments.of("number", 123, "123"));
    }
    
    @Test
    void assertMergeDistinctTimesWithDefaultSeparator() {
        DistinctGroupConcatAggregationUnit unit = new DistinctGroupConcatAggregationUnit(null);
        unit.merge(Collections.singletonList(LocalTime.of(12, 34)));
        unit.merge(Collections.singletonList(LocalTime.of(12, 34)));
        unit.merge(Collections.singletonList(LocalTime.of(12, 35)));
        assertThat(unit.getResult(), is("12:34,12:35"));
    }
    
    @Test
    void assertMergeDistinctTimesWithCustomSeparator() {
        DistinctGroupConcatAggregationUnit unit = new DistinctGroupConcatAggregationUnit(";");
        unit.merge(Collections.singletonList(LocalTime.of(12, 34)));
        unit.merge(Collections.singletonList(LocalTime.of(12, 34)));
        unit.merge(Collections.singletonList(LocalTime.of(12, 35)));
        assertThat(unit.getResult(), is("12:34;12:35"));
    }
}
