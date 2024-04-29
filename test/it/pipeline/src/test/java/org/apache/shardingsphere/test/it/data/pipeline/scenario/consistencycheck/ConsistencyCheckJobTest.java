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

package org.apache.shardingsphere.test.it.data.pipeline.scenario.consistencycheck;

import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.core.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.core.job.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.id.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.progress.ConsistencyCheckJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.config.YamlConsistencyCheckJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobItemManager;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.ConsistencyCheckJob;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.ConsistencyCheckJobId;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.ConsistencyCheckJobType;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.config.ConsistencyCheckJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.config.yaml.config.YamlConsistencyCheckJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.config.yaml.swapper.YamlConsistencyCheckJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.context.ConsistencyCheckJobItemContext;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.test.it.data.pipeline.core.util.JobConfigurationBuilder;
import org.apache.shardingsphere.test.it.data.pipeline.core.util.PipelineContextUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ConsistencyCheckJobTest {
    
    @BeforeAll
    static void beforeClass() {
        PipelineContextUtils.mockModeConfigAndContextManager();
    }
    
    @Test
    void assertBuildPipelineJobItemContext() {
        ConsistencyCheckJobId pipelineJobId = new ConsistencyCheckJobId(new PipelineContextKey(InstanceType.PROXY), JobConfigurationBuilder.createYamlMigrationJobConfiguration().getJobId());
        String checkJobId = PipelineJobIdUtils.marshal(pipelineJobId);
        Map<String, Object> expectTableCheckPosition = Collections.singletonMap("t_order", 100);
        PipelineAPIFactory.getPipelineGovernanceFacade(PipelineContextUtils.getContextKey()).getJobItemFacade().getProcess().persist(checkJobId, 0,
                YamlEngine.marshal(createYamlConsistencyCheckJobItemProgress(expectTableCheckPosition)));
        ConsistencyCheckJob consistencyCheckJob = new ConsistencyCheckJob("j02");
        ConsistencyCheckJobConfiguration jobConfig = new YamlConsistencyCheckJobConfigurationSwapper().swapToObject(createYamlConsistencyCheckJobConfiguration(checkJobId));
        PipelineJobItemManager<ConsistencyCheckJobItemProgress> jobItemManager = new PipelineJobItemManager<>(new ConsistencyCheckJobType().getYamlJobItemProgressSwapper());
        Optional<ConsistencyCheckJobItemProgress> jobItemProgress = jobItemManager.getProgress(jobConfig.getJobId(), 0);
        ConsistencyCheckJobItemContext actual = consistencyCheckJob.buildJobItemContext(jobConfig, 0, jobItemProgress.orElse(null), null);
        assertThat(actual.getProgressContext().getSourceTableCheckPositions(), is(expectTableCheckPosition));
        assertThat(actual.getProgressContext().getTargetTableCheckPositions(), is(expectTableCheckPosition));
    }
    
    private YamlConsistencyCheckJobItemProgress createYamlConsistencyCheckJobItemProgress(final Map<String, Object> expectTableCheckPosition) {
        YamlConsistencyCheckJobItemProgress result = new YamlConsistencyCheckJobItemProgress();
        result.setStatus(JobStatus.RUNNING.name());
        result.setSourceTableCheckPositions(expectTableCheckPosition);
        result.setTargetTableCheckPositions(expectTableCheckPosition);
        return result;
    }
    
    private YamlConsistencyCheckJobConfiguration createYamlConsistencyCheckJobConfiguration(final String checkJobId) {
        YamlConsistencyCheckJobConfiguration result = new YamlConsistencyCheckJobConfiguration();
        result.setJobId(checkJobId);
        result.setParentJobId("");
        result.setAlgorithmTypeName("DATA_MATCH");
        result.setSourceDatabaseType("H2");
        return result;
    }
}
