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

package org.apache.shardingsphere.data.pipeline.core.api.impl;

import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyContentCheckResult;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCountCheckResult;
import org.apache.shardingsphere.data.pipeline.api.config.job.ConsistencyCheckJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.pojo.CreateConsistencyCheckJobParameter;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.util.JobConfigurationBuilder;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineContextUtil;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.ConsistencyCheckJobAPI;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.ConsistencyCheckJobAPIFactory;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.ConsistencyCheckJobId;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobAPI;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobAPIFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public final class ConsistencyCheckJobAPIImplTest {
    
    private static ConsistencyCheckJobAPI checkJobAPI;
    
    private static MigrationJobAPI migrationJobAPI;
    
    @BeforeClass
    public static void beforeClass() {
        PipelineContextUtil.mockModeConfigAndContextManager();
        checkJobAPI = ConsistencyCheckJobAPIFactory.getInstance();
        migrationJobAPI = MigrationJobAPIFactory.getInstance();
    }
    
    @Test
    public void assertCreateJobConfig() {
        String migrationJobId = "j0101test";
        String checkJobId = checkJobAPI.createJobAndStart(new CreateConsistencyCheckJobParameter(migrationJobId, null, null));
        ConsistencyCheckJobConfiguration jobConfig = (ConsistencyCheckJobConfiguration) checkJobAPI.getJobConfiguration(checkJobId);
        int expectedSequence = ConsistencyCheckJobId.MIN_SEQUENCE;
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
        PipelineAPIFactory.getGovernanceRepositoryAPI().persistCheckLatestJobId(jobId.get(), checkJobId);
        Map<String, DataConsistencyCheckResult> expectedCheckResult = Collections.singletonMap("t_order", new DataConsistencyCheckResult(new DataConsistencyCountCheckResult(1, 1),
                new DataConsistencyContentCheckResult(true)));
        PipelineAPIFactory.getGovernanceRepositoryAPI().persistCheckJobResult(jobId.get(), checkJobId, expectedCheckResult);
        Map<String, DataConsistencyCheckResult> actualCheckResult = checkJobAPI.getLatestDataConsistencyCheckResult(jobId.get());
        assertThat(actualCheckResult.size(), is(expectedCheckResult.size()));
        assertThat(actualCheckResult.get("t_order").getCountCheckResult().isMatched(), is(expectedCheckResult.get("t_order").getContentCheckResult().isMatched()));
    }
}
