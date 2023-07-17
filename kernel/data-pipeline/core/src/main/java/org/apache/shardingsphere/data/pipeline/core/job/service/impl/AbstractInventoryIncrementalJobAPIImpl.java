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

package org.apache.shardingsphere.data.pipeline.core.job.service.impl;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.common.config.job.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.common.config.process.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.common.config.process.PipelineProcessConfigurationUtils;
import org.apache.shardingsphere.data.pipeline.common.context.InventoryIncrementalJobItemContext;
import org.apache.shardingsphere.data.pipeline.common.context.InventoryIncrementalProcessContext;
import org.apache.shardingsphere.data.pipeline.common.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.common.context.PipelineJobItemContext;
import org.apache.shardingsphere.data.pipeline.common.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.common.job.progress.InventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.data.pipeline.common.job.progress.JobItemIncrementalTasksProgress;
import org.apache.shardingsphere.data.pipeline.common.job.progress.JobItemInventoryTasksProgress;
import org.apache.shardingsphere.data.pipeline.common.job.progress.JobOffsetInfo;
import org.apache.shardingsphere.data.pipeline.common.job.progress.yaml.YamlInventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.data.pipeline.common.job.progress.yaml.YamlInventoryIncrementalJobItemProgressSwapper;
import org.apache.shardingsphere.data.pipeline.common.job.progress.yaml.YamlJobOffsetInfo;
import org.apache.shardingsphere.data.pipeline.common.job.progress.yaml.YamlJobOffsetInfoSwapper;
import org.apache.shardingsphere.data.pipeline.common.pojo.DataConsistencyCheckAlgorithmInfo;
import org.apache.shardingsphere.data.pipeline.common.pojo.InventoryIncrementalJobItemInfo;
import org.apache.shardingsphere.data.pipeline.common.pojo.TableBasedPipelineJobInfo;
import org.apache.shardingsphere.data.pipeline.common.task.progress.IncrementalTaskProgress;
import org.apache.shardingsphere.data.pipeline.common.task.progress.InventoryTaskProgress;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.ConsistencyCheckJobItemProgressContext;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.PipelineDataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.algorithm.DataConsistencyCalculateAlgorithm;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.DataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.service.InventoryIncrementalJobAPI;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.metadata.PipelineProcessConfigurationPersistService;
import org.apache.shardingsphere.data.pipeline.core.task.PipelineTask;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
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
import java.util.stream.IntStream;

/**
 * Abstract inventory incremental job API implementation.
 */
@Slf4j
public abstract class AbstractInventoryIncrementalJobAPIImpl extends AbstractPipelineJobAPIImpl implements InventoryIncrementalJobAPI {
    
    private final PipelineProcessConfigurationPersistService processConfigPersistService = new PipelineProcessConfigurationPersistService();
    
    @Getter(AccessLevel.PROTECTED)
    private final YamlInventoryIncrementalJobItemProgressSwapper jobItemProgressSwapper = new YamlInventoryIncrementalJobItemProgressSwapper();
    
    private final YamlJobOffsetInfoSwapper jobOffsetInfoSwapper = new YamlJobOffsetInfoSwapper();
    
    @Override
    public abstract InventoryIncrementalProcessContext buildPipelineProcessContext(PipelineJobConfiguration pipelineJobConfig);
    
    @Override
    public void alterProcessConfiguration(final PipelineContextKey contextKey, final PipelineProcessConfiguration processConfig) {
        // TODO check rateLimiter type match or not
        processConfigPersistService.persist(contextKey, getJobType(), processConfig);
    }
    
    @Override
    public PipelineProcessConfiguration showProcessConfiguration(final PipelineContextKey contextKey) {
        return PipelineProcessConfigurationUtils.convertWithDefaultValue(processConfigPersistService.load(contextKey, getJobType()));
    }
    
    @Override
    protected abstract TableBasedPipelineJobInfo getJobInfo(String jobId);
    
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
            TableBasedPipelineJobInfo jobInfo = getJobInfo(jobId);
            InventoryIncrementalJobItemProgress jobItemProgress = entry.getValue();
            String errorMessage = getJobItemErrorMessage(jobId, shardingItem);
            if (null == jobItemProgress) {
                result.add(new InventoryIncrementalJobItemInfo(shardingItem, jobInfo.getTable(), null, startTimeMillis, 0, errorMessage));
                continue;
            }
            int inventoryFinishedPercentage = 0;
            if (JobStatus.EXECUTE_INCREMENTAL_TASK == jobItemProgress.getStatus()) {
                inventoryFinishedPercentage = 100;
            } else if (0 != jobItemProgress.getProcessedRecordsCount() && 0 != jobItemProgress.getInventoryRecordsCount()) {
                inventoryFinishedPercentage = (int) Math.min(100, jobItemProgress.getProcessedRecordsCount() * 100 / jobItemProgress.getInventoryRecordsCount());
            }
            result.add(new InventoryIncrementalJobItemInfo(shardingItem, jobInfo.getTable(), jobItemProgress, startTimeMillis, inventoryFinishedPercentage, errorMessage));
        }
        return result;
    }
    
    @Override
    public void persistJobItemProgress(final PipelineJobItemContext jobItemContext) {
        PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineJobIdUtils.parseContextKey(jobItemContext.getJobId()))
                .persistJobItemProgress(jobItemContext.getJobId(), jobItemContext.getShardingItem(), convertJobItemProgress(jobItemContext));
    }
    
    private String convertJobItemProgress(final PipelineJobItemContext jobItemContext) {
        InventoryIncrementalJobItemContext context = (InventoryIncrementalJobItemContext) jobItemContext;
        InventoryIncrementalJobItemProgress jobItemProgress = new InventoryIncrementalJobItemProgress();
        jobItemProgress.setStatus(context.getStatus());
        jobItemProgress.setSourceDatabaseType(context.getJobConfig().getSourceDatabaseType());
        jobItemProgress.setDataSourceName(context.getDataSourceName());
        jobItemProgress.setIncremental(getIncrementalTasksProgress(context.getIncrementalTasks()));
        jobItemProgress.setInventory(getInventoryTasksProgress(context.getInventoryTasks()));
        jobItemProgress.setProcessedRecordsCount(context.getProcessedRecordsCount());
        jobItemProgress.setInventoryRecordsCount(context.getInventoryRecordsCount());
        return YamlEngine.marshal(jobItemProgressSwapper.swapToYamlConfiguration(jobItemProgress));
    }
    
    @Override
    public void updateJobItemProgress(final PipelineJobItemContext jobItemContext) {
        PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineJobIdUtils.parseContextKey(jobItemContext.getJobId()))
                .updateJobItemProgress(jobItemContext.getJobId(), jobItemContext.getShardingItem(), convertJobItemProgress(jobItemContext));
    }
    
    private JobItemIncrementalTasksProgress getIncrementalTasksProgress(final Collection<PipelineTask> incrementalTasks) {
        return new JobItemIncrementalTasksProgress(incrementalTasks.isEmpty() ? null : (IncrementalTaskProgress) incrementalTasks.iterator().next().getTaskProgress());
    }
    
    private JobItemInventoryTasksProgress getInventoryTasksProgress(final Collection<PipelineTask> inventoryTasks) {
        Map<String, InventoryTaskProgress> inventoryTaskProgressMap = new HashMap<>();
        for (PipelineTask each : inventoryTasks) {
            inventoryTaskProgressMap.put(each.getTaskId(), (InventoryTaskProgress) each.getTaskProgress());
        }
        return new JobItemInventoryTasksProgress(inventoryTaskProgressMap);
    }
    
    @Override
    public void persistJobOffsetInfo(final String jobId, final JobOffsetInfo jobOffsetInfo) {
        String value = YamlEngine.marshal(jobOffsetInfoSwapper.swapToYamlConfiguration(jobOffsetInfo));
        PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineJobIdUtils.parseContextKey(jobId)).persistJobOffsetInfo(jobId, value);
    }
    
    @Override
    public JobOffsetInfo getJobOffsetInfo(final String jobId) {
        Optional<String> offsetInfo = PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineJobIdUtils.parseContextKey(jobId)).getJobOffsetInfo(jobId);
        if (offsetInfo.isPresent()) {
            YamlJobOffsetInfo info = YamlEngine.unmarshal(offsetInfo.get(), YamlJobOffsetInfo.class);
            return jobOffsetInfoSwapper.swapToObject(info);
        }
        return jobOffsetInfoSwapper.swapToObject(new YamlJobOffsetInfo());
    }
    
    @Override
    public Optional<InventoryIncrementalJobItemProgress> getJobItemProgress(final String jobId, final int shardingItem) {
        Optional<String> progress = PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineJobIdUtils.parseContextKey(jobId)).getJobItemProgress(jobId, shardingItem);
        return progress.map(optional -> jobItemProgressSwapper.swapToObject(YamlEngine.unmarshal(optional, YamlInventoryIncrementalJobItemProgress.class)));
    }
    
    @Override
    public void updateJobItemStatus(final String jobId, final int shardingItem, final JobStatus status) {
        Optional<InventoryIncrementalJobItemProgress> jobItemProgress = getJobItemProgress(jobId, shardingItem);
        if (!jobItemProgress.isPresent()) {
            return;
        }
        jobItemProgress.get().setStatus(status);
        PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineJobIdUtils.parseContextKey(jobId)).updateJobItemProgress(jobId, shardingItem,
                YamlEngine.marshal(jobItemProgressSwapper.swapToYamlConfiguration(jobItemProgress.get())));
    }
    
    @Override
    public Collection<DataConsistencyCheckAlgorithmInfo> listDataConsistencyCheckAlgorithms() {
        Collection<DataConsistencyCheckAlgorithmInfo> result = new LinkedList<>();
        for (DataConsistencyCalculateAlgorithm each : ShardingSphereServiceLoader.getServiceInstances(DataConsistencyCalculateAlgorithm.class)) {
            SPIDescription description = each.getClass().getAnnotation(SPIDescription.class);
            result.add(new DataConsistencyCheckAlgorithmInfo(each.getType(), getSupportedDatabaseTypes(each.getSupportedDatabaseTypes()), null == description ? "" : description.value()));
        }
        return result;
    }
    
    private Collection<DatabaseType> getSupportedDatabaseTypes(final Collection<DatabaseType> supportedDatabaseTypes) {
        return supportedDatabaseTypes.isEmpty() ? ShardingSphereServiceLoader.getServiceInstances(DatabaseType.class) : supportedDatabaseTypes;
    }
    
    @Override
    public DataConsistencyCalculateAlgorithm buildDataConsistencyCalculateAlgorithm(final String algorithmType, final Properties algorithmProps) {
        return TypedSPILoader.getService(DataConsistencyCalculateAlgorithm.class, null == algorithmType ? "DATA_MATCH" : algorithmType, algorithmProps);
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
            throw new IllegalArgumentException("checkResults empty, jobId:" + jobId);
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
