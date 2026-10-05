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

import org.apache.shardingsphere.data.pipeline.api.type.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.datasource.rule.PipelineRuleConfigurationReviser;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.yaml.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShardingPipelineRuleConfigurationReviserTest {
    
    @SuppressWarnings("unchecked")
    private final PipelineRuleConfigurationReviser<ShardingRuleConfiguration> reviser = (PipelineRuleConfigurationReviser<ShardingRuleConfiguration>) OrderedSPILoader
            .getServicesByClass(PipelineRuleConfigurationReviser.class, Collections.singleton(ShardingRuleConfiguration.class)).get(ShardingRuleConfiguration.class);
    
    @Test
    void assertReviseInlineAlgorithmsAndAuditStrategies() {
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ruleConfig.getShardingAlgorithms().put("inline", new AlgorithmConfiguration("INLINE", new Properties()));
        ruleConfig.getShardingAlgorithms().put("complex_inline", new AlgorithmConfiguration("complex_inline", new Properties()));
        ruleConfig.getShardingAlgorithms().put("hash_mod", new AlgorithmConfiguration("HASH_MOD", new Properties()));
        ShardingAuditStrategyConfiguration auditStrategy = new ShardingAuditStrategyConfiguration(Collections.singleton("fixture_auditor"), false);
        ruleConfig.setDefaultAuditStrategy(auditStrategy);
        ruleConfig.getAuditors().put("fixture_auditor", new AlgorithmConfiguration("SQL_HINT", new Properties()));
        ShardingTableRuleConfiguration tableRule = new ShardingTableRuleConfiguration("t_order", "ds_0.t_order");
        tableRule.setAuditStrategy(auditStrategy);
        ruleConfig.getTables().add(tableRule);
        ShardingAutoTableRuleConfiguration autoTableRule = new ShardingAutoTableRuleConfiguration("t_auto", "ds_0");
        autoTableRule.setAuditStrategy(auditStrategy);
        ruleConfig.getAutoTables().add(autoTableRule);
        reviser.revise(ruleConfig);
        assertThat(ruleConfig.getShardingAlgorithms().get("inline").getProps().getProperty("allow-range-query-with-inline-sharding"), is(Boolean.TRUE.toString()));
        assertThat(ruleConfig.getShardingAlgorithms().get("complex_inline").getProps().getProperty("allow-range-query-with-inline-sharding"), is(Boolean.TRUE.toString()));
        assertNull(ruleConfig.getShardingAlgorithms().get("hash_mod").getProps().getProperty("allow-range-query-with-inline-sharding"));
        assertNull(ruleConfig.getDefaultAuditStrategy());
        assertTrue(ruleConfig.getAuditors().isEmpty());
        assertNull(tableRule.getAuditStrategy());
        assertNull(autoTableRule.getAuditStrategy());
    }
    
    @Test
    void assertRevisionDoesNotChangeOriginalRuleConfiguration() {
        YamlRootConfiguration rootConfig = new YamlRootConfiguration();
        rootConfig.setDatabaseName("foo_db");
        Map<String, Object> dataSource = new HashMap<>(2, 1F);
        dataSource.put("dataSourceClassName", "com.zaxxer.hikari.HikariDataSource");
        dataSource.put("url", "jdbc:h2:mem:foo_db");
        rootConfig.setDataSources(Collections.singletonMap("ds_0", dataSource));
        YamlAlgorithmConfiguration algorithm = new YamlAlgorithmConfiguration();
        algorithm.setType("INLINE");
        YamlShardingRuleConfiguration yamlRuleConfig = new YamlShardingRuleConfiguration();
        yamlRuleConfig.getShardingAlgorithms().put("inline", algorithm);
        rootConfig.setRules(Collections.singleton(yamlRuleConfig));
        ShardingSpherePipelineDataSourceConfiguration config = new ShardingSpherePipelineDataSourceConfiguration(rootConfig);
        ShardingRuleConfiguration original = (ShardingRuleConfiguration) config.getRuleConfigurations().iterator().next();
        ShardingRuleConfiguration forCreation = (ShardingRuleConfiguration) config.getCreationRuleConfigurations().iterator().next();
        reviser.revise(forCreation);
        assertNull(original.getShardingAlgorithms().get("inline").getProps().getProperty("allow-range-query-with-inline-sharding"));
        assertThat(forCreation.getShardingAlgorithms().get("inline").getProps().getProperty("allow-range-query-with-inline-sharding"), is(Boolean.TRUE.toString()));
        ShardingRuleConfiguration nextCreation = (ShardingRuleConfiguration) config.getCreationRuleConfigurations().iterator().next();
        assertNull(nextCreation.getShardingAlgorithms().get("inline").getProps().getProperty("allow-range-query-with-inline-sharding"));
    }
}
