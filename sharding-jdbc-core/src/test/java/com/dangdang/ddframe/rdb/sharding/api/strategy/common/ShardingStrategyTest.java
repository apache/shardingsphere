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

package com.dangdang.ddframe.rdb.sharding.api.strategy.common;

import com.dangdang.ddframe.rdb.sharding.api.strategy.PreciseShardingValue;
import com.dangdang.ddframe.rdb.sharding.api.strategy.RangeShardingValue;
import com.dangdang.ddframe.rdb.sharding.api.strategy.ShardingValue;
import com.dangdang.ddframe.rdb.sharding.api.strategy.fixture.TestComplexKeysShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.strategy.fixture.TestPreciseShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.strategy.fixture.TestRangeShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.routing.strategy.complex.ComplexShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.routing.strategy.none.NoneShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.routing.strategy.standard.StandardShardingStrategy;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ShardingStrategyTest {
    
    private final Collection<String> targets = Sets.newHashSet("1", "2", "3");
    
    @Test
    public void assertDoStaticShardingWithoutShardingColumns() {
        NoneShardingStrategy strategy = new NoneShardingStrategy();
        assertThat(strategy.doSharding(targets, Collections.<ShardingValue>emptySet()), is(targets));
    }
    
    @Test
    public void assertDoStaticShardingForBetweenSingleKey() {
        StandardShardingStrategy strategy = new StandardShardingStrategy("column", new TestPreciseShardingAlgorithm(), new TestRangeShardingAlgorithm());
        assertThat(strategy.doSharding(targets, Collections.<ShardingValue>singletonList(new RangeShardingValue<>("logicTable", "column", Range.open("1", "3")))), 
                is((Collection<String>) Sets.newHashSet("1", "2", "3")));
    }
    
    @Test
    public void assertDoStaticShardingForMultipleKeys() {
        ComplexShardingStrategy strategy = new ComplexShardingStrategy(Collections.singletonList("column"), new TestComplexKeysShardingAlgorithm());
        assertThat(strategy.doSharding(targets, Collections.<ShardingValue>singletonList(new PreciseShardingValue<>("logicTable", "column", "1"))), 
                is((Collection<String>) Sets.newHashSet("1", "2", "3")));
    }
    
    @Test
    public void assertDoDynamicShardingForBetweenSingleKey() {
        StandardShardingStrategy strategy = new StandardShardingStrategy("column", new TestPreciseShardingAlgorithm(), new TestRangeShardingAlgorithm());
        assertThat(strategy.doSharding(Collections.<String>emptyList(), Collections.<ShardingValue>singletonList(new RangeShardingValue<>("logicTable", "column", Range.open("1", "3")))), 
                is((Collection<String>) Sets.newHashSet("1", "2", "3")));
    }
}
