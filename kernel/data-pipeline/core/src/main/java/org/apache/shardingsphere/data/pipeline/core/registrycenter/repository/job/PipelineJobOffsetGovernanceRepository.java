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
import org.apache.shardingsphere.data.pipeline.core.job.progress.JobOffsetInfo;
import org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.config.YamlJobOffsetInfo;
import org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.swapper.YamlJobOffsetInfoSwapper;
import org.apache.shardingsphere.data.pipeline.core.metadata.node.PipelineMetaDataNode;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

/**
 * Pipeline job offset governance repository.
 */
@RequiredArgsConstructor
public final class PipelineJobOffsetGovernanceRepository {
    
    private final ClusterPersistRepository repository;
    
    /**
     * Persist job offset info.
     *
     * @param jobId job id
     * @param jobOffsetInfo job offset info
     */
    public void persist(final String jobId, final JobOffsetInfo jobOffsetInfo) {
        repository.persist(PipelineMetaDataNode.getJobOffsetPath(jobId), YamlEngine.marshal(new YamlJobOffsetInfoSwapper().swapToYamlConfiguration(jobOffsetInfo)));
    }
    
    /**
     * Load job offset info.
     *
     * @param jobId job id
     * @return job offset info
     */
    public JobOffsetInfo load(final String jobId) {
        String value = repository.getDirectly(PipelineMetaDataNode.getJobOffsetPath(jobId));
        return new YamlJobOffsetInfoSwapper().swapToObject(Strings.isNullOrEmpty(value) ? new YamlJobOffsetInfo() : YamlEngine.unmarshal(value, YamlJobOffsetInfo.class));
    }
}
