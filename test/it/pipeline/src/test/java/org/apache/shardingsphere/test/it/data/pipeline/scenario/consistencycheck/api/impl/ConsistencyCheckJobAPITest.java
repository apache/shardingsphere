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

package org.apache.shardingsphere.test.it.data.pipeline.scenario.consistencycheck.api.impl;

import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyContentCheckResult;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCountCheckResult;
import org.apache.shardingsphere.data.pipeline.api.config.job.ConsistencyCheckJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.job.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.pojo.CreateConsistencyCheckJobParameter;
import org.apache.shardingsphere.data.pipeline.core.api.GovernanceRepositoryAPI;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.ConsistencyCheckJobId;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.api.impl.ConsistencyCheckJobAPI;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.context.ConsistencyCheckJobItemContext;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.util.ConsistencyCheckSequence;
import org.apache.shardingsphere.data.pipeline.scenario.migration.api.impl.MigrationJobAPI;
import org.apache.shardingsphere.test.it.data.pipeline.core.util.JobConfigurationBuilder;
import org.apache.shardingsphere.test.it.data.pipeline.core.util.PipelineContextUtil;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ConsistencyCheckJobAPITest {
    
    private static ConsistencyCheckJobAPI checkJobAPI;
    
    private static MigrationJobAPI migrationJobAPI;
    
    @BeforeClass
    public static void beforeClass() {
        PipelineContextUtil.mockModeConfigAndContextManager();
        checkJobAPI = new ConsistencyCheckJobAPI();
        migrationJobAPI = new MigrationJobAPI();
    }
    
    @Test
    public void assertCreateJobConfig() {
        String migrationJobId = "j0101test";
        String checkJobId = checkJobAPI.createJobAndStart(new CreateConsistencyCheckJobParameter(migrationJobId, null, null));
        ConsistencyCheckJobConfiguration jobConfig = checkJobAPI.getJobConfiguration(checkJobId);
        int expectedSequence = ConsistencyCheckSequence.MIN_SEQUENCE;
        String expectCheckJobId = "j0201" + migrationJobId + expectedSequence;
        assertThat(jobConfig.getJobId(), is(expectCheckJobId));
        assertNull(jobConfig.getAlgorithmTypeName());
        int sequence = ConsistencyCheckJobId.parseSequence(expectCheckJobId);
        assertThat(sequence, is(expectedSequence));
    }
    
    @Test
    public void assertGetLatestDataConsistencyCheckResult() {
        Optional<String> jobId = migrationJobAPI.start(JobConfigurationBuilder.createJobConfiguration());
        assertTrue(jobId.isPresent());
        String checkJobId = checkJobAPI.createJobAndStart(new CreateConsistencyCheckJobParameter(jobId.get(), null, null));
        PipelineAPIFactory.getGovernanceRepositoryAPI().persistLatestCheckJobId(jobId.get(), checkJobId);
        Map<String, DataConsistencyCheckResult> expectedCheckResult = Collections.singletonMap("t_order", new DataConsistencyCheckResult(new DataConsistencyCountCheckResult(1, 1),
                new DataConsistencyContentCheckResult(true)));
        PipelineAPIFactory.getGovernanceRepositoryAPI().persistCheckJobResult(jobId.get(), checkJobId, expectedCheckResult);
        Map<String, DataConsistencyCheckResult> actualCheckResult = checkJobAPI.getLatestDataConsistencyCheckResult(jobId.get());
        assertThat(actualCheckResult.size(), is(expectedCheckResult.size()));
        assertThat(actualCheckResult.get("t_order").getCountCheckResult().isMatched(), is(expectedCheckResult.get("t_order").getContentCheckResult().isMatched()));
    }
    
    @Test
    public void assertDropByParentJobId() {
        String parentJobId = getParentJobId(JobConfigurationBuilder.createJobConfiguration());
        GovernanceRepositoryAPI repositoryAPI = PipelineAPIFactory.getGovernanceRepositoryAPI();
        int expectedSequence = 1;
        for (int i = 0; i < 3; i++) {
            String checkJobId = checkJobAPI.createJobAndStart(new CreateConsistencyCheckJobParameter(parentJobId, null, null));
            ConsistencyCheckJobItemContext checkJobItemContext = new ConsistencyCheckJobItemContext(
                    new ConsistencyCheckJobConfiguration(checkJobId, parentJobId, null, null), 0, JobStatus.FINISHED, null);
            checkJobAPI.persistJobItemProgress(checkJobItemContext);
            Map<String, DataConsistencyCheckResult> dataConsistencyCheckResult = Collections.singletonMap("t_order",
                    new DataConsistencyCheckResult(new DataConsistencyCountCheckResult(0, 0), new DataConsistencyContentCheckResult(true)));
            repositoryAPI.persistCheckJobResult(parentJobId, checkJobId, dataConsistencyCheckResult);
            Optional<String> latestCheckJobId = repositoryAPI.getLatestCheckJobId(parentJobId);
            assertTrue(latestCheckJobId.isPresent());
            assertThat(ConsistencyCheckJobId.parseSequence(latestCheckJobId.get()), is(expectedSequence++));
        }
        expectedSequence = 2;
        for (int i = 0; i < 2; i++) {
            checkJobAPI.dropByParentJobId(parentJobId);
            Optional<String> latestCheckJobId = repositoryAPI.getLatestCheckJobId(parentJobId);
            assertTrue(latestCheckJobId.isPresent());
            assertThat(ConsistencyCheckJobId.parseSequence(latestCheckJobId.get()), is(expectedSequence--));
        }
        checkJobAPI.dropByParentJobId(parentJobId);
        Optional<String> latestCheckJobId = repositoryAPI.getLatestCheckJobId(parentJobId);
        assertFalse(latestCheckJobId.isPresent());
    }
    
    private String getParentJobId(final MigrationJobConfiguration jobConfig) {
        Optional<String> result = migrationJobAPI.start(jobConfig);
        assertTrue(result.isPresent());
        return result.get();
    }
}
