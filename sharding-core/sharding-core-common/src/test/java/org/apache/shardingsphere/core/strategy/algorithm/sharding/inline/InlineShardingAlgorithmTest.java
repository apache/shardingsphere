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

package org.apache.shardingsphere.core.strategy.algorithm.sharding.inline;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.api.config.sharding.strategy.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.core.strategy.route.standard.StandardShardingStrategy;
import org.apache.shardingsphere.core.strategy.route.value.ListRouteValue;
import org.apache.shardingsphere.core.strategy.route.value.RouteValue;
import org.apache.shardingsphere.underlying.common.config.properties.ConfigurationProperties;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class InlineShardingAlgorithmTest {

    private StandardShardingStrategy shardingStrategy;

    private StandardShardingStrategy shardingStrategyWithSimplified;

    @Before
    public void setUp() {
        InlineShardingAlgorithm shardingAlgorithm = new InlineShardingAlgorithm();
        shardingAlgorithm.getProperties().setProperty("algorithm.expression", "t_order_$->{order_id % 4}");
        StandardShardingStrategyConfiguration shardingStrategyConfig = new StandardShardingStrategyConfiguration("order_id", shardingAlgorithm);
        shardingStrategy = new StandardShardingStrategy(shardingStrategyConfig);
        InlineShardingAlgorithm shardingAlgorithmWithSimplified = new InlineShardingAlgorithm();
        shardingAlgorithmWithSimplified.getProperties().setProperty("algorithm.expression", "t_order_${order_id % 4}");
        StandardShardingStrategyConfiguration shardingStrategyConfigWithSimplified = new StandardShardingStrategyConfiguration("order_id", shardingAlgorithmWithSimplified);
        shardingStrategyWithSimplified = new StandardShardingStrategy(shardingStrategyConfigWithSimplified);
    }

    @Test
    public void assertDoSharding() {
        List<String> availableTargetNames = Lists.newArrayList("t_order_0", "t_order_1", "t_order_2", "t_order_3");
        List<RouteValue> shardingValues = Lists.newArrayList(new ListRouteValue<>("order_id", "t_order", Lists.newArrayList(0, 1, 2, 3)));
        Collection<String> actual = shardingStrategy.doSharding(availableTargetNames, shardingValues, new ConfigurationProperties(new Properties()));
        assertThat(actual.size(), is(4));
        Collection<String> actualWithSimplified = shardingStrategyWithSimplified.doSharding(availableTargetNames, shardingValues, new ConfigurationProperties(new Properties()));
        assertThat(actualWithSimplified.size(), is(4));
    }

    @Test
    public void assertDoShardingWithNonExistNodes() {
        List<String> availableTargetNames = Lists.newArrayList("t_order_0", "t_order_1");
        List<RouteValue> shardingValues = Lists.newArrayList(new ListRouteValue<>("order_id", "t_order", Lists.newArrayList(0, 1, 2, 3)));
        Collection<String> actual = shardingStrategy.doSharding(availableTargetNames, shardingValues, new ConfigurationProperties(new Properties()));
        assertThat(actual.size(), is(2));
        Collection<String> actualWithSimplified = shardingStrategyWithSimplified.doSharding(availableTargetNames, shardingValues, new ConfigurationProperties(new Properties()));
        assertThat(actualWithSimplified.size(), is(2));
    }

    @Test
    public void assertGetShardingColumns() {
        assertThat(shardingStrategy.getShardingColumns().size(), is(1));
        assertThat(shardingStrategy.getShardingColumns().iterator().next(), is("order_id"));
        assertThat(shardingStrategyWithSimplified.getShardingColumns().size(), is(1));
        assertThat(shardingStrategyWithSimplified.getShardingColumns().iterator().next(), is("order_id"));
    }
}
