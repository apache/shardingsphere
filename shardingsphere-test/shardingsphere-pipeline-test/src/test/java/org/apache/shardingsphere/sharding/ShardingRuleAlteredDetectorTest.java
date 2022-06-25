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

package org.apache.shardingsphere.sharding;

import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.util.ConfigurationFileUtil;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.data.pipeline.ShardingRuleAlteredDetector;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ShardingRuleAlteredDetectorTest {
    
    @Test
    public void assertGetOnRuleAlteredActionConfigSuccess() {
        assertFalse(new ShardingRuleAlteredDetector().getOnRuleAlteredActionConfig(new ShardingRuleConfiguration()).isPresent());
    }
    
    @Test
    public void assertFindRuleAlteredLogicTablesSucceed() {
        ShardingSpherePipelineDataSourceConfiguration pipelineDataSourceConfig = new ShardingSpherePipelineDataSourceConfiguration(
                ConfigurationFileUtil.readFile("config_sharding_sphere_jdbc_source.yaml"));
        ShardingSpherePipelineDataSourceConfiguration pipelineDataTargetConfig = new ShardingSpherePipelineDataSourceConfiguration(
                ConfigurationFileUtil.readFile("config_sharding_sphere_jdbc_target.yaml"));
        Collection<YamlRuleConfiguration> sourceRules = pipelineDataSourceConfig.getRootConfig().getRules();
        Collection<YamlRuleConfiguration> targetRules = pipelineDataTargetConfig.getRootConfig().getRules();
        assertThat(targetRules.size(), is(1));
        Optional<YamlRuleConfiguration> sourceRule = sourceRules.stream().findFirst();
        assertTrue(sourceRule.isPresent());
        Optional<YamlRuleConfiguration> targetRule = targetRules.stream().findFirst();
        assertTrue(targetRule.isPresent());
        List<String> ruleAlteredLogicTables = new ShardingRuleAlteredDetector().findRuleAlteredLogicTables(sourceRule.get(), targetRule.get(),
                pipelineDataSourceConfig.getRootConfig().getDataSources(), pipelineDataSourceConfig.getRootConfig().getDataSources());
        assertThat(ruleAlteredLogicTables.get(0), is("t_order"));
    }
    
    @Test
    public void assertNoFindRuleAlteredLogicTables() {
        ShardingSpherePipelineDataSourceConfiguration pipelineDataSourceConfig = new ShardingSpherePipelineDataSourceConfiguration(
                ConfigurationFileUtil.readFile("config_sharding_sphere_jdbc_source.yaml"));
        Optional<YamlRuleConfiguration> firstRule = pipelineDataSourceConfig.getRootConfig().getRules().stream().findFirst();
        assertTrue(firstRule.isPresent());
        List<String> ruleAlteredLogicTables = new ShardingRuleAlteredDetector().findRuleAlteredLogicTables(firstRule.get(), firstRule.get(), null, null);
        assertThat("not table rule alter", ruleAlteredLogicTables.size(), is(0));
    }
    
    @Test
    public void assertExtractAllLogicTables() {
        ShardingSpherePipelineDataSourceConfiguration pipelineDataSourceConfig = new ShardingSpherePipelineDataSourceConfiguration(
                ConfigurationFileUtil.readFile("config_sharding_sphere_jdbc_source.yaml"));
        Optional<YamlRuleConfiguration> firstRule = pipelineDataSourceConfig.getRootConfig().getRules().stream().findFirst();
        assertTrue(firstRule.isPresent());
        List<String> ruleAlteredLogicTables = new ShardingRuleAlteredDetector().findRuleAlteredLogicTables(firstRule.get(), null, null, null);
        assertThat(ruleAlteredLogicTables.get(0), is("t_order"));
    }
}
