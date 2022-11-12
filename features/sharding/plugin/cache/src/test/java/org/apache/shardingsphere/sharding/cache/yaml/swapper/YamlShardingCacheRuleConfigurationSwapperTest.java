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

package org.apache.shardingsphere.sharding.cache.yaml.swapper;

import org.apache.shardingsphere.sharding.cache.api.ShardingCacheOptions;
import org.apache.shardingsphere.sharding.cache.api.ShardingCacheRuleConfiguration;
import org.apache.shardingsphere.sharding.cache.yaml.YamlShardingCacheOptionsConfiguration;
import org.apache.shardingsphere.sharding.cache.yaml.YamlShardingCacheRuleConfiguration;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public final class YamlShardingCacheRuleConfigurationSwapperTest {
    
    @Test
    public void assertSwapToYamlConfiguration() {
        YamlShardingCacheRuleConfiguration actual = new YamlShardingCacheRuleConfigurationSwapper()
                .swapToYamlConfiguration(new ShardingCacheRuleConfiguration(100, new ShardingCacheOptions(true, 128, 1024)));
        assertThat(actual.getAllowedMaxSqlLength(), is(100));
        YamlShardingCacheOptionsConfiguration actualRouteCache = actual.getRouteCache();
        assertTrue(actualRouteCache.isSoftValues());
        assertThat(actualRouteCache.getInitialCapacity(), is(128));
        assertThat(actualRouteCache.getMaximumSize(), is(1024));
    }
    
    @Test
    public void assertSwapToObject() {
        YamlShardingCacheRuleConfiguration input = new YamlShardingCacheRuleConfiguration();
        input.setAllowedMaxSqlLength(200);
        YamlShardingCacheOptionsConfiguration yamlConfig = new YamlShardingCacheOptionsConfiguration();
        yamlConfig.setSoftValues(true);
        yamlConfig.setInitialCapacity(256);
        yamlConfig.setMaximumSize(4096);
        input.setRouteCache(yamlConfig);
        ShardingCacheRuleConfiguration actual = new YamlShardingCacheRuleConfigurationSwapper().swapToObject(input);
        assertThat(actual.getAllowedMaxSqlLength(), is(200));
        ShardingCacheOptions actualOptions = actual.getRouteCache();
        assertTrue(actualOptions.isSoftValues());
        assertThat(actualOptions.getInitialCapacity(), is(256));
        assertThat(actualOptions.getMaximumSize(), is(4096));
    }
    
    @Test
    public void assertGetTypeClass() {
        assertThat(new YamlShardingCacheRuleConfigurationSwapper().getTypeClass(), CoreMatchers.<Class<ShardingCacheRuleConfiguration>>is(ShardingCacheRuleConfiguration.class));
    }
    
    @Test
    public void assertGetRuleTagName() {
        assertThat(new YamlShardingCacheRuleConfigurationSwapper().getRuleTagName(), is("SHARDING_CACHE"));
    }
    
    @Test
    public void assertGetOrder() {
        assertThat(new YamlShardingCacheRuleConfigurationSwapper().getOrder(), is(-8));
    }
}
