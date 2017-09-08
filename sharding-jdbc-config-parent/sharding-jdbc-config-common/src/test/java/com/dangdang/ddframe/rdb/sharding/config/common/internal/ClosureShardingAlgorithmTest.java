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

package com.dangdang.ddframe.rdb.sharding.config.common.internal;

import com.dangdang.ddframe.rdb.sharding.api.strategy.ListShardingValue;
import com.dangdang.ddframe.rdb.sharding.api.strategy.RangeShardingValue;
import com.dangdang.ddframe.rdb.sharding.api.strategy.ShardingValue;
import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import groovy.lang.MissingMethodException;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

public final class ClosureShardingAlgorithmTest {
    
    private static final String EXPRESSION = "target_${log.info(id.toString()); id % 2}";
    
    private static final String WRONG_EXPRESSION = "target_${log.info(id.error());}";
    
    private static final String LOG_ROOT = "default";
    
    @Test
    public void assertEqual() {
        Collection<String> result = new ClosureShardingAlgorithm(EXPRESSION, LOG_ROOT).doSharding(
                Collections.singletonList("target_1"), Collections.<ShardingValue>singletonList(new ListShardingValue<>("target", "id", Collections.singletonList(1L))));
        assertThat(result.size(), is(1));
        assertThat(result, hasItem("target_1"));
    }
    
    @Test
    public void assertIn() {
        Collection<String> result = new ClosureShardingAlgorithm(EXPRESSION, LOG_ROOT).doSharding(Arrays.asList("target_0", "target_1"), 
                Collections.<ShardingValue>singletonList(new ListShardingValue<>("target", "id", Arrays.asList(1, 2))));
        assertThat(result.size(), is(2));
        assertThat(result, hasItem("target_0"));
        assertThat(result, hasItem("target_1"));
    }
        
    @Test(expected = UnsupportedOperationException.class)
    public void assertBetween() {
        new ClosureShardingAlgorithm(EXPRESSION, LOG_ROOT).doSharding(Arrays.asList("target_0", "target_1"), 
                Collections.<ShardingValue>singletonList(new RangeShardingValue<>("target", "id", Range.range(1, BoundType.CLOSED, 2, BoundType.OPEN))));
    }
    
    @Test(expected = MissingMethodException.class)
    public void assertEvaluateInlineExpressionFailure() {
        new ClosureShardingAlgorithm(WRONG_EXPRESSION, LOG_ROOT).doSharding(
                Collections.singletonList("target_1"), Collections.<ShardingValue>singletonList(new ListShardingValue<>("target", "id", Collections.singletonList(1L))));
    }
}
