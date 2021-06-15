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
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class InlineShardingAlgorithmTest {
    
    private InlineShardingAlgorithm inlineShardingAlgorithm;
    
    private InlineShardingAlgorithm inlineShardingAlgorithmWithSimplified;
    
    @Before
    public void setUp() {
        initInlineShardingAlgorithm();
        initInlineShardingAlgorithmWithSimplified();
    }
    
    private void initInlineShardingAlgorithm() {
        inlineShardingAlgorithm = new InlineShardingAlgorithm();
        inlineShardingAlgorithm.getProps().setProperty("algorithm-expression", "t_order_$->{order_id % 4}");
        inlineShardingAlgorithm.getProps().setProperty("allow-range-query-with-inline-sharding", "true");
        inlineShardingAlgorithm.init();
    }
    
    private void initInlineShardingAlgorithmWithSimplified() {
        inlineShardingAlgorithmWithSimplified = new InlineShardingAlgorithm();
        inlineShardingAlgorithmWithSimplified.getProps().setProperty("algorithm-expression", "t_order_${order_id % 4}");
        inlineShardingAlgorithmWithSimplified.init();
    }
    
    @Test
    public void assertDoSharding() {
        List<String> availableTargetNames = Lists.newArrayList("t_order_0", "t_order_1", "t_order_2", "t_order_3");
        assertThat(inlineShardingAlgorithm.doSharding(availableTargetNames, new PreciseShardingValue<>("t_order", "order_id", 0)), is("t_order_0"));
    }
    
    @Test
    public void assertDoShardingWithRangeShardingConditionValue() {
        List<String> availableTargetNames = Lists.newArrayList("t_order_0", "t_order_1", "t_order_2", "t_order_3");
        Collection<String> actual = inlineShardingAlgorithm.doSharding(availableTargetNames, new RangeShardingValue<>("t_order", "order_id", mock(Range.class)));
        assertTrue(actual.containsAll(availableTargetNames));
    }
    
    @Test
    public void assertDoShardingWithNonExistNodes() {
        List<String> availableTargetNames = Lists.newArrayList("t_order_0", "t_order_1");
        assertThat(inlineShardingAlgorithm.doSharding(availableTargetNames, new PreciseShardingValue<>("t_order", "order_id", 0)), is("t_order_0"));
        assertThat(inlineShardingAlgorithmWithSimplified.doSharding(availableTargetNames, new PreciseShardingValue<>("t_order", "order_id", 0)), is("t_order_0"));
    }
}
