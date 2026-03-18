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

package org.apache.shardingsphere.shadow.route.determiner;

import org.apache.shardingsphere.shadow.condition.ShadowColumnCondition;
import org.apache.shardingsphere.shadow.condition.ShadowCondition;
import org.apache.shardingsphere.shadow.spi.ShadowOperationType;
import org.apache.shardingsphere.shadow.spi.column.ColumnShadowAlgorithm;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ColumnShadowAlgorithmDeterminerTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("isShadowCases")
    @SuppressWarnings("unchecked")
    void assertIsShadow(final String name, final String tableName, final String columnConditionTable, final Collection<Comparable<?>> values,
                        final boolean algorithmShadowResult, final int expectedAlgorithmCallCount, final boolean expected) {
        ColumnShadowAlgorithm<Comparable<?>> shadowAlgorithm = mock(ColumnShadowAlgorithm.class);
        when(shadowAlgorithm.isShadow(any())).thenReturn(algorithmShadowResult);
        assertThat(ColumnShadowAlgorithmDeterminer.isShadow(shadowAlgorithm,
                new ShadowCondition(tableName, ShadowOperationType.INSERT, new ShadowColumnCondition(columnConditionTable, "user_id", values))), is(expected));
        verify(shadowAlgorithm, times(expectedAlgorithmCallCount)).isShadow(any());
    }
    
    private static Stream<Arguments> isShadowCases() {
        return Stream.of(
                Arguments.of("table mismatch returns false", "t_order", "t_order_item", Collections.singleton(1), true, 0, false),
                Arguments.of("algorithm returns false", "t_order", "t_order", Collections.singleton(1), false, 1, false),
                Arguments.of("algorithm returns true", "t_order", "t_order", Collections.singleton(1), true, 1, true),
                Arguments.of("empty values returns true", "t_order", "t_order", Collections.emptyList(), false, 0, true));
    }
}
