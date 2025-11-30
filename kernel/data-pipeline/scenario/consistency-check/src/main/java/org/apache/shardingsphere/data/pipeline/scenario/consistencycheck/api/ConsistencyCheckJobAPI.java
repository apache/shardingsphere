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
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.table.TableDataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.table.TableDataConsistencyCheckerFactory;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.core.exception.data.UnsupportedPipelineDatabaseTypeException;
import org.apache.shardingsphere.data.pipeline.core.exception.job.UncompletedConsistencyCheckJobExistsException;
import org.apache.shardingsphere.data.pipeline.core.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.core.job.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.id.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.progress.ConsistencyCheckJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.swapper.YamlConsistencyCheckJobItemProgressSwapper;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobConfigurationManager;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobItemManager;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobManager;
import org.apache.shardingsphere.data.pipeline.core.registrycenter.repository.PipelineGovernanceFacade;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.ConsistencyCheckJobId;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.ConsistencyCheckJobType;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.config.ConsistencyCheckJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.config.yaml.config.YamlConsistencyCheckJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.config.yaml.swapper.YamlConsistencyCheckJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.util.ConsistencyCheckSequence;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.datetime.DateTimeFormatterFactory;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Consistency check job API.
 */
public final class ConsistencyCheckJobAPI {
    
    private final YamlConsistencyCheckJobItemProgressSwapper progressSwapper;
    
    private final PipelineJobManager jobManager;
    
    private final PipelineJobConfigurationManager jobConfigManager;
    
    private final PipelineJobItemManager<ConsistencyCheckJobItemProgress> jobItemManager;
    
    public ConsistencyCheckJobAPI(final ConsistencyCheckJobType jobType) {
        progressSwapper = (YamlConsistencyCheckJobItemProgressSwapper) jobType.getOption().getYamlJobItemProgressSwapper();
        jobManager = new PipelineJobManager(jobType);
        jobConfigManager = new PipelineJobConfigurationManager(jobType.getOption());
        jobItemManager = new PipelineJobItemManager<>(progressSwapper);
    }
    
    /**
     * Start consistency check job.
     *
     * @param param create consistency check job parameter
     * @return job id
     */
    public String start(final CreateConsistencyCheckJobParameter param) {
        String parentJobId = param.getParentJobId();
        PipelineGovernanceFacade governanceFacade = PipelineAPIFactory.getPipelineGovernanceFacade(PipelineJobIdUtils.parseContextKey(parentJobId));
        Optional<String> latestCheckJobId = governanceFacade.getJobFacade().getCheck().findLatestCheckJobId(parentJobId);
        if (latestCheckJobId.isPresent()) {
            Optional<ConsistencyCheckJobItemProgress> progress = new PipelineJobItemManager<ConsistencyCheckJobItemProgress>(progressSwapper).getProgress(latestCheckJobId.get(), 0);
            ShardingSpherePreconditions.checkState(progress.isPresent() && (JobStatus.FINISHED == progress.get().getStatus() || JobStatus.CONSISTENCY_CHECK_FAILURE == progress.get().getStatus()),
                    () -> new UncompletedConsistencyCheckJobExistsException(latestCheckJobId.get(), progress.orElse(null)));
        }
        checkPipelineDatabaseTypes(param);
        PipelineContextKey contextKey = PipelineJobIdUtils.parseContextKey(parentJobId);
        String result = PipelineJobIdUtils.marshal(
                latestCheckJobId.map(optional -> new ConsistencyCheckJobId(contextKey, parentJobId, optional)).orElseGet(() -> new ConsistencyCheckJobId(contextKey, parentJobId)));
        governanceFacade.getJobFacade().getCheck().persistLatestCheckJobId(parentJobId, result);
        governanceFacade.getJobFacade().getCheck().initCheckJobResult(parentJobId, result);
        jobManager.drop(result);
        jobManager.start(new YamlConsistencyCheckJobConfigurationSwapper().swapToObject(getYamlConfiguration(result, parentJobId, param)));
        return result;
    }
    
    private void checkPipelineDatabaseTypes(final CreateConsistencyCheckJobParameter param) {
        Collection<DatabaseType> supportedDatabaseTypes;
        try (TableDataConsistencyChecker checker = TableDataConsistencyCheckerFactory.newInstance(param.getAlgorithmTypeName(), param.getAlgorithmProps())) {
            supportedDatabaseTypes = checker.getSupportedDatabaseTypes();
        }
        ShardingSpherePreconditions.checkContains(supportedDatabaseTypes, param.getSourceDatabaseType(), () -> new UnsupportedPipelineDatabaseTypeException(param.getSourceDatabaseType()));
        ShardingSpherePreconditions.checkContains(supportedDatabaseTypes, param.getTargetDatabaseType(), () -> new UnsupportedPipelineDatabaseTypeException(param.getTargetDatabaseType()));
    }
    
    private YamlConsistencyCheckJobConfiguration getYamlConfiguration(final String jobId, final String parentJobId, final CreateConsistencyCheckJobParameter param) {
        YamlConsistencyCheckJobConfiguration result = new YamlConsistencyCheckJobConfiguration();
        result.setJobId(jobId);
        result.setParentJobId(parentJobId);
        result.setAlgorithmTypeName(param.getAlgorithmTypeName());
        result.setAlgorithmProps(param.getAlgorithmProps());
        result.setSourceDatabaseType(param.getSourceDatabaseType().getType());
        return result;
    }
    
    /**
     * Resume disabled consistency check job.
     *
     * @param parentJobId parent job id
     */
    public void resume(final String parentJobId) {
        jobManager.resume(PipelineAPIFactory.getPipelineGovernanceFacade(PipelineJobIdUtils.parseContextKey(parentJobId)).getJobFacade().getCheck().getLatestCheckJobId(parentJobId));
    }
    
    /**
     * Stop consistency check job.
     *
     * @param parentJobId parent job id
     */
    public void stop(final String parentJobId) {
        jobManager.stop(PipelineAPIFactory.getPipelineGovernanceFacade(PipelineJobIdUtils.parseContextKey(parentJobId)).getJobFacade().getCheck().getLatestCheckJobId(parentJobId));
    }
    
    /**
     * Drop consistency check job.
     *
     * @param parentJobId parent job id
     */
    public void drop(final String parentJobId) {
        String latestCheckJobId = PipelineAPIFactory.getPipelineGovernanceFacade(PipelineJobIdUtils.parseContextKey(parentJobId)).getJobFacade().getCheck().getLatestCheckJobId(parentJobId);
        jobManager.stop(latestCheckJobId);
        PipelineContextKey contextKey = PipelineJobIdUtils.parseContextKey(parentJobId);
        PipelineGovernanceFacade governanceFacade = PipelineAPIFactory.getPipelineGovernanceFacade(contextKey);
        Collection<String> checkJobIds = governanceFacade.getJobFacade().getCheck().listCheckJobIds(parentJobId);
        Optional<Integer> previousSequence = ConsistencyCheckSequence.getPreviousSequence(
                checkJobIds.stream().map(ConsistencyCheckJobId::parseSequence).collect(Collectors.toList()), ConsistencyCheckJobId.parseSequence(latestCheckJobId));
        if (previousSequence.isPresent()) {
            String checkJobId = PipelineJobIdUtils.marshal(new ConsistencyCheckJobId(contextKey, parentJobId, previousSequence.get()));
            governanceFacade.getJobFacade().getCheck().persistLatestCheckJobId(parentJobId, checkJobId);
        } else {
            governanceFacade.getJobFacade().getCheck().deleteLatestCheckJobId(parentJobId);
        }
        governanceFacade.getJobFacade().getCheck().deleteCheckJobResult(parentJobId, latestCheckJobId);
        jobManager.drop(latestCheckJobId);
    }
    
    /**
     * Get consistency check job item info.
     *
     * @param parentJobId parent job id
     * @return consistency check job item info
     */
    public ConsistencyCheckJobItemInfo getJobItemInfo(final String parentJobId) {
        String latestCheckJobId = PipelineAPIFactory.getPipelineGovernanceFacade(PipelineJobIdUtils.parseContextKey(parentJobId)).getJobFacade().getCheck().getLatestCheckJobId(parentJobId);
        ConsistencyCheckJobItemInfo result = new ConsistencyCheckJobItemInfo();
        JobConfigurationPOJO jobConfigPOJO = PipelineJobIdUtils.getElasticJobConfigurationPOJO(latestCheckJobId);
        result.setActive(!jobConfigPOJO.isDisabled());
        Optional<ConsistencyCheckJobItemProgress> progress = jobItemManager.getProgress(latestCheckJobId, 0);
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
        fillInJobItemInfoWithCheckAlgorithm(result, latestCheckJobId);
        result.setErrorMessage(PipelineAPIFactory.getPipelineGovernanceFacade(PipelineJobIdUtils.parseContextKey(latestCheckJobId)).getJobItemFacade().getErrorMessage().load(latestCheckJobId, 0));
        Map<String, TableDataConsistencyCheckResult> checkJobResults = PipelineAPIFactory.getPipelineGovernanceFacade(PipelineJobIdUtils.parseContextKey(parentJobId))
                .getJobFacade().getCheck().getCheckJobResult(parentJobId, latestCheckJobId);
        result.setCheckSuccess(checkJobResults.isEmpty() ? null : checkJobResults.values().stream().allMatch(TableDataConsistencyCheckResult::isSuccessful));
        result.setCheckFailedTableNames(checkJobResults.entrySet().stream().filter(each -> !each.getValue().isSuccessful())
                .map(Entry::getKey).collect(Collectors.joining(",")));
        result.setIgnoredTableNames(checkJobResults.entrySet().stream().filter(each -> each.getValue().isIgnored()).map(Entry::getKey).collect(Collectors.joining(",")));
        return result;
    }
    
    private void fillInJobItemInfoWithTimes(final ConsistencyCheckJobItemInfo result, final ConsistencyCheckJobItemProgress jobItemProgress, final JobConfigurationPOJO jobConfigPOJO) {
        long recordsCount = jobItemProgress.getRecordsCount();
        long checkedRecordsCount = Math.min(jobItemProgress.getCheckedRecordsCount(), recordsCount);
        LocalDateTime checkBeginTime = new Timestamp(jobItemProgress.getCheckBeginTimeMillis()).toLocalDateTime();
        result.setCheckBeginTime(DateTimeFormatterFactory.getLongMillisDatetimeFormatter().format(checkBeginTime));
        if (JobStatus.FINISHED == jobItemProgress.getStatus()) {
            LocalDateTime checkEndTime = new Timestamp(jobItemProgress.getCheckEndTimeMillis()).toLocalDateTime();
            Duration duration = Duration.between(checkBeginTime, checkEndTime);
            result.setDurationSeconds(duration.getSeconds());
            result.setCheckEndTime(DateTimeFormatterFactory.getLongMillisDatetimeFormatter().format(checkEndTime));
            result.setInventoryRemainingSeconds(0L);
        } else if (0L != recordsCount && 0L != checkedRecordsCount) {
            result.setInventoryFinishedPercentage(Math.min(100, (int) (checkedRecordsCount * 100L / recordsCount)));
            LocalDateTime stopTime = jobConfigPOJO.isDisabled() ? LocalDateTime.from(DateTimeFormatterFactory.getDatetimeFormatter().parse(jobConfigPOJO.getProps().getProperty("stop_time")))
                    : null;
            long durationMillis = (null != stopTime ? Timestamp.valueOf(stopTime).getTime() : System.currentTimeMillis()) - jobItemProgress.getCheckBeginTimeMillis();
            result.setDurationSeconds(TimeUnit.MILLISECONDS.toSeconds(durationMillis));
            if (null != stopTime) {
                result.setCheckEndTime(jobConfigPOJO.getProps().getProperty("stop_time"));
            }
            long remainingMillis = Math.max(0L, (long) ((recordsCount - checkedRecordsCount) * 1.0D / checkedRecordsCount * durationMillis));
            result.setInventoryRemainingSeconds(remainingMillis / 1000L);
        }
        if (JobStatus.EXECUTE_INCREMENTAL_TASK == jobItemProgress.getStatus() || JobStatus.FINISHED == jobItemProgress.getStatus()) {
            result.setInventoryFinishedPercentage(100);
            result.setInventoryRemainingSeconds(0L);
        }
    }
    
    private void fillInJobItemInfoWithCheckAlgorithm(final ConsistencyCheckJobItemInfo result, final String checkJobId) {
        ConsistencyCheckJobConfiguration jobConfig = jobConfigManager.getJobConfiguration(checkJobId);
        result.setAlgorithmType(jobConfig.getAlgorithmTypeName());
        if (null != jobConfig.getAlgorithmProps()) {
            result.setAlgorithmProps(jobConfig.getAlgorithmProps().entrySet().stream().map(entry -> String.format("'%s'='%s'", entry.getKey(), entry.getValue())).collect(Collectors.joining(",")));
        }
    }
}
