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
import org.apache.commons.io.FileUtils;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.JobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.WorkflowConfiguration;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.progress.JobProgress;
import org.apache.shardingsphere.data.pipeline.core.api.GovernanceRepositoryAPI;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineJobCreationException;
import org.apache.shardingsphere.data.pipeline.core.util.JobConfigurationBuilder;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineContextUtil;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.cache.event.StartScalingEvent;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.schedule.ShardingRuleAlteredDetector;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

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
    
    @Test
    public void assertRuleAlteredActionDisabled() throws IOException {
        URL dataSourceUrl = getClass().getClassLoader().getResource("scaling/detector/datasource_config.yaml");
        assertNotNull(dataSourceUrl);
        URL sourceRuleUrl = getClass().getClassLoader().getResource("scaling/rule_alter/source_rules_config.yaml");
        assertNotNull(sourceRuleUrl);
        URL targetRuleUrl = getClass().getClassLoader().getResource("scaling/rule_alter/target_rules_config.yaml");
        assertNotNull(targetRuleUrl);
        StartScalingEvent startScalingEvent = new StartScalingEvent("logic_db", FileUtils.readFileToString(new File(dataSourceUrl.getFile())),
                FileUtils.readFileToString(new File(sourceRuleUrl.getFile())), FileUtils.readFileToString(new File(dataSourceUrl.getFile())),
                FileUtils.readFileToString(new File(targetRuleUrl.getFile())), 0, 1);
        new RuleAlteredJobWorker().start(startScalingEvent);
    }
    
    @Test
    public void assertHasUncompletedJob() throws IOException {
        StartScalingEvent startScalingEvent = new StartScalingEvent("sharding_db", null, null, null, null, 0, 1);
        final JobConfiguration jobConfiguration = JobConfigurationBuilder.createJobConfiguration();
        jobConfiguration.getWorkflowConfig().setSchemaName("logic_db");
        GovernanceRepositoryAPI repositoryAPI = PipelineAPIFactory.getGovernanceRepositoryAPI();
        RuleAlteredJobContext jobContext = new RuleAlteredJobContext(jobConfiguration);
        JobProgress finishProcess = new JobProgress();
        finishProcess.setStatus(JobStatus.FINISHED);
        jobContext.setInitProgress(finishProcess);
        repositoryAPI.persistJobProgress(jobContext);
        URL jobConfigUrl = getClass().getClassLoader().getResource("scaling/rule_alter/scaling_job_config.yaml");
        assertNotNull(jobConfigUrl);
        repositoryAPI.persist("/scaling/0130317c30317c3054317c6c6f6769635f6462/config", FileUtils.readFileToString(new File(jobConfigUrl.getFile())));
        new RuleAlteredJobWorker().start(startScalingEvent);
    }
}
