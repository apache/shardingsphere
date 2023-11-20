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

package org.apache.shardingsphere.data.pipeline.core.job.service;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.shardingsphere.data.pipeline.common.context.PipelineJobItemContext;
import org.apache.shardingsphere.data.pipeline.common.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.common.job.progress.PipelineJobItemProgress;
import org.apache.shardingsphere.data.pipeline.common.metadata.node.PipelineMetaDataNode;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.yaml.YamlPipelineJobItemProgressConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.yaml.YamlPipelineJobItemProgressSwapper;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;

import java.util.Optional;

/**
 * Pipeline job manager.
 * 
 * @param <T> type of pipeline job item progress
 */
public final class PipelineJobItemManager<T extends PipelineJobItemProgress> {
    
    private final YamlPipelineJobItemProgressSwapper<YamlPipelineJobItemProgressConfiguration, T> swapper;
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    public PipelineJobItemManager(final YamlPipelineJobItemProgressSwapper swapper) {
        this.swapper = swapper;
    }
    
    /**
     * Update job item status.
     *
     * @param jobId job id
     * @param shardingItem sharding item
     * @param status status
     */
    public void updateStatus(final String jobId, final int shardingItem, final JobStatus status) {
        Optional<T> jobItemProgress = getProgress(jobId, shardingItem);
        if (!jobItemProgress.isPresent()) {
            return;
        }
        jobItemProgress.get().setStatus(status);
        PipelineAPIFactory.getGovernanceRepositoryAPI(
                PipelineJobIdUtils.parseContextKey(jobId)).updateJobItemProgress(jobId, shardingItem, YamlEngine.marshal(swapper.swapToYamlConfiguration(jobItemProgress.get())));
    }
    
    /**
     * Get job item progress.
     *
     * @param jobId job id
     * @param shardingItem sharding item
     * @return job item progress
     */
    public Optional<T> getProgress(final String jobId, final int shardingItem) {
        return PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineJobIdUtils.parseContextKey(jobId)).getJobItemProgress(jobId, shardingItem)
                .map(optional -> swapper.swapToObject(YamlEngine.unmarshal(optional, swapper.getYamlProgressClass(), true)));
    }
    
    /**
     * Persist job item progress.
     *
     * @param jobItemContext job item context
     */
    public void persistProgress(final PipelineJobItemContext jobItemContext) {
        PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineJobIdUtils.parseContextKey(jobItemContext.getJobId()))
                .persistJobItemProgress(jobItemContext.getJobId(), jobItemContext.getShardingItem(), convertProgressYamlContent(jobItemContext));
    }
    
    /**
     * Update job item progress.
     *
     * @param jobItemContext job item context
     */
    public void updateProgress(final PipelineJobItemContext jobItemContext) {
        PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineJobIdUtils.parseContextKey(jobItemContext.getJobId()))
                .updateJobItemProgress(jobItemContext.getJobId(), jobItemContext.getShardingItem(), convertProgressYamlContent(jobItemContext));
    }
    
    @SuppressWarnings("unchecked")
    private String convertProgressYamlContent(final PipelineJobItemContext jobItemContext) {
        return YamlEngine.marshal(swapper.swapToYamlConfiguration((T) jobItemContext.toProgress()));
    }
    
    /**
     * Get job item error message.
     *
     * @param jobId job id
     * @param shardingItem sharding item
     * @return map, key is sharding item, value is error message
     */
    public String getErrorMessage(final String jobId, final int shardingItem) {
        return Optional.ofNullable(PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineJobIdUtils.parseContextKey(jobId)).getJobItemErrorMessage(jobId, shardingItem)).orElse("");
    }
    
    /**
     * Update job item error message.
     *
     * @param jobId job id
     * @param shardingItem sharding item
     * @param error error
     */
    public void updateErrorMessage(final String jobId, final int shardingItem, final Object error) {
        String key = PipelineMetaDataNode.getJobItemErrorMessagePath(jobId, shardingItem);
        String value = "";
        if (null != error) {
            value = error instanceof Throwable ? ExceptionUtils.getStackTrace((Throwable) error) : error.toString();
        }
        PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineJobIdUtils.parseContextKey(jobId)).update(key, value);
    }
    
    /**
     * Clean job item error message.
     *
     * @param jobId job id
     * @param shardingItem sharding item
     */
    public void cleanErrorMessage(final String jobId, final int shardingItem) {
        String key = PipelineMetaDataNode.getJobItemErrorMessagePath(jobId, shardingItem);
        PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineJobIdUtils.parseContextKey(jobId)).persist(key, "");
    }
}
