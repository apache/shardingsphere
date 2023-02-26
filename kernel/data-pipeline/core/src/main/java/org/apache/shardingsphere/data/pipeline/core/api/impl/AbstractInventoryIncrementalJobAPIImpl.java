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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.PipelineDataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.api.config.job.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.process.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.api.context.PipelineJobItemContext;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.progress.InventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.data.pipeline.api.job.progress.JobItemIncrementalTasksProgress;
import org.apache.shardingsphere.data.pipeline.api.job.progress.JobItemInventoryTasksProgress;
import org.apache.shardingsphere.data.pipeline.api.pojo.DataConsistencyCheckAlgorithmInfo;
import org.apache.shardingsphere.data.pipeline.api.pojo.InventoryIncrementalJobItemInfo;
import org.apache.shardingsphere.data.pipeline.api.task.progress.InventoryTaskProgress;
import org.apache.shardingsphere.data.pipeline.core.api.InventoryIncrementalJobAPI;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.check.consistency.ConsistencyCheckJobItemProgressContext;
import org.apache.shardingsphere.data.pipeline.core.check.consistency.DataConsistencyCalculateAlgorithmChooser;
import org.apache.shardingsphere.data.pipeline.core.config.process.PipelineProcessConfigurationUtil;
import org.apache.shardingsphere.data.pipeline.core.context.InventoryIncrementalJobItemContext;
import org.apache.shardingsphere.data.pipeline.core.context.InventoryIncrementalProcessContext;
import org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.YamlInventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.YamlInventoryIncrementalJobItemProgressSwapper;
import org.apache.shardingsphere.data.pipeline.core.task.IncrementalTask;
import org.apache.shardingsphere.data.pipeline.core.task.InventoryTask;
import org.apache.shardingsphere.data.pipeline.spi.check.consistency.DataConsistencyCalculateAlgorithm;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.util.spi.annotation.SPIDescription;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Abstract inventory incremental job API implementation.
 */
@Slf4j
public abstract class AbstractInventoryIncrementalJobAPIImpl extends AbstractPipelineJobAPIImpl implements InventoryIncrementalJobAPI {
    
    private final PipelineProcessConfigurationPersistService processConfigPersistService = new PipelineProcessConfigurationPersistService();
    
    @Getter(AccessLevel.PROTECTED)
    private final YamlInventoryIncrementalJobItemProgressSwapper jobItemProgressSwapper = new YamlInventoryIncrementalJobItemProgressSwapper();
    
    protected abstract String getTargetDatabaseType(PipelineJobConfiguration pipelineJobConfig);
    
    @Override
    public abstract InventoryIncrementalProcessContext buildPipelineProcessContext(PipelineJobConfiguration pipelineJobConfig);
    
    @Override
    public void alterProcessConfiguration(final PipelineProcessConfiguration processConfig) {
        // TODO check rateLimiter type match or not
        processConfigPersistService.persist(getJobType(), processConfig);
    }
    
    @Override
    public PipelineProcessConfiguration showProcessConfiguration() {
        PipelineProcessConfiguration result = processConfigPersistService.load(getJobType());
        result = PipelineProcessConfigurationUtil.convertWithDefaultValue(result);
        return result;
    }
    
    @Override
    public Map<Integer, InventoryIncrementalJobItemProgress> getJobProgress(final PipelineJobConfiguration jobConfig) {
        String jobId = jobConfig.getJobId();
        JobConfigurationPOJO jobConfigPOJO = getElasticJobConfigPOJO(jobId);
        return IntStream.range(0, jobConfig.getJobShardingCount()).boxed().collect(LinkedHashMap::new, (map, each) -> {
            Optional<InventoryIncrementalJobItemProgress> jobItemProgress = getJobItemProgress(jobId, each);
            jobItemProgress.ifPresent(optional -> optional.setActive(!jobConfigPOJO.isDisabled()));
            map.put(each, jobItemProgress.orElse(null));
        }, LinkedHashMap::putAll);
    }
    
    @Override
    public List<InventoryIncrementalJobItemInfo> getJobItemInfos(final String jobId) {
        JobConfigurationPOJO jobConfigPOJO = getElasticJobConfigPOJO(jobId);
        PipelineJobConfiguration jobConfig = getJobConfiguration(jobConfigPOJO);
        long startTimeMillis = Long.parseLong(Optional.ofNullable(jobConfigPOJO.getProps().getProperty("start_time_millis")).orElse("0"));
        Map<Integer, InventoryIncrementalJobItemProgress> jobProgress = getJobProgress(jobConfig);
        List<InventoryIncrementalJobItemInfo> result = new LinkedList<>();
        for (Entry<Integer, InventoryIncrementalJobItemProgress> entry : jobProgress.entrySet()) {
            int shardingItem = entry.getKey();
            InventoryIncrementalJobItemProgress jobItemProgress = entry.getValue();
            if (null == jobItemProgress) {
                result.add(new InventoryIncrementalJobItemInfo(shardingItem, null, startTimeMillis, 0, ""));
                continue;
            }
            int inventoryFinishedPercentage = 0;
            if (JobStatus.EXECUTE_INCREMENTAL_TASK == jobItemProgress.getStatus()) {
                inventoryFinishedPercentage = 100;
            } else if (0 != jobItemProgress.getProcessedRecordsCount() && 0 != jobItemProgress.getInventoryRecordsCount()) {
                inventoryFinishedPercentage = (int) Math.min(100, jobItemProgress.getProcessedRecordsCount() * 100 / jobItemProgress.getInventoryRecordsCount());
            }
            String errorMessage = getJobItemErrorMessage(jobId, shardingItem);
            result.add(new InventoryIncrementalJobItemInfo(shardingItem, jobItemProgress, startTimeMillis, inventoryFinishedPercentage, errorMessage));
        }
        return result;
    }
    
    @Override
    public void persistJobItemProgress(final PipelineJobItemContext jobItemContext) {
        InventoryIncrementalJobItemContext context = (InventoryIncrementalJobItemContext) jobItemContext;
        InventoryIncrementalJobItemProgress jobItemProgress = new InventoryIncrementalJobItemProgress();
        jobItemProgress.setStatus(context.getStatus());
        jobItemProgress.setSourceDatabaseType(context.getJobConfig().getSourceDatabaseType());
        jobItemProgress.setDataSourceName(context.getDataSourceName());
        jobItemProgress.setIncremental(getIncrementalTasksProgress(context.getIncrementalTasks()));
        jobItemProgress.setInventory(getInventoryTasksProgress(context.getInventoryTasks()));
        jobItemProgress.setProcessedRecordsCount(context.getProcessedRecordsCount());
        jobItemProgress.setInventoryRecordsCount(context.getInventoryRecordsCount());
        String value = YamlEngine.marshal(jobItemProgressSwapper.swapToYamlConfiguration(jobItemProgress));
        PipelineAPIFactory.getGovernanceRepositoryAPI().persistJobItemProgress(context.getJobId(), context.getShardingItem(), value);
    }
    
    private JobItemIncrementalTasksProgress getIncrementalTasksProgress(final Collection<IncrementalTask> incrementalTasks) {
        IncrementalTask incrementalTask = incrementalTasks.size() > 0 ? incrementalTasks.iterator().next() : null;
        return new JobItemIncrementalTasksProgress(null != incrementalTask ? incrementalTask.getTaskProgress() : null);
    }
    
    private JobItemInventoryTasksProgress getInventoryTasksProgress(final Collection<InventoryTask> inventoryTasks) {
        Map<String, InventoryTaskProgress> inventoryTaskProgressMap = new HashMap<>();
        for (InventoryTask each : inventoryTasks) {
            inventoryTaskProgressMap.put(each.getTaskId(), each.getTaskProgress());
        }
        return new JobItemInventoryTasksProgress(inventoryTaskProgressMap);
    }
    
    @Override
    public Optional<InventoryIncrementalJobItemProgress> getJobItemProgress(final String jobId, final int shardingItem) {
        Optional<String> progress = PipelineAPIFactory.getGovernanceRepositoryAPI().getJobItemProgress(jobId, shardingItem);
        return progress.map(s -> jobItemProgressSwapper.swapToObject(YamlEngine.unmarshal(s, YamlInventoryIncrementalJobItemProgress.class)));
    }
    
    @Override
    public void updateJobItemStatus(final String jobId, final int shardingItem, final JobStatus status) {
        Optional<InventoryIncrementalJobItemProgress> jobItemProgress = getJobItemProgress(jobId, shardingItem);
        if (!jobItemProgress.isPresent()) {
            return;
        }
        jobItemProgress.get().setStatus(status);
        PipelineAPIFactory.getGovernanceRepositoryAPI().persistJobItemProgress(jobId, shardingItem, YamlEngine.marshal(jobItemProgressSwapper.swapToYamlConfiguration(jobItemProgress.get())));
    }
    
    @Override
    public Collection<DataConsistencyCheckAlgorithmInfo> listDataConsistencyCheckAlgorithms() {
        checkModeConfig();
        Collection<DataConsistencyCheckAlgorithmInfo> result = new LinkedList<>();
        for (DataConsistencyCalculateAlgorithm each : ShardingSphereServiceLoader.getServiceInstances(DataConsistencyCalculateAlgorithm.class)) {
            SPIDescription description = each.getClass().getAnnotation(SPIDescription.class);
            result.add(new DataConsistencyCheckAlgorithmInfo(each.getType(), getSupportedDatabaseTypes(each.getSupportedDatabaseTypes()), null == description ? "" : description.value()));
        }
        return result;
    }
    
    private Collection<String> getSupportedDatabaseTypes(final Collection<String> supportedDatabaseTypes) {
        return supportedDatabaseTypes.isEmpty()
                ? ShardingSphereServiceLoader.getServiceInstances(DatabaseType.class).stream().map(DatabaseType::getType).collect(Collectors.toList())
                : supportedDatabaseTypes;
    }
    
    @Override
    public DataConsistencyCalculateAlgorithm buildDataConsistencyCalculateAlgorithm(final PipelineJobConfiguration jobConfig, final String algorithmType, final Properties algorithmProps) {
        ShardingSpherePreconditions.checkState(null != algorithmType || null != jobConfig, () -> new IllegalArgumentException("Algorithm type and job configuration are null."));
        return null == algorithmType
                ? DataConsistencyCalculateAlgorithmChooser.choose(
                        TypedSPILoader.getService(DatabaseType.class, jobConfig.getSourceDatabaseType()),
                        TypedSPILoader.getService(DatabaseType.class, getTargetDatabaseType(jobConfig)))
                : TypedSPILoader.getService(DataConsistencyCalculateAlgorithm.class, algorithmType, algorithmProps);
    }
    
    @Override
    public Map<String, DataConsistencyCheckResult> dataConsistencyCheck(final PipelineJobConfiguration jobConfig, final DataConsistencyCalculateAlgorithm calculateAlgorithm,
                                                                        final ConsistencyCheckJobItemProgressContext progressContext) {
        String jobId = jobConfig.getJobId();
        PipelineDataConsistencyChecker dataConsistencyChecker = buildPipelineDataConsistencyChecker(jobConfig, buildPipelineProcessContext(jobConfig), progressContext);
        Map<String, DataConsistencyCheckResult> result = dataConsistencyChecker.check(calculateAlgorithm);
        log.info("job {} with check algorithm '{}' data consistency checker result {}", jobId, calculateAlgorithm.getType(), result);
        return result;
    }
    
    protected abstract PipelineDataConsistencyChecker buildPipelineDataConsistencyChecker(PipelineJobConfiguration pipelineJobConfig, InventoryIncrementalProcessContext processContext,
                                                                                          ConsistencyCheckJobItemProgressContext progressContext);
    
    @Override
    public boolean aggregateDataConsistencyCheckResults(final String jobId, final Map<String, DataConsistencyCheckResult> checkResults) {
        if (checkResults.isEmpty()) {
            log.info("aggregateDataConsistencyCheckResults, checkResults empty, jobId={}", jobId);
            return false;
        }
        for (Entry<String, DataConsistencyCheckResult> entry : checkResults.entrySet()) {
            DataConsistencyCheckResult checkResult = entry.getValue();
            if (!checkResult.isMatched()) {
                return false;
            }
        }
        return true;
    }
}
