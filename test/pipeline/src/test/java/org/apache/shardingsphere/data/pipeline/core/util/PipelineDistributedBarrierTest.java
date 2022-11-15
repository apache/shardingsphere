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

import org.apache.shardingsphere.data.pipeline.core.api.PipelineDistributedBarrier;
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
        String jobId = "j0130317c3054317c7363616c696e675f626d73716c";
        PersistRepository repository = PipelineContext.getContextManager().getMetaDataContexts().getPersistService().getRepository();
        repository.persist(PipelineMetaDataNode.getJobRootPath(jobId), "");
        PipelineDistributedBarrier instance = PipelineContext.getPipelineDistributedBarrier();
        String parentPath = "/barrier";
        instance.register(parentPath, 1);
        Map countDownLatchMap = ReflectionUtil.getFieldValue(instance, "countDownLatchMap", Map.class);
        assertNotNull(countDownLatchMap);
        assertTrue(countDownLatchMap.containsKey(parentPath));
        instance.unregister(parentPath);
        assertFalse(countDownLatchMap.containsKey(parentPath));
    }
    
    @Test
    public void assertAwait() {
        String jobId = "j0130317c3054317c7363616c696e675f626d73716c";
        PersistRepository repository = PipelineContext.getContextManager().getMetaDataContexts().getPersistService().getRepository();
        repository.persist(PipelineMetaDataNode.getJobRootPath(jobId), "");
        PipelineDistributedBarrier instance = PipelineContext.getPipelineDistributedBarrier();
        String barrierEnablePath = PipelineMetaDataNode.getJobBarrierEnablePath(jobId);
        instance.register(barrierEnablePath, 1);
        instance.persistEphemeralChildrenNode(barrierEnablePath, 1);
        boolean actual = instance.await(barrierEnablePath, 1, TimeUnit.SECONDS);
        assertFalse(actual);
        instance.notifyChildrenNodeCountCheck(new DataChangedEvent(barrierEnablePath + "/0", "", Type.ADDED));
        actual = instance.await(barrierEnablePath, 1, TimeUnit.SECONDS);
        assertTrue(actual);
    }
}
