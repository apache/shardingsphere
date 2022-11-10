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

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.InventoryIncrementalJobPublicAPI;
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
import org.apache.shardingsphere.data.pipeline.core.check.consistency.DataConsistencyCalculateAlgorithmChooser;
import org.apache.shardingsphere.data.pipeline.core.config.process.PipelineProcessConfigurationUtil;
import org.apache.shardingsphere.data.pipeline.core.context.InventoryIncrementalJobItemContext;
import org.apache.shardingsphere.data.pipeline.core.context.InventoryIncrementalProcessContext;
import org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.YamlInventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.YamlInventoryIncrementalJobItemProgressSwapper;
import org.apache.shardingsphere.data.pipeline.core.task.IncrementalTask;
import org.apache.shardingsphere.data.pipeline.core.task.InventoryTask;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.ConsistencyCheckJobItemContext;
import org.apache.shardingsphere.data.pipeline.spi.check.consistency.DataConsistencyCalculateAlgorithm;
import org.apache.shardingsphere.data.pipeline.spi.check.consistency.DataConsistencyCalculateAlgorithmFactory;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
public abstract class AbstractInventoryIncrementalJobAPIImpl extends AbstractPipelineJobAPIImpl implements InventoryIncrementalJobAPI, InventoryIncrementalJobPublicAPI {
    
    private final PipelineProcessConfigurationPersistService processConfigPersistService = new PipelineProcessConfigurationPersistService();
    
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
            InventoryIncrementalJobItemProgress jobItemProgress = getJobItemProgress(jobId, each);
            if (null != jobItemProgress) {
                jobItemProgress.setActive(!jobConfigPOJO.isDisabled());
            }
            map.put(each, jobItemProgress);
        }, LinkedHashMap::putAll);
    }
    
    @Override
    public List<InventoryIncrementalJobItemInfo> getJobItemInfos(final String jobId) {
        JobConfigurationPOJO jobConfigPOJO = getElasticJobConfigPOJO(jobId);
        PipelineJobConfiguration jobConfig = getJobConfiguration(jobConfigPOJO);
        long startTimeMillis = Long.parseLong(Optional.ofNullable(jobConfigPOJO.getProps().getProperty("start_time_millis")).orElse("0"));
        Map<Integer, InventoryIncrementalJobItemProgress> jobProgress = getJobProgress(jobConfig);
        List<InventoryIncrementalJobItemInfo> result = new ArrayList<>(jobProgress.size());
        for (Entry<Integer, InventoryIncrementalJobItemProgress> entry : jobProgress.entrySet()) {
            int shardingItem = entry.getKey();
            String errorMessage = getJobItemErrorMessage(jobId, shardingItem);
            InventoryIncrementalJobItemInfo progressInfo = new InventoryIncrementalJobItemInfo(shardingItem, entry.getValue(), startTimeMillis, errorMessage);
            if (null == entry.getValue()) {
                continue;
            }
            result.add(progressInfo);
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
    public InventoryIncrementalJobItemProgress getJobItemProgress(final String jobId, final int shardingItem) {
        String data = PipelineAPIFactory.getGovernanceRepositoryAPI().getJobItemProgress(jobId, shardingItem);
        return Strings.isNullOrEmpty(data) ? null : jobItemProgressSwapper.swapToObject(YamlEngine.unmarshal(data, YamlInventoryIncrementalJobItemProgress.class));
    }
    
    @Override
    public void updateJobItemStatus(final String jobId, final int shardingItem, final JobStatus status) {
        InventoryIncrementalJobItemProgress jobItemProgress = getJobItemProgress(jobId, shardingItem);
        if (null == jobItemProgress) {
            log.warn("updateJobItemStatus, jobItemProgress is null, jobId={}, shardingItem={}", jobId, shardingItem);
            return;
        }
        jobItemProgress.setStatus(status);
        PipelineAPIFactory.getGovernanceRepositoryAPI().persistJobItemProgress(jobId, shardingItem, YamlEngine.marshal(jobItemProgressSwapper.swapToYamlConfiguration(jobItemProgress)));
    }
    
    @Override
    public Collection<DataConsistencyCheckAlgorithmInfo> listDataConsistencyCheckAlgorithms() {
        checkModeConfig();
        return DataConsistencyCalculateAlgorithmFactory.getAllInstances().stream()
                .map(each -> new DataConsistencyCheckAlgorithmInfo(each.getType(), each.getSupportedDatabaseTypes(), each.getDescription())).collect(Collectors.toList());
    }
    
    @Override
    public DataConsistencyCalculateAlgorithm buildDataConsistencyCalculateAlgorithm(final PipelineJobConfiguration jobConfig, final String algorithmType, final Properties algorithmProps) {
        ShardingSpherePreconditions.checkState(null != algorithmType || null != jobConfig, () -> new IllegalArgumentException("Algorithm type and job configuration are null."));
        return null == algorithmType
                ? DataConsistencyCalculateAlgorithmChooser.choose(
                        DatabaseTypeFactory.getInstance(jobConfig.getSourceDatabaseType()), DatabaseTypeFactory.getInstance(getTargetDatabaseType(jobConfig)))
                : DataConsistencyCalculateAlgorithmFactory.newInstance(algorithmType, algorithmProps);
    }
    
    @Override
    public Map<String, DataConsistencyCheckResult> dataConsistencyCheck(final PipelineJobConfiguration jobConfig, final DataConsistencyCalculateAlgorithm calculateAlgorithm,
                                                                        final ConsistencyCheckJobItemContext checkJobItemContext) {
        String jobId = jobConfig.getJobId();
        PipelineDataConsistencyChecker dataConsistencyChecker = buildPipelineDataConsistencyChecker(jobConfig, buildPipelineProcessContext(jobConfig), checkJobItemContext);
        Map<String, DataConsistencyCheckResult> result = dataConsistencyChecker.check(calculateAlgorithm);
        log.info("job {} with check algorithm '{}' data consistency checker result {}", jobId, calculateAlgorithm.getType(), result);
        return result;
    }
    
    protected abstract PipelineDataConsistencyChecker buildPipelineDataConsistencyChecker(PipelineJobConfiguration pipelineJobConfig, InventoryIncrementalProcessContext processContext,
                                                                                          ConsistencyCheckJobItemContext checkJobItemContext);
    
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
