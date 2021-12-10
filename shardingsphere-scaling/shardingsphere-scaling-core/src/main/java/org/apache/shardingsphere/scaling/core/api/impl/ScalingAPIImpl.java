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

package org.apache.shardingsphere.scaling.core.api.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.core.constant.DataPipelineConstants;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.domain.JobBriefInfo;
import org.apache.shardingsphere.infra.config.TypedSPIConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmFactory;
import org.apache.shardingsphere.infra.config.datasource.typed.TypedDataSourceConfigurationWrap;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.rule.ScalingTaskFinishedEvent;
import org.apache.shardingsphere.scaling.core.api.DataConsistencyCheckAlgorithmInfo;
import org.apache.shardingsphere.scaling.core.api.JobInfo;
import org.apache.shardingsphere.scaling.core.api.ScalingAPI;
import org.apache.shardingsphere.scaling.core.api.ScalingAPIFactory;
import org.apache.shardingsphere.scaling.core.api.ScalingDataConsistencyCheckAlgorithm;
import org.apache.shardingsphere.scaling.core.common.exception.DataCheckFailException;
import org.apache.shardingsphere.scaling.core.common.exception.ScalingJobCreationException;
import org.apache.shardingsphere.scaling.core.common.exception.ScalingJobNotFoundException;
import org.apache.shardingsphere.scaling.core.config.HandleConfiguration;
import org.apache.shardingsphere.scaling.core.config.JobConfiguration;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.core.config.WorkflowConfiguration;
import org.apache.shardingsphere.scaling.core.job.JobContext;
import org.apache.shardingsphere.scaling.core.job.JobStatus;
import org.apache.shardingsphere.scaling.core.job.ScalingJob;
import org.apache.shardingsphere.scaling.core.job.check.EnvironmentCheckerFactory;
import org.apache.shardingsphere.scaling.core.job.check.consistency.DataConsistencyCheckResult;
import org.apache.shardingsphere.scaling.core.job.check.consistency.DataConsistencyChecker;
import org.apache.shardingsphere.scaling.core.job.environment.ScalingEnvironmentManager;
import org.apache.shardingsphere.scaling.core.job.progress.JobProgress;
import org.apache.shardingsphere.scaling.core.job.schedule.JobSchedulerCenter;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

@Slf4j
public final class ScalingAPIImpl implements ScalingAPI {
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public List<JobInfo> list() {
        return getJobBriefInfos().map(each -> getJobInfo(each.getJobName())).collect(Collectors.toList());
    }
    
    private Stream<JobBriefInfo> getJobBriefInfos() {
        return ScalingAPIFactory.getJobStatisticsAPI().getAllJobsBriefInfo().stream().filter(each -> !each.getJobName().startsWith("_"));
    }
    
    private JobInfo getJobInfo(final String jobName) {
        JobInfo result = new JobInfo(Long.parseLong(jobName));
        JobConfigurationPOJO jobConfigPOJO = getElasticJobConfigPOJO(result.getJobId());
        JobConfiguration jobConfig = getJobConfig(jobConfigPOJO);
        result.setActive(!jobConfigPOJO.isDisabled());
        result.setShardingTotalCount(jobConfig.getHandleConfig().getJobShardingCount());
        result.setTables(jobConfig.getHandleConfig().getLogicTables());
        result.setCreateTime(jobConfigPOJO.getProps().getProperty("create_time"));
        result.setStopTime(jobConfigPOJO.getProps().getProperty("stop_time"));
        result.setJobParameter(jobConfigPOJO.getJobParameter());
        return result;
    }
    
    @Override
    public List<Long> getUncompletedJobIds(final String schemaName) {
        return getJobBriefInfos().filter(each -> {
            long jobId = Long.parseLong(each.getJobName());
            return isUncompletedJobOfSchema(schemaName, jobId);
        }).map(each -> Long.parseLong(each.getJobName())).collect(Collectors.toList());
    }
    
    private boolean isUncompletedJobOfSchema(final String schemaName, final long jobId) {
        JobConfigurationPOJO jobConfigPOJO;
        try {
            jobConfigPOJO = getElasticJobConfigPOJO(jobId);
        } catch (final ScalingJobNotFoundException ex) {
            log.warn("scaling job not found, jobId={}", jobId);
            return false;
        }
        JobConfiguration jobConfig = getJobConfig(jobConfigPOJO);
        HandleConfiguration handleConfig = jobConfig.getHandleConfig();
        WorkflowConfiguration workflowConfig;
        if (null == handleConfig || null == (workflowConfig = handleConfig.getWorkflowConfig())) {
            log.warn("handleConfig or workflowConfig null, jobId={}", jobId);
            return false;
        }
        if (!schemaName.equals(workflowConfig.getSchemaName())) {
            return false;
        }
        return !jobConfigPOJO.isDisabled();
    }
    
    @Override
    public void start(final long jobId) {
        log.info("Start scaling job {}", jobId);
        JobConfigurationPOJO jobConfigPOJO = getElasticJobConfigPOJO(jobId);
        jobConfigPOJO.setDisabled(false);
        jobConfigPOJO.getProps().remove("stop_time");
        ScalingAPIFactory.getJobConfigurationAPI().updateJobConfiguration(jobConfigPOJO);
    }
    
    @Override
    public Optional<Long> start(final JobConfiguration jobConfig) {
        jobConfig.fillInProperties();
        if (jobConfig.getHandleConfig().getJobShardingCount() == 0) {
            log.warn("Invalid scaling job config!");
            throw new ScalingJobCreationException("handleConfig shardingTotalCount is 0");
        }
        log.info("Start scaling job by {}", jobConfig.getHandleConfig());
        ScalingAPIFactory.getGovernanceRepositoryAPI().persist(String.format("%s/%d",
                DataPipelineConstants.DATA_PIPELINE_ROOT, jobConfig.getHandleConfig().getJobId()), ScalingJob.class.getCanonicalName());
        ScalingAPIFactory.getGovernanceRepositoryAPI().persist(String.format("%s/%d/config",
                DataPipelineConstants.DATA_PIPELINE_ROOT, jobConfig.getHandleConfig().getJobId()), createJobConfig(jobConfig));
        return Optional.of(jobConfig.getHandleConfig().getJobId());
    }
    
    private String createJobConfig(final JobConfiguration jobConfig) {
        JobConfigurationPOJO jobConfigPOJO = new JobConfigurationPOJO();
        jobConfigPOJO.setJobName(String.valueOf(jobConfig.getHandleConfig().getJobId()));
        jobConfigPOJO.setShardingTotalCount(jobConfig.getHandleConfig().getJobShardingCount());
        jobConfigPOJO.setJobParameter(YamlEngine.marshal(jobConfig));
        jobConfigPOJO.getProps().setProperty("create_time", LocalDateTime.now().format(DATE_TIME_FORMATTER));
        return YamlEngine.marshal(jobConfigPOJO);
    }
    
    @Override
    public void stop(final long jobId) {
        log.info("Stop scaling job {}", jobId);
        JobConfigurationPOJO jobConfigPOJO = getElasticJobConfigPOJO(jobId);
        jobConfigPOJO.setDisabled(true);
        jobConfigPOJO.getProps().setProperty("stop_time", LocalDateTime.now().format(DATE_TIME_FORMATTER));
        ScalingAPIFactory.getJobConfigurationAPI().updateJobConfiguration(jobConfigPOJO);
    }
    
    @Override
    public void remove(final long jobId) {
        log.info("Remove scaling job {}", jobId);
        ScalingAPIFactory.getJobOperateAPI().remove(String.valueOf(jobId), null);
        ScalingAPIFactory.getGovernanceRepositoryAPI().deleteJob(jobId);
    }
    
    @Override
    public Map<Integer, JobProgress> getProgress(final long jobId) {
        return IntStream.range(0, getJobConfig(jobId).getHandleConfig().getJobShardingCount()).boxed()
                .collect(LinkedHashMap::new, (map, each) -> map.put(each, ScalingAPIFactory.getGovernanceRepositoryAPI().getJobProgress(jobId, each)), LinkedHashMap::putAll);
    }
    
    @Override
    public void stopClusterWriteDB(final long jobId) {
        //TODO stopClusterWriteDB
    }
    
    @Override
    public Collection<DataConsistencyCheckAlgorithmInfo> listDataConsistencyCheckAlgorithms() {
        return ShardingSphereServiceLoader.getSingletonServiceInstances(ScalingDataConsistencyCheckAlgorithm.class)
                .stream().map(each -> {
                    DataConsistencyCheckAlgorithmInfo algorithmInfo = new DataConsistencyCheckAlgorithmInfo();
                    algorithmInfo.setType(each.getType());
                    algorithmInfo.setDescription(each.getDescription());
                    algorithmInfo.setSupportedDatabaseTypes(each.getSupportedDatabaseTypes());
                    algorithmInfo.setProvider(each.getProvider());
                    return algorithmInfo;
                }).collect(Collectors.toList());
    }
    
    @Override
    public boolean isDataConsistencyCheckNeeded() {
        return null != ScalingContext.getInstance().getDataConsistencyCheckAlgorithm();
    }
    
    @Override
    public Map<String, DataConsistencyCheckResult> dataConsistencyCheck(final long jobId) {
        log.info("Data consistency check for job {}", jobId);
        if (!isDataConsistencyCheckNeeded()) {
            log.info("dataConsistencyCheckAlgorithm is not configured, data consistency check is ignored.");
            return Collections.emptyMap();
        }
        return dataConsistencyCheck0(jobId, ScalingContext.getInstance().getDataConsistencyCheckAlgorithm());
    }
    
    @Override
    public Map<String, DataConsistencyCheckResult> dataConsistencyCheck(final long jobId, final String algorithmType) {
        log.info("Data consistency check for job {}, algorithmType: {}", jobId, algorithmType);
        TypedSPIConfiguration typedSPIConfig = new ShardingSphereAlgorithmConfiguration(algorithmType, new Properties());
        ScalingDataConsistencyCheckAlgorithm checkAlgorithm = ShardingSphereAlgorithmFactory.createAlgorithm(typedSPIConfig, ScalingDataConsistencyCheckAlgorithm.class);
        return dataConsistencyCheck0(jobId, checkAlgorithm);
    }
    
    private Map<String, DataConsistencyCheckResult> dataConsistencyCheck0(final long jobId, final ScalingDataConsistencyCheckAlgorithm checkAlgorithm) {
        JobConfiguration jobConfig = getJobConfig(jobId);
        DataConsistencyChecker dataConsistencyChecker = EnvironmentCheckerFactory.newInstance(new JobContext(jobConfig));
        Map<String, DataConsistencyCheckResult> result = dataConsistencyChecker.countCheck();
        if (result.values().stream().allMatch(DataConsistencyCheckResult::isCountValid)) {
            Map<String, Boolean> dataCheckResult = dataConsistencyChecker.dataCheck(checkAlgorithm);
            result.forEach((key, value) -> value.setDataValid(dataCheckResult.getOrDefault(key, false)));
        }
        log.info("Scaling job {} with check algorithm '{}' data consistency checker result {}", jobId, checkAlgorithm.getClass().getName(), result);
        ScalingAPIFactory.getGovernanceRepositoryAPI().persistJobCheckResult(jobId, aggregateDataConsistencyCheckResults(jobId, result));
        return result;
    }
    
    @Override
    public boolean aggregateDataConsistencyCheckResults(final long jobId, final Map<String, DataConsistencyCheckResult> checkResultMap) {
        if (checkResultMap.isEmpty()) {
            return false;
        }
        for (Entry<String, DataConsistencyCheckResult> entry : checkResultMap.entrySet()) {
            boolean isDataValid = entry.getValue().isDataValid();
            boolean isCountValid = entry.getValue().isCountValid();
            if (!isDataValid || !isCountValid) {
                log.error("Scaling job: {}, table: {} data consistency check failed, dataValid: {}, countValid: {}", jobId, entry.getKey(), isDataValid, isCountValid);
                return false;
            }
        }
        return true;
    }
    
    @Override
    public void switchClusterConfiguration(final long jobId) {
        log.info("Switch cluster configuration for job {}", jobId);
        if (isDataConsistencyCheckNeeded()) {
            Optional<Boolean> checkResultOptional = ScalingAPIFactory.getGovernanceRepositoryAPI().getJobCheckResult(jobId);
            if (!checkResultOptional.isPresent() || !checkResultOptional.get()) {
                throw new DataCheckFailException("Data consistency check not finished or failed.");
            }
        }
        JobConfiguration jobConfig = getJobConfig(jobId);
        Optional<Collection<JobContext>> optionalJobContexts = JobSchedulerCenter.getJobContexts(jobId);
        optionalJobContexts.ifPresent(jobContexts -> jobContexts.forEach(each -> each.setStatus(JobStatus.ALMOST_FINISHED)));
        TypedDataSourceConfigurationWrap targetConfig = jobConfig.getRuleConfig().getTarget();
        YamlRootConfiguration yamlRootConfig = YamlEngine.unmarshal(targetConfig.getParameter(), YamlRootConfiguration.class);
        WorkflowConfiguration workflowConfig = jobConfig.getHandleConfig().getWorkflowConfig();
        String schemaName = workflowConfig.getSchemaName();
        String ruleCacheId = workflowConfig.getRuleCacheId();
        ScalingTaskFinishedEvent taskFinishedEvent = new ScalingTaskFinishedEvent(schemaName, yamlRootConfig, ruleCacheId);
        ShardingSphereEventBus.getInstance().post(taskFinishedEvent);
        optionalJobContexts.ifPresent(jobContexts -> jobContexts.forEach(each -> {
            each.setStatus(JobStatus.FINISHED);
            JobSchedulerCenter.persistJobProgress(each);
        }));
        stop(jobId);
    }
    
    @Override
    public void reset(final long jobId) throws SQLException {
        log.info("Scaling job {} reset target table", jobId);
        ScalingAPIFactory.getGovernanceRepositoryAPI().deleteJobProgress(jobId);
        new ScalingEnvironmentManager().resetTargetTable(new JobContext(getJobConfig(jobId)));
    }
    
    @Override
    public JobConfiguration getJobConfig(final long jobId) {
        return getJobConfig(getElasticJobConfigPOJO(jobId));
    }
    
    private JobConfiguration getJobConfig(final JobConfigurationPOJO elasticJobConfigPOJO) {
        return YamlEngine.unmarshal(elasticJobConfigPOJO.getJobParameter(), JobConfiguration.class, true);
    }
    
    private JobConfigurationPOJO getElasticJobConfigPOJO(final long jobId) {
        try {
            return ScalingAPIFactory.getJobConfigurationAPI().getJobConfiguration(String.valueOf(jobId));
        } catch (final NullPointerException ex) {
            throw new ScalingJobNotFoundException(String.format("Can not find scaling job %s", jobId), jobId);
        }
    }
}
