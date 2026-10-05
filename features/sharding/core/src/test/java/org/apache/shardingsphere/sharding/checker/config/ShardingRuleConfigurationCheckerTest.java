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

package org.apache.shardingsphere.sharding.checker.config;

import org.apache.shardingsphere.infra.config.rule.checker.DatabaseRuleConfigurationChecker;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ShardingRuleConfigurationCheckerTest {
    
    private ShardingRuleConfigurationChecker checker;
    
    @BeforeEach
    void setUp() {
        checker = (ShardingRuleConfigurationChecker) OrderedSPILoader.getServicesByClass(
                DatabaseRuleConfigurationChecker.class, Collections.singleton(ShardingRuleConfiguration.class)).get(ShardingRuleConfiguration.class);
    }
    
    @Test
    void assertGetRequiredDataSourceNames() {
        ShardingRuleConfiguration ruleConfig = createRuleConfiguration();
        ruleConfig.setTables(Collections.singleton(new ShardingTableRuleConfiguration("foo_tbl", "ds_0.foo_tbl")));
        ruleConfig.setAutoTables(Collections.singleton(new ShardingAutoTableRuleConfiguration("bar_tbl", "ds_1")));
        assertThat(checker.getRequiredDataSourceNames(ruleConfig), is(new LinkedHashSet<>(Arrays.asList("ds_0", "ds_1"))));
    }
    
    @Test
    void assertGetTableNames() {
        ShardingRuleConfiguration ruleConfig = createRuleConfiguration();
        ruleConfig.setTables(Collections.singleton(new ShardingTableRuleConfiguration("foo_tbl", "ds_0.foo_tbl")));
        ruleConfig.setAutoTables(Collections.singleton(new ShardingAutoTableRuleConfiguration("bar_tbl", "ds_1")));
        assertThat(checker.getTableNames(ruleConfig), is(Arrays.asList("foo_tbl", "bar_tbl")));
    }
    
    private ShardingRuleConfiguration createRuleConfiguration() {
        return new ShardingRuleConfiguration();
    }
}
