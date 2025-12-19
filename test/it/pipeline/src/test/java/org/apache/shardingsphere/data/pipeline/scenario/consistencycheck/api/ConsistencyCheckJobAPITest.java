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

package org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.api;

import org.apache.shardingsphere.data.pipeline.core.consistencycheck.pojo.ConsistencyCheckJobItemInfo;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.TableDataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.core.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.core.job.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.id.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.progress.ConsistencyCheckJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.TransmissionJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobConfigurationManager;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobItemManager;
import org.apache.shardingsphere.data.pipeline.core.registrycenter.repository.PipelineGovernanceFacade;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.ConsistencyCheckJobId;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.ConsistencyCheckJobType;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.config.ConsistencyCheckJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.context.ConsistencyCheckJobItemContext;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.util.ConsistencyCheckSequence;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.yaml.swapper.YamlMigrationJobConfigurationSwapper;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.it.data.pipeline.core.util.JobConfigurationBuilder;
import org.apache.shardingsphere.test.it.data.pipeline.core.util.PipelineContextUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ConsistencyCheckJobAPITest {
    
    private final ConsistencyCheckJobType jobType = new ConsistencyCheckJobType();
    
    private final ConsistencyCheckJobAPI jobAPI = new ConsistencyCheckJobAPI(jobType);
    
    private final PipelineJobItemManager<TransmissionJobItemProgress> jobItemManager = new PipelineJobItemManager<>(jobType.getOption().getYamlJobItemProgressSwapper());
    
    private final YamlMigrationJobConfigurationSwapper jobConfigSwapper = new YamlMigrationJobConfigurationSwapper();
    
    @BeforeAll
    static void beforeClass() {
        PipelineContextUtils.initPipelineContextManager();
    }
    
    @Test
    void assertStart() {
        MigrationJobConfiguration parentJobConfig = jobConfigSwapper.swapToObject(JobConfigurationBuilder.createYamlMigrationJobConfiguration());
        String parentJobId = parentJobConfig.getJobId();
        String checkJobId = jobAPI.start(new CreateConsistencyCheckJobParameter(parentJobId, null, null,
                parentJobConfig.getSourceDatabaseType(), parentJobConfig.getTargetDatabaseType()));
        ConsistencyCheckJobConfiguration checkJobConfig = new PipelineJobConfigurationManager(jobType.getOption()).getJobConfiguration(checkJobId);
        int expectedSequence = ConsistencyCheckSequence.MIN_SEQUENCE;
        String expectCheckJobId = PipelineJobIdUtils.marshal(new ConsistencyCheckJobId(PipelineJobIdUtils.parseContextKey(parentJobId), parentJobId, expectedSequence));
        assertThat(checkJobConfig.getJobId(), is(expectCheckJobId));
        assertNull(checkJobConfig.getAlgorithmTypeName());
        int sequence = ConsistencyCheckJobId.parseSequence(expectCheckJobId);
        assertThat(sequence, is(expectedSequence));
        PipelineGovernanceFacade governanceFacade = PipelineAPIFactory.getPipelineGovernanceFacade(PipelineContextUtils.getContextKey());
        Collection<String> actualCheckJobIds = governanceFacade.getJobFacade().getCheck().listCheckJobIds(parentJobId);
        assertThat(actualCheckJobIds.size(), is(1));
        assertThat(actualCheckJobIds.iterator().next(), is(expectCheckJobId));
        jobAPI.drop(parentJobId);
        assertFalse(governanceFacade.getJobFacade().getConfiguration().isExisted(expectCheckJobId));
    }
    
    @Test
    void assertDropByParentJobId() {
        MigrationJobConfiguration parentJobConfig = jobConfigSwapper.swapToObject(JobConfigurationBuilder.createYamlMigrationJobConfiguration());
        String parentJobId = parentJobConfig.getJobId();
        PipelineGovernanceFacade governanceFacade = PipelineAPIFactory.getPipelineGovernanceFacade(PipelineContextUtils.getContextKey());
        int expectedSequence = 1;
        for (int i = 0; i < 3; i++) {
            String checkJobId = jobAPI.start(new CreateConsistencyCheckJobParameter(parentJobId, null, null,
                    parentJobConfig.getSourceDatabaseType(), parentJobConfig.getTargetDatabaseType()));
            ConsistencyCheckJobItemContext checkJobItemContext = new ConsistencyCheckJobItemContext(
                    new ConsistencyCheckJobConfiguration(checkJobId, parentJobId, null, null, TypedSPILoader.getService(DatabaseType.class, "H2")), 0, JobStatus.FINISHED, null);
            jobItemManager.persistProgress(checkJobItemContext);
            Map<String, TableDataConsistencyCheckResult> dataConsistencyCheckResult = Collections.singletonMap("t_order", new TableDataConsistencyCheckResult(true));
            governanceFacade.getJobFacade().getCheck().persistCheckJobResult(parentJobId, checkJobId, dataConsistencyCheckResult);
            String latestCheckJobId = governanceFacade.getJobFacade().getCheck().getLatestCheckJobId(parentJobId);
            assertThat(ConsistencyCheckJobId.parseSequence(latestCheckJobId), is(expectedSequence++));
        }
        expectedSequence = 2;
        for (int i = 0; i < 2; i++) {
            jobAPI.drop(parentJobId);
            String latestCheckJobId = governanceFacade.getJobFacade().getCheck().getLatestCheckJobId(parentJobId);
            assertThat(ConsistencyCheckJobId.parseSequence(latestCheckJobId), is(expectedSequence--));
        }
        jobAPI.drop(parentJobId);
        assertFalse(governanceFacade.getJobFacade().getCheck().findLatestCheckJobId(parentJobId).isPresent());
    }
    
    @Test
    void assertEmptyJobProgress() {
        MigrationJobConfiguration parentJobConfig = jobConfigSwapper.swapToObject(JobConfigurationBuilder.createYamlMigrationJobConfiguration());
        String parentJobId = parentJobConfig.getJobId();
        String checkJobId = jobAPI.start(new CreateConsistencyCheckJobParameter(parentJobId, null, null,
                parentJobConfig.getSourceDatabaseType(), parentJobConfig.getTargetDatabaseType()));
        persistCheckJobProgress(createEmptyCheckJobItemProgress(), checkJobId, JobStatus.RUNNING, 0);
        ConsistencyCheckJobItemInfo actual = jobAPI.getJobItemInfo(parentJobId);
        assertThat(actual.getTableNames(), is(""));
        assertNull(actual.getCheckSuccess());
        assertThat(actual.getCheckFailedTableNames(), is(""));
        assertThat(actual.getIgnoredTableNames(), is(""));
        assertThat(actual.getInventoryFinishedPercentage(), is(0));
        assertThat(actual.getInventoryRemainingSeconds(), is(0L));
        assertNotNull(actual.getCheckBeginTime());
        assertNull(actual.getCheckEndTime());
        assertThat(actual.getDurationSeconds(), is(0L));
    }
    
    private ConsistencyCheckJobItemProgress createEmptyCheckJobItemProgress() {
        ConsistencyCheckJobItemProgress result = new ConsistencyCheckJobItemProgress("", "", 0L, 0L, 0L, 0L, "H2");
        result.setStatus(JobStatus.RUNNING);
        return result;
    }
    
    @Test
    void assertRunningJobProgress() {
        MigrationJobConfiguration parentJobConfig = jobConfigSwapper.swapToObject(JobConfigurationBuilder.createYamlMigrationJobConfiguration());
        String parentJobId = parentJobConfig.getJobId();
        String checkJobId = jobAPI.start(new CreateConsistencyCheckJobParameter(parentJobId, null, null,
                parentJobConfig.getSourceDatabaseType(), parentJobConfig.getTargetDatabaseType()));
        persistCheckJobProgress(createRunningCheckJobItemProgress(), checkJobId, JobStatus.RUNNING, 1000);
        ConsistencyCheckJobItemInfo actual = jobAPI.getJobItemInfo(parentJobId);
        assertNull(actual.getCheckSuccess());
        assertThat(actual.getCheckFailedTableNames(), is(""));
        assertThat(actual.getIgnoredTableNames(), is(""));
        assertThat(actual.getInventoryFinishedPercentage(), is(10));
        assertNotNull(actual.getCheckBeginTime());
        assertNull(actual.getCheckEndTime());
    }
    
    private ConsistencyCheckJobItemProgress createRunningCheckJobItemProgress() {
        ConsistencyCheckJobItemProgress result = new ConsistencyCheckJobItemProgress("t_order", "", 100L, 1000L, 0L, 0L, "H2");
        result.setStatus(JobStatus.RUNNING);
        return result;
    }
    
    @Test
    void assertExecuteInventoryTaskJobProgress() {
        MigrationJobConfiguration parentJobConfig = jobConfigSwapper.swapToObject(JobConfigurationBuilder.createYamlMigrationJobConfiguration());
        String parentJobId = parentJobConfig.getJobId();
        String checkJobId = jobAPI.start(new CreateConsistencyCheckJobParameter(parentJobId, null, null,
                parentJobConfig.getSourceDatabaseType(), parentJobConfig.getTargetDatabaseType()));
        persistCheckJobProgress(createExecuteInventoryTaskCheckJobItemProgress(), checkJobId, JobStatus.EXECUTE_INVENTORY_TASK, 1000);
        ConsistencyCheckJobItemInfo actual = jobAPI.getJobItemInfo(parentJobId);
        assertNull(actual.getCheckSuccess());
        assertThat(actual.getCheckFailedTableNames(), is(""));
        assertThat(actual.getIgnoredTableNames(), is(""));
        assertThat(actual.getInventoryFinishedPercentage(), is(50));
        assertNotNull(actual.getCheckBeginTime());
        assertNull(actual.getCheckEndTime());
    }
    
    private ConsistencyCheckJobItemProgress createExecuteInventoryTaskCheckJobItemProgress() {
        ConsistencyCheckJobItemProgress result = new ConsistencyCheckJobItemProgress("t_order", "", 500L, 1000L, 0L, 0L, "H2");
        result.setStatus(JobStatus.EXECUTE_INVENTORY_TASK);
        return result;
    }
    
    @Test
    void assertExecuteIncrementalTaskJobProgress() {
        MigrationJobConfiguration parentJobConfig = jobConfigSwapper.swapToObject(JobConfigurationBuilder.createYamlMigrationJobConfiguration());
        String parentJobId = parentJobConfig.getJobId();
        String checkJobId = jobAPI.start(new CreateConsistencyCheckJobParameter(parentJobId, null, null,
                parentJobConfig.getSourceDatabaseType(), parentJobConfig.getTargetDatabaseType()));
        persistCheckJobProgress(createExecuteIncrementalTaskCheckJobItemProgress(), checkJobId, JobStatus.EXECUTE_INCREMENTAL_TASK, 1000);
        ConsistencyCheckJobItemInfo actual = jobAPI.getJobItemInfo(parentJobId);
        assertNull(actual.getCheckSuccess());
        assertThat(actual.getCheckFailedTableNames(), is(""));
        assertThat(actual.getIgnoredTableNames(), is(""));
        assertThat(actual.getInventoryFinishedPercentage(), is(100));
        assertThat(actual.getInventoryRemainingSeconds(), is(0L));
        assertNotNull(actual.getCheckBeginTime());
        assertNull(actual.getCheckEndTime());
    }
    
    private ConsistencyCheckJobItemProgress createExecuteIncrementalTaskCheckJobItemProgress() {
        ConsistencyCheckJobItemProgress result = new ConsistencyCheckJobItemProgress("t_order", "", 900L, 1000L, 0L, 0L, "H2");
        result.setStatus(JobStatus.EXECUTE_INCREMENTAL_TASK);
        return result;
    }
    
    @Test
    void assertFinishedJobProgress() {
        MigrationJobConfiguration parentJobConfig = jobConfigSwapper.swapToObject(JobConfigurationBuilder.createYamlMigrationJobConfiguration());
        String parentJobId = parentJobConfig.getJobId();
        String checkJobId = jobAPI.start(new CreateConsistencyCheckJobParameter(parentJobId, null, null,
                parentJobConfig.getSourceDatabaseType(), parentJobConfig.getTargetDatabaseType()));
        persistCheckJobProgress(createFinishedCheckJobItemProgress(), checkJobId, JobStatus.FINISHED, 1000);
        persistCheckJobResult(parentJobId, checkJobId);
        ConsistencyCheckJobItemInfo actual = jobAPI.getJobItemInfo(parentJobId);
        assertThat(actual.getCheckSuccess(), is(true));
        assertThat(actual.getCheckFailedTableNames(), is(""));
        assertThat(actual.getIgnoredTableNames(), is(""));
        assertThat(actual.getInventoryFinishedPercentage(), is(100));
        assertThat(actual.getInventoryRemainingSeconds(), is(0L));
        assertNotNull(actual.getCheckBeginTime());
        assertNotNull(actual.getCheckEndTime());
        assertThat(actual.getDurationSeconds(), is(86400L));
    }
    
    private ConsistencyCheckJobItemProgress createFinishedCheckJobItemProgress() {
        ConsistencyCheckJobItemProgress result = new ConsistencyCheckJobItemProgress("t_order", "", 900L, 1000L, 0L, 0L, "H2");
        result.setStatus(JobStatus.FINISHED);
        return result;
    }
    
    private void persistCheckJobResult(final String parentJobId, final String checkJobId) {
        Map<String, TableDataConsistencyCheckResult> dataConsistencyCheckResult = Collections.singletonMap("t_order", new TableDataConsistencyCheckResult(true));
        PipelineGovernanceFacade governanceFacade = PipelineAPIFactory.getPipelineGovernanceFacade(PipelineContextUtils.getContextKey());
        governanceFacade.getJobFacade().getCheck().persistCheckJobResult(parentJobId, checkJobId, dataConsistencyCheckResult);
    }
    
    private void persistCheckJobProgress(final ConsistencyCheckJobItemProgress checkJobItemProgress, final String checkJobId, final JobStatus jobStatus, final int recordCount) {
        ConsistencyCheckJobConfiguration checkJobConfig = new PipelineJobConfigurationManager(jobType.getOption()).getJobConfiguration(checkJobId);
        ConsistencyCheckJobItemContext checkJobItemContext = new ConsistencyCheckJobItemContext(checkJobConfig, 0, jobStatus, checkJobItemProgress);
        LocalDateTime checkBeginTime = new Timestamp(checkJobItemContext.getProgressContext().getCheckBeginTimeMillis()).toLocalDateTime();
        checkJobItemContext.getProgressContext().setRecordsCount(recordCount);
        checkJobItemContext.getProgressContext().setCheckEndTimeMillis(Timestamp.valueOf(checkBeginTime.plusDays(1)).getTime());
        jobItemManager.persistProgress(checkJobItemContext);
    }
}
