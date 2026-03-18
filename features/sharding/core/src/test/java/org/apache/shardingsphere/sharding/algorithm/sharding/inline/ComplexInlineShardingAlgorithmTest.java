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

package org.apache.shardingsphere.sharding.algorithm.sharding.inline;

import com.google.common.collect.Range;
import org.apache.shardingsphere.infra.algorithm.core.exception.AlgorithmInitializationException;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingValue;
import org.apache.shardingsphere.sharding.exception.algorithm.MismatchedComplexInlineShardingAlgorithmColumnAndValueSizeException;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComplexInlineShardingAlgorithmTest {
    
    private ComplexInlineShardingAlgorithm shardingAlgorithm;
    
    @BeforeEach
    void setUp() {
        Properties props = PropertiesBuilder.build(new Property("algorithm-expression", "t_order_${type % 2}_${order_id % 2}"), new Property("sharding-columns", "type,order_id"),
                new Property("allow-range-query-with-inline-sharding", Boolean.TRUE.toString()));
        shardingAlgorithm = (ComplexInlineShardingAlgorithm) TypedSPILoader.getService(ShardingAlgorithm.class, "COMPLEX_INLINE", props);
    }
    
    @Test
    void assertInitWithNullClass() {
        assertThrows(AlgorithmInitializationException.class, () -> TypedSPILoader.getService(ShardingAlgorithm.class, "COMPLEX_INLINE", PropertiesBuilder.build(new Property("wrong", ""))));
    }
    
    @Test
    void assertInitWithEmptyClassName() {
        assertThrows(AlgorithmInitializationException.class,
                () -> TypedSPILoader.getService(ShardingAlgorithm.class, "COMPLEX_INLINE", PropertiesBuilder.build(new Property("algorithm-expression", ""))));
    }
    
    @Test
    void assertDoShardingWithSingleValue() {
        List<String> availableTargetNames = Arrays.asList("t_order_0_0", "t_order_0_1", "t_order_1_0", "t_order_1_1");
        Collection<String> actual = shardingAlgorithm.doSharding(availableTargetNames, createComplexKeysShardingValue(Collections.singletonList(2)));
        assertTrue(1 == actual.size() && actual.contains("t_order_0_0"));
    }
    
    @Test
    void assertDoShardingWithMultiValues() {
        List<String> availableTargetNames = Arrays.asList("t_order_0_0", "t_order_0_1", "t_order_1_0", "t_order_1_1");
        Collection<String> actual = shardingAlgorithm.doSharding(availableTargetNames, createComplexKeysShardingValue(Arrays.asList(1, 2)));
        assertTrue(actual.containsAll(availableTargetNames));
    }
    
    private ComplexKeysShardingValue<Comparable<?>> createComplexKeysShardingValue(final List<Comparable<?>> values) {
        Map<String, Collection<Comparable<?>>> columnNameAndShardingValuesMap = new HashMap<>(2, 1F);
        columnNameAndShardingValuesMap.put("type", values);
        columnNameAndShardingValuesMap.put("order_id", values);
        return new ComplexKeysShardingValue<>("t_order", columnNameAndShardingValuesMap, Collections.emptyMap());
    }
    
    @Test
    void assertDoShardingWithRangeValue() {
        List<String> availableTargetNames = Arrays.asList("t_order_0_0", "t_order_0_1", "t_order_1_0", "t_order_1_1");
        Collection<String> actual = shardingAlgorithm.doSharding(
                availableTargetNames, new ComplexKeysShardingValue<>("t_order", Collections.emptyMap(), Collections.singletonMap("type", Range.all())));
        assertTrue(actual.containsAll(availableTargetNames));
    }
    
    @Test
    void assertDoShardingWithRangeValueAndEmptyColumns() {
        List<String> availableTargetNames = Arrays.asList("t_order_0_0", "t_order_0_1", "t_order_1_0", "t_order_1_1");
        Properties props = PropertiesBuilder.build(
                new Property("algorithm-expression", "t_order_${type % 2}_${order_id % 2}"), new Property("allow-range-query-with-inline-sharding", Boolean.TRUE.toString()));
        shardingAlgorithm = (ComplexInlineShardingAlgorithm) TypedSPILoader.getService(ShardingAlgorithm.class, "COMPLEX_INLINE", props);
        Collection<String> actual = shardingAlgorithm.doSharding(availableTargetNames, new ComplexKeysShardingValue<>("t_order", Collections.emptyMap(), Collections.emptyMap()));
        assertTrue(actual.isEmpty());
    }
    
    @Test
    void assertDoShardingWithRangeValueButNotAllowRangeQuery() {
        List<String> availableTargetNames = Arrays.asList("t_order_0_0", "t_order_0_1", "t_order_1_0", "t_order_1_1");
        Map<String, Collection<Comparable<?>>> columnNameAndShardingValuesMap = Collections.singletonMap("type", Arrays.asList(1, 2));
        Properties props = PropertiesBuilder.build(new Property("algorithm-expression", "t_order_${type % 2}_${order_id % 2}"), new Property("sharding-columns", "type,order_id"));
        shardingAlgorithm = (ComplexInlineShardingAlgorithm) TypedSPILoader.getService(ShardingAlgorithm.class, "COMPLEX_INLINE", props);
        assertThrows(UnsupportedSQLOperationException.class,
                () -> shardingAlgorithm.doSharding(availableTargetNames, new ComplexKeysShardingValue<>("t_order", columnNameAndShardingValuesMap, Collections.singletonMap("type", Range.all()))));
    }
    
    @Test
    void assertDoShardingWithRangeValueAndMismatchedComplexInlineShardingAlgorithmColumnAndValueSize() {
        List<String> availableTargetNames = Arrays.asList("t_order_0_0", "t_order_0_1", "t_order_1_0", "t_order_1_1");
        assertThrows(MismatchedComplexInlineShardingAlgorithmColumnAndValueSizeException.class,
                () -> shardingAlgorithm.doSharding(availableTargetNames, new ComplexKeysShardingValue<>("t_order", Collections.emptyMap(), Collections.emptyMap())));
    }
}
