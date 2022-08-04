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
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class YamlShardingAuditStrategyConfigurationSwapperTest {
    
    @Test
    public void assertSwapToYaml() {
        YamlShardingAuditStrategyConfiguration actual = new YamlShardingAuditStrategyConfigurationSwapper()
                .swapToYamlConfiguration(new ShardingAuditStrategyConfiguration(Arrays.asList("audit_algorithm1", "audit_algorithm2"), false));
        assertThat(actual.getAuditorNames().size(), is(2));
        assertTrue(actual.getAuditorNames().containsAll(Arrays.asList("audit_algorithm1", "audit_algorithm2")));
        assertFalse(actual.isAllowHintDisable());
    }
    
    @Test
    public void assertSwapToObject() {
        YamlShardingAuditStrategyConfiguration yamlShardingAuditStrategyConfig = new YamlShardingAuditStrategyConfiguration();
        yamlShardingAuditStrategyConfig.setAuditorNames(Arrays.asList("audit_algorithm1", "audit_algorithm2"));
        yamlShardingAuditStrategyConfig.setAllowHintDisable(false);
        ShardingAuditStrategyConfiguration actual = new YamlShardingAuditStrategyConfigurationSwapper().swapToObject(yamlShardingAuditStrategyConfig);
        assertThat(actual.getAuditorNames().size(), is(2));
        assertTrue(actual.getAuditorNames().containsAll(Arrays.asList("audit_algorithm1", "audit_algorithm2")));
        assertFalse(actual.isAllowHintDisable());
    }
    
    @Test(expected = NullPointerException.class)
    public void assertSwapToObjectWithNull() {
        YamlShardingAuditStrategyConfiguration yamlShardingAuditStrategyConfig = new YamlShardingAuditStrategyConfiguration();
        new YamlShardingAuditStrategyConfigurationSwapper().swapToObject(yamlShardingAuditStrategyConfig);
    }
}
