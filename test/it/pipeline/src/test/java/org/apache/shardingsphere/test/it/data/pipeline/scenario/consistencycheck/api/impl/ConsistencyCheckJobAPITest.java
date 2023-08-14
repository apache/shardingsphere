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

import org.apache.shardingsphere.data.pipeline.common.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.common.registrycenter.repository.GovernanceRepositoryAPI;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.TableDataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.TableDataConsistencyContentCheckResult;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.TableDataConsistencyCountCheckResult;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.ConsistencyCheckJobId;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.api.impl.ConsistencyCheckJobAPI;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.api.pojo.CreateConsistencyCheckJobParameter;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.config.ConsistencyCheckJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.context.ConsistencyCheckJobItemContext;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.util.ConsistencyCheckSequence;
import org.apache.shardingsphere.test.it.data.pipeline.core.util.JobConfigurationBuilder;
import org.apache.shardingsphere.test.it.data.pipeline.core.util.PipelineContextUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsistencyCheckJobAPITest {
    
    private final ConsistencyCheckJobAPI checkJobAPI = new ConsistencyCheckJobAPI();
    
    @BeforeAll
    public static void beforeClass() {
        PipelineContextUtils.mockModeConfigAndContextManager();
    }
    
    @Test
    void assertCreateJobConfig() {
        String parentJobId = JobConfigurationBuilder.createYamlMigrationJobConfiguration().getJobId();
        String checkJobId = checkJobAPI.createJobAndStart(new CreateConsistencyCheckJobParameter(parentJobId, null, null));
        ConsistencyCheckJobConfiguration jobConfig = checkJobAPI.getJobConfiguration(checkJobId);
        int expectedSequence = ConsistencyCheckSequence.MIN_SEQUENCE;
        String expectCheckJobId = checkJobAPI.marshalJobId(new ConsistencyCheckJobId(PipelineJobIdUtils.parseContextKey(parentJobId), parentJobId, expectedSequence));
        assertThat(jobConfig.getJobId(), is(expectCheckJobId));
        assertNull(jobConfig.getAlgorithmTypeName());
        int sequence = ConsistencyCheckJobId.parseSequence(expectCheckJobId);
        assertThat(sequence, is(expectedSequence));
    }
    
    @Test
    void assertGetLatestDataConsistencyCheckResult() {
        String parentJobId = JobConfigurationBuilder.createYamlMigrationJobConfiguration().getJobId();
        String checkJobId = checkJobAPI.createJobAndStart(new CreateConsistencyCheckJobParameter(parentJobId, null, null));
        GovernanceRepositoryAPI governanceRepositoryAPI = PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineContextUtils.getContextKey());
        governanceRepositoryAPI.persistLatestCheckJobId(parentJobId, checkJobId);
        Map<String, TableDataConsistencyCheckResult> expectedCheckResult = Collections.singletonMap("t_order", new TableDataConsistencyCheckResult(new TableDataConsistencyCountCheckResult(1, 1),
                new TableDataConsistencyContentCheckResult(true)));
        governanceRepositoryAPI.persistCheckJobResult(parentJobId, checkJobId, expectedCheckResult);
        Map<String, TableDataConsistencyCheckResult> actualCheckResult = checkJobAPI.getLatestDataConsistencyCheckResult(parentJobId);
        assertThat(actualCheckResult.size(), is(expectedCheckResult.size()));
        assertThat(actualCheckResult.get("t_order").getCountCheckResult().isMatched(), is(expectedCheckResult.get("t_order").getContentCheckResult().isMatched()));
    }
    
    @Test
    void assertDropByParentJobId() {
        String parentJobId = JobConfigurationBuilder.createYamlMigrationJobConfiguration().getJobId();
        GovernanceRepositoryAPI repositoryAPI = PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineContextUtils.getContextKey());
        int expectedSequence = 1;
        for (int i = 0; i < 3; i++) {
            String checkJobId = checkJobAPI.createJobAndStart(new CreateConsistencyCheckJobParameter(parentJobId, null, null));
            ConsistencyCheckJobItemContext checkJobItemContext = new ConsistencyCheckJobItemContext(
                    new ConsistencyCheckJobConfiguration(checkJobId, parentJobId, null, null), 0, JobStatus.FINISHED, null);
            checkJobAPI.persistJobItemProgress(checkJobItemContext);
            Map<String, TableDataConsistencyCheckResult> dataConsistencyCheckResult = Collections.singletonMap("t_order",
                    new TableDataConsistencyCheckResult(new TableDataConsistencyCountCheckResult(0, 0), new TableDataConsistencyContentCheckResult(true)));
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
}
