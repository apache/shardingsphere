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

package org.apache.shardingsphere.data.pipeline.scenario.rulealtered;

import com.google.common.collect.ImmutableMap;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.JobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.WorkflowConfiguration;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineJobCreationException;
import org.apache.shardingsphere.data.pipeline.core.util.JobConfigurationBuilder;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineContextUtil;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.schedule.ShardingRuleAlteredDetector;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public final class RuleAlteredJobWorkerTest {
    
    static {
        ShardingSphereServiceLoader.register(ShardingRuleAlteredDetector.class);
    }
    
    @BeforeClass
    public static void beforeClass() {
        PipelineContextUtil.mockModeConfigAndContextManager();
    }
    
    @Test(expected = PipelineJobCreationException.class)
    public void assertCreateRuleAlteredContextNoAlteredRule() {
        JobConfiguration jobConfig = JobConfigurationBuilder.createJobConfiguration();
        jobConfig.setWorkflowConfig(new WorkflowConfiguration("logic_db", ImmutableMap.of(), 0, 1));
        RuleAlteredJobWorker.createRuleAlteredContext(jobConfig);
    }
    
    @Test
    public void assertCreateRuleAlteredContextSuccess() {
        JobConfiguration jobConfig = JobConfigurationBuilder.createJobConfiguration();
        RuleAlteredContext ruleAlteredContext = RuleAlteredJobWorker.createRuleAlteredContext(jobConfig);
        assertNotNull(ruleAlteredContext.getOnRuleAlteredActionConfig());
    }
    
    @Test
    public void assertRuleAlteredActionEnabled() {
        ShardingRuleConfiguration ruleConfiguration = new ShardingRuleConfiguration();
        ruleConfiguration.setScalingName("default_scaling");
        assertTrue(RuleAlteredJobWorker.isOnRuleAlteredActionEnabled(ruleConfiguration));
    }
}
