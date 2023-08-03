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

package org.apache.shardingsphere.sharding.algorithm.sharding.hint;

import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.api.sharding.hint.HintShardingValue;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HintInlineShardingAlgorithmTest {
    
    private HintInlineShardingAlgorithm hintInlineShardingAlgorithm;
    
    private HintInlineShardingAlgorithm hintInlineShardingAlgorithmDefault;
    
    @BeforeEach
    void setUp() {
        hintInlineShardingAlgorithm = (HintInlineShardingAlgorithm) TypedSPILoader.getService(ShardingAlgorithm.class,
                "HINT_INLINE", PropertiesBuilder.build(new Property("algorithm-expression", "t_order_$->{value % 4}")));
        hintInlineShardingAlgorithmDefault = new HintInlineShardingAlgorithm();
        hintInlineShardingAlgorithmDefault.init(new Properties());
    }
    
    @Test
    void assertDoShardingWithSingleValueOfDefault() {
        List<String> availableTargetNames = Arrays.asList("t_order_0", "t_order_1", "t_order_2", "t_order_3");
        HintShardingValue<Comparable<?>> shardingValue = new HintShardingValue<>("t_order", "order_id", Collections.singleton("t_order_0"));
        Collection<String> actual = hintInlineShardingAlgorithmDefault.doSharding(availableTargetNames, shardingValue);
        assertTrue(actual.contains("t_order_0"));
    }
    
    @Test
    void assertDoShardingWithSingleValue() {
        List<String> availableTargetNames = Arrays.asList("t_order_0", "t_order_1", "t_order_2", "t_order_3");
        HintShardingValue<Comparable<?>> shardingValue = new HintShardingValue<>("t_order", "order_id", Collections.singleton(4));
        Collection<String> actual = hintInlineShardingAlgorithm.doSharding(availableTargetNames, shardingValue);
        assertTrue(actual.contains("t_order_0"));
    }
    
    @Test
    void assertDoShardingWithMultiValues() {
        List<String> availableTargetNames = Arrays.asList("t_order_0", "t_order_1", "t_order_2", "t_order_3");
        HintShardingValue<Comparable<?>> shardingValue = new HintShardingValue<>("t_order", "order_id", Arrays.asList(1, 2, 3, 4));
        Collection<String> actual = hintInlineShardingAlgorithm.doSharding(availableTargetNames, shardingValue);
        assertTrue(actual.containsAll(availableTargetNames));
    }
}
