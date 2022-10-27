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

import org.apache.shardingsphere.data.pipeline.core.context.PipelineContext;
import org.apache.shardingsphere.data.pipeline.core.metadata.node.PipelineMetaDataNode;
import org.apache.shardingsphere.mode.persist.PersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent.Type;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("rawtypes")
public final class PipelineDistributedBarrierTest {
    
    @BeforeClass
    public static void setUp() {
        PipelineContextUtil.mockModeConfigAndContextManager();
    }
    
    @Test
    public void assertRegisterAndRemove() throws NoSuchFieldException, IllegalAccessException {
        String jobId = "123";
        PersistRepository repository = PipelineContext.getContextManager().getMetaDataContexts().getPersistService().getRepository();
        repository.persist(PipelineMetaDataNode.getJobBarrierEnablePath(jobId), "");
        PipelineDistributedBarrier instance = PipelineDistributedBarrier.getInstance();
        instance.register(jobId, 1);
        Map countDownLatchMap = ReflectionUtil.getFieldValue(instance, "countDownLatchMap", Map.class);
        assertNotNull(countDownLatchMap);
        assertTrue(countDownLatchMap.containsKey(PipelineMetaDataNode.getJobBarrierEnablePath(jobId)));
        instance.unregister(jobId);
        assertFalse(countDownLatchMap.containsKey(jobId));
    }
    
    @Test
    public void assertAwait() {
        String jobId = "j0130317c3054317c7363616c696e675f626d73716c";
        PersistRepository repository = PipelineContext.getContextManager().getMetaDataContexts().getPersistService().getRepository();
        repository.persist(PipelineMetaDataNode.getJobBarrierEnablePath(jobId), "");
        PipelineDistributedBarrier instance = PipelineDistributedBarrier.getInstance();
        instance.register(jobId, 1);
        instance.persistEphemeralChildrenNode(jobId, 1);
        boolean actual = instance.await(jobId, 1, TimeUnit.SECONDS);
        assertFalse(actual);
        instance.checkChildrenNodeCount(new DataChangedEvent(PipelineMetaDataNode.getJobBarrierEnablePath(jobId) + "/0", "", Type.ADDED));
        actual = instance.await(jobId, 1, TimeUnit.SECONDS);
        assertTrue(actual);
    }
}
