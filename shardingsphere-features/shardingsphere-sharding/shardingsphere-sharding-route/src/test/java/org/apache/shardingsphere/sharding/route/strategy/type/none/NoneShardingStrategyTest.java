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

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class NoneShardingStrategyTest {
    
    private NoneShardingStrategy noneShardingStrategy;
    
    @Before
    public void setUp() {
        noneShardingStrategy = new NoneShardingStrategy();
    }
    
    @Test
    public void assertGetShardingAlgorithm() {
        Collection<String> targets = Sets.newHashSet("1", "2", "3");
        Collection<String> actualSharding = noneShardingStrategy.doSharding(targets, Collections.emptySet(), new ConfigurationProperties(new Properties()));
        assertThat(actualSharding.size(), is(3));
        assertThat(actualSharding, is(targets));
    }
    
    @Test
    public void assertDoSharding() {
        assertNull(noneShardingStrategy.getShardingAlgorithm());
    }
}
