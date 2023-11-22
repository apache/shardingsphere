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

import lombok.Getter;
import org.apache.shardingsphere.data.pipeline.common.metadata.node.PipelineNodePath;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEventListener;

/**
 * Governance repository API impl.
 */
@Getter
public final class GovernanceRepositoryAPIImpl implements GovernanceRepositoryAPI {
    
    private final ClusterPersistRepository repository;
    
    private final PipelineJobConfigurationGovernanceRepository jobConfigurationGovernanceRepository;
    
    private final PipelineJobOffsetGovernanceRepository jobOffsetGovernanceRepository;
    
    private final PipelineJobItemProcessGovernanceRepository jobItemProcessGovernanceRepository;
    
    private final PipelineJobItemErrorMessageGovernanceRepository jobItemErrorMessageGovernanceRepository;
    
    private final PipelineJobCheckGovernanceRepository jobCheckGovernanceRepository;
    
    private final PipelineJobGovernanceRepository jobGovernanceRepository;
    
    private final PipelineMetaDataDataSourceGovernanceRepository metaDataDataSourceGovernanceRepository;
    
    private final PipelineMetaDataProcessConfigurationGovernanceRepository metaDataProcessConfigurationGovernanceRepository;
    
    public GovernanceRepositoryAPIImpl(final ClusterPersistRepository repository) {
        this.repository = repository;
        jobConfigurationGovernanceRepository = new PipelineJobConfigurationGovernanceRepository(repository);
        jobOffsetGovernanceRepository = new PipelineJobOffsetGovernanceRepository(repository);
        jobItemProcessGovernanceRepository = new PipelineJobItemProcessGovernanceRepository(repository);
        jobItemErrorMessageGovernanceRepository = new PipelineJobItemErrorMessageGovernanceRepository(repository);
        jobCheckGovernanceRepository = new PipelineJobCheckGovernanceRepository(repository);
        jobGovernanceRepository = new PipelineJobGovernanceRepository(repository);
        metaDataDataSourceGovernanceRepository = new PipelineMetaDataDataSourceGovernanceRepository(repository);
        metaDataProcessConfigurationGovernanceRepository = new PipelineMetaDataProcessConfigurationGovernanceRepository(repository);
    }
    
    @Override
    public void watchPipeLineRootPath(final DataChangedEventListener listener) {
        repository.watch(PipelineNodePath.DATA_PIPELINE_ROOT, listener);
    }
}
