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

package org.apache.shardingsphere.data.pipeline.core.registrycenter.repository.item;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.data.pipeline.core.metadata.node.PipelineMetaDataNode;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

import java.util.Optional;

/**
 * Pipeline job item process governance repository.
 */
@RequiredArgsConstructor
public final class PipelineJobItemProcessGovernanceRepository {
    
    private final ClusterPersistRepository repository;
    
    /**
     * Persist job item progress.
     *
     * @param jobId job id
     * @param shardingItem sharding item
     * @param progressValue progress value
     */
    public void persist(final String jobId, final int shardingItem, final String progressValue) {
        repository.persist(PipelineMetaDataNode.getJobOffsetItemPath(jobId, shardingItem), progressValue);
    }
    
    /**
     * Update job item progress.
     *
     * @param jobId job id
     * @param shardingItem sharding item
     * @param progressValue progress value
     */
    public void update(final String jobId, final int shardingItem, final String progressValue) {
        repository.update(PipelineMetaDataNode.getJobOffsetItemPath(jobId, shardingItem), progressValue);
    }
    
    /**
     * Load job item progress.
     *
     * @param jobId job id
     * @param shardingItem sharding item
     * @return job item progress
     */
    public Optional<String> load(final String jobId, final int shardingItem) {
        String text = repository.getDirectly(PipelineMetaDataNode.getJobOffsetItemPath(jobId, shardingItem));
        return Strings.isNullOrEmpty(text) ? Optional.empty() : Optional.of(text);
    }
}
