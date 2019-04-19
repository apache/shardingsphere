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
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.InlineShardingStrategyConfiguration;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.metadata.ShardingMetaData;
import org.apache.shardingsphere.core.parse.cache.ParsingResultCache;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public final class BaseShardingEngineTest extends AbstractShardingEngineTest {

    private BaseShardingEngine shardingEngine;

    public BaseShardingEngineTest() {
        super("update user set country_id = 1 where id = 100", Collections.emptyList());
    }

    private ShardingRule createRuleWithDefaultDatabaseStrategy() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("country_id", "ds_${country_id % 2}"));
        Collection<String> dataSourceNames = Arrays.asList("ds_0", "ds_1");
        ShardingRule shardingRule = new ShardingRule(shardingRuleConfig, dataSourceNames);
        return shardingRule;
    }

    private ShardingRule createRuleWithDefaultTableStrategy() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.setDefaultTableShardingStrategyConfig(new InlineShardingStrategyConfiguration("country_id", "user_$->{country_id % 2}"));
        Collection<String> dataSourceNames = Arrays.asList("ds");
        ShardingRule shardingRule = new ShardingRule(shardingRuleConfig, dataSourceNames);
        return shardingRule;
    }

    private ShardingRule createRuleWithTableRuleDatabaseStrategy() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration("user");
        tableRuleConfig.setDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("country_id", "ds_${country_id % 2}"));
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        Collection<String> dataSourceNames = Arrays.asList("ds_0", "ds_1");
        ShardingRule shardingRule = new ShardingRule(shardingRuleConfig, dataSourceNames);
        return shardingRule;
    }

    private ShardingRule createRuleWithTableRuleTableStrategy() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration("user");
        tableRuleConfig.setTableShardingStrategyConfig(new InlineShardingStrategyConfiguration("country_id", "user_$->{country_id % 2}"));
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        Collection<String> dataSourceNames = Arrays.asList("ds");
        ShardingRule shardingRule = new ShardingRule(shardingRuleConfig, dataSourceNames);
        return shardingRule;
    }

    @Override
    protected void assertShard() {
    }

    @Test(expected = UnsupportedOperationException.class)
    public void assertUpdateShardKeyDefaultDatabaseStrategy() {
        shardingEngine = new SimpleQueryShardingEngine(createRuleWithDefaultDatabaseStrategy(), getShardingProperties(), mock(ShardingMetaData.class), DatabaseType.MySQL, new ParsingResultCache());
        shardingEngine.shard(getSql(), getParameters());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void assertUpdateShardKeyDefaultTableStrategy() {
        shardingEngine = new SimpleQueryShardingEngine(createRuleWithDefaultTableStrategy(), getShardingProperties(), mock(ShardingMetaData.class), DatabaseType.MySQL, new ParsingResultCache());
        shardingEngine.shard(getSql(), getParameters());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void assertUpdateShardKeyTableRuleDatabaseStrategy() {
        shardingEngine = new SimpleQueryShardingEngine(createRuleWithTableRuleDatabaseStrategy(), getShardingProperties(), mock(ShardingMetaData.class), DatabaseType.MySQL, new ParsingResultCache());
        shardingEngine.shard(getSql(), getParameters());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void assertUpdateShardKeyTableRuleTableStrategy() {
        shardingEngine = new SimpleQueryShardingEngine(createRuleWithTableRuleTableStrategy(), getShardingProperties(), mock(ShardingMetaData.class), DatabaseType.MySQL, new ParsingResultCache());
        shardingEngine.shard(getSql(), getParameters());
    }
}
