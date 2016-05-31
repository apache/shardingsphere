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

import com.dangdang.ddframe.rdb.sharding.api.ShardingValue;
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

public abstract class AbstractClosureShardingAlgorithmTest {
    
    protected static final String EXPRESSION = "target_${log.info(id.toString()); id.longValue() % 2}";
    
    protected static final String WRONG_EXPRESSION = "target_${log.info(id.error());}";
    
    protected static final String LOG_ROOT = "default";
    
    protected abstract ClosureShardingAlgorithm createClosureShardingAlgorithm();
    
    protected abstract ClosureShardingAlgorithm createErrorClosureShardingAlgorithm();
    
    @Test
    public void assertEqual() {
        Collection<String> result = createClosureShardingAlgorithm().doSharding(
                Collections.singletonList("target_1"), Collections.<ShardingValue<?>>singletonList(new ShardingValue<>("target", "id", 1L)));
        assertThat(result.size(), is(1));
        assertThat(result, hasItem("target_1"));
    }
    
    @Test
    public void assertIn() {
        Collection<String> result = createClosureShardingAlgorithm().doSharding(Arrays.asList("target_0", "target_1"), 
                Collections.<ShardingValue<?>>singletonList(new ShardingValue<>("target", "id", Arrays.asList(1, 2))));
        assertThat(result.size(), is(2));
        assertThat(result, hasItem("target_0"));
        assertThat(result, hasItem("target_1"));
    }
        
    @Test(expected = UnsupportedOperationException.class)
    public void assertBetween() {
        createClosureShardingAlgorithm().doSharding(Arrays.asList("target_0", "target_1"), 
                Collections.<ShardingValue<?>>singletonList(new ShardingValue<>("target", "id", Range.range(1, BoundType.CLOSED, 2, BoundType.OPEN))));
    }
    
    @Test(expected = MissingMethodException.class)
    public void assertEvaluateInlineExpressionFailure() {
        createErrorClosureShardingAlgorithm().doSharding(Collections.singletonList("target_1"), Collections.<ShardingValue<?>>singletonList(new ShardingValue<>("target", "id", 1L)));
    }
}
