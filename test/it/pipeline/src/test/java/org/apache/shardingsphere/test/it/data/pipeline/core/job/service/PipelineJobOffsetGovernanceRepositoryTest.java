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
import org.apache.shardingsphere.data.pipeline.common.job.progress.JobOffsetInfo;
import org.apache.shardingsphere.data.pipeline.common.registrycenter.repository.PipelineJobOffsetGovernanceRepository;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.test.it.data.pipeline.core.util.PipelineContextUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PipelineJobOffsetGovernanceRepositoryTest {
    
    @BeforeAll
    static void beforeClass() {
        PipelineContextUtils.mockModeConfigAndContextManager();
    }
    
    @Test
    void assertPersist() {
        ClusterPersistRepository clusterPersistRepository = getClusterPersistRepository();
        PipelineJobOffsetGovernanceRepository repository = new PipelineJobOffsetGovernanceRepository(clusterPersistRepository);
        assertFalse(repository.get("1").isTargetSchemaTableCreated());
        repository.persist("1", new JobOffsetInfo(true));
        assertTrue(repository.get("1").isTargetSchemaTableCreated());
    }
    
    private ClusterPersistRepository getClusterPersistRepository() {
        ContextManager contextManager = PipelineContextManager.getContext(PipelineContextUtils.getContextKey()).getContextManager();
        return (ClusterPersistRepository) contextManager.getMetaDataContexts().getPersistService().getRepository();
    }
}
