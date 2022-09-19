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
import org.apache.shardingsphere.data.pipeline.api.job.JobType;
import org.apache.shardingsphere.data.pipeline.core.api.GovernanceRepositoryAPI;
import org.apache.shardingsphere.data.pipeline.core.metadata.node.PipelineMetaDataNode;
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
    
    private final ClusterPersistRepository repository;
    
    @Override
    public boolean isExisted(final String key) {
        return null != repository.get(key);
    }
    
    @Override
    public void persistJobItemProgress(final String jobId, final int shardingItem, final String progressValue) {
        repository.persist(PipelineMetaDataNode.getJobOffsetItemPath(jobId, shardingItem), progressValue);
    }
    
    @Override
    public String getJobItemProgress(final String jobId, final int shardingItem) {
        return repository.get(PipelineMetaDataNode.getJobOffsetItemPath(jobId, shardingItem));
    }
    
    @Override
    public void persistJobCheckResult(final String jobId, final boolean checkSuccess) {
        log.info("persist job check result '{}' for job {}", checkSuccess, jobId);
        repository.persist(PipelineMetaDataNode.getJobCheckResultPath(jobId), String.valueOf(checkSuccess));
    }
    
    @Override
    public Optional<Boolean> getJobCheckResult(final String jobId) {
        String data = repository.get(PipelineMetaDataNode.getJobCheckResultPath(jobId));
        return Strings.isNullOrEmpty(data) ? Optional.empty() : Optional.of(Boolean.parseBoolean(data));
    }
    
    @Override
    public void deleteJob(final String jobId) {
        log.info("delete job {}", jobId);
        repository.delete(PipelineMetaDataNode.getJobRootPath(jobId));
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
        List<String> result = getChildrenKeys(PipelineMetaDataNode.getJobOffsetPath(jobId));
        log.info("getShardingItems, jobId={}, offsetKeys={}", jobId, result);
        return result.stream().map(Integer::parseInt).collect(Collectors.toList());
    }
    
    @Override
    public String getMetaDataDataSources(final JobType jobType) {
        return repository.get(PipelineMetaDataNode.getMetaDataDataSourcesPath(jobType));
    }
    
    @Override
    public void persistMetaDataDataSources(final JobType jobType, final String metaDataDataSources) {
        repository.persist(PipelineMetaDataNode.getMetaDataDataSourcesPath(jobType), metaDataDataSources);
    }
    
    @Override
    public String getMetaDataProcessConfiguration(final JobType jobType) {
        return repository.get(PipelineMetaDataNode.getMetaDataProcessConfigPath(jobType));
    }
    
    @Override
    public void persistMetaDataProcessConfiguration(final JobType jobType, final String processConfigYamlText) {
        repository.persist(PipelineMetaDataNode.getMetaDataProcessConfigPath(jobType), processConfigYamlText);
    }
    
    @Override
    public String getJobItemErrorMessage(final String jobId, final int shardingItem) {
        return repository.get(PipelineMetaDataNode.getJobItemErrorMessagePath(jobId, shardingItem));
    }
    
    @Override
    public void cleanJobItemErrorMessage(final String jobId, final int shardingItem) {
        repository.delete(PipelineMetaDataNode.getJobItemErrorMessagePath(jobId, shardingItem));
    }
}
