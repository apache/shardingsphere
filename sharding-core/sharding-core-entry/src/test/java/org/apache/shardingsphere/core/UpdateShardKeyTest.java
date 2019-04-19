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

package org.apache.shardingsphere.core;

import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.InlineShardingStrategyConfiguration;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.metadata.ShardingMetaData;
import org.apache.shardingsphere.core.parse.cache.ParsingResultCache;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public final class UpdateShardKeyTest extends BaseShardingEngineTest {

    private BaseShardingEngine shardingEngine;

    public UpdateShardKeyTest() {
        super("update user set country_id = 1 where id = 100", Collections.emptyList());
    }

    @Before
    public void setUp() {
        shardingEngine = new SimpleQueryShardingEngine(createShardingRule(), getShardingProperties(), mock(ShardingMetaData.class), DatabaseType.MySQL, new ParsingResultCache());
    }

    private ShardingRule createShardingRule() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("country_id", "ds_${country_id % 2}"));
        Collection<String> dataSourceNames = Arrays.asList("ds_0", "ds_1");
        ShardingRule shardingRule = new ShardingRule(shardingRuleConfig, dataSourceNames);
        return shardingRule;
    }

    @Override
    protected void assertShard() {
    }

    @Test(expected = UnsupportedOperationException.class)
    public void assertUpdateShardKey() {
        shardingEngine.shard(getSql(), getParameters());
    }
}
