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

package org.apache.shardingsphere.sharding.yaml.swapper.cache;

import org.apache.shardingsphere.sharding.api.config.cache.ShardingCacheOptionsConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.cache.YamlShardingCacheOptionsConfiguration;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlShardingCacheOptionsConfigurationSwapperTest {
    
    @Test
    void assertSwapToYamlConfiguration() {
        YamlShardingCacheOptionsConfiguration actual = new YamlShardingCacheOptionsConfigurationSwapper().swapToYamlConfiguration(new ShardingCacheOptionsConfiguration(true, 128, 1024));
        assertTrue(actual.isSoftValues());
        assertThat(actual.getInitialCapacity(), is(128));
        assertThat(actual.getMaximumSize(), is(1024));
    }
    
    @Test
    void assertSwapToObject() {
        YamlShardingCacheOptionsConfiguration input = new YamlShardingCacheOptionsConfiguration();
        input.setSoftValues(true);
        input.setInitialCapacity(256);
        input.setMaximumSize(4096);
        ShardingCacheOptionsConfiguration actual = new YamlShardingCacheOptionsConfigurationSwapper().swapToObject(input);
        assertTrue(actual.isSoftValues());
        assertThat(actual.getInitialCapacity(), is(256));
        assertThat(actual.getMaximumSize(), is(4096));
    }
}
