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

package org.apache.shardingsphere.data.pipeline.scenario.migration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.api.config.TaskConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.job.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.job.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.job.yaml.YamlMigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.job.yaml.YamlMigrationJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.api.config.job.yaml.YamlPipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.context.PipelineJobItemContext;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfigurationFactory;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.JobType;
import org.apache.shardingsphere.data.pipeline.api.job.PipelineJobId;
import org.apache.shardingsphere.data.pipeline.api.job.progress.InventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.data.pipeline.api.pojo.DataConsistencyCheckAlgorithmInfo;
import org.apache.shardingsphere.data.pipeline.api.pojo.JobInfo;
import org.apache.shardingsphere.data.pipeline.core.api.GovernanceRepositoryAPI;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineJobItemAPI;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineResourceAPI;
import org.apache.shardingsphere.data.pipeline.core.api.impl.AbstractPipelineJobAPIImpl;
import org.apache.shardingsphere.data.pipeline.core.api.impl.InventoryIncrementalJobItemAPIImpl;
import org.apache.shardingsphere.data.pipeline.core.api.impl.PipelineResourceAPIImpl;
import org.apache.shardingsphere.data.pipeline.core.check.consistency.DataConsistencyCalculateAlgorithmFactory;
import org.apache.shardingsphere.data.pipeline.core.check.consistency.DataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContext;
import org.apache.shardingsphere.data.pipeline.core.exception.AddMigrationSourceResourceException;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineVerifyFailedException;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobCenter;
import org.apache.shardingsphere.data.pipeline.core.job.progress.PipelineJobProgressDetector;
import org.apache.shardingsphere.data.pipeline.spi.check.consistency.DataConsistencyCalculateAlgorithm;
import org.apache.shardingsphere.data.pipeline.spi.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.data.pipeline.spi.rulealtered.RuleAlteredJobConfigurationPreparerFactory;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.domain.JobBriefInfo;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.rule.data.pipeline.PipelineProcessConfiguration;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.lock.LockContext;
import org.apache.shardingsphere.infra.lock.LockDefinition;
import org.apache.shardingsphere.mode.lock.ExclusiveLockDefinition;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.rule.ScalingTaskFinishedEvent;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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
 * Migration job API impl.
 */
@Slf4j
public final class MigrationJobAPIImpl extends AbstractPipelineJobAPIImpl implements MigrationJobAPI {
    
    private final PipelineJobItemAPI jobItemAPI = new InventoryIncrementalJobItemAPIImpl();
    
    private final PipelineResourceAPI pipelineResourceAPI = new PipelineResourceAPIImpl();
    
    @Override
    protected String marshalJobIdLeftPart(final PipelineJobId pipelineJobId) {
        MigrationJobId jobId = (MigrationJobId) pipelineJobId;
        String text = jobId.getFormatVersion() + "|" + jobId.getCurrentMetadataVersion() + "T" + jobId.getNewMetadataVersion() + "|" + jobId.getDatabaseName();
        return Hex.encodeHexString(text.getBytes(StandardCharsets.UTF_8), true);
    }
    
    @Override
    public void extendYamlJobConfiguration(final YamlPipelineJobConfiguration yamlJobConfig) {
        YamlMigrationJobConfiguration config = (YamlMigrationJobConfiguration) yamlJobConfig;
        if (null == config.getJobShardingDataNodes()) {
            RuleAlteredJobConfigurationPreparerFactory.getInstance().extendJobConfiguration(config);
        }
        if (null == yamlJobConfig.getJobId()) {
            config.setJobId(generateJobId(config));
        }
        if (Strings.isNullOrEmpty(config.getSourceDatabaseType())) {
            PipelineDataSourceConfiguration sourceDataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(config.getSource().getType(), config.getSource().getParameter());
            config.setSourceDatabaseType(sourceDataSourceConfig.getDatabaseType().getType());
        }
        if (Strings.isNullOrEmpty(config.getTargetDatabaseType())) {
            PipelineDataSourceConfiguration targetDataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(config.getTarget().getType(), config.getTarget().getParameter());
            config.setTargetDatabaseType(targetDataSourceConfig.getDatabaseType().getType());
        }
    }
    
    private String generateJobId(final YamlMigrationJobConfiguration config) {
        MigrationJobId jobId = new MigrationJobId();
        jobId.setTypeCode(JobType.MIGRATION.getTypeCode());
        jobId.setFormatVersion(MigrationJobId.CURRENT_VERSION);
        jobId.setCurrentMetadataVersion(config.getActiveVersion());
        jobId.setNewMetadataVersion(config.getNewVersion());
        jobId.setDatabaseName(config.getDatabaseName());
        return marshalJobId(jobId);
    }
    
    @Override
    protected YamlPipelineJobConfiguration swapToYamlJobConfiguration(final PipelineJobConfiguration jobConfig) {
        return new YamlMigrationJobConfigurationSwapper().swapToYamlConfiguration((MigrationJobConfiguration) jobConfig);
    }
    
    @Override
    public MigrationJobConfiguration getJobConfiguration(final String jobId) {
        return getJobConfiguration(getElasticJobConfigPOJO(jobId));
    }
    
    private MigrationJobConfiguration getJobConfiguration(final JobConfigurationPOJO jobConfigPOJO) {
        return YamlMigrationJobConfigurationSwapper.swapToObject(jobConfigPOJO.getJobParameter());
    }
    
    @Override
    public TaskConfiguration buildTaskConfiguration(final MigrationJobConfiguration jobConfig, final int jobShardingItem, final PipelineProcessConfiguration pipelineProcessConfig) {
        return RuleAlteredJobConfigurationPreparerFactory.getInstance().createTaskConfiguration(jobConfig, jobShardingItem, pipelineProcessConfig);
    }
    
    @Override
    public MigrationProcessContext buildPipelineProcessContext(final PipelineJobConfiguration pipelineJobConfig) {
        // TODO add jobType
        // TODO read process config from registry center
        PipelineProcessConfiguration processConfig = new PipelineProcessConfiguration(null, null, null);
        return new MigrationProcessContext(pipelineJobConfig.getJobId(), processConfig);
    }
    
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
        MigrationJobConfiguration jobConfig = getJobConfiguration(jobConfigPOJO);
        result.setActive(!jobConfigPOJO.isDisabled());
        result.setShardingTotalCount(jobConfig.getJobShardingCount());
        result.setTables(jobConfig.getLogicTables());
        result.setCreateTime(jobConfigPOJO.getProps().getProperty("create_time"));
        result.setStopTime(jobConfigPOJO.getProps().getProperty("stop_time"));
        result.setJobParameter(jobConfigPOJO.getJobParameter());
        return result;
    }
    
    @Override
    public Map<Integer, InventoryIncrementalJobItemProgress> getJobProgress(final String jobId) {
        checkModeConfig();
        return getJobProgress(getJobConfiguration(jobId));
    }
    
    @Override
    public Map<Integer, InventoryIncrementalJobItemProgress> getJobProgress(final MigrationJobConfiguration jobConfig) {
        String jobId = jobConfig.getJobId();
        JobConfigurationPOJO jobConfigPOJO = getElasticJobConfigPOJO(jobId);
        return IntStream.range(0, jobConfig.getJobShardingCount()).boxed().collect(LinkedHashMap::new, (map, each) -> {
            InventoryIncrementalJobItemProgress jobItemProgress = getJobItemProgress(jobId, each);
            if (null != jobItemProgress) {
                jobItemProgress.setActive(!jobConfigPOJO.isDisabled());
            }
            map.put(each, jobItemProgress);
        }, LinkedHashMap::putAll);
    }
    
    @Override
    public InventoryIncrementalJobItemProgress getJobItemProgress(final String jobId, final int shardingItem) {
        return (InventoryIncrementalJobItemProgress) jobItemAPI.getJobItemProgress(jobId, shardingItem);
    }
    
    @Override
    public void persistJobItemProgress(final PipelineJobItemContext jobItemContext) {
        jobItemAPI.persistJobItemProgress(jobItemContext);
    }
    
    @Override
    public void updateJobItemStatus(final String jobId, final int shardingItem, final JobStatus status) {
        jobItemAPI.updateJobItemStatus(jobId, shardingItem, status);
    }
    
    private void verifyManualMode(final MigrationJobConfiguration jobConfig) {
        MigrationProcessContext processContext = buildPipelineProcessContext(jobConfig);
        if (null != processContext.getCompletionDetectAlgorithm()) {
            throw new PipelineVerifyFailedException("It's not necessary to do it in auto mode.");
        }
    }
    
    private void verifyJobNotCompleted(final MigrationJobConfiguration jobConfig) {
        if (PipelineJobProgressDetector.isJobCompleted(jobConfig.getJobShardingCount(), getJobProgress(jobConfig).values())) {
            throw new PipelineVerifyFailedException("Job is completed, it's not necessary to do it.");
        }
    }
    
    @Override
    public void stopClusterWriteDB(final String jobId) {
        checkModeConfig();
        log.info("stopClusterWriteDB for job {}", jobId);
        JobConfigurationPOJO jobConfigPOJO = getElasticJobConfigPOJO(jobId);
        MigrationJobConfiguration jobConfig = getJobConfiguration(jobConfigPOJO);
        verifyManualMode(jobConfig);
        verifyJobNotStopped(jobConfigPOJO);
        verifyJobNotCompleted(jobConfig);
        stopClusterWriteDB(jobConfig);
    }
    
    @Override
    public void stopClusterWriteDB(final MigrationJobConfiguration jobConfig) {
        String databaseName = jobConfig.getDatabaseName();
        LockContext lockContext = PipelineContext.getContextManager().getInstanceContext().getLockContext();
        LockDefinition lockDefinition = new ExclusiveLockDefinition(databaseName);
        if (lockContext.isLocked(lockDefinition)) {
            log.info("stopClusterWriteDB, already stopped");
            return;
        }
        if (lockContext.tryLock(lockDefinition)) {
            log.info("stopClusterWriteDB, tryLockSuccess=true");
            return;
        }
        throw new RuntimeException("Stop source writing failed");
    }
    
    @Override
    public void restoreClusterWriteDB(final String jobId) {
        checkModeConfig();
        log.info("restoreClusterWriteDB for job {}", jobId);
        JobConfigurationPOJO jobConfigPOJO = getElasticJobConfigPOJO(jobId);
        MigrationJobConfiguration jobConfig = getJobConfiguration(jobConfigPOJO);
        verifyManualMode(jobConfig);
        restoreClusterWriteDB(jobConfig);
    }
    
    @Override
    public void restoreClusterWriteDB(final MigrationJobConfiguration jobConfig) {
        String databaseName = jobConfig.getDatabaseName();
        LockContext lockContext = PipelineContext.getContextManager().getInstanceContext().getLockContext();
        LockDefinition lockDefinition = new ExclusiveLockDefinition(databaseName);
        if (lockContext.isLocked(lockDefinition)) {
            log.info("restoreClusterWriteDB, before unlock, databaseName={}, jobId={}", databaseName, jobConfig.getJobId());
            lockContext.unlock(lockDefinition);
            return;
        }
        log.info("restoreClusterWriteDB, isLocked false, databaseName={}", databaseName);
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
        MigrationJobConfiguration jobConfig = getJobConfiguration(jobId);
        return isDataConsistencyCheckNeeded(jobConfig);
    }
    
    @Override
    public boolean isDataConsistencyCheckNeeded(final MigrationJobConfiguration jobConfig) {
        MigrationProcessContext processContext = buildPipelineProcessContext(jobConfig);
        return isDataConsistencyCheckNeeded(processContext);
    }
    
    private boolean isDataConsistencyCheckNeeded(final MigrationProcessContext processContext) {
        return null != processContext.getDataConsistencyCalculateAlgorithm();
    }
    
    @Override
    public Map<String, DataConsistencyCheckResult> dataConsistencyCheck(final String jobId) {
        checkModeConfig();
        log.info("Data consistency check for job {}", jobId);
        MigrationJobConfiguration jobConfig = getJobConfiguration(getElasticJobConfigPOJO(jobId));
        verifyDataConsistencyCheck(jobConfig);
        return dataConsistencyCheck(jobConfig);
    }
    
    @Override
    public Map<String, DataConsistencyCheckResult> dataConsistencyCheck(final MigrationJobConfiguration jobConfig) {
        MigrationProcessContext processContext = buildPipelineProcessContext(jobConfig);
        if (!isDataConsistencyCheckNeeded(processContext)) {
            log.info("DataConsistencyCalculatorAlgorithm is not configured, data consistency check is ignored.");
            return Collections.emptyMap();
        }
        return dataConsistencyCheck(jobConfig, processContext.getDataConsistencyCalculateAlgorithm());
    }
    
    @Override
    public Map<String, DataConsistencyCheckResult> dataConsistencyCheck(final String jobId, final String algorithmType, final Properties algorithmProps) {
        checkModeConfig();
        log.info("Data consistency check for job {}, algorithmType: {}", jobId, algorithmType);
        MigrationJobConfiguration jobConfig = getJobConfiguration(getElasticJobConfigPOJO(jobId));
        verifyDataConsistencyCheck(jobConfig);
        return dataConsistencyCheck(jobConfig, DataConsistencyCalculateAlgorithmFactory.newInstance(algorithmType, algorithmProps));
    }
    
    private Map<String, DataConsistencyCheckResult> dataConsistencyCheck(final MigrationJobConfiguration jobConfig, final DataConsistencyCalculateAlgorithm calculator) {
        String jobId = jobConfig.getJobId();
        JobRateLimitAlgorithm readRateLimitAlgorithm = buildPipelineProcessContext(jobConfig).getReadRateLimitAlgorithm();
        Map<String, DataConsistencyCheckResult> result = new DataConsistencyChecker(jobConfig, readRateLimitAlgorithm).check(calculator);
        log.info("Scaling job {} with check algorithm '{}' data consistency checker result {}", jobId, calculator.getType(), result);
        PipelineAPIFactory.getGovernanceRepositoryAPI().persistJobCheckResult(jobId, aggregateDataConsistencyCheckResults(jobId, result));
        return result;
    }
    
    private void verifyDataConsistencyCheck(final MigrationJobConfiguration jobConfig) {
        verifyManualMode(jobConfig);
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
        MigrationJobConfiguration jobConfig = getJobConfiguration(jobConfigPOJO);
        verifyManualMode(jobConfig);
        verifyJobNotStopped(jobConfigPOJO);
        verifyJobNotCompleted(jobConfig);
        switchClusterConfiguration(jobConfig);
    }
    
    @Override
    public void switchClusterConfiguration(final MigrationJobConfiguration jobConfig) {
        String jobId = jobConfig.getJobId();
        MigrationProcessContext processContext = buildPipelineProcessContext(jobConfig);
        GovernanceRepositoryAPI repositoryAPI = PipelineAPIFactory.getGovernanceRepositoryAPI();
        if (isDataConsistencyCheckNeeded(processContext)) {
            Optional<Boolean> checkResult = repositoryAPI.getJobCheckResult(jobId);
            if (!checkResult.isPresent() || !checkResult.get()) {
                throw new PipelineVerifyFailedException("Data consistency check is not finished or failed.");
            }
        }
        ScalingTaskFinishedEvent taskFinishedEvent = new ScalingTaskFinishedEvent(jobConfig.getDatabaseName(), jobConfig.getActiveVersion(), jobConfig.getNewVersion());
        PipelineContext.getContextManager().getInstanceContext().getEventBusContext().post(taskFinishedEvent);
        for (int each : repositoryAPI.getShardingItems(jobId)) {
            PipelineJobCenter.getJobItemContext(jobId, each).ifPresent(jobItemContext -> jobItemContext.setStatus(JobStatus.FINISHED));
            updateJobItemStatus(jobId, each, JobStatus.FINISHED);
        }
        PipelineJobCenter.stop(jobId);
        stop(jobId);
    }
    
    @Override
    public void reset(final String jobId) {
        checkModeConfig();
        log.info("Scaling job {} reset target table", jobId);
        JobConfigurationPOJO jobConfigPOJO = getElasticJobConfigPOJO(jobId);
        verifyJobStopped(jobConfigPOJO);
    }
    
    @Override
    public void addMigrationSourceResources(final Map<String, DataSourceProperties> dataSourceProperties) {
        log.info("Add migration source resources {}", dataSourceProperties.keySet());
        Map<String, DataSourceProperties> existDataSources = pipelineResourceAPI.getMetaDataDataSource(JobType.MIGRATION);
        Collection<String> duplicateDataSourceNames = new HashSet<>(dataSourceProperties.size(), 1);
        for (Entry<String, DataSourceProperties> entry : dataSourceProperties.entrySet()) {
            if (existDataSources.containsKey(entry.getKey())) {
                duplicateDataSourceNames.add(entry.getKey());
            }
        }
        if (!duplicateDataSourceNames.isEmpty()) {
            throw new AddMigrationSourceResourceException(String.format("Duplicate resource names %s.", duplicateDataSourceNames));
        }
        Map<String, DataSourceProperties> result = new LinkedHashMap<>(existDataSources);
        result.putAll(dataSourceProperties);
        pipelineResourceAPI.persistMetaDataDataSource(JobType.MIGRATION, result);
    }
    
    @Override
    public String getType() {
        return JobType.MIGRATION.getTypeName();
    }
}
