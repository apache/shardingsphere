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

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.RuleAlteredJobAPI;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.RuleAlteredJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.WorkflowConfiguration;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.progress.JobProgress;
import org.apache.shardingsphere.data.pipeline.api.pojo.DataConsistencyCheckAlgorithmInfo;
import org.apache.shardingsphere.data.pipeline.api.pojo.JobInfo;
import org.apache.shardingsphere.data.pipeline.core.api.GovernanceRepositoryAPI;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.check.consistency.DataConsistencyCalculateAlgorithmFactory;
import org.apache.shardingsphere.data.pipeline.core.check.consistency.DataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.core.constant.DataPipelineConstants;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContext;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineJobCreationException;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineJobExecutionException;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineVerifyFailedException;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.RuleAlteredContext;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.RuleAlteredJob;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.RuleAlteredJobProgressDetector;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.RuleAlteredJobSchedulerCenter;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.RuleAlteredJobWorker;
import org.apache.shardingsphere.data.pipeline.spi.check.consistency.DataConsistencyCalculateAlgorithm;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.domain.JobBriefInfo;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.lock.LockContext;
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.rule.ScalingTaskFinishedEvent;
import org.apache.shardingsphere.scaling.core.job.environment.ScalingEnvironmentManager;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Rule altered job API impl.
 */
@Slf4j
public final class RuleAlteredJobAPIImpl extends AbstractPipelineJobAPIImpl implements RuleAlteredJobAPI {
    
    @Override
    public List<JobInfo> list() {
        checkModeConfig();
        return getJobBriefInfos().map(each -> getJobInfo(each.getJobName())).collect(Collectors.toList());
    }
    
    private void checkModeConfig() {
        ModeConfiguration modeConfig = PipelineContext.getModeConfig();
        Preconditions.checkNotNull(modeConfig, "Mode configuration is required.");
        Preconditions.checkArgument("Cluster".equalsIgnoreCase(modeConfig.getType()), "Mode must be `Cluster`.");
    }
    
    private Stream<JobBriefInfo> getJobBriefInfos() {
        return PipelineAPIFactory.getJobStatisticsAPI().getAllJobsBriefInfo().stream().filter(each -> !each.getJobName().startsWith("_"));
    }
    
    private JobInfo getJobInfo(final String jobName) {
        JobInfo result = new JobInfo(jobName);
        JobConfigurationPOJO jobConfigPOJO = getElasticJobConfigPOJO(result.getJobId());
        RuleAlteredJobConfiguration jobConfig = getJobConfig(jobConfigPOJO);
        result.setActive(!jobConfigPOJO.isDisabled());
        result.setShardingTotalCount(jobConfig.getHandleConfig().getJobShardingCount());
        result.setTables(jobConfig.getHandleConfig().getLogicTables());
        result.setCreateTime(jobConfigPOJO.getProps().getProperty("create_time"));
        result.setStopTime(jobConfigPOJO.getProps().getProperty("stop_time"));
        result.setJobParameter(jobConfigPOJO.getJobParameter());
        return result;
    }
    
    @Override
    public Optional<String> start(final RuleAlteredJobConfiguration jobConfig) {
        jobConfig.buildHandleConfig();
        if (jobConfig.getHandleConfig().getJobShardingCount() == 0) {
            log.warn("Invalid scaling job config!");
            throw new PipelineJobCreationException("handleConfig shardingTotalCount is 0");
        }
        log.info("Start scaling job by {}", jobConfig.getHandleConfig());
        GovernanceRepositoryAPI repositoryAPI = PipelineAPIFactory.getGovernanceRepositoryAPI();
        String jobId = jobConfig.getJobId();
        String jobConfigKey = String.format("%s/%s/config", DataPipelineConstants.DATA_PIPELINE_ROOT, jobId);
        if (repositoryAPI.isExisted(jobConfigKey)) {
            log.warn("jobId already exists in registry center, ignore, jobConfigKey={}", jobConfigKey);
            return Optional.of(jobId);
        }
        repositoryAPI.persist(String.format("%s/%s", DataPipelineConstants.DATA_PIPELINE_ROOT, jobId), RuleAlteredJob.class.getName());
        repositoryAPI.persist(jobConfigKey, createJobConfig(jobConfig));
        return Optional.of(jobId);
    }
    
    private String createJobConfig(final RuleAlteredJobConfiguration jobConfig) {
        JobConfigurationPOJO jobConfigPOJO = new JobConfigurationPOJO();
        jobConfigPOJO.setJobName(jobConfig.getJobId());
        jobConfigPOJO.setShardingTotalCount(jobConfig.getHandleConfig().getJobShardingCount());
        jobConfigPOJO.setJobParameter(YamlEngine.marshal(jobConfig));
        jobConfigPOJO.getProps().setProperty("create_time", LocalDateTime.now().format(DATE_TIME_FORMATTER));
        return YamlEngine.marshal(jobConfigPOJO);
    }
    
    @Override
    public Map<Integer, JobProgress> getProgress(final String jobId) {
        checkModeConfig();
        return getProgress(getJobConfig(jobId));
    }
    
    @Override
    public Map<Integer, JobProgress> getProgress(final RuleAlteredJobConfiguration jobConfig) {
        String jobId = jobConfig.getJobId();
        JobConfigurationPOJO jobConfigPOJO = getElasticJobConfigPOJO(jobId);
        return IntStream.range(0, jobConfig.getHandleConfig().getJobShardingCount()).boxed().collect(LinkedHashMap::new, (map, each) -> {
            JobProgress jobProgress = PipelineAPIFactory.getGovernanceRepositoryAPI().getJobProgress(jobId, each);
            if (null != jobProgress) {
                jobProgress.setActive(!jobConfigPOJO.isDisabled());
            }
            map.put(each, jobProgress);
        }, LinkedHashMap::putAll);
    }
    
    private void verifyManualMode(final RuleAlteredJobConfiguration jobConfig) {
        RuleAlteredContext ruleAlteredContext = RuleAlteredJobWorker.createRuleAlteredContext(jobConfig);
        if (null != ruleAlteredContext.getCompletionDetectAlgorithm()) {
            throw new PipelineVerifyFailedException("It's not necessary to do it in auto mode.");
        }
    }
    
    private void verifyJobNotCompleted(final RuleAlteredJobConfiguration jobConfig) {
        if (RuleAlteredJobProgressDetector.isJobCompleted(jobConfig.getHandleConfig().getJobShardingCount(), getProgress(jobConfig).values())) {
            throw new PipelineVerifyFailedException("Job is completed, it's not necessary to do it.");
        }
    }
    
    private void verifySourceWritingStopped(final RuleAlteredJobConfiguration jobConfig) {
        LockContext lockContext = PipelineContext.getContextManager().getInstanceContext().getLockContext();
        String schemaName = jobConfig.getDatabaseName();
        ShardingSphereLock lock = lockContext.getGlobalLock(schemaName);
        if (null == lock || !lock.isLocked(schemaName)) {
            throw new PipelineVerifyFailedException("Source writing is not stopped. You could run `STOP SCALING SOURCE WRITING {jobId}` to stop it.");
        }
    }
    
    @Override
    public void stopClusterWriteDB(final String jobId) {
        checkModeConfig();
        log.info("stopClusterWriteDB for job {}", jobId);
        JobConfigurationPOJO jobConfigPOJO = getElasticJobConfigPOJO(jobId);
        RuleAlteredJobConfiguration jobConfig = getJobConfig(jobConfigPOJO);
        verifyManualMode(jobConfig);
        verifyJobNotStopped(jobConfigPOJO);
        verifyJobNotCompleted(jobConfig);
        String databaseName = jobConfig.getDatabaseName();
        stopClusterWriteDB(databaseName, jobId);
    }
    
    @Override
    public void stopClusterWriteDB(final String databaseName, final String jobId) {
        LockContext lockContext = PipelineContext.getContextManager().getInstanceContext().getLockContext();
        ShardingSphereLock lock = lockContext.getOrCreateGlobalLock(databaseName);
        if (lock.isLocked(databaseName)) {
            log.info("stopClusterWriteDB, already stopped");
            return;
        }
        boolean tryLockSuccess = lock.tryLock(databaseName);
        log.info("stopClusterWriteDB, tryLockSuccess={}", tryLockSuccess);
        if (!tryLockSuccess) {
            throw new RuntimeException("Stop source writing failed");
        }
    }
    
    @Override
    public void restoreClusterWriteDB(final String jobId) {
        checkModeConfig();
        log.info("restoreClusterWriteDB for job {}", jobId);
        JobConfigurationPOJO jobConfigPOJO = getElasticJobConfigPOJO(jobId);
        RuleAlteredJobConfiguration jobConfig = getJobConfig(jobConfigPOJO);
        verifyManualMode(jobConfig);
        String databaseName = jobConfig.getDatabaseName();
        restoreClusterWriteDB(databaseName, jobId);
    }
    
    @Override
    public void restoreClusterWriteDB(final String databaseName, final String jobId) {
        LockContext lockContext = PipelineContext.getContextManager().getInstanceContext().getLockContext();
        ShardingSphereLock lock = lockContext.getGlobalLock(databaseName);
        if (null == lock) {
            log.info("restoreClusterWriteDB, lock is null");
            return;
        }
        boolean isLocked = lock.isLocked(databaseName);
        if (!isLocked) {
            log.info("restoreClusterWriteDB, isLocked false, databaseName={}", databaseName);
            return;
        }
        log.info("restoreClusterWriteDB, before releaseLock, databaseName={}, jobId={}", databaseName, jobId);
        lock.releaseLock(databaseName);
    }
    
    @Override
    public Collection<DataConsistencyCheckAlgorithmInfo> listDataConsistencyCheckAlgorithms() {
        checkModeConfig();
        return DataConsistencyCalculateAlgorithmFactory.getAllInstances().stream().map(each -> {
            DataConsistencyCheckAlgorithmInfo result = new DataConsistencyCheckAlgorithmInfo();
            result.setType(each.getType());
            result.setDescription(each.getDescription());
            result.setSupportedDatabaseTypes(each.getSupportedDatabaseTypes());
            return result;
        }).collect(Collectors.toList());
    }
    
    @Override
    public boolean isDataConsistencyCheckNeeded(final String jobId) {
        log.info("isDataConsistencyCheckNeeded for job {}", jobId);
        RuleAlteredJobConfiguration jobConfig = getJobConfig(jobId);
        return isDataConsistencyCheckNeeded(jobConfig);
    }
    
    @Override
    public boolean isDataConsistencyCheckNeeded(final RuleAlteredJobConfiguration jobConfig) {
        RuleAlteredContext ruleAlteredContext = RuleAlteredJobWorker.createRuleAlteredContext(jobConfig);
        return isDataConsistencyCheckNeeded(ruleAlteredContext);
    }
    
    private boolean isDataConsistencyCheckNeeded(final RuleAlteredContext ruleAlteredContext) {
        return null != ruleAlteredContext.getDataConsistencyCalculateAlgorithm();
    }
    
    @Override
    public Map<String, DataConsistencyCheckResult> dataConsistencyCheck(final String jobId) {
        checkModeConfig();
        log.info("Data consistency check for job {}", jobId);
        RuleAlteredJobConfiguration jobConfig = getJobConfig(getElasticJobConfigPOJO(jobId));
        verifyDataConsistencyCheck(jobConfig);
        return dataConsistencyCheck(jobConfig);
    }
    
    @Override
    public Map<String, DataConsistencyCheckResult> dataConsistencyCheck(final RuleAlteredJobConfiguration jobConfig) {
        RuleAlteredContext ruleAlteredContext = RuleAlteredJobWorker.createRuleAlteredContext(jobConfig);
        if (!isDataConsistencyCheckNeeded(ruleAlteredContext)) {
            log.info("DataConsistencyCalculatorAlgorithm is not configured, data consistency check is ignored.");
            return Collections.emptyMap();
        }
        return dataConsistencyCheck(jobConfig, ruleAlteredContext.getDataConsistencyCalculateAlgorithm());
    }
    
    @Override
    public Map<String, DataConsistencyCheckResult> dataConsistencyCheck(final String jobId, final String algorithmType) {
        checkModeConfig();
        log.info("Data consistency check for job {}, algorithmType: {}", jobId, algorithmType);
        RuleAlteredJobConfiguration jobConfig = getJobConfig(getElasticJobConfigPOJO(jobId));
        verifyDataConsistencyCheck(jobConfig);
        return dataConsistencyCheck(jobConfig, DataConsistencyCalculateAlgorithmFactory.newInstance(algorithmType, new Properties()));
    }
    
    private Map<String, DataConsistencyCheckResult> dataConsistencyCheck(final RuleAlteredJobConfiguration jobConfig, final DataConsistencyCalculateAlgorithm calculator) {
        String jobId = jobConfig.getJobId();
        Map<String, DataConsistencyCheckResult> result = new DataConsistencyChecker(jobConfig).check(calculator);
        log.info("Scaling job {} with check algorithm '{}' data consistency checker result {}", jobId, calculator.getType(), result);
        PipelineAPIFactory.getGovernanceRepositoryAPI().persistJobCheckResult(jobId, aggregateDataConsistencyCheckResults(jobId, result));
        return result;
    }
    
    private void verifyDataConsistencyCheck(final RuleAlteredJobConfiguration jobConfig) {
        verifyManualMode(jobConfig);
        verifySourceWritingStopped(jobConfig);
    }
    
    @Override
    public boolean aggregateDataConsistencyCheckResults(final String jobId, final Map<String, DataConsistencyCheckResult> checkResults) {
        if (checkResults.isEmpty()) {
            return false;
        }
        for (Entry<String, DataConsistencyCheckResult> entry : checkResults.entrySet()) {
            DataConsistencyCheckResult checkResult = entry.getValue();
            boolean isCountMatched = checkResult.getCountCheckResult().isMatched();
            boolean isContentMatched = checkResult.getContentCheckResult().isMatched();
            if (!isCountMatched || !isContentMatched) {
                log.error("Scaling job: {}, table: {} data consistency check failed, count matched: {}, content matched: {}", jobId, entry.getKey(), isCountMatched, isContentMatched);
                return false;
            }
        }
        return true;
    }
    
    @Override
    public void switchClusterConfiguration(final String jobId) {
        checkModeConfig();
        log.info("Switch cluster configuration for job {}", jobId);
        JobConfigurationPOJO jobConfigPOJO = getElasticJobConfigPOJO(jobId);
        RuleAlteredJobConfiguration jobConfig = getJobConfig(jobConfigPOJO);
        verifyManualMode(jobConfig);
        verifyJobNotStopped(jobConfigPOJO);
        verifyJobNotCompleted(jobConfig);
        switchClusterConfiguration(jobConfig);
    }
    
    @Override
    public void switchClusterConfiguration(final RuleAlteredJobConfiguration jobConfig) {
        String jobId = jobConfig.getJobId();
        RuleAlteredContext ruleAlteredContext = RuleAlteredJobWorker.createRuleAlteredContext(jobConfig);
        GovernanceRepositoryAPI repositoryAPI = PipelineAPIFactory.getGovernanceRepositoryAPI();
        if (isDataConsistencyCheckNeeded(ruleAlteredContext)) {
            Optional<Boolean> checkResultOptional = repositoryAPI.getJobCheckResult(jobId);
            if (!checkResultOptional.isPresent() || !checkResultOptional.get()) {
                throw new PipelineVerifyFailedException("Data consistency check is not finished or failed.");
            }
        }
        WorkflowConfiguration workflowConfig = jobConfig.getWorkflowConfig();
        ScalingTaskFinishedEvent taskFinishedEvent = new ScalingTaskFinishedEvent(jobConfig.getDatabaseName(), workflowConfig.getActiveVersion(), workflowConfig.getNewVersion());
        ShardingSphereEventBus.getInstance().post(taskFinishedEvent);
        // TODO rewrite job status update after job progress structure refactor
        RuleAlteredJobSchedulerCenter.updateJobStatus(jobId, JobStatus.FINISHED);
        for (int each : repositoryAPI.getShardingItems(jobId)) {
            repositoryAPI.updateShardingJobStatus(jobId, each, JobStatus.FINISHED);
        }
        RuleAlteredJobSchedulerCenter.stop(jobId);
        stop(jobId);
    }
    
    @Override
    public void reset(final String jobId) {
        checkModeConfig();
        log.info("Scaling job {} reset target table", jobId);
        JobConfigurationPOJO jobConfigPOJO = getElasticJobConfigPOJO(jobId);
        verifyJobStopped(jobConfigPOJO);
        try {
            new ScalingEnvironmentManager().cleanupTargetTables(getJobConfig(jobConfigPOJO));
        } catch (final SQLException ex) {
            throw new PipelineJobExecutionException("Reset target table failed for job " + jobId, ex);
        }
    }
    
    @Override
    public RuleAlteredJobConfiguration getJobConfig(final String jobId) {
        return getJobConfig(getElasticJobConfigPOJO(jobId));
    }
    
    private RuleAlteredJobConfiguration getJobConfig(final JobConfigurationPOJO elasticJobConfigPOJO) {
        return YamlEngine.unmarshal(elasticJobConfigPOJO.getJobParameter(), RuleAlteredJobConfiguration.class, true);
    }
}
