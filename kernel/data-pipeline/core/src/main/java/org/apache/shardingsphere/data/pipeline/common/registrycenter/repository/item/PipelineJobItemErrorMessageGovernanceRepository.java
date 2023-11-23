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

package org.apache.shardingsphere.data.pipeline.common.registrycenter.repository.item;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.data.pipeline.common.metadata.node.PipelineMetaDataNode;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

/**
 * Pipeline job item error message governance repository.
 */
@RequiredArgsConstructor
public final class PipelineJobItemErrorMessageGovernanceRepository {
    
    private final ClusterPersistRepository repository;
    
    /**
     * Update job item error message.
     *
     * @param jobId job ID
     * @param shardingItem sharding item
     * @param errorMessage error message
     */
    public void update(final String jobId, final int shardingItem, final String errorMessage) {
        repository.update(PipelineMetaDataNode.getJobItemErrorMessagePath(jobId, shardingItem), errorMessage);
    }
    
    /**
     * Load job item error msg.
     *
     * @param jobId job id
     * @param shardingItem sharding item
     * @return error msg
     */
    public String load(final String jobId, final int shardingItem) {
        return repository.getDirectly(PipelineMetaDataNode.getJobItemErrorMessagePath(jobId, shardingItem));
    }
}
