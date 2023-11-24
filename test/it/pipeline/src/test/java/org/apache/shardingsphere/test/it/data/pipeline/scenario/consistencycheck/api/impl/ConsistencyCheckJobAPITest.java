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
import org.apache.shardingsphere.data.pipeline.common.job.progress.TransmissionJobItemProgress;
import org.apache.shardingsphere.data.pipeline.common.registrycenter.repository.PipelineGovernanceFacade;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.TableDataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobConfigurationLoader;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobItemManager;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.ConsistencyCheckJobId;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.api.impl.ConsistencyCheckJobAPI;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.api.pojo.CreateConsistencyCheckJobParameter;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.config.ConsistencyCheckJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.context.ConsistencyCheckJobItemContext;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.util.ConsistencyCheckSequence;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.yaml.job.YamlMigrationJobConfigurationSwapper;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
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
    
    private final ConsistencyCheckJobAPI jobAPI = new ConsistencyCheckJobAPI();
    
    private final PipelineJobItemManager<TransmissionJobItemProgress> jobItemManager = new PipelineJobItemManager<>(jobAPI.getYamlJobItemProgressSwapper());
    
    private final YamlMigrationJobConfigurationSwapper jobConfigSwapper = new YamlMigrationJobConfigurationSwapper();
    
    @BeforeAll
    public static void beforeClass() {
        PipelineContextUtils.mockModeConfigAndContextManager();
    }
    
    @Test
    void assertCreateJobConfig() {
        MigrationJobConfiguration parentJobConfig = jobConfigSwapper.swapToObject(JobConfigurationBuilder.createYamlMigrationJobConfiguration());
        String parentJobId = parentJobConfig.getJobId();
        String checkJobId = jobAPI.createJobAndStart(new CreateConsistencyCheckJobParameter(parentJobId, null, null,
                parentJobConfig.getSourceDatabaseType(), parentJobConfig.getTargetDatabaseType()));
        ConsistencyCheckJobConfiguration checkJobConfig = new PipelineJobConfigurationLoader(jobAPI).getJobConfiguration(checkJobId);
        int expectedSequence = ConsistencyCheckSequence.MIN_SEQUENCE;
        String expectCheckJobId = new ConsistencyCheckJobId(PipelineJobIdUtils.parseContextKey(parentJobId), parentJobId, expectedSequence).marshal();
        assertThat(checkJobConfig.getJobId(), is(expectCheckJobId));
        assertNull(checkJobConfig.getAlgorithmTypeName());
        int sequence = ConsistencyCheckJobId.parseSequence(expectCheckJobId);
        assertThat(sequence, is(expectedSequence));
    }
    
    @Test
    void assertDropByParentJobId() {
        MigrationJobConfiguration parentJobConfig = jobConfigSwapper.swapToObject(JobConfigurationBuilder.createYamlMigrationJobConfiguration());
        String parentJobId = parentJobConfig.getJobId();
        PipelineGovernanceFacade governanceFacade = PipelineAPIFactory.getPipelineGovernanceFacade(PipelineContextUtils.getContextKey());
        int expectedSequence = 1;
        for (int i = 0; i < 3; i++) {
            String checkJobId = jobAPI.createJobAndStart(new CreateConsistencyCheckJobParameter(parentJobId, null, null,
                    parentJobConfig.getSourceDatabaseType(), parentJobConfig.getTargetDatabaseType()));
            ConsistencyCheckJobItemContext checkJobItemContext = new ConsistencyCheckJobItemContext(
                    new ConsistencyCheckJobConfiguration(checkJobId, parentJobId, null, null, TypedSPILoader.getService(DatabaseType.class, "H2")), 0, JobStatus.FINISHED, null);
            jobItemManager.persistProgress(checkJobItemContext);
            Map<String, TableDataConsistencyCheckResult> dataConsistencyCheckResult = Collections.singletonMap("t_order", new TableDataConsistencyCheckResult(true));
            governanceFacade.getJobFacade().getCheck().persistCheckJobResult(parentJobId, checkJobId, dataConsistencyCheckResult);
            Optional<String> latestCheckJobId = governanceFacade.getJobFacade().getCheck().getLatestCheckJobId(parentJobId);
            assertTrue(latestCheckJobId.isPresent());
            assertThat(ConsistencyCheckJobId.parseSequence(latestCheckJobId.get()), is(expectedSequence++));
        }
        expectedSequence = 2;
        for (int i = 0; i < 2; i++) {
            jobAPI.dropByParentJobId(parentJobId);
            Optional<String> latestCheckJobId = governanceFacade.getJobFacade().getCheck().getLatestCheckJobId(parentJobId);
            assertTrue(latestCheckJobId.isPresent());
            assertThat(ConsistencyCheckJobId.parseSequence(latestCheckJobId.get()), is(expectedSequence--));
        }
        jobAPI.dropByParentJobId(parentJobId);
        Optional<String> latestCheckJobId = governanceFacade.getJobFacade().getCheck().getLatestCheckJobId(parentJobId);
        assertFalse(latestCheckJobId.isPresent());
    }
}
