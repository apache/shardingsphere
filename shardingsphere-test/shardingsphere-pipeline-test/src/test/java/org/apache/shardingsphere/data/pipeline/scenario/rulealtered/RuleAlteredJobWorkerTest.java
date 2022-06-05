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

import org.apache.commons.io.FileUtils;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.RuleAlteredJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.yaml.RuleAlteredJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.yaml.YamlRuleAlteredJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.progress.JobProgress;
import org.apache.shardingsphere.data.pipeline.core.api.GovernanceRepositoryAPI;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineJobCreationException;
import org.apache.shardingsphere.data.pipeline.core.metadata.node.PipelineMetaDataNode;
import org.apache.shardingsphere.data.pipeline.core.util.ConfigurationFileUtil;
import org.apache.shardingsphere.data.pipeline.core.util.JobConfigurationBuilder;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineContextUtil;
import org.apache.shardingsphere.data.pipeline.core.util.ReflectionUtil;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.cache.event.StartScalingEvent;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public final class RuleAlteredJobWorkerTest {
    
    @BeforeClass
    public static void beforeClass() {
        PipelineContextUtil.mockModeConfigAndContextManager();
    }
    
    @Test(expected = PipelineJobCreationException.class)
    public void assertCreateRuleAlteredContextNoAlteredRule() {
        RuleAlteredJobConfiguration jobConfig = JobConfigurationBuilder.createJobConfiguration();
        RuleAlteredJobConfigurationSwapper swapper = new RuleAlteredJobConfigurationSwapper();
        YamlRuleAlteredJobConfiguration yamlJobConfig = swapper.swapToYamlConfiguration(jobConfig);
        yamlJobConfig.setAlteredRuleYamlClassNameTablesMap(Collections.emptyMap());
        RuleAlteredJobWorker.createRuleAlteredContext(swapper.swapToObject(yamlJobConfig));
    }
    
    @Test
    public void assertCreateRuleAlteredContextSuccess() {
        assertNotNull(RuleAlteredJobWorker.createRuleAlteredContext(JobConfigurationBuilder.createJobConfiguration()).getOnRuleAlteredActionConfig());
    }
    
    @Test
    public void assertRuleAlteredActionEnabled() {
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ruleConfig.setScalingName("default_scaling");
        assertTrue(RuleAlteredJobWorker.isOnRuleAlteredActionEnabled(ruleConfig));
    }
    
    @Test
    public void assertRuleAlteredActionDisabled() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        ShardingSpherePipelineDataSourceConfiguration pipelineDataSourceConfig = new ShardingSpherePipelineDataSourceConfiguration(
                ConfigurationFileUtil.readFile("config_sharding_sphere_jdbc_source.yaml"));
        ShardingSpherePipelineDataSourceConfiguration pipelineDataTargetConfig = new ShardingSpherePipelineDataSourceConfiguration(
                ConfigurationFileUtil.readFile("config_sharding_sphere_jdbc_target.yaml"));
        StartScalingEvent startScalingEvent = new StartScalingEvent("logic_db", YamlEngine.marshal(pipelineDataSourceConfig.getRootConfig().getDataSources()),
                YamlEngine.marshal(pipelineDataSourceConfig.getRootConfig().getRules()), YamlEngine.marshal(pipelineDataTargetConfig.getRootConfig().getDataSources()),
                YamlEngine.marshal(pipelineDataTargetConfig.getRootConfig().getRules()), 0, 1);
        RuleAlteredJobWorker ruleAlteredJobWorker = new RuleAlteredJobWorker();
        Object result = ReflectionUtil.invokeMethod(ruleAlteredJobWorker, "createJobConfig", new Class[]{StartScalingEvent.class}, new Object[]{startScalingEvent});
        assertTrue(((Optional<?>) result).isPresent());
    }
    
    // TODO improve assertHasUncompletedJob, refactor hasUncompletedJobOfSameDatabaseName for easier unit test
    // @Test
    public void assertHasUncompletedJob() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
        final RuleAlteredJobConfiguration jobConfig = JobConfigurationBuilder.createJobConfiguration();
        RuleAlteredJobContext jobContext = new RuleAlteredJobContext(jobConfig, 0, new JobProgress(), new PipelineDataSourceManager(), new RuleAlteredJobPreparer());
        jobContext.setStatus(JobStatus.PREPARING);
        GovernanceRepositoryAPI repositoryAPI = PipelineAPIFactory.getGovernanceRepositoryAPI();
        repositoryAPI.persistJobProgress(jobContext);
        URL jobConfigUrl = getClass().getClassLoader().getResource("scaling/rule_alter/scaling_job_config.yaml");
        assertNotNull(jobConfigUrl);
        repositoryAPI.persist(PipelineMetaDataNode.getJobConfigPath(jobContext.getJobId()), FileUtils.readFileToString(new File(jobConfigUrl.getFile())));
        Object result = ReflectionUtil.invokeMethod(new RuleAlteredJobWorker(), "hasUncompletedJobOfSameDatabaseName", new Class[]{String.class},
                new String[]{jobConfig.getDatabaseName()});
        assertFalse((Boolean) result);
    }
}
