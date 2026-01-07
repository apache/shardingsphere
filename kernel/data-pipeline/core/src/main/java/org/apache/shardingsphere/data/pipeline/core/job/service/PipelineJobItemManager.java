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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineJobItemContext;
import org.apache.shardingsphere.data.pipeline.core.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.core.job.progress.PipelineJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.id.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.config.YamlPipelineJobItemProgressConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.swapper.YamlPipelineJobItemProgressSwapper;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;

import java.util.Optional;

/**
 * Pipeline job item manager.
 * 
 * @param <T> type of pipeline job item progress
 */
@RequiredArgsConstructor
public final class PipelineJobItemManager<T extends PipelineJobItemProgress> {
    
    private final YamlPipelineJobItemProgressSwapper<YamlPipelineJobItemProgressConfiguration, T> swapper;
    
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
        PipelineAPIFactory.getPipelineGovernanceFacade(PipelineJobIdUtils.parseContextKey(jobId))
                .getJobItemFacade().getProcess().update(jobId, shardingItem, YamlEngine.marshal(swapper.swapToYamlConfiguration(jobItemProgress.get())));
    }
    
    /**
     * Get job item progress.
     *
     * @param jobId job id
     * @param shardingItem sharding item
     * @return job item progress
     */
    public Optional<T> getProgress(final String jobId, final int shardingItem) {
        return PipelineAPIFactory.getPipelineGovernanceFacade(PipelineJobIdUtils.parseContextKey(jobId)).getJobItemFacade().getProcess().load(jobId, shardingItem)
                .map(optional -> swapper.swapToObject(YamlEngine.unmarshal(optional, swapper.getYamlProgressClass(), true)));
    }
    
    /**
     * Persist job item progress.
     *
     * @param jobItemContext job item context
     */
    public void persistProgress(final PipelineJobItemContext jobItemContext) {
        PipelineAPIFactory.getPipelineGovernanceFacade(PipelineJobIdUtils.parseContextKey(jobItemContext.getJobId()))
                .getJobItemFacade().getProcess().persist(jobItemContext.getJobId(), jobItemContext.getShardingItem(), convertProgressYamlContent(jobItemContext));
    }
    
    /**
     * Update job item progress.
     *
     * @param jobItemContext job item context
     */
    public void updateProgress(final PipelineJobItemContext jobItemContext) {
        PipelineAPIFactory.getPipelineGovernanceFacade(PipelineJobIdUtils.parseContextKey(jobItemContext.getJobId()))
                .getJobItemFacade().getProcess().update(jobItemContext.getJobId(), jobItemContext.getShardingItem(), convertProgressYamlContent(jobItemContext));
    }
    
    @SuppressWarnings("unchecked")
    private String convertProgressYamlContent(final PipelineJobItemContext jobItemContext) {
        return YamlEngine.marshal(swapper.swapToYamlConfiguration((T) jobItemContext.toProgress()));
    }
}
