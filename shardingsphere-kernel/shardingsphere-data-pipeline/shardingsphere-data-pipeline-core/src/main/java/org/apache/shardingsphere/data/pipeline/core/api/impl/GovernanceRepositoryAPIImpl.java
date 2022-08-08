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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.progress.JobItemIncrementalTasksProgress;
import org.apache.shardingsphere.data.pipeline.api.job.progress.JobItemInventoryTasksProgress;
import org.apache.shardingsphere.data.pipeline.api.job.progress.JobProgress;
import org.apache.shardingsphere.data.pipeline.core.api.GovernanceRepositoryAPI;
import org.apache.shardingsphere.data.pipeline.api.context.PipelineJobContext;
import org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.YamlJobProgressSwapper;
import org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.YamlJobProgress;
import org.apache.shardingsphere.data.pipeline.core.metadata.node.PipelineMetaDataNode;
import org.apache.shardingsphere.data.pipeline.core.task.IncrementalTask;
import org.apache.shardingsphere.data.pipeline.core.task.InventoryTask;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.RuleAlteredJobContext;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEventListener;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Governance repository API impl.
 */
@RequiredArgsConstructor
@Slf4j
public final class GovernanceRepositoryAPIImpl implements GovernanceRepositoryAPI {
    
    private static final YamlJobProgressSwapper SWAPPER = new YamlJobProgressSwapper();
    
    private final ClusterPersistRepository repository;
    
    @Override
    public boolean isExisted(final String key) {
        return null != repository.get(key);
    }
    
    @Override
    public void persistJobProgress(final PipelineJobContext context) {
        RuleAlteredJobContext jobContext = (RuleAlteredJobContext) context;
        JobProgress jobProgress = new JobProgress();
        jobProgress.setStatus(jobContext.getStatus());
        jobProgress.setSourceDatabaseType(jobContext.getJobConfig().getSourceDatabaseType());
        jobProgress.setIncremental(getIncrementalTasksProgress(jobContext));
        jobProgress.setInventory(getInventoryTasksProgress(jobContext));
        String value = YamlEngine.marshal(SWAPPER.swapToYamlConfiguration(jobProgress));
        repository.persist(PipelineMetaDataNode.getScalingJobOffsetPath(jobContext.getJobId(), jobContext.getShardingItem()), value);
    }
    
    private JobItemIncrementalTasksProgress getIncrementalTasksProgress(final RuleAlteredJobContext jobContext) {
        return new JobItemIncrementalTasksProgress(
                jobContext.getIncrementalTasks()
                        .stream().collect(Collectors.toMap(IncrementalTask::getTaskId, IncrementalTask::getProgress)));
    }
    
    private JobItemInventoryTasksProgress getInventoryTasksProgress(final RuleAlteredJobContext jobContext) {
        return new JobItemInventoryTasksProgress(
                jobContext.getInventoryTasks()
                        .stream()
                        .collect(Collectors.toMap(InventoryTask::getTaskId, InventoryTask::getProgress)));
    }
    
    @Override
    public JobProgress getJobProgress(final String jobId, final int shardingItem) {
        String data = repository.get(PipelineMetaDataNode.getScalingJobOffsetPath(jobId, shardingItem));
        if (Strings.isNullOrEmpty(data)) {
            return null;
        }
        return SWAPPER.swapToObject(YamlEngine.unmarshal(data, YamlJobProgress.class));
    }
    
    @Override
    public void persistJobCheckResult(final String jobId, final boolean checkSuccess) {
        log.info("persist job check result '{}' for job {}", checkSuccess, jobId);
        repository.persist(PipelineMetaDataNode.getScalingCheckResultPath(jobId), String.valueOf(checkSuccess));
    }
    
    @Override
    public Optional<Boolean> getJobCheckResult(final String jobId) {
        String data = repository.get(PipelineMetaDataNode.getScalingCheckResultPath(jobId));
        return Strings.isNullOrEmpty(data) ? Optional.empty() : Optional.of(Boolean.parseBoolean(data));
    }
    
    @Override
    public void deleteJob(final String jobId) {
        log.info("delete job {}", jobId);
        repository.delete(PipelineMetaDataNode.getScalingJobPath(jobId));
    }
    
    @Override
    public List<String> getChildrenKeys(final String key) {
        return repository.getChildrenKeys(key);
    }
    
    @Override
    public void watch(final String key, final DataChangedEventListener listener) {
        repository.watch(key, listener);
    }
    
    @Override
    public void persist(final String key, final String value) {
        repository.persist(key, value);
    }
    
    @Override
    public List<Integer> getShardingItems(final String jobId) {
        List<String> result = getChildrenKeys(PipelineMetaDataNode.getScalingJobOffsetPath(jobId));
        log.info("getShardingItems, jobId={}, offsetKeys={}", jobId, result);
        return result.stream().map(Integer::parseInt).collect(Collectors.toList());
    }
    
    @Override
    public void updateShardingJobStatus(final String jobId, final int shardingItem, final JobStatus status) {
        JobProgress jobProgress = getJobProgress(jobId, shardingItem);
        if (null == jobProgress) {
            log.warn("updateShardingJobStatus, jobProgress is null, jobId={}, shardingItem={}", jobId, shardingItem);
            return;
        }
        jobProgress.setStatus(status);
        persist(PipelineMetaDataNode.getScalingJobOffsetPath(jobId, shardingItem), YamlEngine.marshal(SWAPPER.swapToYamlConfiguration(jobProgress)));
    }
}
