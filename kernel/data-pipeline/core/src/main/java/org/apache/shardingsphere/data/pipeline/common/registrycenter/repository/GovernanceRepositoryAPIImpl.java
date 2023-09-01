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

package org.apache.shardingsphere.data.pipeline.common.registrycenter.repository;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.common.job.type.JobType;
import org.apache.shardingsphere.data.pipeline.common.metadata.node.PipelineMetaDataNode;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.TableDataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.yaml.YamlTableDataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.yaml.YamlTableDataConsistencyCheckResultSwapper;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEventListener;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
        // TODO delegate to repository isExisted
        return null != repository.getDirectly(key);
    }
    
    @Override
    public void persistJobOffsetInfo(final String jobId, final String jobOffsetInfo) {
        repository.persist(PipelineMetaDataNode.getJobOffsetPath(jobId), jobOffsetInfo);
    }
    
    @Override
    public Optional<String> getJobOffsetInfo(final String jobId) {
        String text = repository.getDirectly(PipelineMetaDataNode.getJobOffsetPath(jobId));
        return Strings.isNullOrEmpty(text) ? Optional.empty() : Optional.of(text);
    }
    
    @Override
    public void persistJobItemProgress(final String jobId, final int shardingItem, final String progressValue) {
        repository.persist(PipelineMetaDataNode.getJobOffsetItemPath(jobId, shardingItem), progressValue);
    }
    
    @Override
    public void updateJobItemProgress(final String jobId, final int shardingItem, final String progressValue) {
        repository.update(PipelineMetaDataNode.getJobOffsetItemPath(jobId, shardingItem), progressValue);
    }
    
    @Override
    public Optional<String> getJobItemProgress(final String jobId, final int shardingItem) {
        String text = repository.getDirectly(PipelineMetaDataNode.getJobOffsetItemPath(jobId, shardingItem));
        return Strings.isNullOrEmpty(text) ? Optional.empty() : Optional.of(text);
    }
    
    @Override
    public Optional<String> getLatestCheckJobId(final String parentJobId) {
        return Optional.ofNullable(repository.getDirectly(PipelineMetaDataNode.getLatestCheckJobIdPath(parentJobId)));
    }
    
    @Override
    public void persistLatestCheckJobId(final String parentJobId, final String checkJobId) {
        repository.persist(PipelineMetaDataNode.getLatestCheckJobIdPath(parentJobId), String.valueOf(checkJobId));
    }
    
    @Override
    public void deleteLatestCheckJobId(final String parentJobId) {
        repository.delete(PipelineMetaDataNode.getLatestCheckJobIdPath(parentJobId));
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Map<String, TableDataConsistencyCheckResult> getCheckJobResult(final String parentJobId, final String checkJobId) {
        String yamlCheckResultMapText = repository.getDirectly(PipelineMetaDataNode.getCheckJobResultPath(parentJobId, checkJobId));
        if (Strings.isNullOrEmpty(yamlCheckResultMapText)) {
            return Collections.emptyMap();
        }
        YamlTableDataConsistencyCheckResultSwapper swapper = new YamlTableDataConsistencyCheckResultSwapper();
        Map<String, String> yamlCheckResultMap = YamlEngine.unmarshal(yamlCheckResultMapText, Map.class, true);
        Map<String, TableDataConsistencyCheckResult> result = new HashMap<>(yamlCheckResultMap.size(), 1F);
        for (Entry<String, String> entry : yamlCheckResultMap.entrySet()) {
            result.put(entry.getKey(), swapper.swapToObject(entry.getValue()));
        }
        return result;
    }
    
    @Override
    public void persistCheckJobResult(final String parentJobId, final String checkJobId, final Map<String, TableDataConsistencyCheckResult> checkResultMap) {
        if (null == checkResultMap) {
            return;
        }
        Map<String, String> yamlCheckResultMap = new LinkedHashMap<>();
        for (Entry<String, TableDataConsistencyCheckResult> entry : checkResultMap.entrySet()) {
            YamlTableDataConsistencyCheckResult yamlCheckResult = new YamlTableDataConsistencyCheckResultSwapper().swapToYamlConfiguration(entry.getValue());
            yamlCheckResultMap.put(entry.getKey(), YamlEngine.marshal(yamlCheckResult));
        }
        repository.persist(PipelineMetaDataNode.getCheckJobResultPath(parentJobId, checkJobId), YamlEngine.marshal(yamlCheckResultMap));
    }
    
    @Override
    public void deleteCheckJobResult(final String parentJobId, final String checkJobId) {
        repository.delete(PipelineMetaDataNode.getCheckJobResultPath(parentJobId, checkJobId));
    }
    
    @Override
    public Collection<String> listCheckJobIds(final String parentJobId) {
        return repository.getChildrenKeys(PipelineMetaDataNode.getCheckJobIdsRootPath(parentJobId));
    }
    
    @Override
    public void deleteJob(final String jobId) {
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
        return result.stream().map(Integer::parseInt).collect(Collectors.toList());
    }
    
    @Override
    public String getMetaDataDataSources(final JobType jobType) {
        return repository.getDirectly(PipelineMetaDataNode.getMetaDataDataSourcesPath(jobType));
    }
    
    @Override
    public void persistMetaDataDataSources(final JobType jobType, final String metaDataDataSources) {
        repository.persist(PipelineMetaDataNode.getMetaDataDataSourcesPath(jobType), metaDataDataSources);
    }
    
    @Override
    public String getMetaDataProcessConfiguration(final JobType jobType) {
        return repository.getDirectly(PipelineMetaDataNode.getMetaDataProcessConfigPath(jobType));
    }
    
    @Override
    public void persistMetaDataProcessConfiguration(final JobType jobType, final String processConfigYamlText) {
        repository.persist(PipelineMetaDataNode.getMetaDataProcessConfigPath(jobType), processConfigYamlText);
    }
    
    @Override
    public String getJobItemErrorMessage(final String jobId, final int shardingItem) {
        return repository.getDirectly(PipelineMetaDataNode.getJobItemErrorMessagePath(jobId, shardingItem));
    }
    
    @Override
    public void cleanJobItemErrorMessage(final String jobId, final int shardingItem) {
        repository.delete(PipelineMetaDataNode.getJobItemErrorMessagePath(jobId, shardingItem));
    }
}
