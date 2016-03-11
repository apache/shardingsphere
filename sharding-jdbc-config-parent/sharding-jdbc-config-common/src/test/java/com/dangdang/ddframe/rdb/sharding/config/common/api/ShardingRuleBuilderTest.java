/**
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

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.config.common.api.config.ShardingRuleConfig;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ShardingRuleBuilderTest {
    
    @Test
    public void testParseObject() {
        Yaml yaml = new Yaml(new Constructor(ShardingRuleConfig.class));
        ShardingRuleConfig config = (ShardingRuleConfig) yaml.load(ShardingRuleBuilderTest.class.getResourceAsStream("/config.yaml"));
        ShardingRule shardingRule = new ShardingRuleBuilder().parse(config).build();
        assertThat(shardingRule.getTableRules().size(), is(3));
        assertThat(shardingRule.getBindingTableRules().size(), is(1));
    }
    
}
