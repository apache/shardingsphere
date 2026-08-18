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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;

class DistinctAverageAggregationUnitTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getNullValues")
    void assertMergeWithNullValue(final String name, final List<Comparable<?>> values) {
        DistinctAverageAggregationUnit unit = new DistinctAverageAggregationUnit();
        unit.merge(values);
        assertNull(unit.getResult());
    }
    
    private static Stream<Arguments> getNullValues() {
        return Stream.of(
                Arguments.of("Null values", null),
                Arguments.of("Null distinct value", Arrays.asList(null, 1)),
                Arguments.of("Null sum value", Arrays.asList(1, null)));
    }
    
    @Test
    void assertGetResultWithDistinctValues() {
        DistinctAverageAggregationUnit unit = new DistinctAverageAggregationUnit();
        unit.merge(Arrays.asList(1, 1));
        unit.merge(Arrays.asList(2, 2));
        unit.merge(Arrays.asList(1, 1));
        assertThat(unit.getResult(), is(new BigDecimal("1.5000")));
    }
    
    @Test
    void assertGetResultWithoutMergedValues() {
        assertNull(new DistinctAverageAggregationUnit().getResult());
    }
}
