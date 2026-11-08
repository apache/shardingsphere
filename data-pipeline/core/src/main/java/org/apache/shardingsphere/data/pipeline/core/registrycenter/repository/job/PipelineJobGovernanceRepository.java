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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJob;
import org.apache.shardingsphere.data.pipeline.core.metadata.node.PipelineMetaDataNode;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

/**
 * Pipeline job governance repository.
 */
@RequiredArgsConstructor
public final class PipelineJobGovernanceRepository {
    
    private final ClusterPersistRepository repository;
    
    /**
     * Create job.
     *
     * @param jobId job ID
     * @param jobClass job class
     */
    public void create(final String jobId, final Class<? extends PipelineJob> jobClass) {
        repository.persist(PipelineMetaDataNode.getJobRootPath(jobId), jobClass.getName());
    }
    
    /**
     * Delete job.
     *
     * @param jobId job id
     */
    public void delete(final String jobId) {
        repository.delete(PipelineMetaDataNode.getJobRootPath(jobId));
    }
}
