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
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingValue;
import org.apache.shardingsphere.sharding.exception.algorithm.sharding.ShardingAlgorithmInitializationException;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
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
    
    @Test
    void assertInitWithNullClass() {
        assertThrows(ShardingAlgorithmInitializationException.class,
                () -> TypedSPILoader.getService(ShardingAlgorithm.class, "COMPLEX_INLINE", PropertiesBuilder.build(new Property("wrong", ""))));
    }
    
    @Test
    void assertInitWithEmptyClassName() {
        assertThrows(ShardingAlgorithmInitializationException.class,
                () -> TypedSPILoader.getService(ShardingAlgorithm.class, "COMPLEX_INLINE", PropertiesBuilder.build(new Property("algorithm-expression", ""))));
    }
    
    @Test
    void assertDoSharding() {
        Properties props = PropertiesBuilder.build(new Property("algorithm-expression", "t_order_${type % 2}_${order_id % 2}"), new Property("sharding-columns", "type,order_id"));
        ComplexInlineShardingAlgorithm algorithm = (ComplexInlineShardingAlgorithm) TypedSPILoader.getService(ShardingAlgorithm.class, "COMPLEX_INLINE", props);
        List<String> availableTargetNames = Arrays.asList("t_order_0_0", "t_order_0_1", "t_order_1_0", "t_order_1_1");
        Collection<String> actual = algorithm.doSharding(availableTargetNames, createComplexKeysShardingValue(Collections.singletonList(2)));
        assertTrue(1 == actual.size() && actual.contains("t_order_0_0"));
    }
    
    @Test
    void assertDoShardingWithMultiValue() {
        Properties props = PropertiesBuilder.build(new Property("algorithm-expression", "t_order_${type % 2}_${order_id % 2}"), new Property("sharding-columns", "type,order_id"));
        ComplexInlineShardingAlgorithm algorithm = (ComplexInlineShardingAlgorithm) TypedSPILoader.getService(ShardingAlgorithm.class, "COMPLEX_INLINE", props);
        List<String> availableTargetNames = Arrays.asList("t_order_0_0", "t_order_0_1", "t_order_1_0", "t_order_1_1");
        Collection<String> actual = algorithm.doSharding(availableTargetNames, createComplexKeysShardingValue(Arrays.asList(1, 2)));
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
        Properties props = PropertiesBuilder.build(new Property("algorithm-expression", "t_order_${type % 2}_${order_id % 2}"),
                new Property("sharding-columns", "type,order_id"), new Property("allow-range-query-with-inline-sharding", Boolean.TRUE.toString()));
        ComplexInlineShardingAlgorithm algorithm = (ComplexInlineShardingAlgorithm) TypedSPILoader.getService(ShardingAlgorithm.class, "COMPLEX_INLINE", props);
        List<String> availableTargetNames = Arrays.asList("t_order_0_0", "t_order_0_1", "t_order_1_0", "t_order_1_1");
        Collection<String> actual = algorithm.doSharding(availableTargetNames, new ComplexKeysShardingValue<>("t_order", Collections.emptyMap(), Collections.singletonMap("type", Range.all())));
        assertTrue(actual.containsAll(availableTargetNames));
    }
}
