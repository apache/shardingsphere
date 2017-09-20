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

package io.shardingjdbc.core.api.algorithm.common;

import io.shardingjdbc.core.api.algorithm.sharding.ShardingValue;
import io.shardingjdbc.core.api.algorithm.sharding.PreciseShardingValue;
import io.shardingjdbc.core.api.algorithm.sharding.RangeShardingValue;
import io.shardingjdbc.core.api.algorithm.fixture.TestComplexKeysShardingAlgorithm;
import io.shardingjdbc.core.api.algorithm.fixture.TestPreciseShardingAlgorithm;
import io.shardingjdbc.core.api.algorithm.fixture.TestRangeShardingAlgorithm;
import io.shardingjdbc.core.routing.strategy.complex.ComplexShardingStrategy;
import io.shardingjdbc.core.routing.strategy.none.NoneShardingStrategy;
import io.shardingjdbc.core.routing.strategy.standard.StandardShardingStrategy;
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
    public void assertDoShardingWithoutShardingColumns() {
        NoneShardingStrategy strategy = new NoneShardingStrategy();
        assertThat(strategy.doSharding(targets, Collections.<ShardingValue>emptySet()), is(targets));
    }
    
    @Test
    public void assertDoShardingForBetweenSingleKey() {
        StandardShardingStrategy strategy = new StandardShardingStrategy("column", new TestPreciseShardingAlgorithm(), new TestRangeShardingAlgorithm());
        assertThat(strategy.doSharding(targets, Collections.<ShardingValue>singletonList(new RangeShardingValue<>("logicTable", "column", Range.open("1", "3")))), 
                is((Collection<String>) Sets.newHashSet("1", "2", "3")));
    }
    
    @Test
    public void assertDoShardingForMultipleKeys() {
        ComplexShardingStrategy strategy = new ComplexShardingStrategy(Collections.singletonList("column"), new TestComplexKeysShardingAlgorithm());
        assertThat(strategy.doSharding(targets, Collections.<ShardingValue>singletonList(new PreciseShardingValue<>("logicTable", "column", "1"))), 
                is((Collection<String>) Sets.newHashSet("1", "2", "3")));
    }
}
