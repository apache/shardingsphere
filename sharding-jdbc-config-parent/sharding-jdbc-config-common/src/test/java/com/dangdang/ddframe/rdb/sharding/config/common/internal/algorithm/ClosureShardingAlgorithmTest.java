/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.config.common.internal.algorithm;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.dangdang.ddframe.rdb.sharding.api.ShardingValue;
import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

public class ClosureShardingAlgorithmTest {
    
    private static ClosureShardingAlgorithm algorithm;
    
    @BeforeClass
    public static void initAlgorithm() {
        algorithm = new ClosureDatabaseShardingAlgorithm("t_order_${log.info(id.toString()); id.longValue() % 2}", "default");
    }
    
    @Test
    public void testEqual() {
        Collection<String> result = algorithm.doSharding(Collections.singletonList("t_order_1"), Collections.<ShardingValue<?>>singletonList(new ShardingValue<>("id", 1L)));
        assertThat(result.size(), is(1));
        assertThat(result, hasItem("t_order_1"));
    }
    
    @Test
    public void testIn() {
        Collection<String> result = algorithm.doSharding(Arrays.asList("t_order_0", "t_order_1"), Collections.<ShardingValue<?>>singletonList(new ShardingValue<>("id", Arrays.asList(1, 2))));
        assertThat(result.size(), is(2));
        assertThat(result, hasItem("t_order_0"));
        assertThat(result, hasItem("t_order_1"));
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void testBetween() {
        algorithm.doSharding(Arrays.asList("t_order_0", "t_order_1"), Collections.<ShardingValue<?>>singletonList(new ShardingValue<>("id", Range.range(1, BoundType.CLOSED, 2, BoundType.OPEN))));
    }
    
    @Test(expected = NullPointerException.class)
    public void testNoShardingValue() {
        algorithm.doSharding(Collections.singletonList("t_order_no"), Collections.<ShardingValue<?>>emptyList());
    }
}
