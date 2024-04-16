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

package org.apache.shardingsphere.sharding.yaml.swapper;

import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlTableRuleConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShardingRuleConfigurationConverterTest {
    
    private Collection<YamlRuleConfiguration> yamlRuleConfig;
    
    @BeforeEach
    void setUp() {
        YamlShardingRuleConfiguration yamlShardingRuleConfig = new YamlShardingRuleConfiguration();
        YamlTableRuleConfiguration tableRuleConfig = new YamlTableRuleConfiguration();
        tableRuleConfig.setActualDataNodes("ds_${0..1}.table_${0..2}");
        yamlShardingRuleConfig.getTables().put("LOGIC_TABLE", tableRuleConfig);
        yamlRuleConfig = Collections.singletonList(yamlShardingRuleConfig);
    }
    
    @Test
    void assertFindAndConvertShardingRuleConfiguration() {
        Optional<ShardingRuleConfiguration> actual = ShardingRuleConfigurationConverter.findAndConvertShardingRuleConfiguration(yamlRuleConfig);
        assertTrue(actual.isPresent());
        assertThat(actual.get().getTables().size(), is(1));
        assertThat(actual.get().getTables().iterator().next().getLogicTable(), is("LOGIC_TABLE"));
    }
    
    @Test
    void assertFindYamlShardingRuleConfiguration() {
        Optional<YamlShardingRuleConfiguration> actual = ShardingRuleConfigurationConverter.findYamlShardingRuleConfiguration(yamlRuleConfig);
        assertTrue(actual.isPresent());
        assertThat(actual.get().getTables().size(), is(1));
        assertTrue(actual.get().getTables().containsKey("LOGIC_TABLE"));
    }
}
