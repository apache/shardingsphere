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

package org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.api.impl;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.common.config.job.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.common.config.job.yaml.YamlPipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.common.config.process.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.common.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.common.context.PipelineJobItemContext;
import org.apache.shardingsphere.data.pipeline.common.context.PipelineProcessContext;
import org.apache.shardingsphere.data.pipeline.common.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.common.job.progress.ConsistencyCheckJobItemProgress;
import org.apache.shardingsphere.data.pipeline.common.job.progress.yaml.YamlConsistencyCheckJobItemProgress;
import org.apache.shardingsphere.data.pipeline.common.job.progress.yaml.YamlConsistencyCheckJobItemProgressSwapper;
import org.apache.shardingsphere.data.pipeline.common.pojo.ConsistencyCheckJobItemInfo;
import org.apache.shardingsphere.data.pipeline.common.pojo.PipelineJobInfo;
import org.apache.shardingsphere.data.pipeline.common.registrycenter.repository.GovernanceRepositoryAPI;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.ConsistencyCheckJobItemProgressContext;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.TableDataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.table.TableDataConsistencyCheckerFactory;
import org.apache.shardingsphere.data.pipeline.core.exception.data.UnsupportedPipelineDatabaseTypeException;
import org.apache.shardingsphere.data.pipeline.core.exception.job.ConsistencyCheckJobNotFoundException;
import org.apache.shardingsphere.data.pipeline.core.exception.job.UncompletedConsistencyCheckJobExistsException;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.service.InventoryIncrementalJobAPI;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobAPI;
import org.apache.shardingsphere.data.pipeline.core.job.service.impl.AbstractPipelineJobAPIImpl;
import org.apache.shardingsphere.data.pipeline.core.task.config.PipelineTaskConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.ConsistencyCheckJob;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.ConsistencyCheckJobId;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.api.pojo.CreateConsistencyCheckJobParameter;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.config.ConsistencyCheckJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.config.yaml.YamlConsistencyCheckJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.config.yaml.YamlConsistencyCheckJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.context.ConsistencyCheckJobItemContext;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.util.ConsistencyCheckSequence;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Consistency check job API.
 */
@Slf4j
public final class ConsistencyCheckJobAPI extends AbstractPipelineJobAPIImpl {
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    private final YamlConsistencyCheckJobItemProgressSwapper swapper = new YamlConsistencyCheckJobItemProgressSwapper();
    
    /**
     * Create consistency check configuration and start job.
     *
     * @param param create consistency check job parameter
     * @return job id
     * @throws UncompletedConsistencyCheckJobExistsException uncompleted consistency check job exists exception
     */
    public String createJobAndStart(final CreateConsistencyCheckJobParameter param) {
        String parentJobId = param.getParentJobId();
        GovernanceRepositoryAPI repositoryAPI = PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineJobIdUtils.parseContextKey(parentJobId));
        Optional<String> latestCheckJobId = repositoryAPI.getLatestCheckJobId(parentJobId);
        if (latestCheckJobId.isPresent()) {
            Optional<ConsistencyCheckJobItemProgress> progress = getJobItemProgress(latestCheckJobId.get(), 0);
            if (!progress.isPresent() || JobStatus.FINISHED != progress.get().getStatus()) {
                log.info("check job already exists and status is not FINISHED, progress={}", progress);
                throw new UncompletedConsistencyCheckJobExistsException(latestCheckJobId.get());
            }
        }
        verifyPipelineDatabaseType(param);
        PipelineContextKey contextKey = PipelineJobIdUtils.parseContextKey(parentJobId);
        String result = latestCheckJobId.map(s -> new ConsistencyCheckJobId(contextKey, parentJobId, s)).orElseGet(() -> new ConsistencyCheckJobId(contextKey, parentJobId)).marshal();
        repositoryAPI.persistLatestCheckJobId(parentJobId, result);
        repositoryAPI.deleteCheckJobResult(parentJobId, result);
        dropJob(result);
        YamlConsistencyCheckJobConfiguration yamlConfig = new YamlConsistencyCheckJobConfiguration();
        yamlConfig.setJobId(result);
        yamlConfig.setParentJobId(parentJobId);
        yamlConfig.setAlgorithmTypeName(param.getAlgorithmTypeName());
        yamlConfig.setAlgorithmProps(param.getAlgorithmProps());
        yamlConfig.setSourceDatabaseType(param.getSourceDatabaseType().getType());
        start(new YamlConsistencyCheckJobConfigurationSwapper().swapToObject(yamlConfig));
        return result;
    }
    
    private void verifyPipelineDatabaseType(final CreateConsistencyCheckJobParameter param) {
        Collection<DatabaseType> supportedDatabaseTypes = TableDataConsistencyCheckerFactory.newInstance(param.getAlgorithmTypeName(), param.getAlgorithmProps()).getSupportedDatabaseTypes();
        ShardingSpherePreconditions.checkState(supportedDatabaseTypes.contains(param.getSourceDatabaseType()), () -> new UnsupportedPipelineDatabaseTypeException(param.getSourceDatabaseType()));
        ShardingSpherePreconditions.checkState(supportedDatabaseTypes.contains(param.getTargetDatabaseType()), () -> new UnsupportedPipelineDatabaseTypeException(param.getTargetDatabaseType()));
    }
    
    /**
     * Get latest data consistency check result.
     *
     * @param parentJobId parent job id
     * @return latest data consistency check result
     */
    public Map<String, TableDataConsistencyCheckResult> getLatestDataConsistencyCheckResult(final String parentJobId) {
        GovernanceRepositoryAPI governanceRepositoryAPI = PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineJobIdUtils.parseContextKey(parentJobId));
        Optional<String> latestCheckJobId = governanceRepositoryAPI.getLatestCheckJobId(parentJobId);
        if (!latestCheckJobId.isPresent()) {
            return Collections.emptyMap();
        }
        return governanceRepositoryAPI.getCheckJobResult(parentJobId, latestCheckJobId.get());
    }
    
    @Override
    public void persistJobItemProgress(final PipelineJobItemContext jobItemContext) {
        PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineJobIdUtils.parseContextKey(jobItemContext.getJobId()))
                .persistJobItemProgress(jobItemContext.getJobId(), jobItemContext.getShardingItem(), convertJobItemProgress(jobItemContext));
    }
    
    private String convertJobItemProgress(final PipelineJobItemContext jobItemContext) {
        ConsistencyCheckJobItemContext context = (ConsistencyCheckJobItemContext) jobItemContext;
        ConsistencyCheckJobItemProgressContext progressContext = context.getProgressContext();
        String tableNames = String.join(",", progressContext.getTableNames());
        String ignoredTableNames = String.join(",", progressContext.getIgnoredTableNames());
        ConsistencyCheckJobItemProgress jobItemProgress = new ConsistencyCheckJobItemProgress(tableNames, ignoredTableNames, progressContext.getCheckedRecordsCount().get(),
                progressContext.getRecordsCount(), progressContext.getCheckBeginTimeMillis(), progressContext.getCheckEndTimeMillis(),
                progressContext.getSourceTableCheckPositions(), progressContext.getTargetTableCheckPositions(), progressContext.getSourceDatabaseType());
        jobItemProgress.setStatus(context.getStatus());
        return YamlEngine.marshal(swapper.swapToYamlConfiguration(jobItemProgress));
    }
    
    @Override
    public void updateJobItemProgress(final PipelineJobItemContext jobItemContext) {
        PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineJobIdUtils.parseContextKey(jobItemContext.getJobId()))
                .updateJobItemProgress(jobItemContext.getJobId(), jobItemContext.getShardingItem(), convertJobItemProgress(jobItemContext));
    }
    
    @Override
    public Optional<ConsistencyCheckJobItemProgress> getJobItemProgress(final String jobId, final int shardingItem) {
        Optional<String> progress = PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineJobIdUtils.parseContextKey(jobId)).getJobItemProgress(jobId, shardingItem);
        return progress.map(s -> swapper.swapToObject(YamlEngine.unmarshal(s, YamlConsistencyCheckJobItemProgress.class, true)));
    }
    
    @Override
    public void updateJobItemStatus(final String jobId, final int shardingItem, final JobStatus status) {
        Optional<ConsistencyCheckJobItemProgress> jobItemProgress = getJobItemProgress(jobId, shardingItem);
        if (!jobItemProgress.isPresent()) {
            log.warn("updateJobItemStatus, jobProgress is null, jobId={}, shardingItem={}", jobId, shardingItem);
            return;
        }
        jobItemProgress.get().setStatus(status);
        PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineJobIdUtils.parseContextKey(jobId)).updateJobItemProgress(jobId, shardingItem,
                YamlEngine.marshal(swapper.swapToYamlConfiguration(jobItemProgress.get())));
    }
    
    @Override
    public void startDisabledJob(final String jobId) {
        Optional<ConsistencyCheckJobItemProgress> jobItemProgress = getJobItemProgress(jobId, 0);
        if (jobItemProgress.isPresent() && JobStatus.FINISHED == jobItemProgress.get().getStatus()) {
            log.info("job status is FINISHED, ignore, jobId={}", jobId);
            return;
        }
        super.startDisabledJob(jobId);
    }
    
    /**
     * Start by parent job id.
     *
     * @param parentJobId parent job id
     */
    public void startByParentJobId(final String parentJobId) {
        startDisabledJob(getLatestCheckJobId(parentJobId));
    }
    
    private String getLatestCheckJobId(final String parentJobId) {
        Optional<String> result = PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineJobIdUtils.parseContextKey(parentJobId)).getLatestCheckJobId(parentJobId);
        ShardingSpherePreconditions.checkState(result.isPresent(), () -> new ConsistencyCheckJobNotFoundException(parentJobId));
        return result.get();
    }
    
    /**
     * Start by parent job id.
     *
     * @param parentJobId parent job id
     */
    public void stopByParentJobId(final String parentJobId) {
        stop(getLatestCheckJobId(parentJobId));
    }
    
    /**
     * Drop by parent job id.
     *
     * @param parentJobId parent job id
     */
    public void dropByParentJobId(final String parentJobId) {
        String latestCheckJobId = getLatestCheckJobId(parentJobId);
        stop(latestCheckJobId);
        PipelineContextKey contextKey = PipelineJobIdUtils.parseContextKey(parentJobId);
        GovernanceRepositoryAPI repositoryAPI = PipelineAPIFactory.getGovernanceRepositoryAPI(contextKey);
        Collection<String> checkJobIds = repositoryAPI.listCheckJobIds(parentJobId);
        Optional<Integer> previousSequence = ConsistencyCheckSequence.getPreviousSequence(
                checkJobIds.stream().map(ConsistencyCheckJobId::parseSequence).collect(Collectors.toList()), ConsistencyCheckJobId.parseSequence(latestCheckJobId));
        if (previousSequence.isPresent()) {
            String checkJobId = new ConsistencyCheckJobId(contextKey, parentJobId, previousSequence.get()).marshal();
            repositoryAPI.persistLatestCheckJobId(parentJobId, checkJobId);
        } else {
            repositoryAPI.deleteLatestCheckJobId(parentJobId);
        }
        repositoryAPI.deleteCheckJobResult(parentJobId, latestCheckJobId);
        dropJob(latestCheckJobId);
    }
    
    /**
     * Get consistency job item infos.
     *
     * @param parentJobId parent job id
     * @return consistency job item infos
     */
    public List<ConsistencyCheckJobItemInfo> getJobItemInfos(final String parentJobId) {
        GovernanceRepositoryAPI governanceRepositoryAPI = PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineJobIdUtils.parseContextKey(parentJobId));
        Optional<String> latestCheckJobId = governanceRepositoryAPI.getLatestCheckJobId(parentJobId);
        ShardingSpherePreconditions.checkState(latestCheckJobId.isPresent(), () -> new ConsistencyCheckJobNotFoundException(parentJobId));
        String checkJobId = latestCheckJobId.get();
        Optional<ConsistencyCheckJobItemProgress> progress = getJobItemProgress(checkJobId, 0);
        if (!progress.isPresent()) {
            return Collections.emptyList();
        }
        List<ConsistencyCheckJobItemInfo> result = new LinkedList<>();
        ConsistencyCheckJobItemProgress jobItemProgress = progress.get();
        if (!Strings.isNullOrEmpty(jobItemProgress.getIgnoredTableNames())) {
            Map<String, TableDataConsistencyCheckResult> checkJobResult = governanceRepositoryAPI.getCheckJobResult(parentJobId, latestCheckJobId.get());
            result.addAll(buildIgnoredTableInfo(jobItemProgress.getIgnoredTableNames().split(","), checkJobResult));
        }
        if (Objects.equals(jobItemProgress.getIgnoredTableNames(), jobItemProgress.getTableNames())) {
            return result;
        }
        result.add(getJobItemInfo(parentJobId));
        return result;
    }
    
    private List<ConsistencyCheckJobItemInfo> buildIgnoredTableInfo(final String[] ignoredTables, final Map<String, TableDataConsistencyCheckResult> checkJobResult) {
        if (null == ignoredTables) {
            return Collections.emptyList();
        }
        List<ConsistencyCheckJobItemInfo> result = new LinkedList<>();
        for (String each : ignoredTables) {
            ConsistencyCheckJobItemInfo info = new ConsistencyCheckJobItemInfo();
            info.setTableNames(each);
            info.setCheckSuccess(null);
            TableDataConsistencyCheckResult checkResult = checkJobResult.get(each);
            if (null != checkResult && checkResult.isIgnored()) {
                info.setErrorMessage(checkResult.getIgnoredType().getMessage());
            }
            result.add(info);
        }
        return result;
    }
    
    private ConsistencyCheckJobItemInfo getJobItemInfo(final String parentJobId) {
        GovernanceRepositoryAPI governanceRepositoryAPI = PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineJobIdUtils.parseContextKey(parentJobId));
        Optional<String> latestCheckJobId = governanceRepositoryAPI.getLatestCheckJobId(parentJobId);
        ShardingSpherePreconditions.checkState(latestCheckJobId.isPresent(), () -> new ConsistencyCheckJobNotFoundException(parentJobId));
        String checkJobId = latestCheckJobId.get();
        Optional<ConsistencyCheckJobItemProgress> progress = getJobItemProgress(checkJobId, 0);
        ConsistencyCheckJobItemInfo result = new ConsistencyCheckJobItemInfo();
        JobConfigurationPOJO jobConfigPOJO = getElasticJobConfigPOJO(checkJobId);
        result.setActive(!jobConfigPOJO.isDisabled());
        if (!progress.isPresent()) {
            return result;
        }
        ConsistencyCheckJobItemProgress jobItemProgress = progress.get();
        if (null == jobItemProgress.getRecordsCount() || null == jobItemProgress.getCheckedRecordsCount()) {
            result.setInventoryFinishedPercentage(0);
            result.setCheckSuccess(null);
            return result;
        }
        fillInJobItemInfoWithTimes(result, jobItemProgress, jobConfigPOJO);
        result.setTableNames(Optional.ofNullable(jobItemProgress.getTableNames()).orElse(""));
        fillInJobItemInfoWithCheckAlgorithm(result, checkJobId);
        result.setErrorMessage(getJobItemErrorMessage(checkJobId, 0));
        Map<String, TableDataConsistencyCheckResult> checkJobResult = governanceRepositoryAPI.getCheckJobResult(parentJobId, checkJobId);
        fillInJobItemInfoWithCheckResult(result, checkJobResult, parentJobId);
        result.setCheckFailedTableNames(checkJobResult.entrySet().stream().filter(each -> !each.getValue().isIgnored() && !each.getValue().isMatched())
                .map(Entry::getKey).collect(Collectors.joining(",")));
        return result;
    }
    
    private void fillInJobItemInfoWithTimes(final ConsistencyCheckJobItemInfo result, final ConsistencyCheckJobItemProgress jobItemProgress, final JobConfigurationPOJO jobConfigPOJO) {
        long recordsCount = jobItemProgress.getRecordsCount();
        long checkedRecordsCount = Math.min(jobItemProgress.getCheckedRecordsCount(), recordsCount);
        LocalDateTime checkBeginTime = new Timestamp(jobItemProgress.getCheckBeginTimeMillis()).toLocalDateTime();
        result.setCheckBeginTime(DATE_TIME_FORMATTER.format(checkBeginTime));
        if (JobStatus.FINISHED == jobItemProgress.getStatus()) {
            result.setInventoryFinishedPercentage(100);
            LocalDateTime checkEndTime = new Timestamp(jobItemProgress.getCheckEndTimeMillis()).toLocalDateTime();
            Duration duration = Duration.between(checkBeginTime, checkEndTime);
            result.setDurationSeconds(duration.getSeconds());
            result.setCheckEndTime(DATE_TIME_FORMATTER.format(checkEndTime));
            result.setInventoryRemainingSeconds(0L);
        } else if (0 != recordsCount && 0 != checkedRecordsCount) {
            result.setInventoryFinishedPercentage((int) (checkedRecordsCount * 100 / recordsCount));
            Long stopTimeMillis = jobConfigPOJO.isDisabled() ? Long.parseLong(jobConfigPOJO.getProps().getProperty("stop_time_millis")) : null;
            long durationMillis = (null != stopTimeMillis ? stopTimeMillis : System.currentTimeMillis()) - jobItemProgress.getCheckBeginTimeMillis();
            result.setDurationSeconds(TimeUnit.MILLISECONDS.toSeconds(durationMillis));
            if (null != stopTimeMillis) {
                result.setCheckEndTime(DATE_TIME_FORMATTER.format(new Timestamp(stopTimeMillis).toLocalDateTime()));
            }
            long remainingMills = Math.max(0, (long) ((recordsCount - checkedRecordsCount) * 1.0D / checkedRecordsCount * durationMillis));
            result.setInventoryRemainingSeconds(remainingMills / 1000);
        }
    }
    
    private void fillInJobItemInfoWithCheckAlgorithm(final ConsistencyCheckJobItemInfo result, final String checkJobId) {
        ConsistencyCheckJobConfiguration jobConfig = getJobConfiguration(checkJobId);
        result.setAlgorithmType(jobConfig.getAlgorithmTypeName());
        if (null != jobConfig.getAlgorithmProps()) {
            result.setAlgorithmProps(jobConfig.getAlgorithmProps().entrySet().stream().map(entry -> String.format("'%s'='%s'", entry.getKey(), entry.getValue())).collect(Collectors.joining(",")));
        }
    }
    
    private void fillInJobItemInfoWithCheckResult(final ConsistencyCheckJobItemInfo result, final Map<String, TableDataConsistencyCheckResult> checkJobResult, final String parentJobId) {
        if (checkJobResult.isEmpty()) {
            result.setCheckSuccess(null);
        } else {
            InventoryIncrementalJobAPI inventoryIncrementalJobAPI = (InventoryIncrementalJobAPI) TypedSPILoader.getService(
                    PipelineJobAPI.class, PipelineJobIdUtils.parseJobType(parentJobId).getType());
            result.setCheckSuccess(inventoryIncrementalJobAPI.aggregateDataConsistencyCheckResults(parentJobId, checkJobResult));
        }
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
    public void extendYamlJobConfiguration(final PipelineContextKey contextKey, final YamlPipelineJobConfiguration yamlJobConfig) {
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
    public String getType() {
        return "CONSISTENCY_CHECK";
    }
}
