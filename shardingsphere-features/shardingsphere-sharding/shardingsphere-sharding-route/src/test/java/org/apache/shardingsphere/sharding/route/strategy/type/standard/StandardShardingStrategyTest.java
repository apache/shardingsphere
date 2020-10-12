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

package org.apache.shardingsphere.sharding.route.strategy.type.standard;

import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ListShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.RangeShardingConditionValue;
import org.apache.shardingsphere.sharding.route.strategy.fixture.StandardShardingAlgorithmFixture;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class StandardShardingStrategyTest {
    
    private final Collection<String> targets = Sets.newHashSet("1", "2", "3");
    
    private StandardShardingStrategy standardShardingStrategy;
    
    @Before
    public void setUp() {
        standardShardingStrategy = new StandardShardingStrategy("column", new StandardShardingAlgorithmFixture());
    }
    
    @Test
    public void assertDoShardingForRangeSharding() {
        Collection<String> actualRangeSharding = standardShardingStrategy
            .doSharding(targets, Collections.singletonList(new RangeShardingConditionValue<>("column", "logicTable", Range.open(1, 3))), new ConfigurationProperties(new Properties()));
        assertThat(actualRangeSharding.size(), is(1));
        assertThat(actualRangeSharding.iterator().next(), is("1"));
    }
    
    @Test
    public void assertDoShardingForListSharding() {
        Collection<String> actualListSharding = standardShardingStrategy
            .doSharding(targets, Collections.singletonList(new ListShardingConditionValue<>("column", "logicTable", Collections.singletonList(1))), new ConfigurationProperties(new Properties()));
        assertThat(actualListSharding.size(), is(1));
        assertThat(actualListSharding.iterator().next(), is("1"));
    }
    
    @Test
    public void assertGetShardingColumns() {
        Collection<String> actualShardingColumns = standardShardingStrategy.getShardingColumns();
        assertThat(actualShardingColumns.size(), is(1));
        assertThat(actualShardingColumns.iterator().next(), is("column"));
    }
}
