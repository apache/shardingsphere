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

package org.apache.shardingsphere.sharding.strategy;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.sharding.strategy.fixture.StandardShardingAlgorithmFixture;
import org.apache.shardingsphere.sharding.strategy.complex.ComplexShardingStrategy;
import org.apache.shardingsphere.sharding.strategy.fixture.ComplexKeysShardingAlgorithmFixture;
import org.apache.shardingsphere.sharding.strategy.none.NoneShardingStrategy;
import org.apache.shardingsphere.sharding.strategy.standard.StandardShardingStrategy;
import org.apache.shardingsphere.sharding.strategy.value.ListRouteValue;
import org.apache.shardingsphere.sharding.strategy.value.RangeRouteValue;
import org.apache.shardingsphere.sharding.strategy.value.RouteValue;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ShardingStrategyTest {
    
    private final Collection<String> targets = Sets.newHashSet("1", "2", "3");
    
    @Test
    public void assertDoShardingWithoutShardingColumns() {
        NoneShardingStrategy strategy = new NoneShardingStrategy();
        assertThat(strategy.doSharding(targets, Collections.emptySet(), new ConfigurationProperties(new Properties())), is(targets));
    }
    
    @Test
    public void assertDoShardingForBetweenSingleKey() {
        StandardShardingStrategy strategy = new StandardShardingStrategy("column", new StandardShardingAlgorithmFixture());
        assertThat(strategy.doSharding(targets, Collections.singletonList(new RangeRouteValue<>("column", "logicTable", Range.open(1, 3))), new ConfigurationProperties(new Properties())),
                is(Sets.newHashSet("1")));
    }
    
    @Test
    public void assertDoShardingForMultipleKeys() {
        ComplexShardingStrategy strategy = new ComplexShardingStrategy("column1, column2", new ComplexKeysShardingAlgorithmFixture());
        List<RouteValue> routeValues = Lists.newArrayList(
                new ListRouteValue<>("column1", "logicTable", Collections.singletonList(1)), new RangeRouteValue<>("column2", "logicTable", Range.open(1, 3)));
        assertThat(strategy.doSharding(targets, routeValues, new ConfigurationProperties(new Properties())), is(Sets.newHashSet("1", "2", "3")));
    }
}
