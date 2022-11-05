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

package org.apache.shardingsphere.data.pipeline.scenario.consistencycheck;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.api.config.PipelineTaskConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.job.ConsistencyCheckJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.job.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.job.yaml.YamlPipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.process.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.api.context.PipelineJobItemContext;
import org.apache.shardingsphere.data.pipeline.api.context.PipelineProcessContext;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.JobType;
import org.apache.shardingsphere.data.pipeline.api.job.PipelineJobId;
import org.apache.shardingsphere.data.pipeline.api.job.progress.ConsistencyCheckJobProgress;
import org.apache.shardingsphere.data.pipeline.api.job.progress.PipelineJobItemProgress;
import org.apache.shardingsphere.data.pipeline.api.pojo.ConsistencyCheckJobItemInfo;
import org.apache.shardingsphere.data.pipeline.api.pojo.CreateConsistencyCheckJobParameter;
import org.apache.shardingsphere.data.pipeline.api.pojo.PipelineJobInfo;
import org.apache.shardingsphere.data.pipeline.core.api.GovernanceRepositoryAPI;
import org.apache.shardingsphere.data.pipeline.core.api.InventoryIncrementalJobAPI;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.api.impl.AbstractPipelineJobAPIImpl;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PipelineJobNotFoundException;
import org.apache.shardingsphere.data.pipeline.core.exception.job.UncompletedConsistencyCheckJobExistsException;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.YamlConsistencyCheckJobProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.YamlConsistencyCheckJobProgressSwapper;
import org.apache.shardingsphere.data.pipeline.yaml.job.YamlConsistencyCheckJobConfiguration;
import org.apache.shardingsphere.data.pipeline.yaml.job.YamlConsistencyCheckJobConfigurationSwapper;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Consistency check job API impl.
 */
@Slf4j
public final class ConsistencyCheckJobAPIImpl extends AbstractPipelineJobAPIImpl implements ConsistencyCheckJobAPI {
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    private final YamlConsistencyCheckJobProgressSwapper swapper = new YamlConsistencyCheckJobProgressSwapper();
    
    @Override
    protected String marshalJobIdLeftPart(final PipelineJobId pipelineJobId) {
        ConsistencyCheckJobId jobId = (ConsistencyCheckJobId) pipelineJobId;
        return jobId.getParentJobId() + jobId.getSequence();
    }
    
    @Override
    public String createJobAndStart(final CreateConsistencyCheckJobParameter parameter) {
        GovernanceRepositoryAPI repositoryAPI = PipelineAPIFactory.getGovernanceRepositoryAPI();
        String parentJobId = parameter.getJobId();
        Optional<String> checkLatestJobId = repositoryAPI.getCheckLatestJobId(parentJobId);
        if (checkLatestJobId.isPresent()) {
            PipelineJobItemProgress progress = getJobItemProgress(checkLatestJobId.get(), 0);
            if (null == progress || JobStatus.FINISHED != progress.getStatus()) {
                log.info("check job already exists and status is not FINISHED, progress={}", progress);
                throw new UncompletedConsistencyCheckJobExistsException(checkLatestJobId.get());
            }
        }
        int sequence = checkLatestJobId.map(optional -> ConsistencyCheckJobId.parseSequence(optional) + 1).orElse(ConsistencyCheckJobId.MIN_SEQUENCE);
        String result = marshalJobId(new ConsistencyCheckJobId(parentJobId, sequence));
        repositoryAPI.persistCheckLatestJobId(parentJobId, result);
        repositoryAPI.deleteCheckJobResult(parentJobId, result);
        dropJob(result);
        YamlConsistencyCheckJobConfiguration yamlConfig = new YamlConsistencyCheckJobConfiguration();
        yamlConfig.setJobId(result);
        yamlConfig.setParentJobId(parentJobId);
        yamlConfig.setAlgorithmTypeName(parameter.getAlgorithmTypeName());
        yamlConfig.setAlgorithmProps(parameter.getAlgorithmProps());
        start(new YamlConsistencyCheckJobConfigurationSwapper().swapToObject(yamlConfig));
        return result;
    }
    
    @Override
    public Map<String, DataConsistencyCheckResult> getLatestDataConsistencyCheckResult(final String jobId) {
        Optional<String> checkLatestJobId = PipelineAPIFactory.getGovernanceRepositoryAPI().getCheckLatestJobId(jobId);
        if (!checkLatestJobId.isPresent()) {
            return Collections.emptyMap();
        }
        return PipelineAPIFactory.getGovernanceRepositoryAPI().getCheckJobResult(jobId, checkLatestJobId.get());
    }
    
    @Override
    public void persistJobItemProgress(final PipelineJobItemContext jobItemContext) {
        ConsistencyCheckJobItemContext checkJobItemContext = (ConsistencyCheckJobItemContext) jobItemContext;
        ConsistencyCheckJobProgress jobProgress = new ConsistencyCheckJobProgress();
        jobProgress.setStatus(jobItemContext.getStatus());
        jobProgress.setCheckedRecordsCount(checkJobItemContext.getCheckedRecordsCount().get());
        jobProgress.setRecordsCount(checkJobItemContext.getRecordsCount());
        jobProgress.setCheckBeginTimeMillis(checkJobItemContext.getCheckBeginTimeMillis());
        jobProgress.setCheckEndTimeMillis(checkJobItemContext.getCheckEndTimeMillis());
        jobProgress.setTableNames(String.join(",", checkJobItemContext.getTableNames()));
        YamlConsistencyCheckJobProgress yamlJobProgress = swapper.swapToYamlConfiguration(jobProgress);
        PipelineAPIFactory.getGovernanceRepositoryAPI().persistJobItemProgress(jobItemContext.getJobId(), jobItemContext.getShardingItem(), YamlEngine.marshal(yamlJobProgress));
    }
    
    @Override
    public ConsistencyCheckJobProgress getJobItemProgress(final String jobId, final int shardingItem) {
        String progress = PipelineAPIFactory.getGovernanceRepositoryAPI().getJobItemProgress(jobId, shardingItem);
        return Strings.isNullOrEmpty(progress) ? null : swapper.swapToObject(YamlEngine.unmarshal(progress, YamlConsistencyCheckJobProgress.class, true));
    }
    
    @Override
    public void updateJobItemStatus(final String jobId, final int shardingItem, final JobStatus status) {
        ConsistencyCheckJobProgress jobProgress = getJobItemProgress(jobId, shardingItem);
        if (null == jobProgress) {
            log.warn("updateJobItemStatus, jobProgress is null, jobId={}, shardingItem={}", jobId, shardingItem);
            return;
        }
        jobProgress.setStatus(status);
        PipelineAPIFactory.getGovernanceRepositoryAPI().persistJobItemProgress(jobId, shardingItem, YamlEngine.marshal(swapper.swapToYamlConfiguration(jobProgress)));
    }
    
    @Override
    public void startDisabledJob(final String jobId) {
        log.info("Start disable check job {}", jobId);
        PipelineJobItemProgress jobProgress = getJobItemProgress(jobId, 0);
        if (null != jobProgress && JobStatus.FINISHED == jobProgress.getStatus()) {
            log.info("job status is FINISHED, ignore, jobId={}", jobId);
            return;
        }
        super.startDisabledJob(jobId);
    }
    
    @Override
    public void startByParentJobId(final String parentJobId) {
        log.info("Start check job by parent job id: {}", parentJobId);
        Optional<String> checkLatestJobId = PipelineAPIFactory.getGovernanceRepositoryAPI().getCheckLatestJobId(parentJobId);
        ShardingSpherePreconditions.checkState(checkLatestJobId.isPresent(), () -> new PipelineJobNotFoundException(parentJobId));
        startDisabledJob(checkLatestJobId.get());
    }
    
    @Override
    public void stopByParentJobId(final String parentJobId) {
        log.info("Stop check job by parent job id: {}", parentJobId);
        Optional<String> checkLatestJobId = PipelineAPIFactory.getGovernanceRepositoryAPI().getCheckLatestJobId(parentJobId);
        ShardingSpherePreconditions.checkState(checkLatestJobId.isPresent(), () -> new PipelineJobNotFoundException(parentJobId));
        stop(checkLatestJobId.get());
    }
    
    @Override
    public ConsistencyCheckJobItemInfo getJobItemInfo(final String parentJobId) {
        Optional<String> checkLatestJobId = PipelineAPIFactory.getGovernanceRepositoryAPI().getCheckLatestJobId(parentJobId);
        ShardingSpherePreconditions.checkState(checkLatestJobId.isPresent(), () -> new PipelineJobNotFoundException(parentJobId));
        String checkJobId = checkLatestJobId.get();
        ConsistencyCheckJobProgress jobItemProgress = getJobItemProgress(checkJobId, 0);
        ConsistencyCheckJobItemInfo result = new ConsistencyCheckJobItemInfo();
        if (null == jobItemProgress) {
            return result;
        }
        LocalDateTime checkBeginTime = new Timestamp(jobItemProgress.getCheckBeginTimeMillis()).toLocalDateTime();
        if (null == jobItemProgress.getRecordsCount()) {
            result.setFinishedPercentage(0);
            result.setCheckSuccess(false);
            return result;
        }
        long recordsCount = jobItemProgress.getRecordsCount();
        if (JobStatus.FINISHED == jobItemProgress.getStatus()) {
            result.setFinishedPercentage(100);
            LocalDateTime checkEndTime = new Timestamp(jobItemProgress.getCheckEndTimeMillis()).toLocalDateTime();
            Duration duration = Duration.between(checkBeginTime, checkEndTime);
            result.setDurationSeconds(duration.getSeconds());
            result.setCheckEndTime(DATE_TIME_FORMATTER.format(checkEndTime));
            result.setRemainingSeconds(0L);
        } else {
            long checkedRecordsCount = Math.min(jobItemProgress.getCheckedRecordsCount(), recordsCount);
            result.setFinishedPercentage(0 == recordsCount ? 0 : (int) (checkedRecordsCount * 100 / recordsCount));
            JobConfigurationPOJO jobConfigPOJO = getElasticJobConfigPOJO(checkJobId);
            Long stopTimeMillis = jobConfigPOJO.isDisabled() ? Long.parseLong(jobConfigPOJO.getProps().getProperty("stop_time_millis")) : null;
            long durationMillis = (null != stopTimeMillis ? stopTimeMillis : System.currentTimeMillis()) - jobItemProgress.getCheckBeginTimeMillis();
            result.setDurationSeconds(TimeUnit.MILLISECONDS.toSeconds(durationMillis));
            if (null != stopTimeMillis) {
                result.setCheckEndTime(DATE_TIME_FORMATTER.format(new Timestamp(stopTimeMillis).toLocalDateTime()));
            }
            long remainingMills = (long) ((recordsCount - checkedRecordsCount) * 1.0D / checkedRecordsCount * durationMillis);
            result.setRemainingSeconds(remainingMills / 1000);
        }
        String tableNames = jobItemProgress.getTableNames();
        result.setTableNames(Optional.ofNullable(tableNames).orElse(""));
        result.setCheckBeginTime(DATE_TIME_FORMATTER.format(checkBeginTime));
        result.setErrorMessage(getJobItemErrorMessage(checkJobId, 0));
        Map<String, DataConsistencyCheckResult> checkJobResult = PipelineAPIFactory.getGovernanceRepositoryAPI().getCheckJobResult(parentJobId, checkJobId);
        InventoryIncrementalJobAPI inventoryIncrementalJobAPI = (InventoryIncrementalJobAPI) PipelineAPIFactory.getPipelineJobAPI(PipelineJobIdUtils.parseJobType(parentJobId));
        result.setCheckSuccess(inventoryIncrementalJobAPI.aggregateDataConsistencyCheckResults(parentJobId, checkJobResult));
        return result;
    }
    
    @Override
    public ConsistencyCheckJobConfiguration getJobConfiguration(final String jobId) {
        return getJobConfiguration(getElasticJobConfigPOJO(jobId));
    }
    
    @Override
    protected ConsistencyCheckJobConfiguration getJobConfiguration(final JobConfigurationPOJO jobConfigPOJO) {
        return new YamlConsistencyCheckJobConfigurationSwapper().swapToObject(jobConfigPOJO.getJobParameter());
    }
    
    @Override
    protected YamlPipelineJobConfiguration swapToYamlJobConfiguration(final PipelineJobConfiguration jobConfig) {
        return new YamlConsistencyCheckJobConfigurationSwapper().swapToYamlConfiguration((ConsistencyCheckJobConfiguration) jobConfig);
    }
    
    @Override
    public void extendYamlJobConfiguration(final YamlPipelineJobConfiguration yamlJobConfig) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public PipelineTaskConfiguration buildTaskConfiguration(final PipelineJobConfiguration pipelineJobConfig, final int jobShardingItem, final PipelineProcessConfiguration pipelineProcessConfig) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public PipelineProcessContext buildPipelineProcessContext(final PipelineJobConfiguration pipelineJobConfig) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    protected PipelineJobInfo getJobInfo(final String jobId) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    protected String getJobClassName() {
        return ConsistencyCheckJob.class.getName();
    }
    
    @Override
    public JobType getJobType() {
        return JobType.CONSISTENCY_CHECK;
    }
}
