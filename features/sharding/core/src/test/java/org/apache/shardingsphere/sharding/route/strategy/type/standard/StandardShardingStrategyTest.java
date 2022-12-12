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
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.datanode.DataNodeInfo;
import org.apache.shardingsphere.sharding.fixture.CoreStandardShardingAlgorithmFixture;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ListShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.RangeShardingConditionValue;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class StandardShardingStrategyTest {
    
    private final Collection<String> targets = new HashSet<>(Arrays.asList("1", "2", "3"));
    
    private StandardShardingStrategy standardShardingStrategy;
    
    private DataNodeInfo dataNodeSegment;
    
    @Before
    public void setUp() {
        standardShardingStrategy = new StandardShardingStrategy("column", new CoreStandardShardingAlgorithmFixture());
        dataNodeSegment = new DataNodeInfo("logicTable_", 1, '0');
    }
    
    @Test
    public void assertDoShardingForRangeSharding() {
        Collection<String> actualRangeSharding = standardShardingStrategy.doSharding(targets, Collections.singletonList(
                new RangeShardingConditionValue<>("column", "logicTable", Range.open(1, 3))), dataNodeSegment, new ConfigurationProperties(new Properties()));
        assertThat(actualRangeSharding.size(), is(1));
        assertThat(actualRangeSharding.iterator().next(), is("1"));
    }
    
    @Test
    public void assertDoShardingForListSharding() {
        Collection<String> actualListSharding = standardShardingStrategy.doSharding(targets, Collections.singletonList(
                new ListShardingConditionValue<>("column", "logicTable", Collections.singletonList(1))), dataNodeSegment, new ConfigurationProperties(new Properties()));
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
