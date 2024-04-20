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

package org.apache.shardingsphere.sharding.yaml.swapper.strategy;

import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.audit.YamlShardingAuditStrategyConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class YamlShardingAuditStrategyConfigurationSwapperTest {
    
    @Test
    void assertSwapToYamlConfiguration() {
        ShardingAuditStrategyConfiguration data = new ShardingAuditStrategyConfiguration(Collections.singletonList("audit_algorithm"), false);
        YamlShardingAuditStrategyConfigurationSwapper swapper = new YamlShardingAuditStrategyConfigurationSwapper();
        YamlShardingAuditStrategyConfiguration actual = swapper.swapToYamlConfiguration(data);
        assertThat(actual.getAuditorNames(), is(Collections.singletonList("audit_algorithm")));
        assertFalse(actual.isAllowHintDisable());
    }
    
    @Test
    void assertSwapToObject() {
        YamlShardingAuditStrategyConfiguration yamlConfig = new YamlShardingAuditStrategyConfiguration();
        yamlConfig.setAuditorNames(Collections.singletonList("audit_algorithm"));
        yamlConfig.setAllowHintDisable(false);
        YamlShardingAuditStrategyConfigurationSwapper swapper = new YamlShardingAuditStrategyConfigurationSwapper();
        ShardingAuditStrategyConfiguration actual = swapper.swapToObject(yamlConfig);
        assertThat(actual.getAuditorNames(), is(Collections.singletonList("audit_algorithm")));
        assertFalse(actual.isAllowHintDisable());
    }
}
