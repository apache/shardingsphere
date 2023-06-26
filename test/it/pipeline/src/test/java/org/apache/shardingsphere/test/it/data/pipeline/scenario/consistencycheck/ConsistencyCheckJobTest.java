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

import org.apache.shardingsphere.data.pipeline.common.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.common.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.common.job.progress.yaml.YamlConsistencyCheckJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.ConsistencyCheckJob;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.ConsistencyCheckJobId;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.api.impl.ConsistencyCheckJobAPI;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.config.yaml.YamlConsistencyCheckJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.context.ConsistencyCheckJobItemContext;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.test.it.data.pipeline.core.util.JobConfigurationBuilder;
import org.apache.shardingsphere.test.it.data.pipeline.core.util.PipelineContextUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ConsistencyCheckJobTest {
    
    @BeforeAll
    static void beforeClass() {
        PipelineContextUtils.mockModeConfigAndContextManager();
    }
    
    @Test
    void assertBuildPipelineJobItemContext() {
        ConsistencyCheckJobId pipelineJobId = new ConsistencyCheckJobId(PipelineContextKey.buildForProxy(), JobConfigurationBuilder.createYamlMigrationJobConfiguration().getJobId());
        String checkJobId = new ConsistencyCheckJobAPI().marshalJobId(pipelineJobId);
        Map<String, Object> expectTableCheckPosition = Collections.singletonMap("t_order", 100);
        PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineContextUtils.getContextKey()).persistJobItemProgress(checkJobId, 0,
                YamlEngine.marshal(createYamlConsistencyCheckJobItemProgress(expectTableCheckPosition)));
        ConsistencyCheckJob consistencyCheckJob = new ConsistencyCheckJob(checkJobId);
        ConsistencyCheckJobItemContext actual = consistencyCheckJob.buildPipelineJobItemContext(
                new ShardingContext(checkJobId, "", 1, YamlEngine.marshal(createYamlConsistencyCheckJobConfiguration(checkJobId)), 0, ""));
        assertThat(actual.getProgressContext().getTableCheckPositions(), is(expectTableCheckPosition));
    }
    
    private YamlConsistencyCheckJobItemProgress createYamlConsistencyCheckJobItemProgress(final Map<String, Object> expectTableCheckPosition) {
        YamlConsistencyCheckJobItemProgress result = new YamlConsistencyCheckJobItemProgress();
        result.setStatus(JobStatus.RUNNING.name());
        result.setTableCheckPositions(expectTableCheckPosition);
        return result;
    }
    
    private YamlConsistencyCheckJobConfiguration createYamlConsistencyCheckJobConfiguration(final String checkJobId) {
        YamlConsistencyCheckJobConfiguration result = new YamlConsistencyCheckJobConfiguration();
        result.setJobId(checkJobId);
        result.setParentJobId("");
        return result;
    }
}
