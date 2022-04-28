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

package org.apache.shardingsphere.sharding.schedule;

import com.google.common.collect.ImmutableMap;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.RuleAlteredJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.PipelineConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.TaskConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.WorkflowConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.yaml.YamlPipelineDataSourceConfiguration;
import org.apache.shardingsphere.infra.config.rulealtered.OnRuleAlteredActionConfiguration;
import org.apache.shardingsphere.infra.config.rulealtered.OnRuleAlteredActionConfiguration.OutputConfiguration;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingRuleAlteredJobConfigurationPreparerTest {
    
    @Mock
    private OnRuleAlteredActionConfiguration mockOnRuleAlteredActionConfiguration;
    
    private WorkflowConfiguration workflowConfiguration;
    
    @Before
    public void setUp() {
        workflowConfiguration = new WorkflowConfiguration("logic_db", ImmutableMap.of(YamlShardingRuleConfiguration.class.getName(),
                Collections.singletonList("t_order")), 0, 1);
        when(mockOnRuleAlteredActionConfiguration.getOutput()).thenReturn(new OutputConfiguration(5, 1000, null));
    }
    
    @Test
    public void assertAutoTableRuleCreateTaskConfiguration() throws IOException {
        PipelineConfiguration pipelineConfiguration = new PipelineConfiguration();
        setPipelineConfigurationSource(pipelineConfiguration);
        URL targetUrl = ShardingRuleAlteredJobConfigurationPreparerTest.class.getClassLoader().getResource("yaml/scaling/prepare/auto_table_alter_rule_target.yaml");
        assertNotNull(targetUrl);
        YamlPipelineDataSourceConfiguration target = YamlEngine.unmarshal(new File(targetUrl.getFile()), YamlPipelineDataSourceConfiguration.class);
        pipelineConfiguration.setTarget(target);
        RuleAlteredJobConfiguration jobConfig = new RuleAlteredJobConfiguration(workflowConfiguration, pipelineConfiguration);
        jobConfig.buildHandleConfig();
        ShardingRuleAlteredJobConfigurationPreparer preparer = new ShardingRuleAlteredJobConfigurationPreparer();
        TaskConfiguration taskConfiguration = preparer.createTaskConfiguration(pipelineConfiguration, jobConfig.getHandleConfig(), mockOnRuleAlteredActionConfiguration);
        assertEquals(taskConfiguration.getHandleConfig().getLogicTables(), "t_order");
    }
    
    @Test
    public void assertTableRuleCreateTaskConfiguration() throws IOException {
        PipelineConfiguration pipelineConfiguration = new PipelineConfiguration();
        setPipelineConfigurationSource(pipelineConfiguration);
        URL targetUrl = ShardingRuleAlteredJobConfigurationPreparerTest.class.getClassLoader().getResource("yaml/scaling/prepare/table_alter_rule_target.yaml");
        assertNotNull(targetUrl);
        YamlPipelineDataSourceConfiguration target = YamlEngine.unmarshal(new File(targetUrl.getFile()), YamlPipelineDataSourceConfiguration.class);
        pipelineConfiguration.setTarget(target);
        RuleAlteredJobConfiguration jobConfig = new RuleAlteredJobConfiguration(workflowConfiguration, pipelineConfiguration);
        jobConfig.buildHandleConfig();
        ShardingRuleAlteredJobConfigurationPreparer preparer = new ShardingRuleAlteredJobConfigurationPreparer();
        TaskConfiguration taskConfiguration = preparer.createTaskConfiguration(pipelineConfiguration, jobConfig.getHandleConfig(), mockOnRuleAlteredActionConfiguration);
        assertThat(taskConfiguration.getHandleConfig().getLogicTables(), is("t_order"));
    }
    
    private void setPipelineConfigurationSource(final PipelineConfiguration pipelineConfiguration) throws IOException {
        URL sourceUrl = ShardingRuleAlteredJobConfigurationPreparerTest.class.getClassLoader().getResource("yaml/scaling/prepare/alter_rule_source.yaml");
        assertNotNull(sourceUrl);
        YamlPipelineDataSourceConfiguration source = YamlEngine.unmarshal(new File(sourceUrl.getFile()), YamlPipelineDataSourceConfiguration.class);
        pipelineConfiguration.setSource(source);
    }
}
