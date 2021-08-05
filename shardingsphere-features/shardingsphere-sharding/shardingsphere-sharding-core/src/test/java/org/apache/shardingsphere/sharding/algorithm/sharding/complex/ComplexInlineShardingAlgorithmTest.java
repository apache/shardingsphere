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

package org.apache.shardingsphere.sharding.algorithm.sharding.complex;

import com.google.common.collect.Range;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingValue;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public final class ComplexInlineShardingAlgorithmTest {
    
    private ComplexInlineShardingAlgorithm complexInlineShardingAlgorithm;
    
    private ComplexInlineShardingAlgorithm complexInlineShardingAlgorithmAllowRangeQuery;
    
    @Before
    public void setUp() {
        initComplexInlineShardingAlgorithm();
        initComplexInlineShardingAlgorithmAllowRangeQuery();
    }
    
    private void initComplexInlineShardingAlgorithm() {
        complexInlineShardingAlgorithm = new ComplexInlineShardingAlgorithm();
        complexInlineShardingAlgorithm.getProps().setProperty("algorithm-expression", "t_order_${type % 2}_${order_id % 2}");
        complexInlineShardingAlgorithm.getProps().setProperty("sharding-columns", "type,order_id");
        complexInlineShardingAlgorithm.init();
    }
    
    private void initComplexInlineShardingAlgorithmAllowRangeQuery() {
        complexInlineShardingAlgorithmAllowRangeQuery = new ComplexInlineShardingAlgorithm();
        complexInlineShardingAlgorithmAllowRangeQuery.getProps().setProperty("algorithm-expression", "t_order_${type % 2}_${order_id % 2}");
        complexInlineShardingAlgorithmAllowRangeQuery.getProps().setProperty("sharding-columns", "type,order_id");
        complexInlineShardingAlgorithmAllowRangeQuery.getProps().setProperty("allow-range-query-with-inline-sharding", "true");
        complexInlineShardingAlgorithmAllowRangeQuery.init();
    }
    
    @Test
    public void assertDoSharding() {
        List<String> availableTargetNames = Arrays.asList("t_order_0_0", "t_order_0_1", "t_order_1_0", "t_order_1_1");
        Map<String, Collection<Comparable<?>>> sharingValueMap = new HashMap<>(2, 1);
        sharingValueMap.put("type", Collections.singletonList(2));
        sharingValueMap.put("order_id", Collections.singletonList(2));
        ComplexKeysShardingValue<Comparable<?>> shardingValue = new ComplexKeysShardingValue<>("t_order", sharingValueMap, Collections.emptyMap());
        Collection<String> actual = complexInlineShardingAlgorithm.doSharding(availableTargetNames, shardingValue);
        assertTrue(1 == actual.size() && actual.contains("t_order_0_0"));
    }
    
    @Test
    public void assertDoShardingWithMultiValue() {
        List<String> availableTargetNames = Arrays.asList("t_order_0_0", "t_order_0_1", "t_order_1_0", "t_order_1_1");
        Map<String, Collection<Comparable<?>>> sharingValueMap = new HashMap<>(2, 1);
        sharingValueMap.put("type", Arrays.asList(1, 2));
        sharingValueMap.put("order_id", Arrays.asList(1, 2));
        ComplexKeysShardingValue<Comparable<?>> shardingValue = new ComplexKeysShardingValue<>("t_order", sharingValueMap, Collections.emptyMap());
        Collection<String> actual = complexInlineShardingAlgorithm.doSharding(availableTargetNames, shardingValue);
        assertTrue(actual.containsAll(availableTargetNames));
    }
    
    @Test
    public void assertDoShardingWithRangeValue() {
        List<String> availableTargetNames = Arrays.asList("t_order_0_0", "t_order_0_1", "t_order_1_0", "t_order_1_1");
        ComplexKeysShardingValue<Comparable<?>> shardingValue = new ComplexKeysShardingValue<>("t_order", Collections.emptyMap(), Collections.singletonMap("type", Range.all()));
        Collection<String> actual = complexInlineShardingAlgorithmAllowRangeQuery.doSharding(availableTargetNames, shardingValue);
        assertTrue(actual.containsAll(availableTargetNames));
    }
}
