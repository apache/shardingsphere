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

package org.apache.shardingsphere.test.it.data.pipeline.core.job.service;

import org.apache.shardingsphere.data.pipeline.common.context.PipelineContextManager;
import org.apache.shardingsphere.data.pipeline.common.metadata.node.PipelineNodePath;
import org.apache.shardingsphere.data.pipeline.common.registrycenter.repository.PipelineJobGovernanceRepository;
import org.apache.shardingsphere.data.pipeline.common.registrycenter.repository.PipelineJobItemProcessGovernanceRepository;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.test.it.data.pipeline.core.util.PipelineContextUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;

class PipelineJobGovernanceRepositoryTest {
    
    @BeforeAll
    static void beforeClass() {
        PipelineContextUtils.mockModeConfigAndContextManager();
    }
    
    @Test
    void assertDelete() {
        ClusterPersistRepository clusterPersistRepository = getClusterPersistRepository();
        PipelineJobGovernanceRepository repository = new PipelineJobGovernanceRepository(clusterPersistRepository);
        clusterPersistRepository.persist(PipelineNodePath.DATA_PIPELINE_ROOT + "/1", "");
        repository.delete("1");
        Optional<String> actual = new PipelineJobItemProcessGovernanceRepository(clusterPersistRepository).load("1", 0);
        assertFalse(actual.isPresent());
    }
    
    private ClusterPersistRepository getClusterPersistRepository() {
        ContextManager contextManager = PipelineContextManager.getContext(PipelineContextUtils.getContextKey()).getContextManager();
        return (ClusterPersistRepository) contextManager.getMetaDataContexts().getPersistService().getRepository();
    }
}
