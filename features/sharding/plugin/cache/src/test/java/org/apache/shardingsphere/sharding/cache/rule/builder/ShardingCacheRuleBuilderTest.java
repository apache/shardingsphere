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

package org.apache.shardingsphere.sharding.cache.rule.builder;

import org.apache.shardingsphere.infra.rule.identifier.scope.DatabaseRule;
import org.apache.shardingsphere.sharding.cache.api.ShardingCacheOptions;
import org.apache.shardingsphere.sharding.cache.api.ShardingCacheRuleConfiguration;
import org.apache.shardingsphere.sharding.cache.rule.ShardingCacheRule;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

public final class ShardingCacheRuleBuilderTest {
    
    @Test
    public void assertBuildShardingCacheRule() {
        ShardingRule expectedShardingRule = mock(ShardingRule.class);
        ShardingCacheRuleConfiguration expectedConfig = new ShardingCacheRuleConfiguration(100, new ShardingCacheOptions(true, 1, 1));
        DatabaseRule actual = new ShardingCacheRuleBuilder().build(expectedConfig, "", Collections.emptyMap(), Collections.singletonList(expectedShardingRule), null);
        assertThat(actual, instanceOf(ShardingCacheRule.class));
        ShardingCacheRule actualShardingCacheRule = (ShardingCacheRule) actual;
        assertThat(actualShardingCacheRule.getConfiguration(), is(expectedConfig));
        assertThat(actualShardingCacheRule.getShardingRule(), is(expectedShardingRule));
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertBuildShardingCacheRuleWithoutShardingRule() {
        new ShardingCacheRuleBuilder().build(null, "", Collections.emptyMap(), Collections.emptyList(), null);
    }
    
    @Test
    public void assertGetOrder() {
        assertThat(new ShardingCacheRuleBuilder().getOrder(), is(-8));
    }
    
    @Test
    public void assertGetTypeClass() {
        assertThat(new ShardingCacheRuleBuilder().getTypeClass(), CoreMatchers.<Class<ShardingCacheRuleConfiguration>>is(ShardingCacheRuleConfiguration.class));
    }
}
