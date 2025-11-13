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

package org.apache.shardingsphere.data.pipeline.sharding;

import org.apache.shardingsphere.data.pipeline.core.datasource.yaml.PipelineYamlRuleConfigurationReviser;
import org.apache.shardingsphere.infra.algorithm.core.yaml.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.audit.YamlShardingAuditStrategyConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;

class ShardingPipelineYamlRuleConfigurationReviserTest {
    
    @SuppressWarnings("unchecked")
    private final PipelineYamlRuleConfigurationReviser<YamlShardingRuleConfiguration> reviser = (PipelineYamlRuleConfigurationReviser<YamlShardingRuleConfiguration>) OrderedSPILoader
            .getServicesByClass(PipelineYamlRuleConfigurationReviser.class, Collections.singleton(YamlShardingRuleConfiguration.class)).get(YamlShardingRuleConfiguration.class);
    
    @Test
    void assertReviseInlineAlgorithmsAndAuditStrategies() {
        YamlShardingRuleConfiguration yamlConfig = new YamlShardingRuleConfiguration();
        yamlConfig.getShardingAlgorithms().put("inline", createAlgorithm("INLINE"));
        yamlConfig.getShardingAlgorithms().put("complex_inline", createAlgorithm("complex_inline"));
        yamlConfig.getShardingAlgorithms().put("hash_mod", createAlgorithm("HASH_MOD"));
        yamlConfig.setDefaultAuditStrategy(new YamlShardingAuditStrategyConfiguration());
        yamlConfig.getAuditors().put("fixture_auditor", createAlgorithm("SQL_HINT"));
        yamlConfig.getTables().put("t_order", createTableRuleWithAudit());
        yamlConfig.getAutoTables().put("t_auto", createAutoTableRuleWithAudit());
        reviser.revise(yamlConfig);
        assertThat(yamlConfig.getShardingAlgorithms().get("inline").getProps().getProperty("allow-range-query-with-inline-sharding"), is(Boolean.TRUE.toString()));
        assertThat(yamlConfig.getShardingAlgorithms().get("complex_inline").getProps().getProperty("allow-range-query-with-inline-sharding"), is(Boolean.TRUE.toString()));
        assertNull(yamlConfig.getShardingAlgorithms().get("hash_mod").getProps().getProperty("allow-range-query-with-inline-sharding"));
        assertNull(yamlConfig.getDefaultAuditStrategy());
        assertNull(yamlConfig.getAuditors());
        assertNull(yamlConfig.getTables().get("t_order").getAuditStrategy());
        assertNull(yamlConfig.getAutoTables().get("t_auto").getAuditStrategy());
    }
    
    @Test
    void assertReviseWhenTablesAbsent() {
        YamlShardingRuleConfiguration yamlConfig = new YamlShardingRuleConfiguration();
        yamlConfig.setTables(null);
        yamlConfig.setAutoTables(null);
        yamlConfig.setDefaultAuditStrategy(new YamlShardingAuditStrategyConfiguration());
        yamlConfig.setAuditors(Collections.singletonMap("auditor", createAlgorithm("SQL_HINT")));
        reviser.revise(yamlConfig);
        assertNull(yamlConfig.getTables());
        assertNull(yamlConfig.getAutoTables());
        assertNull(yamlConfig.getDefaultAuditStrategy());
        assertNull(yamlConfig.getAuditors());
    }
    
    private YamlAlgorithmConfiguration createAlgorithm(final String type) {
        YamlAlgorithmConfiguration result = new YamlAlgorithmConfiguration();
        result.setType(type);
        return result;
    }
    
    private YamlTableRuleConfiguration createTableRuleWithAudit() {
        YamlTableRuleConfiguration result = new YamlTableRuleConfiguration();
        result.setLogicTable("t_order");
        result.setAuditStrategy(new YamlShardingAuditStrategyConfiguration());
        return result;
    }
    
    private YamlShardingAutoTableRuleConfiguration createAutoTableRuleWithAudit() {
        YamlShardingAutoTableRuleConfiguration result = new YamlShardingAutoTableRuleConfiguration();
        result.setLogicTable("t_auto");
        result.setAuditStrategy(new YamlShardingAuditStrategyConfiguration());
        return result;
    }
}
