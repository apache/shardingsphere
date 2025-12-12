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

package org.apache.shardingsphere.data.pipeline.core.util;

import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextManager;
import org.apache.shardingsphere.data.pipeline.core.metadata.node.PipelineMetaDataNode;
import org.apache.shardingsphere.test.it.data.pipeline.core.util.JobConfigurationBuilder;
import org.apache.shardingsphere.test.it.data.pipeline.core.util.PipelineContextUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PipelineDistributedBarrierTest {
    
    @BeforeAll
    static void setUp() {
        PipelineContextUtils.initPipelineContextManager();
    }
    
    @Test
    void assertRegisterAndRemove() throws ReflectiveOperationException {
        String jobId = JobConfigurationBuilder.createYamlMigrationJobConfiguration().getJobId();
        PipelineContextKey contextKey = PipelineContextUtils.getContextKey();
        PipelineContextManager.getContext(contextKey).getPersistServiceFacade().getRepository().persist(PipelineMetaDataNode.getJobRootPath(jobId), "");
        PipelineDistributedBarrier instance = PipelineDistributedBarrier.getInstance(contextKey);
        String parentPath = "/barrier";
        instance.register(parentPath, 1);
        Map<?, ?> countDownLatchMap = (Map<?, ?>) Plugins.getMemberAccessor().get(PipelineDistributedBarrier.class.getDeclaredField("countDownLatchHolders"), instance);
        assertNotNull(countDownLatchMap);
        assertTrue(countDownLatchMap.containsKey(parentPath));
        instance.unregister(parentPath);
        assertFalse(countDownLatchMap.containsKey(parentPath));
    }
    
    @Test
    void assertAwait() {
        String jobId = JobConfigurationBuilder.createYamlMigrationJobConfiguration().getJobId();
        PipelineContextKey contextKey = PipelineContextUtils.getContextKey();
        PipelineContextManager.getContext(contextKey).getPersistServiceFacade().getRepository().persist(PipelineMetaDataNode.getJobRootPath(jobId), "");
        PipelineDistributedBarrier instance = PipelineDistributedBarrier.getInstance(contextKey);
        String barrierEnablePath = PipelineMetaDataNode.getJobBarrierEnablePath(jobId);
        instance.register(barrierEnablePath, 1);
        instance.persistEphemeralChildrenNode(barrierEnablePath, 1);
        boolean actual = instance.await(barrierEnablePath, 1L, TimeUnit.SECONDS);
        assertFalse(actual);
        instance.notifyChildrenNodeCountCheck(barrierEnablePath + "/0");
        actual = instance.await(barrierEnablePath, 1L, TimeUnit.SECONDS);
        assertTrue(actual);
    }
}
