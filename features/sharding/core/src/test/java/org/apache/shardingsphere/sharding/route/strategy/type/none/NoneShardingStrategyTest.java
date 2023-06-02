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

package org.apache.shardingsphere.sharding.route.strategy.type.none;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.datanode.DataNodeInfo;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

class NoneShardingStrategyTest {
    
    private final NoneShardingStrategy noneShardingStrategy = new NoneShardingStrategy();
    
    @Test
    void assertGetShardingAlgorithm() {
        Collection<String> targets = new HashSet<>(Arrays.asList("1", "2", "3"));
        DataNodeInfo dataNodeInfo = new DataNodeInfo("logicTable_", 1, '0');
        Collection<String> actualSharding = noneShardingStrategy.doSharding(targets, Collections.emptySet(), dataNodeInfo, new ConfigurationProperties(new Properties()));
        assertThat(actualSharding.size(), is(3));
        assertThat(actualSharding, is(targets));
    }
    
    @Test
    void assertDoSharding() {
        assertNull(noneShardingStrategy.getShardingAlgorithm());
    }
}
