/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.config.common.api;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.config.common.api.config.ShardingRuleConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@Slf4j
public class ShardingRuleBuilderTest {
    
    @Test
    public void testAll() {
        Yaml yaml = new Yaml(new Constructor(ShardingRuleConfig.class));
        ShardingRuleConfig config = (ShardingRuleConfig) yaml.load(ShardingRuleBuilderTest.class.getResourceAsStream("/config/config-all.yaml"));
        ShardingRule shardingRule = new ShardingRuleBuilder("config-all.yaml", config).build();
        assertThat(shardingRule.getTableRules().size(), is(3));
        assertThat(shardingRule.getBindingTableRules().size(), is(1));
        assertThat(Arrays.asList(shardingRule.getTableRules().toArray()), hasItems(shardingRule.getBindingTableRules().iterator().next().getTableRules().toArray()));
    }
    
    @Test
    public void testMin() {
        Yaml yaml = new Yaml(new Constructor(ShardingRuleConfig.class));
        Map<String, DataSource> dsMap = new HashMap<>();
        dsMap.put("ds", new BasicDataSource());
        ShardingRuleConfig config = (ShardingRuleConfig) yaml.load(ShardingRuleBuilderTest.class.getResourceAsStream("/config/config-min.yaml"));
        ShardingRule shardingRule = new ShardingRuleBuilder("config-min.yaml", dsMap, config).build();
        assertThat(shardingRule.getTableRules().size(), is(1));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testClassNotFound() {
        Yaml yaml = new Yaml(new Constructor(ShardingRuleConfig.class));
        ShardingRuleConfig config = (ShardingRuleConfig) yaml.load(ShardingRuleBuilderTest.class.getResourceAsStream("/config/config-classNotFound.yaml"));
        new ShardingRuleBuilder(config).build();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBindingError() {
        Yaml yaml = new Yaml(new Constructor(ShardingRuleConfig.class));
        Map<String, DataSource> dsMap = new HashMap<>();
        dsMap.put("ds", new BasicDataSource());
        ShardingRuleConfig config = (ShardingRuleConfig) yaml.load(ShardingRuleBuilderTest.class.getResourceAsStream("/config/config-bindingError.yaml"));
        ShardingRule shardingRule = new ShardingRuleBuilder("config-bindingError.yaml", dsMap, config).build();
        for (TableRule tableRule : shardingRule.getBindingTableRules().iterator().next().getTableRules()) {
            log.info(tableRule.toString());
        }
    }
}
