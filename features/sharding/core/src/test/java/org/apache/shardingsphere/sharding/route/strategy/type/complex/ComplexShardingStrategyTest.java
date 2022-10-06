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

package org.apache.shardingsphere.sharding.route.strategy.type.complex;

import com.google.common.collect.Range;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.datanode.DataNodeInfo;
import org.apache.shardingsphere.sharding.fixture.CoreComplexKeysShardingAlgorithmFixture;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ListShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.RangeShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ShardingConditionValue;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class ComplexShardingStrategyTest {
    
    private static final DataNodeInfo DATA_NODE_INFO = new DataNodeInfo("logicTable_", 1, '0');
    
    @Test
    public void assertDoSharding() {
        Collection<String> targets = new HashSet<>(Arrays.asList("1", "2", "3"));
        ComplexShardingStrategy complexShardingStrategy = new ComplexShardingStrategy("column1, column2", new CoreComplexKeysShardingAlgorithmFixture());
        List<ShardingConditionValue> shardingConditionValues =
                Arrays.asList(new ListShardingConditionValue<>("column1", "logicTable", Collections.singletonList(1)), new RangeShardingConditionValue<>("column2", "logicTable", Range.open(1, 3)));
        Collection<String> actualSharding = complexShardingStrategy.doSharding(targets, shardingConditionValues, DATA_NODE_INFO, new ConfigurationProperties(new Properties()));
        assertThat(actualSharding.size(), is(3));
        assertThat(actualSharding, is(targets));
    }
}
