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

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import org.apache.shardingsphere.infra.datanode.DataNodeInfo;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.exception.algorithm.sharding.MismatchedInlineShardingAlgorithmExpressionAndColumnException;
import org.apache.shardingsphere.sharding.exception.algorithm.sharding.ShardingAlgorithmInitializationException;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class InlineShardingAlgorithmTest {
    
    private static final DataNodeInfo DATA_NODE_INFO = new DataNodeInfo("t_order_", 1, '0');
    
    private InlineShardingAlgorithm inlineShardingAlgorithm;
    
    private InlineShardingAlgorithm inlineShardingAlgorithmWithSimplified;
    
    private InlineShardingAlgorithm negativeNumberInlineShardingAlgorithm;
    
    @BeforeEach
    void setUp() {
        inlineShardingAlgorithm = (InlineShardingAlgorithm) TypedSPILoader.getService(ShardingAlgorithm.class, "INLINE",
                PropertiesBuilder.build(new Property("algorithm-expression", "t_order_$->{order_id % 4}"), new Property("allow-range-query-with-inline-sharding", Boolean.TRUE.toString())));
        inlineShardingAlgorithmWithSimplified = (InlineShardingAlgorithm) TypedSPILoader.getService(ShardingAlgorithm.class, "INLINE",
                PropertiesBuilder.build(new Property("algorithm-expression", "t_order_${order_id % 4}")));
        negativeNumberInlineShardingAlgorithm = (InlineShardingAlgorithm) TypedSPILoader.getService(ShardingAlgorithm.class, "INLINE",
                PropertiesBuilder.build(new Property("algorithm-expression", "t_order_$->{(order_id % 4).abs()}"), new Property("allow-range-query-with-inline-sharding", Boolean.TRUE.toString())));
    }
    
    @Test
    void assertInitWithNullClass() {
        assertThrows(ShardingAlgorithmInitializationException.class,
                () -> TypedSPILoader.getService(ShardingAlgorithm.class, "INLINE", PropertiesBuilder.build(new Property("wrong", ""))));
    }
    
    @Test
    void assertInitWithEmptyClassName() {
        assertThrows(ShardingAlgorithmInitializationException.class,
                () -> TypedSPILoader.getService(ShardingAlgorithm.class, "INLINE", PropertiesBuilder.build(new Property("algorithm-expression", ""))));
    }
    
    @Test
    void assertDoSharding() {
        List<String> availableTargetNames = Arrays.asList("t_order_0", "t_order_1", "t_order_2", "t_order_3");
        assertThat(inlineShardingAlgorithm.doSharding(availableTargetNames, new PreciseShardingValue<>("t_order", "order_id", DATA_NODE_INFO, 0)), is("t_order_0"));
        assertThrows(MismatchedInlineShardingAlgorithmExpressionAndColumnException.class,
                () -> inlineShardingAlgorithm.doSharding(availableTargetNames, new PreciseShardingValue<>("t_order", "non_existent_column1", DATA_NODE_INFO, 0)));
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void assertDoShardingWithRangeShardingConditionValue() {
        List<String> availableTargetNames = Arrays.asList("t_order_0", "t_order_1", "t_order_2", "t_order_3");
        Collection<String> actual = inlineShardingAlgorithm.doSharding(availableTargetNames, new RangeShardingValue<>("t_order", "order_id", DATA_NODE_INFO, mock(Range.class)));
        assertTrue(actual.containsAll(availableTargetNames));
    }
    
    @Test
    void assertDoShardingWithNonExistNodes() {
        List<String> availableTargetNames = Arrays.asList("t_order_0", "t_order_1");
        assertThat(inlineShardingAlgorithm.doSharding(availableTargetNames, new PreciseShardingValue<>("t_order", "order_id", DATA_NODE_INFO, 0)), is("t_order_0"));
        assertThat(inlineShardingAlgorithmWithSimplified.doSharding(availableTargetNames, new PreciseShardingValue<>("t_order", "order_id", DATA_NODE_INFO, 0)), is("t_order_0"));
    }
    
    @Test
    void assertDoShardingWithNegative() {
        List<String> availableTargetNames = Lists.newArrayList("t_order_0", "t_order_1", "t_order_2", "t_order_3");
        assertThat(negativeNumberInlineShardingAlgorithm.doSharding(availableTargetNames, new PreciseShardingValue<>("t_order", "order_id", DATA_NODE_INFO, -1)), is("t_order_1"));
        assertThat(negativeNumberInlineShardingAlgorithm.doSharding(availableTargetNames, new PreciseShardingValue<>("t_order", "order_id", DATA_NODE_INFO, -4)), is("t_order_0"));
    }
    
    @Test
    void assertDoShardingWithLargeValues() {
        List<String> availableTargetNames = Lists.newArrayList("t_order_0", "t_order_1", "t_order_2", "t_order_3");
        assertThat(inlineShardingAlgorithm.doSharding(availableTargetNames,
                new PreciseShardingValue<>("t_order", "order_id", DATA_NODE_INFO, 787694822390497280L)), is("t_order_0"));
        assertThat(inlineShardingAlgorithm.doSharding(availableTargetNames,
                new PreciseShardingValue<>("t_order", "order_id", DATA_NODE_INFO, new BigInteger("787694822390497280787694822390497280"))), is("t_order_0"));
        assertThat(inlineShardingAlgorithmWithSimplified.doSharding(availableTargetNames,
                new PreciseShardingValue<>("t_order", "order_id", DATA_NODE_INFO, 787694822390497280L)), is("t_order_0"));
        assertThat(inlineShardingAlgorithmWithSimplified.doSharding(availableTargetNames,
                new PreciseShardingValue<>("t_order", "order_id", DATA_NODE_INFO, new BigInteger("787694822390497280787694822390497280"))), is("t_order_0"));
    }
}
