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

package org.apache.shardingsphere.data.pipeline.core.registrycenter.repository.metadata;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.data.pipeline.core.metadata.node.PipelineMetaDataNode;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

/**
 * Pipeline meta data process configuration governance repository.
 */
@RequiredArgsConstructor
public final class PipelineMetaDataProcessConfigurationGovernanceRepository {
    
    private final ClusterPersistRepository repository;
    
    /**
     * Persist meta data process configuration.
     *
     * @param jobType job type, nullable
     * @param processConfigYamlText process configuration YAML text
     */
    public void persist(final String jobType, final String processConfigYamlText) {
        repository.persist(PipelineMetaDataNode.getMetaDataProcessConfigPath(jobType), processConfigYamlText);
    }
    
    /**
     * Load meta data process configuration.
     *
     * @param jobType job type, nullable
     * @return process configuration YAML text
     */
    public String load(final String jobType) {
        return repository.getDirectly(PipelineMetaDataNode.getMetaDataProcessConfigPath(jobType));
    }
}
