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

package org.apache.shardingsphere.data.pipeline.core.registrycenter.repository.job;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.data.pipeline.core.metadata.node.PipelineMetaDataNode;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.TableDataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.yaml.YamlTableDataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.yaml.YamlTableDataConsistencyCheckResultSwapper;
import org.apache.shardingsphere.data.pipeline.core.exception.job.ConsistencyCheckJobNotFoundException;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Pipeline job check governance repository.
 */
@RequiredArgsConstructor
public final class PipelineJobCheckGovernanceRepository {
    
    private final ClusterPersistRepository repository;
    
    /**
     * Find latest check job id.
     *
     * @param parentJobId parent job id
     * @return check job id
     */
    public Optional<String> findLatestCheckJobId(final String parentJobId) {
        return Optional.ofNullable(repository.getDirectly(PipelineMetaDataNode.getLatestCheckJobIdPath(parentJobId)));
    }
    
    /**
     * Get latest check job id.
     *
     * @param parentJobId parent job id
     * @return check job id
     */
    public String getLatestCheckJobId(final String parentJobId) {
        return findLatestCheckJobId(parentJobId).orElseThrow(() -> new ConsistencyCheckJobNotFoundException(parentJobId));
    }
    
    /**
     * Persist latest check job id.
     *
     * @param parentJobId job id
     * @param checkJobId check job id
     */
    public void persistLatestCheckJobId(final String parentJobId, final String checkJobId) {
        repository.persist(PipelineMetaDataNode.getLatestCheckJobIdPath(parentJobId), String.valueOf(checkJobId));
    }
    
    /**
     * Delete latest check job id.
     *
     * @param parentJobId parent job id
     */
    public void deleteLatestCheckJobId(final String parentJobId) {
        repository.delete(PipelineMetaDataNode.getLatestCheckJobIdPath(parentJobId));
    }
    
    /**
     * Get check job result.
     *
     * @param parentJobId parent job id
     * @param checkJobId check job id
     * @return check job result
     */
    @SuppressWarnings("unchecked")
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
    
    /**
     * Init check job result.
     *
     * @param parentJobId parent job id
     * @param checkJobId check job id
     */
    public void initCheckJobResult(final String parentJobId, final String checkJobId) {
        repository.persist(PipelineMetaDataNode.getCheckJobResultPath(parentJobId, checkJobId), "");
    }
    
    /**
     * Persist check job result.
     *
     * @param parentJobId parent job id
     * @param checkJobId check job id
     * @param checkResultMap check result map
     */
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
    
    /**
     * Delete check job result.
     *
     * @param parentJobId parent job id
     * @param checkJobId check job id
     */
    public void deleteCheckJobResult(final String parentJobId, final String checkJobId) {
        repository.delete(PipelineMetaDataNode.getCheckJobResultPath(parentJobId, checkJobId));
    }
    
    /**
     * List check job ids.
     *
     * @param parentJobId parent job id
     * @return check job ids
     */
    public Collection<String> listCheckJobIds(final String parentJobId) {
        return repository.getChildrenKeys(PipelineMetaDataNode.getCheckJobIdsRootPath(parentJobId));
    }
}
